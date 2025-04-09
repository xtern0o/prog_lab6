package org.example.server;

import org.example.common.utils.Printable;
import org.example.server.cli.ConsoleOutput;
import org.example.server.command.Command;
import org.example.server.command.commands.*;
import org.example.server.managers.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {
    static int port;
    static CollectionManager collectionManager = new CollectionManager();
    static CommandManager commandManager = new CommandManager();
    static RequestCommandHandler requestCommandHandler = new RequestCommandHandler(commandManager);
    static ConsoleOutput consoleOutput = new ConsoleOutput();

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
                new PrintFieldDescendingPersonCommand()
//                new ExecuteScriptCommand(consoleOutput, commandManager)
        )
        );
        commandManager.addCommands(commands);

        Server server = new Server(port, requestCommandHandler, consoleOutput);
        RuntimeManager runtimeManager = new RuntimeManager(consoleOutput, server, fileManager);

        try {
            runtimeManager.run();
        } catch (RuntimeException runtimeException) {
            consoleOutput.printError("Ощибка выполнения программы: " + runtimeException.getMessage());
        }

    }

    public static boolean validateArgs(String[] args) {
        if (args.length == 0) {
            consoleOutput.printError("Вы не ввели путь файла.\nКорректный запуск программы: java -jar <путь до программы> <файл с данными>.json\nДо свидания! :)");
            return false;
        }
        else if (args.length == 1) {
            consoleOutput.printError("Программа принимает 2 аргумента. До свидания! :)");
            return false;
        } else if (args.length < 3) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                consoleOutput.printError("Некорректный порт");
                return false;
            }

        }
        return true;
    }

}