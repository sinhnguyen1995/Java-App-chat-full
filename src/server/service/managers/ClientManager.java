package server.service.managers;

import models.User;
import org.json.simple.JSONObject;
import server.concretestate.AuthenticateState;
import server.service.listeners.Observer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static server.service.MyExecutors.fromStringToJson;

public class ClientManager implements Observer {
    private int id;
    private SocketChannel socketChannel;
    private User user;

    public ClientManager() {
        this.user = new User();
    }

    public ClientManager(int id, SocketChannel socketChannel, User user) {
        this.id = id;
        this.socketChannel = socketChannel;
        this.user = user;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public void setSocketChannel(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "ClientManager{" +
                "id=" + id +
                ", socketChannel=" + socketChannel +
                ", user=" + user +
                '}';
    }

    @Override
    public void update(String message) {
        if (this.user.getUserState() instanceof AuthenticateState) {
            JSONObject objJson = fromStringToJson("hot", true, message);
            try {
                this.socketChannel.write(ByteBuffer.wrap(objJson.toString().getBytes()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
