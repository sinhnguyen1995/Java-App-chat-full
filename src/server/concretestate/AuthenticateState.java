package server.concretestate;


import models.User;
import org.json.simple.JSONObject;
import server.state.UserState;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static server.service.MyExecutors.fromStringToJson;
import static server.service.MyExecutors.getMessageFromRequest;

public class AuthenticateState implements UserState, Serializable {
    @Serial
    private static final long serialVersionUID = -6500665823330706018L;

    private User user;

    public AuthenticateState() {
    }

    public AuthenticateState(User user) {
        this.user = user;
    }

    @Override
    public boolean wake(SocketChannel socketChannel) throws IOException {
        JSONObject objJson = fromStringToJson("wake", false, "No effect! You are already wake up!");
        socketChannel.write(ByteBuffer.wrap(objJson.toString().getBytes()));
        return false;
    }

    @Override
    public boolean sleep(SocketChannel socketChannel) throws IOException {
        user.setState(user.getSleepState());
        JSONObject objJson = fromStringToJson("sleep", true, "Change to sleep mode successful!");
        socketChannel.write(ByteBuffer.wrap(objJson.toString().getBytes()));
        return true;
    }

    @Override
    public boolean login(SocketChannel socketChannel) throws IOException {
        JSONObject objJson = fromStringToJson("echo", false, "You cannot perform this action! You are in " + user.getUserState());
        socketChannel.write(ByteBuffer.wrap(objJson.toString().getBytes()));
        return false;
    }

    @Override
    public boolean register(SocketChannel socketChannel, String task) throws IOException {
        JSONObject objJson = fromStringToJson("register", false, "You cannot perform this action! You are in " + user.getUserState());
        socketChannel.write(ByteBuffer.wrap(objJson.toString().getBytes()));
        return false;
    }

    @Override
    public void echo(SocketChannel socketChannel, String message) throws IOException {
        String messageToEcho = getMessageFromRequest(message);
        messageToEcho = handleMessageEcho(messageToEcho);
        JSONObject objJson = fromStringToJson("echo", true, messageToEcho);
        socketChannel.write(ByteBuffer.wrap(objJson.toString().getBytes()));
    }

    @Override
    public boolean broadcast(SocketChannel socketChannel) {
        return true;
    }

    @Override
    public boolean logout(SocketChannel socketChannel) throws IOException {
        this.user.setState(user.getIdleState());
        JSONObject objJson = fromStringToJson("logout", false, "Logout successful!");
        socketChannel.write(ByteBuffer.wrap(objJson.toString().getBytes()));
        return true;
    }

    @Override
    public boolean subscribe(SocketChannel socketChannel) throws IOException {
        return true;
    }

    @Override
    public boolean hot(SocketChannel socketChannel) throws IOException {
        return true;
    }

    private synchronized String handleMessageEcho(String message) {
        message = message.replaceAll("[^a-zA-Z0-9]", " ");
        return message;
    }

    @Override
    public String toString() {
        return "Authenticate state";
    }
}
