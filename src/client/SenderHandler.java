package client;

import models.HotTopic;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SenderHandler implements Runnable {
    private final Scanner sc;
    private SocketChannel sChannel;

    public SenderHandler(SocketChannel sChannel, Scanner scanner) {
        this.sChannel = sChannel;
        this.sc = scanner;
    }


    public static String inputMessage(Scanner sc) {
        System.out.print("CLIENT>>> ");
        return sc.nextLine();
    }

    public static boolean isContinue(Scanner sc) {
        String temp;
        while (true) {
            System.out.print("Do you want continue action (Y/N):  ");
            temp = sc.nextLine();
            boolean quit = temp.equalsIgnoreCase("y");
            boolean notQuit = temp.equalsIgnoreCase("n");
            if (quit || notQuit) {
                return quit;
            }
        }
    }

    public static int topicOptions(Scanner sc, String title) throws IOException {
        System.out.println("\n-----" + title + "-----");
        System.out.println("""
                1: War
                2: Hate
                3: Love
                4: Back""");
        int actionNumber = 0;
        String tempActionNumber = null;

        while (true) {
            do {
                System.out.print("Enter number 1 to 4: ");
                tempActionNumber = sc.nextLine();
            } while (!(tempActionNumber.matches("[0-9]+") && tempActionNumber.length() > 0));
            actionNumber = Integer.parseInt(tempActionNumber);
            if (actionNumber >= 1 && actionNumber <= 4) {
                break;
            }
        }
        return actionNumber;
    }


    public boolean checkPassword(String password) {
        String regex = "^(?=.*[0-9])" + "(?=.*[a-z])(?=.*[A-Z])" + "(?=.*[@#$%^&+=])" + "(?=\\S+$).{8,20}$";
        Pattern p = Pattern.compile(regex);

        if (password == null) {
            return false;
        }
        Matcher m = p.matcher(password);
        return m.matches();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        String message;
        try {
            while (true) {
                System.out.println("\n-----MENU-----");
                System.out.println("""
                        1: Login
                        2: Register
                        3: Echo
                        4: Broadcast
                        5: Logout
                        6: Sleep
                        7: Wake
                        8: Subscribe
                        9: Hot (Broadcast to specific topic)
                        10: Exit""");
                int actionNumber = 0;
                String tempActionNumber = null;

                while (true) {
                    do {
                        System.out.print("Enter number 1 to 10: ");
                        tempActionNumber = sc.nextLine();
                    } while (!(tempActionNumber.matches("[0-9]+") && tempActionNumber.length() > 0));
                    actionNumber = Integer.parseInt(tempActionNumber);
                    if (actionNumber >= 1 && actionNumber <= 10) {
                        break;
                    }
                }
                if (actionNumber == 10) {
                    System.out.println("\nClient has exited ");
//                    os.writeObject(new Message<>(Type.EXIT, "Try to Exit."));
//                    os.flush();
                    sc.close();
//                  socket.close();
                    break;
                }

                switch (actionNumber) {
                    case 1 -> {
                        while (true) {
                            System.out.println("\n-----LOGIN-----");
                            Scanner myObj = new Scanner(System.in);
                            System.out.print("Enter username: ");
                            String username = myObj.nextLine();
                            System.out.print("Enter password: ");
                            String password = myObj.nextLine();

                            JSONObject objJson1 = new JSONObject();

                            objJson1.put("type", "login");
                            objJson1.put("username", username);
                            objJson1.put("password", password);

                            handleByteBuffer(objJson1);

                            Thread.sleep(1000);
                            Scanner tempSc = new Scanner(System.in);
                            boolean isContinue = isContinue(tempSc);
                            if (!isContinue) {
                                break;
                            }
                        }
                    }
                    case 2 -> {
                        while (true) {
                            System.out.println("\n-----REGISTER-----");
                            /*sending the user input to server*/
                            Scanner myObj = new Scanner(System.in);
                            String username;
                            String password;
                            while (true) {
                                System.out.print("Enter username: ");
                                username = myObj.nextLine();
                                System.out.print("Enter password: ");
                                password = myObj.nextLine();
                                if (checkPassword(password)) {
                                    break;
                                } else {
                                    System.out.println("Password must contain at least 8 characters, includes upper and lower case and number!!");
                                }
                            }

                            JSONObject objJson1 = new JSONObject();

                            objJson1.put("type", "register");
                            objJson1.put("username", username);
                            objJson1.put("password", password);

                            handleByteBuffer(objJson1);

                            Thread.sleep(1000);

                            Scanner tempSc = new Scanner(System.in);
                            boolean isContinue = isContinue(tempSc);
                            if (!isContinue) {
                                break;
                            }
                        }
                    }
                    case 3 -> {
                        while (true) {
                            System.out.println("\n-----ECHO-----");
                            System.out.println("1: back");
                            Scanner sc1 = new Scanner(System.in);
                            String echoMessage = inputMessage(sc1);
                            if (echoMessage.equals("")) {
                                continue;
                            }
                            if (echoMessage.equalsIgnoreCase("1")) {
                                break;
                            }

                            JSONObject objJson1 = new JSONObject();
                            objJson1.put("type", "echo");
                            objJson1.put("message", echoMessage);
                            handleByteBuffer(objJson1);
                            Thread.sleep(500);
                        }
                    }
                    case 4 -> {
                        System.out.println("\n-----BROADCAST-----");
                        System.out.println("1: back");
                        Scanner sc1 = new Scanner(System.in);
                        String broadcastMessage = inputMessage(sc1);

                        if (broadcastMessage.equals("")) {
                            continue;
                        }
                        if (broadcastMessage.equalsIgnoreCase("1")) {
                            break;
                        }

                        JSONObject objJson1 = new JSONObject();
                        objJson1.put("type", "broadcast");
                        objJson1.put("message", broadcastMessage);

                        handleByteBuffer(objJson1);

                        Thread.sleep(500);
                    }
                    case 5 -> {
                        JSONObject objJson1 = new JSONObject();
                        objJson1.put("type", "logout");
                        objJson1.put("message", "Try to logout.");

                        handleByteBuffer(objJson1);
                        Thread.sleep(500);
                    }
                    case 6 -> {
                        JSONObject objJson1 = new JSONObject();
                        objJson1.put("type", "sleep");
                        objJson1.put("message", "Try to sleep current client.");

                        handleByteBuffer(objJson1);
                        Thread.sleep(500);
                    }
                    case 7 -> {
                        JSONObject objJson1 = new JSONObject();
                        objJson1.put("type", "wake");
                        objJson1.put("message", "Try to wake up client.");

                        handleByteBuffer(objJson1);
                        Thread.sleep(500);
                    }
                    case 8 -> {
                        Scanner ignoreSc = new Scanner(System.in);
                        int topicOptionNumber = topicOptions(ignoreSc, "TOPIC");
                        switch (topicOptionNumber) {
                            case 1 -> {
                                JSONObject objJson1 = new JSONObject();
                                objJson1.put("type", "subscribe");
                                objJson1.put("message", "war");

                                handleByteBuffer(objJson1);
                            }
                            case 2 -> {
                                JSONObject objJson1 = new JSONObject();
                                objJson1.put("type", "subscribe");
                                objJson1.put("message", "hate");

                                handleByteBuffer(objJson1);
                            }
                            case 3 -> {
                                JSONObject objJson1 = new JSONObject();
                                objJson1.put("type", "subscribe");
                                objJson1.put("message", "love");

                                handleByteBuffer(objJson1);
                            }
                        }
                        Thread.sleep(1000);
                    }
                    case 9 -> {
                        Scanner ignoreSc = new Scanner(System.in);
                        int topicOptionNumber = topicOptions(ignoreSc, "HOT TO");
                        Thread.sleep(500);
                        switch (topicOptionNumber) {
                            case 1 -> {
                                JSONObject objJson1 = new JSONObject();
                                objJson1.put("type", "hot");
                                objJson1.put("topic", "war");

                                System.out.print("Enter message to " + HotTopic.WAR + ": ");
                                String messageToWar = sc.nextLine();

                                objJson1.put("message", messageToWar);

                                handleByteBuffer(objJson1);
                            }
                            case 2 -> {
                                JSONObject objJson2 = new JSONObject();
                                objJson2.put("type", "hot");
                                objJson2.put("topic", "hate");

                                System.out.print("Enter message to " + HotTopic.HATE + ": ");
                                String messageToHate = sc.nextLine();
                                objJson2.put("message", messageToHate);

                                handleByteBuffer(objJson2);
                            }
                            case 3 -> {
                                JSONObject objJson1 = new JSONObject();
                                objJson1.put("type", "hot");
                                objJson1.put("topic", "love");

                                System.out.print("Enter message to " + HotTopic.LOVE + ": ");
                                String messageToLove = sc.nextLine();
                                objJson1.put("message", messageToLove);

                                handleByteBuffer(objJson1);
                            }
                        }
                        Thread.sleep(1000);
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
              /*  if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }*/
        }
    }

    public void handleByteBuffer(JSONObject jsonObject) throws IOException {
        sChannel.write(ByteBuffer.wrap(jsonObject.toString().getBytes()));
    }
}
