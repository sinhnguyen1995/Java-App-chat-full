package server.state;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public interface UserState {

    boolean wake(SocketChannel socketChannel) throws IOException;

    boolean sleep(SocketChannel socketChannel) throws IOException;

    boolean login(SocketChannel oos) throws IOException;

    boolean register(SocketChannel socketChannel, String task) throws IOException;

    void echo(SocketChannel socketChannel, String message) throws IOException;

    boolean broadcast(SocketChannel socketChannel) throws IOException;

    boolean subscribe(SocketChannel socketChannel) throws IOException;

    boolean hot(SocketChannel socketChannel) throws IOException;

    boolean logout(SocketChannel socketChannel) throws IOException;

}
