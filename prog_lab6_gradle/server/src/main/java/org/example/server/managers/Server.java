package org.example.server.managers;

import java.io.BufferedInputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class Server {
    private final int port;
    private ServerSocketChannel serverSocketChannel;
    private SocketChannel socketChannel;
    private final RequestCommandHandler requestCommandHandler;
    private final FileManager fileManager;

    private BufferedInputStream bufferedInputStream;

    public Server(int port, RequestCommandHandler requestCommandHandler, FileManager fileManager) {
        this.port = port;
        this.requestCommandHandler = requestCommandHandler;
        this.fileManager = fileManager;
    }

    private void openServerSocketChannel() {
        try {
            SocketAddress socketAddress = new InetSocketAddress(port);

        }
    }


}
