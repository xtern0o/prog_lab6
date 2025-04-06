package org.example.client.managers;

import org.example.common.dtp.RequestCommand;
import org.example.common.dtp.Response;
import org.example.common.dtp.ResponseStatus;
import org.example.common.utils.Printable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Менеджер клиента, управляющий соединением с сервером, запросами и приемом ответов
 * @author maxkarn
 */
public class Client {
    private final int port;
    private final String host;
    private final int maxReconnectionAttempts;
    private final int reconnectionDelay;
    private final Printable consoleOutput;

    private Socket socket;
    private ObjectOutputStream serverWriter;
    private ObjectInputStream serverReader;
    private int currentReconnectionAttempt;

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

    public boolean connectToServer() {
        try {
            if (this.currentReconnectionAttempt > 0) {
                consoleOutput.println(
                        String.format(
                                "Повторное подключение. Попытка: %d/%d",
                                this.currentReconnectionAttempt,
                                this.maxReconnectionAttempts
                        )
                );
            }
            this.socket = new Socket(host, port);
            this.serverWriter = new ObjectOutputStream(socket.getOutputStream());
            return true;
        } catch (UnknownHostException e) {
            consoleOutput.printError("Неверный адрес сервера: проверьте корректность хоста и порта");
            return false;
        } catch (IOException e) {
            consoleOutput.printError("Произошла ошибка на стороне сервера");
            return false;
        }
    }

    /**
     * отправляет запрос, принимая ответ от сервера
     * @param requestCommand запрос (команда)
     * @return response object
     */
    public Response sendRequest(RequestCommand requestCommand) {
        while (true) {
            try {
                if (serverWriter == null) throw new IOException();

                if (requestCommand.isEmpty()) return new Response(ResponseStatus.ARGS_ERROR, "Пустой запрос");

                serverWriter.writeObject(requestCommand);
                serverWriter.flush();

                this.serverReader = new ObjectInputStream(socket.getInputStream());
                Response response = (Response) serverReader.readObject();

                this.currentReconnectionAttempt = 0;
                return response;
            }
            catch (IOException ioException) {
                if (currentReconnectionAttempt == 0) {
                    connectToServer();
                    currentReconnectionAttempt++;
                }
                else {
//                    consoleOutput.println("Соединение с сервером не установлено");
                    try {
                        if (currentReconnectionAttempt >= maxReconnectionAttempts) {
                            currentReconnectionAttempt = 0;
                            return new Response(ResponseStatus.SERVER_ERROR, "Не удалось установать соединение с сервером");
                        }
                        else {
                            currentReconnectionAttempt++;
                            consoleOutput.println("Повторная попытка подключения через " + reconnectionDelay + " мс");
                            Thread.sleep(reconnectionDelay);
                            connectToServer();
                        }
                    }
                    catch (Exception exception) {
                        consoleOutput.printError("Неудачная попытка подключения: " + exception.getMessage());
                    }
                }

            } catch (ClassNotFoundException classNotFoundException) {
                throw new RuntimeException(classNotFoundException);
            }
        }
    }

    /**
     * Закрывает соединение с сервером
     */
    public void closeConnection() {
        try {
            this.serverReader.close();
            this.serverWriter.close();
            this.socket.close();
        } catch (IOException ioException) {
            consoleOutput.printError("Вы итак не подключены к серверу");
        }

    }



}
