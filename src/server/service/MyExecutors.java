package server.service;

import models.HotTopic;
import models.Message;
import models.User;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import server.concretestate.IdleState;
import server.service.managers.ClientManager;
import server.service.publisher.ConcreteSubject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.*;

public class MyExecutors implements Runnable {
    public final static int SERVER_PORT = 9091;
    private static final ArrayList<SocketChannel> totalConnected = new ArrayList<>();
    private static final ArrayList<SocketChannel> totalAuthenticated = new ArrayList<>();
    private static final ArrayList<SocketChannel> totalAuthenticatedNotSleeping = new ArrayList<>();
    private static final ArrayList<SocketChannel> totalSleeping = new ArrayList<>();
    private static final ArrayList<SocketChannel> totalIdle = new ArrayList<>();
    private final ConcreteSubject concreteSubject;
    private final Queue<Message<User>> taskQueue;
    private final Selector selector;
    private final List<Callable<Message<User>>> callableList;
    private final ExecutorService executor;
    private final ExecutorService executorResponse;
    private final ScheduledExecutorService executorService;
    private final Map<Integer, ClientManager> clientManagers = new HashMap<>();
    private final Queue<Future<Message<User>>> futures;

    public MyExecutors() throws IOException {
        this.taskQueue = new LinkedList<>();
        this.concreteSubject = new ConcreteSubject();
        this.selector = Selector.open();
        this.executor = java.util.concurrent.Executors.newFixedThreadPool(10);
        this.executorResponse = java.util.concurrent.Executors.newFixedThreadPool(10);
        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.callableList = new ArrayList<>();
        this.futures = new LinkedList<>();
        handleResponse();
    }

    private static synchronized String handleMessageEcho(String message) {
        message = message.replaceAll("[^a-zA-Z0-9]", " ");
        return message;
    }

    public static JSONObject fromStringToJson(String typeAction, boolean isSuccess, String messageToSend) {
        JSONObject objJson = new JSONObject();

        objJson.put("type", typeAction);
        objJson.put("message", messageToSend);
        objJson.put("isSuccess", isSuccess);

        return objJson;
    }

    public static String getMessageFromRequest(String request) {
        Object obj = JSONValue.parse(request);
        JSONObject jsonObject = (JSONObject) obj;
        return (String) jsonObject.get("message");
    }

    public static String getActionFromRequest(String request) {
        Object obj = JSONValue.parse(request);
        JSONObject jsonObject = (JSONObject) obj;
        return (String) jsonObject.get("type");
    }

    public static String getSomethingFromTask(String request, String field) {
        Object obj = JSONValue.parse(request);
        JSONObject jsonObject = (JSONObject) obj;
        return (String) jsonObject.get(field);
    }

    public void init() {
        System.out.println("Binding to port " + SERVER_PORT + ", please wait  ...");
    }

