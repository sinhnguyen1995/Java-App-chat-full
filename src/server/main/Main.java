package server.main;

import server.service.MyExecutors;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        MyExecutors server = new MyExecutors();
        server.run();
    }
}
