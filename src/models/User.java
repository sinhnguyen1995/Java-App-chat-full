package models;

import server.concretestate.AuthenticateState;
import server.concretestate.IdleState;
import server.concretestate.SleepState;
import server.state.UserState;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.nio.channels.SocketChannel;
import java.util.Scanner;


public class User implements Serializable {
    @Serial
    private static final long serialVersionUID = -6500665823330706018L;
    private UserState sleepState;
    private UserState authenticateState;
    private UserState idleState;
    private UserState userState;
    private String username;
    private String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.idleState = new IdleState(this);
        this.authenticateState = new AuthenticateState(this);
        this.sleepState = new SleepState(this);
        userState = idleState;
    }

    public User() {
        this.idleState = new IdleState(this);
        this.authenticateState = new AuthenticateState(this);
        this.sleepState = new SleepState(this);
        userState = idleState;
    }

    public UserState getSleepState() {
        return sleepState;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserState getUserState() {
        return userState;
    }

    public void setUserState(UserState userState) {
        this.userState = userState;
    }

    public void setState(UserState state) {
        userState = state;
    }

    public void wake(SocketChannel socketChannel) throws IOException {
        this.userState.wake(socketChannel);
    }

    public void sleep(SocketChannel socketChannel) throws IOException {
        this.userState.sleep(socketChannel);
    }

    public boolean broadcast(SocketChannel socketChannel) throws IOException {
        return this.userState.broadcast(socketChannel);
    }


    public boolean subscribe(SocketChannel socketChannel) throws IOException {
        return this.userState.subscribe(socketChannel);
    }


    public boolean hot(SocketChannel socketChannel) throws IOException {
        return this.userState.hot(socketChannel);
    }

    public UserState getAuthenticateState() {
        return authenticateState;
    }

    public UserState getIdleState() {
        return idleState;
    }

    public boolean login(SocketChannel socketChannel) throws IOException {
        return userState.login(socketChannel);
    }

    public boolean register(SocketChannel socketChannel, String task) throws IOException {
        return userState.register(socketChannel, task);
    }

    public void echo(SocketChannel socketChannel, String message) throws IOException {
        userState.echo(socketChannel, message);
    }

    public boolean logout(SocketChannel socketChannel) throws IOException {
        return userState.logout(socketChannel);
    }

    @Override
    public String toString() {
        return "User{" +
                "sleepState=" + sleepState +
                ", authenticateState=" + authenticateState +
                ", idleState=" + idleState +
                ", userState=" + userState +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

    public void inputUser() {
        Scanner myObj = new Scanner(System.in);
        System.out.print("Enter username: ");
        this.username = myObj.nextLine();
        System.out.print("Enter password: ");
        this.password = myObj.nextLine();
    }
}


