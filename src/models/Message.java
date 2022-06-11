package models;

import org.json.simple.JSONObject;

import java.io.Serial;
import java.io.Serializable;
import java.nio.channels.SocketChannel;

public class Message<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = -6500665823330706018L;
    private Type type;
    private String task;
    private JSONObject taskJsonForm;
    private SocketChannel socketChannel;
    private T t;

    public Message() {
    }

    public Message(Type type, T t) {
        this.type = type;
        this.t = t;
    }

    public Message(Type type, T t, SocketChannel socketChannel) {
        this.type = type;
        this.t = t;
        this.socketChannel = socketChannel;
    }

    public Message(JSONObject taskJsonForm, SocketChannel socketChannel) {
        this.taskJsonForm = taskJsonForm;
        this.socketChannel = socketChannel;
    }

    public Message(String task, SocketChannel socketChannel, T t) {
        this.task = task;
        this.socketChannel = socketChannel;
        this.t = t;
    }

    public Message(String task, SocketChannel socketChannel) {
        this.task = task;
        this.socketChannel = socketChannel;
    }

    public Message(Type type, String task, SocketChannel socketChannel, T t) {
        this.type = type;
        this.task = task;
        this.socketChannel = socketChannel;
        this.t = t;
    }

    public Message(Type type, String task, JSONObject taskJsonForm, SocketChannel socketChannel, T t) {
        this.type = type;
        this.task = task;
        this.taskJsonForm = taskJsonForm;
        this.socketChannel = socketChannel;
        this.t = t;
    }



    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public T getT() {
        return t;
    }

    public void setT(T t) {
        this.t = t;
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public void setSocketChannel(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public JSONObject getTaskJsonForm() {
        return taskJsonForm;
    }

    public void setTaskJsonForm(JSONObject taskJsonForm) {
        this.taskJsonForm = taskJsonForm;
    }

    @Override
    public String toString() {
        return "Message{" +
                "type=" + type +
                ", task='" + task + '\'' +
                ", taskJsonForm=" + taskJsonForm +
                ", socketChannel=" + socketChannel +
                ", t=" + t +
                '}';
    }
}
