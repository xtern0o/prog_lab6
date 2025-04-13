package org.example.server;

import org.example.server.cli.ConsoleOutput;
import org.example.server.command.Command;
import org.example.server.command.commands.*;
import org.example.server.managers.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {
    static int port;
    static CollectionManager collectionManager = new CollectionManager();
    static CommandManager commandManager = new CommandManager();
    static RequestCommandHandler requestCommandHandler = new RequestCommandHandler(commandManager);
    static ConsoleOutput consoleOutput = new ConsoleOutput();

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        if (!validateArgs(args)) return;

        FileManager fileManager = new FileManager(new File(args[0]), consoleOutput);

        if (!fileManager.validate()) return;

        fileManager.deserializeCollectionFromJSON();

        ArrayList<Command> commands = new ArrayList<>(Arrays.asList(
                new HelpCommand(commandManager),
                new HistoryCommand(commandManager),
                new AddCommand(collectionManager),
                new ShowCommand(),
                new InfoCommand(collectionManager),
                new ClearCommand(collectionManager),
                new UpdateCommand(collectionManager),
                new RemoveByIdCommand(collectionManager),
                new HeadCommand(collectionManager),
                new RemoveHeadCommand(collectionManager),
                new FilterStartsWithNameCommand(collectionManager),
                new PrintUniqueDiscountCommand(),
                new PrintFieldDescendingPersonCommand(),
                new ExecuteScriptCommand()
        )
        );
        commandManager.addCommands(commands);

        Server server = new Server(port, requestCommandHandler, consoleOutput);
        RuntimeManager runtimeManager = new RuntimeManager(consoleOutput, server, fileManager);

        try {
            runtimeManager.run();
        } catch (RuntimeException runtimeException) {
            logger.error("Ошибка выполнения программы: {}", runtimeException.getMessage());
        }

    }

    public static boolean validateArgs(String[] args) {
        if (args.length != 2) {
            logger.error("Неверное количество аргументов при запуске");
            consoleOutput.println("* Корректный запуск программы: java -jar <путь до программы> <файл с данными>.json <порт прослушивания>");
            return false;
        }
        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            logger.error("Некорректный порт");
            return false;
        }
        return true;
    }

}