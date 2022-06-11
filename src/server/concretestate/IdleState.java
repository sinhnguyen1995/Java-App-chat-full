package server.concretestate;

import database.DBConnection;
import models.User;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import server.state.UserState;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static server.service.MyExecutors.fromStringToJson;

public class IdleState implements UserState, Serializable {
    public static final String USER_CREATE = "INSERT INTO user (name, password) VALUES (?, ?);";
    @Serial
    private static final long serialVersionUID = -6500665823330706018L;
    private static final String USER_LOGIN = "SELECT * FROM user WHERE name = ? and password = ?";
    private static final String GET_USER_BY_USERNAME = "SELECT * FROM user WHERE name = ?";
    private final User user;

    public IdleState(User user) {
        this.user = user;
    }

    @Override
    public boolean wake(SocketChannel socketChannel) throws IOException {
        JSONObject objJson = fromStringToJson("wake", false, "No effect! You cannot perform this action! You are in " + user.getUserState());
        socketChannel.write(ByteBuffer.wrap(objJson.toString().getBytes()));
        return false;
    }

    @Override
    public boolean sleep(SocketChannel socketChannel) throws IOException {
        JSONObject objJson = fromStringToJson("sleep", false, "No effect! You cannot perform this action! You are in " + user.getUserState());
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
    public boolean login(SocketChannel socketChannel) throws IOException {
        User existedUser = null;
        try {
            existedUser = checkLogin(user);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return existedUser != null;
    }

    @Override
    public boolean register(SocketChannel socketChannel, String task) throws IOException {
        String username = getUsername(task);
        String password = getPassword(task);

        User userToLogin = new User(username, password);
        User isUserExisted = null;
        try {
            isUserExisted = isUserExisted(userToLogin);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (isUserExisted == null) {
            boolean isCreated = false;
            try {
                isCreated = createUser(userToLogin);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            if (isCreated) {
                JSONObject objJson = fromStringToJson("register", true, "Register succeed!");
                socketChannel.write(ByteBuffer.wrap(objJson.toString().getBytes()));
            } else {
                JSONObject objJson = fromStringToJson("register", false, "Register fail! Please check database! ");
                socketChannel.write(ByteBuffer.wrap(objJson.toString().getBytes()));
            }
            return isCreated;
        } else {
            return false;
        }
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
    public boolean logout(SocketChannel socketChannel) throws IOException {
        JSONObject objJson = fromStringToJson("logout", false, "No effect! You cannot perform this action! You are in " + user.getUserState());
        socketChannel.write(ByteBuffer.wrap(objJson.toString().getBytes()));
        return false;
    }

    private synchronized User checkLogin(User user) throws SQLException {
        try (DBConnection dbHelper = DBConnection.getDBHelper();
             Connection connection = dbHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(USER_LOGIN)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPassword());
            ResultSet result = statement.executeQuery();
            User existedUser = null;
            if (result.next()) {
                existedUser = new User();
                existedUser.setUsername(result.getString("name"));
            }
            return existedUser;
        }
    }


    private synchronized User isUserExisted(User user) throws SQLException {
        User existedUser = null;
        try (DBConnection dbHelper = DBConnection.getDBHelper();
             Connection connection = dbHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_USER_BY_USERNAME)) {
            statement.setString(1, user.getUsername());
            ResultSet results = statement.executeQuery();
            if (results.next()) {
                existedUser = new User();
                existedUser.setUsername(results.getString("name"));
            }
            return existedUser;
        }
    }


    private synchronized boolean createUser(User user) throws SQLException {
        boolean rowUpdated = false;
        try (DBConnection dbHelper = DBConnection.getDBHelper();
             Connection connection = dbHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(USER_CREATE)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPassword());
            rowUpdated = statement.executeUpdate() > 0;

            return rowUpdated;
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


    @Override
    public String toString() {
        return "Idle State";
    }
}
