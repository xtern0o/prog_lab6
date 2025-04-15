package org.example.server.managers;

import org.example.common.dtp.ObjectSerializer;
import org.example.common.dtp.RequestCommand;
import org.example.common.dtp.Response;
import org.example.common.dtp.ResponseStatus;
import org.example.server.cli.ConsoleOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

public class Server {
    private final int port;
    private final RequestCommandHandler requestCommandHandler;
    private final ConsoleOutput consoleOutput;

    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private boolean isRunning = false;

    public static int BUFFER_SIZE = 1024;

    public static Logger logger = LoggerFactory.getLogger(Server.class);

    public Server(int port, RequestCommandHandler requestCommandHandler, ConsoleOutput consoleOutput) {
        this.port = port;
        this.requestCommandHandler = requestCommandHandler;
        this.consoleOutput = consoleOutput;
    }

    public void start() throws IOException {
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        try {
            serverSocketChannel.bind(new InetSocketAddress(port));
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new IOException("Недопустимый порт");
        }
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        isRunning = true;
        while (isRunning) {
            try {
                selector.select(200);
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iter = selectedKeys.iterator();

                while (iter.hasNext()) {
                    SelectionKey key = iter.next();
                    iter.remove();

                    if (key.isAcceptable()) {
                        handleAccept(key);
                    }
                    else if (key.isReadable()) {
                        handleRead(key);
                    }
                }
            } catch (ClosedSelectorException e) {
                logger.error("Селектор закрыт: {}", e.getMessage());
                break;
            } catch (SocketException e) {
                logger.error("Ошибка сокетов: {}", e.getMessage());
            }

        }
    }

    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel keyChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = keyChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);

        logger.info("Connected to: {}", clientChannel.getRemoteAddress());
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        int bytesRead;
        while ((bytesRead = clientChannel.read(buffer)) > 0) {
            buffer.flip();
            byteStream.write(buffer.array(), 0, bytesRead);
            buffer.clear();
        }

        if (bytesRead == -1) {
            clientChannel.close();
            return;
        }

        byte[] receivedData = byteStream.toByteArray();

        // ignore stupid requests
        if (receivedData.length == 0) return;

        logger.info("Got REQUEST from: {}", clientChannel.getRemoteAddress());

        try {
            RequestCommand requestCommand = (RequestCommand) ObjectSerializer.deserializeObject(receivedData);
            Response response = requestCommandHandler.handleRequestCommand(requestCommand);
            clientChannel.write(ByteBuffer.wrap(ObjectSerializer.serializeObject(response)));

            logger.info("COMMAND NAME: \"{}\"; ARGS: \"{}\"", requestCommand.getCommandName(), requestCommand.getArgs());
            logger.info("Sent RESPONSE to \"{}\" successfully ({})", clientChannel.getRemoteAddress(), response.getResponseStatus());

        } catch (ClassNotFoundException e) {
            Response errorResponse = new Response(ResponseStatus.COMMAND_ERROR, "Некорректный объект команды");
            clientChannel.write(ByteBuffer.wrap(ObjectSerializer.serializeObject(errorResponse)));

            logger.warn("Got INCORRECT request FROM \"{}\". Sent response successfully ({})", clientChannel.getRemoteAddress(), errorResponse.getResponseStatus());
        }
    }

    public void stop() throws IOException {
        isRunning = false;
        selector.wakeup();
        serverSocketChannel.close();
        selector.close();

        logger.info("Сокеты и селекторы были закрыты");
    }

}
