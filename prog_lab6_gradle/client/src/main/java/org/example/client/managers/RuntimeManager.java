package org.example.client.managers;

import lombok.AllArgsConstructor;
import org.example.client.builders.TicketBuilder;
import org.example.client.cli.ConsoleInput;
import org.example.common.dtp.RequestCommand;
import org.example.common.dtp.Response;
import org.example.common.dtp.ResponseStatus;
import org.example.common.entity.Ticket;
import org.example.common.utils.Printable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.NoSuchElementException;

@AllArgsConstructor
public class RuntimeManager implements Runnable {
    private final Printable consoleOutput;
    private final ConsoleInput consoleInput;
    private final NewClient client;
    private final RunnableScriptsManager runnableScriptsManager;

    @Override
    public void run() {
        // hook, срабатывающий при завершении программы
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            consoleOutput.println("Завершение работы программы. До свидания!!");
        }));

//        if (!client.connectToServer()) return;
        client.connectToServer();

        consoleOutput.println(
                "Добро пожаловать в клиентское приложение для работы с коллекцией Ticket.\n" +
                "> \"help\" для справки по доступным командам"
        );

        while (true) {
            try {
                consoleOutput.print("$ ");
                String queryString = consoleInput.readLine().trim();

                if (queryString.isBlank()) continue;

                String[] queryParts = queryString.split(" ");

                // TODO: подумать, как не хардкодить
                processSpecialCommands(queryParts);

                Response response = client.send(
                        new RequestCommand(
                                queryParts[0],
                                new ArrayList<>(Arrays.asList(Arrays.copyOfRange(queryParts, 1, queryParts.length)))
                        )
                );
                this.printResponse(response);

                switch (response.getResponseStatus()) {
                    case OBJECT_REQUIRED -> buildObject(queryParts);
                    case EXIT -> {
                        return;
                    }
                    default -> {}
                }
            }
            catch (NoSuchElementException noSuchElementException) {
                consoleOutput.println("Конец ввода");
                return;
            }
            catch (Exception exception) {
                consoleOutput.printError(exception.getMessage());
            }
        }
    }

    public void printResponse(Response response) {
        switch (response.getResponseStatus()) {
            case OK -> {
                consoleOutput.println(response.getMessage());
                if (response.getCollection() != null) {
                    consoleOutput.println(response.getCollection().toString());
                }
            }
            case COMMAND_ERROR -> {
                consoleOutput.printError("Ошибка выполнения команды: " + response.getMessage());
            }
            case ARGS_ERROR -> {
                consoleOutput.printError("Некорректное использование аргументов команды. " + response.getMessage());
            }
            case NO_SUCH_COMMAND -> {
                consoleOutput.printError("Команда не найдена. " + response.getMessage());
            }
            case SERVER_ERROR -> {
                consoleOutput.printError(response.getMessage());
            }
            default -> {}
        }
    }

    public void buildObject(String[] queryParts) {
        Ticket ticket = new TicketBuilder(consoleOutput, consoleInput).build();
        Response responseOnBuild = client.send(
                new RequestCommand(queryParts[0], ticket)
        );
        if (responseOnBuild.getResponseStatus() != ResponseStatus.OK) {
            consoleOutput.printError("При создании объекта произошла ошибка. " + responseOnBuild.getMessage());
        }
        else {
            consoleOutput.println(responseOnBuild.getMessage());
        }
    }

    public void processSpecialCommands(String[] queryParts) {
        if (queryParts[0].equals("exit")) {
            System.exit(0);
        }
    }
}
