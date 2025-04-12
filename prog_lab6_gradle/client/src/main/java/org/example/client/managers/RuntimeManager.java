package org.example.client.managers;

import lombok.AllArgsConstructor;
import org.example.client.builders.TicketBuilder;
import org.example.client.cli.ConsoleInput;
import org.example.common.dtp.RequestCommand;
import org.example.common.dtp.Response;
import org.example.common.dtp.ResponseStatus;
import org.example.common.entity.Ticket;
import org.example.common.utils.Printable;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.NoSuchElementException;

@AllArgsConstructor
public class RuntimeManager implements Runnable {
    private final Printable consoleOutput;
    private final ConsoleInput consoleInput;
    private final SimpleClient client;
    private final RunnableScriptsManager runnableScriptsManager;

    @Override
    public void run() {
        // hook, срабатывающий при завершении программы
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            consoleOutput.println("Завершение работы программы. До свидания!!");
        }));

//        if (!client.connectToServer()) return;
//        client.connectToServer();

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
                // ну, 1 команда терпимо
                processSpecialCommands(queryParts);

                Response response = client.send(
                        new RequestCommand(
                                queryParts[0],
                                new ArrayList<>(Arrays.asList(Arrays.copyOfRange(queryParts, 1, queryParts.length)))
                        )
                );
                if (response == null) {
                    consoleOutput.println("Запрос пустой");
                    continue;
                }
                this.printResponse(response);

                switch (response.getResponseStatus()) {
                    case OBJECT_REQUIRED -> {
                        buildObject(queryParts);
                        System.out.println(Arrays.toString(queryParts));
                    }
                    default -> {}
                }
            } catch (NoSuchElementException noSuchElementException) {
                consoleOutput.println("Конец ввода");
                return;
            } catch (Exception exception) {
                consoleOutput.printError(exception.getMessage());
            }
        }
    }

    public void printResponse(Response response) {
        switch (response.getResponseStatus()) {
            case OK -> {
                consoleOutput.println(response.getMessage());
                if (response.getCollection() != null) {
                    for (Ticket t : response.getCollection()) {
                        consoleOutput.println(t.toString());
                    }
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
            case EXECUTE_SCRIPT -> {
                handleExecuteScript(response);
            }
            default -> {}
        }
    }

    public void handleExecuteScript(Response response) {
        try {
            File scriptFile = new File(response.getMessage());
            if (!scriptFile.exists()) throw new FileNotFoundException(String.format("Исполняемый файл \"%s\" не найден", scriptFile.getName()));

            consoleOutput.println(String.format("* Исполнение файла \"%s\"", scriptFile.getName()));

            if (RunnableScriptsManager.checkIfLaunchedInStack(scriptFile)) {
                consoleOutput.printError(String.format("Исполняемый файл %s был вызван более одного раза в рамках одного скрипта. Исправьте код.", scriptFile.getName()));
                return;
            }

            ConsoleInput.setFileMode(true);
            RunnableScriptsManager.addFile(scriptFile);

            for (String line = runnableScriptsManager.readLine(); line != null; line = runnableScriptsManager.readLine()) {
                String queryString = line.trim();

                if (queryString.isBlank()) continue;

                String[] queryParts = queryString.split(" ");

                Response response1 = client.send(
                        new RequestCommand(
                                queryParts[0],
                                new ArrayList<>(Arrays.asList(Arrays.copyOfRange(queryParts, 1, queryParts.length)))
                        )
                );

                if (response1 == null) {
                    consoleOutput.println("Запрос пустой");
                    continue;
                }
                this.printResponse(response1);

                switch (response1.getResponseStatus()) {
                    case OBJECT_REQUIRED -> {
                        buildObject(queryParts);
                        System.out.println(Arrays.toString(queryParts));
                    }
                    default -> {}
                }
            }

            consoleOutput.println("* Завершение исполнения файла " + scriptFile.getName());

            RunnableScriptsManager.removeFile(scriptFile);

            ConsoleInput.setFileMode(false);

        } catch (FileNotFoundException fileNotFoundException) {
            consoleOutput.printError(String.format("Исполняемый файл \"%s\" не найден", response.getMessage()));
        } catch (Exception exception) {
            RunnableScriptsManager.clear();
            throw new RuntimeException(exception);
        }
    }

    public void buildObject(String[] queryParts) {
        Ticket ticket = new TicketBuilder(consoleOutput, consoleInput).build();
        Response responseOnBuild = client.send(
                new RequestCommand(
                        queryParts[0],
                        new ArrayList<>(Arrays.asList(Arrays.copyOfRange(queryParts, 1, queryParts.length))),
                        ticket
                )
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
