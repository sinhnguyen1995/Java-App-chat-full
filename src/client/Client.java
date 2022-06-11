//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class Client {
    public Client() {
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            System.out.println("Client Start");
            SocketChannel sChannel = SocketChannel.open();
            sChannel.configureBlocking(true);
            if (sChannel.connect(new InetSocketAddress("localhost", 9091))) {
                SenderHandler senderHandler = new SenderHandler(sChannel, scanner);
                Thread sender = new Thread(senderHandler);
                sender.start();
                ReceiverHandler receiverHandler = new ReceiverHandler(sChannel);
                Thread receiver = new Thread(receiverHandler);
                receiver.start();
            }
        } catch (IOException var10) {
            var10.printStackTrace();
        }

    }
}
