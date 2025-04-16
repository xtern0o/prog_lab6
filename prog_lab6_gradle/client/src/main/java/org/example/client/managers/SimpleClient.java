package org.example.client.managers;

import org.example.common.dtp.ObjectSerializer;
import org.example.common.dtp.RequestCommand;
import org.example.common.dtp.Response;
import org.example.common.dtp.ResponseStatus;
import org.example.common.utils.Printable;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnresolvedAddressException;

/**
 * Класс клиента, отвечающий за общение с сервером
 */
public class SimpleClient implements Closeable {
    private final int port;

    private final String host;

    /**
     * максимальное число попыток переподключений прежде чем эти попытки прекратятся...
     */
    private final int maxReconnectionAttempts;

    /**
     * Задержка между переподключениями в мс
     */
    private final int reconnectionDelay;

    private final Printable consoleOutput;

    /**
     * Флаг: true -> завершить работу клиента при неудаче попыток соединения с сервером;
     * false -> не завершать работу
     */
    private boolean exitIfUnsuccessfulConnection;

    private SocketChannel socketChannel;
    private int currentReconnectionAttempt;

    public static long TIMEOUT_MS = 5000;

    public SimpleClient(
            String host,
            int port,
            int maxReconnectionAttempts,
            int reconnectionDelay,
            Printable consoleOutput,
            boolean exitIfUnsuccessfulConnection
    ) {
        this.host = host;
        this.port = port;
        this.maxReconnectionAttempts = maxReconnectionAttempts;
        this.reconnectionDelay = reconnectionDelay;
        this.consoleOutput = consoleOutput;
        this.exitIfUnsuccessfulConnection = exitIfUnsuccessfulConnection;
    }

    /**
     * Метод для соединения с сервером
     * @return true если удачно, false если анлак тотальный
     */
    public boolean connectToServer() {
        try {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress(host, port));

            // заканчиваем соединение до таймаута
            long startTime = System.currentTimeMillis();
            while (!socketChannel.finishConnect()) {
                if (System.currentTimeMillis() - startTime > TIMEOUT_MS) {
                    this.currentReconnectionAttempt = 1;
                    throw new RuntimeException("Timeout");
                }
                Thread.sleep(100);
            }

            consoleOutput.println("Подключение к серверу: " + host + ":" + port);

            return true;

        } catch (IOException ioException) {
            handleConnectionError(ioException);
            return isConnected();
        } catch (UnresolvedAddressException unresolvedAddressException) {
            consoleOutput.printError("Некорректный адрес сервака");
            return false;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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
     * Отправка запроса на сервер...
     * @param requestCommand реквест аа заеблся докать это все
     * @return ответ в формте Response
     */
    public Response send(RequestCommand requestCommand) {
        if (requestCommand.isEmpty()) return new Response(ResponseStatus.COMMAND_ERROR, "Ответ пустой");

        if (!ensureConnected()) return new Response(ResponseStatus.SERVER_ERROR, "Не удалось подключиться к серверу");
        try {
            if (!isConnected()) {
                connectToServer();
                if (!isConnected()) throw new IOException("Соединение не установлено");
                return send(requestCommand);
            }

            // Сериализация и отправка запроса
            ByteBuffer requestBuffer = ByteBuffer.wrap(ObjectSerializer.serializeObject(requestCommand));
            while (requestBuffer.hasRemaining()) socketChannel.write(requestBuffer);

            // Чтение ответа
            Thread.sleep(50);
            ByteBuffer responseBuffer = ByteBuffer.allocate(16384);

            long startTime = System.currentTimeMillis();
            while (true) {
                int bytesRead = socketChannel.read(responseBuffer);
                if (bytesRead > 0) break;
                if (bytesRead == -1) {
                    throw new IOException("Соединение закрыто");
                }

                if (System.currentTimeMillis() - startTime > TIMEOUT_MS) {
                    return new Response(ResponseStatus.SERVER_ERROR, "Превышено время ожидания ответа");
                }
            }

            responseBuffer.flip();
            byte[] responseBytes = new byte[responseBuffer.remaining()];
            responseBuffer.get(responseBytes);

            // Десериализация ответа
            return (Response) ObjectSerializer.deserializeObject(responseBytes);
        } catch (IOException ioException) {
            reconnect();
            if (!isConnected()) return new Response(ResponseStatus.SERVER_ERROR, "Ошибка сервера: " + ioException.getMessage());
            return send(requestCommand);
        } catch (ClassNotFoundException classNotFoundException) {
            return new Response(ResponseStatus.SERVER_ERROR, "Некорректный формат данных от сервера");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (UnresolvedAddressException unresolvedAddressException) {
            throw new RuntimeException("Неверный адрес сервака");
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
     * Переподсоединение к серверу через делэй
     */
    private void reconnect() {
        if (currentReconnectionAttempt < maxReconnectionAttempts) {
            try {
                consoleOutput.println("Переподключение через: " + reconnectionDelay + " мс");
                currentReconnectionAttempt++;

                close();
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
     * Закрытие ресурсов для завершения подключения
     */
    public void close() {
        try {
            if (socketChannel != null && socketChannel.isOpen()) socketChannel.close();
        } catch (IOException ioException) {
            consoleOutput.printError("Ошибка закрытия ресурсов: " + ioException.getMessage());
        }
    }

    /**
     * Соединены или нет.
     * @return ДА или НЕТ.
     */
    public boolean isConnected() {
        try {
            Thread.sleep(50);
            return socketChannel != null && socketChannel.isConnected();
        } catch (InterruptedException interruptedException) {
            throw new RuntimeException(interruptedException);
        }
    }
}
