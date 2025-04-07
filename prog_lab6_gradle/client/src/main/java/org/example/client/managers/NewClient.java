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

public class NewClient {
    private final int port;
    private final String host;
    private final int maxReconnectionAttempts;
    private final int reconnectionDelay;
    private final Printable consoleOutput;
    private boolean exitIfUnsuccessfulConnection;

    private SocketChannel socketChannel;
    private Selector selector;
    private int currentReconnectionAttempt;
    private long nextReconnectionTime;

    private RequestCommand pendingRequest;
    private ByteBuffer byteBuffer = ByteBuffer.allocate(16384);

    public NewClient(
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
        this.exitIfUnsuccessfulConnection = false;
    }

    public NewClient(
            String host,
            int port,
            int maxReconnectionAttempts,
            int reconnectionDelay,
            Printable consoleOutput,
            boolean exitIfUnsuccessfulConnection
    ) {
        this(host, port, maxReconnectionAttempts, reconnectionDelay, consoleOutput);
        this.exitIfUnsuccessfulConnection = true;
    }

    /**
     * Устанавливает соединение с серваком
     * @return тру если да
     */
    public boolean connectToServer() {
        try {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress(host, port));

            selector = Selector.open();
            socketChannel.register(selector, SelectionKey.OP_CONNECT);

            if (!isConnected()) throw new IOException();

            return true;
        } catch (IOException | UnresolvedAddressException ioException) {
            handleConnectionError(ioException);
            return false;
        }
    }

    /**
     * Метод для примитивной отправки сериализованного объекта данных
     * @throws IOException
     */
    private void sendRequest() throws IOException {
        byte[] serializedRequest = serializeObject(pendingRequest);
        ByteBuffer data = ByteBuffer.wrap(serializedRequest);
        while (data.hasRemaining()) {
            socketChannel.write(data);
        }
    }

    /**
     * Метод для примитивного чтения ответа сервера
     * @return ответ (Response)
     * @throws IOException
     */
    private Response readResponse() throws IOException {
        byteBuffer.clear();
        int read = socketChannel.read(byteBuffer);

        if (read == -1) throw new IOException("Соединение закрыто");
        if (read == 0) return null;

        byteBuffer.flip();
        try {
            return (Response) deserializeObject(byteBuffer.array());
        } catch (ClassNotFoundException e) {
            throw new IOException("Неверный формат ответа сервера", e);
        }
    }

    /**
     * Метод для отправки запроса и чтения ответа
     * @param requestCommand request
     * @return Response
     */
    public Response send(RequestCommand requestCommand) {
        if (requestCommand.isEmpty()) return new Response(ResponseStatus.ARGS_ERROR, "Запрос пустой");

        this.pendingRequest = requestCommand;

        if (!ensureConnected()) return new Response(ResponseStatus.SERVER_ERROR, "Не удалось подключиться к серваку");

        try {
            processNetworkEvents();
            return readResponse();
        } catch (IOException ioException) {
            handleConnectionError(ioException);
            if (isConnected()) return send(requestCommand);
            return new Response(ResponseStatus.SERVER_ERROR, "Ошибка сервера: " + ioException.getMessage());
        }

    }

    /**
     * Переподсоединение к серверу через делэй
     */
    private void reconnect() {
        if (currentReconnectionAttempt < maxReconnectionAttempts) {
            try {
                consoleOutput.println("Переподключение через: " + reconnectionDelay + " мс");
                currentReconnectionAttempt++;

                closeResources();
                Thread.sleep(reconnectionDelay);
                consoleOutput.println(String.format("Попытка: %d/%d", currentReconnectionAttempt, maxReconnectionAttempts));

                connectToServer();

                return;

            } catch (InterruptedException interruptedIOException) {
                consoleOutput.printError("Прерывание во время переподключения");
            }
        }
        handleFailedReconnect();
    }

    /**
     * Сценарий для программы в случае неудачного подключения по истечении <maxReconnectionAttempts> попыток
     */
    private void handleFailedReconnect() {
        currentReconnectionAttempt = 0;
        if (exitIfUnsuccessfulConnection) {
            consoleOutput.println("Не удалось подключиться к серверу. Завершение работы");
            System.exit(-1);
        }
    }

    /**
     * Сценарий для программы в случае ошибки подключения
     * @param exception
     */
    private void handleConnectionError(Exception exception) {
        consoleOutput.printError("Соединение НЕ установлено.");

        // если неверный адрес то до свидания
        if (exception instanceof UnresolvedAddressException) {
            consoleOutput.printError("Некорректный адрес сервака");
            currentReconnectionAttempt = maxReconnectionAttempts;
            return;
        }

        reconnect();
    }

    /**
     * Метод для проверки, соединены ли мы с сервером на данный момент
     * @return true если соединены, false если...нет...
     */
    public boolean isConnected() {
        return socketChannel != null && socketChannel.isConnected();
    }

    /**
     * Метод для инициации переподключения в случае если подключение потеряно где то при выполнении
     * @return true если подсоединились, false если нет
     */
    private boolean ensureConnected() {
        if (isConnected()) return true;
        if (currentReconnectionAttempt >= maxReconnectionAttempts) return false;

        reconnect();

        return isConnected();
    }

    /**
     * Сериализация объекта в поток байтов
     * @param obj объект для сериализации
     * @return байты
     * @throws IOException если чот не то)
     */
    private byte[] serializeObject(Object obj) throws IOException {
        try (
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        ) {
            objectOutputStream.writeObject(obj);
            objectOutputStream.flush();
            return byteArrayOutputStream.toByteArray();
        }
    }

    /**
     * десериализация объекта из потока байтов
     * @param bytes поток байтов
     * @return объект
     * @throws IOException если в стримах чот не то)
     * @throws ClassNotFoundException если сервак послал не тот класс)
     */
    private Object deserializeObject(byte[] bytes) throws IOException, ClassNotFoundException {
        try (
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)
        ) {
            return objectInputStream.readObject();
        }
    }

    /**
     * Закрытие ресурсов для завершения подключения
     */
    private void closeResources() {
        try {
            if (socketChannel != null && socketChannel.isOpen()) socketChannel.close();
            if (selector != null && selector.isOpen()) selector.close();
        } catch (IOException ioException) {
            consoleOutput.printError("Ошибка закрытия ресурсов: " + ioException.getMessage());
        }
    }

    /**
     * Отправить запрос туда, куда надо отправить, и подключиться туда, куда надо подключиться
     * @throws IOException
     */
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

    /**
     * Завершение соединения
     * @param key
     * @throws IOException
     */
    private void finishConnection(SelectionKey key) throws IOException {
        if (socketChannel.finishConnect()) {
            consoleOutput.println(String.format("Установлено соединение (%s:%d)", host, port));
            key.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
            currentReconnectionAttempt = 0;
        }
    }

}