    public void loop() throws IOException {
        while (true) {
            InetAddress ip = InetAddress.getByName("localhost");
            ServerSocketChannel serverSocket = ServerSocketChannel.open();
            serverSocket.bind(new InetSocketAddress(ip, SERVER_PORT));
            serverSocket.configureBlocking(false);
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("Waiting for a client ...");

            while (true) {
                selector.select();
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iter = selectedKeys.iterator();
                while (iter.hasNext()) {
                    SelectionKey key = iter.next();

                    if (key.isAcceptable()) {
                        SocketChannel client = serverSocket.accept();
                        ClientManager clientManager = new ClientManager();
                        clientManager.setId(client.hashCode());
                        clientManager.setSocketChannel(client);
                        this.clientManagers.put(client.hashCode(), clientManager);
                        System.out.printf("Incoming Connection from %s%n", client.getRemoteAddress());
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_READ);
                        SelectionKey newKey = client.register(selector, SelectionKey.OP_READ);
                        newKey.attach(new ConnectionHandler());
                    }

                    if (key.isReadable()) {
                        SocketChannel client = (SocketChannel) key.channel();
                        ConnectionHandler connectionHandler = (ConnectionHandler) key.attachment();
                        try {
                            CharBuffer newCharBuffer = (connectionHandler.read(client));
                            ClientManager clientManager = this.clientManagers.get(client.hashCode());

                            // This will be submitted to Executor
                            Callable<Message<User>> callable = () -> {
                                String task = newCharBuffer.toString();
                                String action = getActionFromRequest(task);
                                switch (action) {
                                    case "login" -> {
                                        String username = getUsername(task);
                                        String password = getPassword(task);

                                        User userToLogin = new User(username, password);

                                        if (clientManager.getUser().getUserState() instanceof IdleState) {
                                            clientManager.setUser(userToLogin);

                                            boolean isLoginSuccess = clientManager.getUser().login(client);
                                            if (isLoginSuccess) {
                                                System.out.println("Login success!");
                                                clientManager.getUser().setState(clientManager.getUser().getAuthenticateState());

                                                /*add user to authenticated user (active mode)*/
                                                addInstance(client);
                                                addInstanceAuthenticatedNotSleeping(client);
                                                removeInstanceTotalIdle(client);
                                                printStat();
                                                JSONObject objJson = fromStringToJson("login", true, "Login Succeed!");
                                                client.write(ByteBuffer.wrap(objJson.toString().getBytes()));
                                            } else {
                                                System.out.println("Fail to login!");
                                                JSONObject objJson = fromStringToJson("login", false, "Fail to login!");
                                                client.write(ByteBuffer.wrap(objJson.toString().getBytes()));
                                            }
                                        } else {
                                            System.out.println("Fail to login!");
                                            JSONObject objJson = fromStringToJson("login", false, "You cannot perform action in this state! You are in: " + clientManager.getUser().getUserState());
                                            client.write(ByteBuffer.wrap(objJson.toString().getBytes()));
                                        }
                                    }
                                    case "echo" -> {
                                        clientManager.getUser().echo(client, task);
                                    }
                                    case "register" -> {
                                        clientManager.getUser().register(client, task);
                                    }
                                    case "broadcast" -> {
                                        String messageContent = getMessageFromRequest(task);
                                        if (clientManager.getUser().broadcast(client)) {
                                            this.concreteSubject.setMessageContentBroadcastToAll(messageContent);
//                                            broadcastMessage(messageContent, client);
                                        }
                                    }
                                    case "sleep" -> {
                                        clientManager.getUser().sleep(client);
                                        addInstanceTotalSleeping(client);
                                        removeInstanceAuthenticatedNotSleeping(client);
                                        printStat();
                                    }
                                    case "wake" -> {
                                        clientManager.getUser().wake(client);
                                        removeInstanceTotalSleeping(client);
                                        addInstanceAuthenticatedNotSleeping(client);
                                    }
                                    case "logout" -> {
                                        boolean isLogoutSuccess = clientManager.getUser().logout(client);
                                        if (isLogoutSuccess) {
                                            clientManagers.remove(client.hashCode());
                                            removeInstance(client);
                                            removeInstanceAuthenticatedNotSleeping(client);
                                            addInstanceTotalIdle(client);
                                            printStat();
                                        }
                                    }
                                    case "subscribe" -> {
                                        String topic = getMessageFromRequest(task);

                                        boolean isAllowAction = clientManager.getUser().subscribe(client);
                                        boolean isSubscribeSucceed = false;
                                        if (isAllowAction) {
                                            switch (topic) {
                                                case "war" -> {
                                                    isSubscribeSucceed = registerObserver(HotTopic.WAR, clientManager);
                                                }
                                                case "love" -> {
                                                    isSubscribeSucceed = registerObserver(HotTopic.LOVE, clientManager);
                                                }
                                                case "hate" -> {
                                                    isSubscribeSucceed = registerObserver(HotTopic.HATE, clientManager);
                                                }
                                            }
                                            if (isSubscribeSucceed) {
                                                JSONObject objJson = fromStringToJson("subscribe", true, "Subscribe to " + topic.toUpperCase() + " succeed!");
                                                client.write(ByteBuffer.wrap(objJson.toString().getBytes()));
                                            } else {
                                                JSONObject objJson = fromStringToJson("subscribe", false, "Subscribe to " + topic.toUpperCase() + " fail! You already subscribe!");
                                                client.write(ByteBuffer.wrap(objJson.toString().getBytes()));
                                            }
                                        }
                                    }
                                    case "hot" -> {
//                                        handleResponse();
                                        String topic = getSomethingFromTask(task, "topic");
                                        System.out.println("topic: " + topic);
                                        String message = getMessageFromRequest(task);
                                        System.out.println("message: " + message);

                                        JSONObject objJson1 = new JSONObject();
                                        objJson1.put("type", "hot");
                                        objJson1.put("message", message);

                                        boolean isAllowAction = clientManager.getUser().hot(client);
                                        System.out.println("isAllowAction: " + isAllowAction);
                                        if (isAllowAction) {
                                            String responseMessage = "";
                                            switch (topic) {
                                                case "war" -> {
                                                    this.concreteSubject.setMessageContent(HotTopic.WAR, message);
                                                    objJson1.put("topic", "war");
                                                    responseMessage = "Sent to War topic succeed!";
                                                }
                                                case "love" -> {
                                                    this.concreteSubject.setMessageContent(HotTopic.LOVE, message);
                                                    objJson1.put("topic", "love");
                                                    responseMessage = "Sent to Love topic succeed!";
                                                }
                                                case "hate" -> {
                                                    this.concreteSubject.setMessageContent(HotTopic.HATE, message);
                                                    objJson1.put("topic", "hate");
                                                    responseMessage = "Sent to Hate topic succeed!";
                                                }
                                            }
                                            JSONObject objJson = fromStringToJson("self-hot", true, responseMessage);
                                            client.write(ByteBuffer.wrap(objJson.toString().getBytes()));
                                        }
                                        return new Message<>(objJson1, client);
                                    }
                                    case "exit" -> {

                                    }
                                }
                                return null;
//                                return new Message<>(newCharBuffer.toString(), client);
                            };

//                            callableList.add(callable);
                            Future<Message<User>> future = executor.submit(callable);
                            futures.add(future);
                            printFuture();

                        } catch (ClosedChannelException e) {
                            System.out.printf("Connection from %s closed%n", client.getRemoteAddress());
                            removeInstanceAuthenticatedNotSleeping(client);
                            removeInstance(client);
                            removeInstanceTotalIdle(client);
                            removeInstanceTotalConnected(client);
                            removeInstanceTotalSleeping(client);
                            this.concreteSubject.removeObserver(HotTopic.LOVE, clientManagers.get(client.hashCode()));
                            this.concreteSubject.removeObserver(HotTopic.WAR, clientManagers.get(client.hashCode()));
                            this.concreteSubject.removeObserver(HotTopic.HATE, clientManagers.get(client.hashCode()));
                            key.cancel();
                            client.close();
                        }
                    }

                    iter.remove();
                }
            }
        }
    }

    private void handleResponse() {
        Runnable runnable = () -> {
            while (true) {
                System.out.println("inside out!");
                Future<Message<User>> message = this.futures.remove();
                Message<User> message2 = null;
                try {
                    message2 = message.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                if (message2 != null) {
                    System.out.println(message2);
                    JSONObject jsonObject = message2.getTaskJsonForm();
                    String messageToSend = (String) jsonObject.get("message");
                    String topic = (String) jsonObject.get("topic");
                    System.out.println("jsonObject: " + jsonObject);
                    Queue<Integer> queue = new LinkedList<>();
                    switch (topic) {
                        case "war" -> {
                            this.concreteSubject.setMessageContent(HotTopic.WAR, messageToSend);
                            System.out.println("QUEUE IS: " + queue);
                        }
                        case "love" -> {
                            this.concreteSubject.setMessageContent(HotTopic.LOVE, messageToSend);
                            System.out.println("QUEUE IS: " + queue);
                        }
                        case "hate" -> {
                            this.concreteSubject.setMessageContent(HotTopic.HATE, messageToSend);
                            System.out.println("QUEUE IS: " + queue);
                        }
                    }
                    while (!queue.isEmpty()){
                        ClientManager clientManager = this.clientManagers.get(queue.remove());
//                        JSONObject objJson = fromStringToJson("hot", true, messageToSend);
                        boolean isContain = totalAuthenticatedNotSleeping.contains(clientManager.getSocketChannel());
                        if (isContain){
                            System.out.println("Found in map!!");
                        }
//                            clientManager.getSocketChannel().write(ByteBuffer.wrap(objJson.toString().getBytes()));
//                        System.out.println("Write finished!");
                    }
                    queue.remove();
                    System.out.println("QUEUE IS: " + queue);
                }
            }
        };
        executorService.execute(runnable);
    }

    private synchronized boolean registerObserver(HotTopic hotTopic, ClientManager clientManager) {
        return concreteSubject.registerObserver(hotTopic, clientManager);
    }

    public void broadcastMessage(String msgToSend, SocketChannel socketChannel) throws IOException {
        int counter = 0;

        JSONObject jsonObject = fromStringToJson("broadcast", true, msgToSend);

        for (SocketChannel client : totalAuthenticatedNotSleeping) {
            if (!client.equals(socketChannel)) {
                try {
                    counter++;
                    msgToSend = "\n\nBroadcast from: " + clientManagers.get(socketChannel.hashCode()).getUser().getUsername() + " | Content: " + msgToSend + "\n";
                    client.write(ByteBuffer.wrap(jsonObject.toString().getBytes()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("Broadcast to " + counter + " clients.");
        if (counter > 0) {
            JSONObject jsonObjectToCurrent = fromStringToJson("self-broadcast", true, "Broadcast to " + counter + " active clients successful!.");
            socketChannel.write(ByteBuffer.wrap(jsonObjectToCurrent.toString().getBytes()));
        } else {
            JSONObject jsonObjectToCurrent = fromStringToJson("self-broadcast", true, "Broadcast successfully, but no one is active at this moment!");
            socketChannel.write(ByteBuffer.wrap(jsonObjectToCurrent.toString().getBytes()));
        }
    }

    private String getUsername(String task) {
        Object obj = JSONValue.parse(task);
        JSONObject jsonObject = (JSONObject) obj;
        return (String) jsonObject.get("username");
    }

    private String getPassword(String task) {
        Object obj = JSONValue.parse(task);
        JSONObject jsonObject = (JSONObject) obj;
        return (String) jsonObject.get("password");
    }

    private void printFuture() {
        System.out.println("Size of all futures: " + futures.size());
    }

    private synchronized void addInstance(SocketChannel socketChannel) {
        totalAuthenticated.add(socketChannel);
    }

    private synchronized void removeInstance(SocketChannel socketChannel) {
        totalAuthenticated.remove(socketChannel);
    }

    private synchronized void addInstanceTotalConnected(SocketChannel socketChannel) {
        totalConnected.add(socketChannel);
    }

    private synchronized void removeInstanceTotalConnected(SocketChannel socketChannel) {
        totalConnected.remove(socketChannel);
    }

    private synchronized void addInstanceTotalIdle(SocketChannel socketChannel) {
        totalIdle.add(socketChannel);
    }

    private synchronized void removeInstanceTotalIdle(SocketChannel socketChannel) {
        totalIdle.remove(socketChannel);
    }

    private synchronized void addInstanceTotalSleeping(SocketChannel socketChannel) {
        totalSleeping.add(socketChannel);
    }

    private synchronized void removeInstanceTotalSleeping(SocketChannel socketChannel) {
        totalSleeping.remove(socketChannel);
    }

    private synchronized void addInstanceAuthenticatedNotSleeping(SocketChannel socketChannel) {
        totalAuthenticatedNotSleeping.add(socketChannel);
    }

    private synchronized void removeInstanceAuthenticatedNotSleeping(SocketChannel socketChannel) {
        totalAuthenticatedNotSleeping.remove(socketChannel);
    }

    public void printStat() {
        String leftAlignFormat = "| %-15s | %-4d |%n";
        System.out.format("+-----------------+------+%n");
        System.out.format("| State           | No.  |%n");
        System.out.format("+-----------------+------+%n");
        System.out.format(leftAlignFormat, "Connected", totalConnected.size());
        System.out.format(leftAlignFormat, "Authenticated", totalAuthenticated.size());
        System.out.format(leftAlignFormat, "Au Not Sleep", totalAuthenticatedNotSleeping.size());
        System.out.format(leftAlignFormat, "Sleeping", totalSleeping.size());
        System.out.format(leftAlignFormat, "Idle", totalIdle.size());
        System.out.format("+-----------------+------+%n");
    }

    public void run() {
        try {
            init();
            loop();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
