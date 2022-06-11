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

public class SleepState implements UserState, Serializable {
    @Serial
    private static final long serialVersionUID = -6500665823330706018L;
    private final User user;

    public SleepState(User user) {
        this.user = user;
    }

    @Override
    public boolean wake(SocketChannel socketChannel) throws IOException {
        user.setState(user.getAuthenticateState());
        JSONObject objJson = fromStringToJson("wake", true, "Wake up successful!");
        socketChannel.write(ByteBuffer.wrap(objJson.toString().getBytes()));
        return true;
    }

    @Override
    public boolean sleep(SocketChannel socketChannel) throws IOException {
        JSONObject objJson = fromStringToJson("sleep", false, "No effect! You cannot perform this action! You are in " + user.getUserState());
        socketChannel.write(ByteBuffer.wrap(objJson.toString().getBytes()));
        return false;
    }

    @Override
    public boolean login(SocketChannel socketChannel) throws IOException {
        String message = "You cannot perform this action! You are in " + user.getUserState();
        JSONObject objJson1 = new JSONObject();
        objJson1.put("type", "login");
        objJson1.put("isSuccess", "false");
        objJson1.put("message", message);
        socketChannel.write(ByteBuffer.wrap(objJson1.toString().getBytes()));
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
        JSONObject objJson = fromStringToJson("echo", false, "You cannot perform this action! You are in " + user.getUserState());
        socketChannel.write(ByteBuffer.wrap(objJson.toString().getBytes()));
    }

    @Override
    public boolean broadcast(SocketChannel socketChannel) throws IOException {
        JSONObject objJson = fromStringToJson("self-broadcast", false, "No effect! You cannot perform this action! You are in " + user.getUserState());
        socketChannel.write(ByteBuffer.wrap(objJson.toString().getBytes()));
        return false;
    }

    @Override
    public boolean subscribe(SocketChannel socketChannel) throws IOException {
        JSONObject objJson = fromStringToJson("subscribe", false, "No effect! You cannot perform this action! You are in " + user.getUserState());
        socketChannel.write(ByteBuffer.wrap(objJson.toString().getBytes()));
        return false;
    }

    @Override
    public boolean hot(SocketChannel socketChannel) throws IOException {
        JSONObject objJson = fromStringToJson("hot", false, "No effect! You cannot perform this action! You are in " + user.getUserState());
        socketChannel.write(ByteBuffer.wrap(objJson.toString().getBytes()));
        return false;
    }

    @Override
    public boolean logout(SocketChannel socketChannel) throws IOException {
        JSONObject objJson = fromStringToJson("logout", false, "No effect! You cannot perform this action! You are in " + user.getUserState());
        socketChannel.write(ByteBuffer.wrap(objJson.toString().getBytes()));
        return false;
    }

    @Override
    public String toString() {
        return "Sleep State";
    }
}
