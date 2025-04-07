package org.example.client.managers;

import org.example.common.dtp.RequestCommand;
import org.example.common.dtp.Response;
import org.example.common.dtp.ResponseStatus;
import org.example.common.utils.Printable;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnresolvedAddressException;
import java.util.Iterator;
import java.util.Set;

public class Client {
    private final int port;
    private final String host;
    private final int maxReconnectionAttempts;
    private final int reconnectionDelay;
    private final Printable consoleOutput;

    private int currentReconnectionAttempt;
    private SocketChannel socketChannel;
    private Selector selector;
    private ByteBuffer byteBuffer = ByteBuffer.allocate(16384);
    private RequestCommand pendingRequest;
    private boolean connectionInProgress;
    private long nextReconnectionTime;

    public Client(
            String host,
            int port,
            int maxReconnectionAttempts,
            int reconnectionDelay,
            Printable consoleOutput
    ) {
        this.host = host;
        this.port = port;
        this.maxReconnectionAttempts = maxReconnectionAttempts;
        this.reconnectionDelay = reconnectionDelay;
        this.consoleOutput = consoleOutput;
    }

    public void handleConnectionError(Exception exception) {
        consoleOutput.printError("Ошибка соединения: " + exception.getMessage());

        if (exception instanceof UnresolvedAddressException) {
            consoleOutput.printError("Неверный адрес сервера");
            currentReconnectionAttempt = maxReconnectionAttempts;
        }

        if (currentReconnectionAttempt < maxReconnectionAttempts) {
            consoleOutput.println("Переподключение через: " + calculateDelay() + " мс");
            closeResources();
            connectToServer();
            currentReconnectionAttempt++;

        }

        closeResources();

    }

    private void closeResources() {
        try {
            if (socketChannel != null && socketChannel.isOpen()) socketChannel.close();
            if (selector != null && selector.isOpen()) selector.close();
        } catch (IOException ioException) {
            consoleOutput.printError("Ошибка закрытия ресурсов: " + ioException.getMessage());
        }
    }

    public boolean connectToServer() {
        try {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress(host, port));

            selector = Selector.open();
            socketChannel.register(selector, SelectionKey.OP_CONNECT);

            return true;
        } catch (IOException | UnresolvedAddressException ioException) {
            handleConnectionError(ioException);
            return false;
        }
    }

    public boolean isConnected() {
        return socketChannel != null && socketChannel.isConnected();
    }

    private void resetConnectionState() {
        currentReconnectionAttempt = 0;
    }

    public Response send(RequestCommand requestCommand) {
        if (requestCommand.isEmpty()) return new Response(ResponseStatus.ARGS_ERROR, "Пустой запрос");

        this.pendingRequest = requestCommand;

        try {
            if (!ensureConnected()) {
                return new Response(ResponseStatus.SERVER_ERROR, "Не удалось подключиться");
            }

            processNetworkEvents();
            return readResponse();
        } catch (IOException ioException) {
            handleConnectionError(ioException);
            return new Response(ResponseStatus.SERVER_ERROR, "Ошибка сервера: " + ioException.getMessage());
        }
    }

    private boolean ensureConnected() throws IOException {
        if (isConnected()) return true;
        if (currentReconnectionAttempt >= maxReconnectionAttempts) return false;

        long now = System.currentTimeMillis();
        if (now < nextReconnectionTime) return false;

        consoleOutput.println(String.format("Попытка подключения (%d/%d)", currentReconnectionAttempt + 1, maxReconnectionAttempts));

        closeResources();
        connectToServer();
        currentReconnectionAttempt++;
        nextReconnectionTime = now + calculateDelay();

        return true;
    }

    /**
     * Метод для расчета текущего делэя (полезно для экспоненциального делэя)
     * @return милисекунды делэй
     */
    private int calculateDelay() {
        return reconnectionDelay;
    }

    private void processNetworkEvents() throws IOException {
        if (selector.select(300) == 0) return;

        Set<SelectionKey> keys = selector.selectedKeys();
        Iterator<SelectionKey> iterKeys = keys.iterator();

        while (iterKeys.hasNext()) {
            SelectionKey key = iterKeys.next();
            iterKeys.remove();

            if (key.isConnectable()) finishConnection(key);
            if (key.isWritable()) sendRequest();
        }
    }

    private void finishConnection(SelectionKey key) throws IOException {
        if (socketChannel.finishConnect()) {
            consoleOutput.println(String.format("Установлено соединение (%s:%d)", host, port));
            key.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
            currentReconnectionAttempt = 0;
        }
    }

    private void sendRequest() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(pendingRequest);
        }

        ByteBuffer data = ByteBuffer.wrap(baos.toByteArray());
        while (data.hasRemaining()) {
            socketChannel.write(data);
        }
    }

    private Response readResponse() throws IOException {
        byteBuffer.clear();
        int read = socketChannel.read(byteBuffer);

        if (read == -1) throw new IOException("Соединение закрыто");
        if (read == 0) return null;

        byteBuffer.flip();
        try (ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(byteBuffer.array(), 0, byteBuffer.limit()))) {
            return (Response) objectInputStream.readObject();
        } catch (ClassNotFoundException classNotFoundException) {
            throw new IOException("Неверный формат ответа сервера", classNotFoundException);
        }
    }


}
