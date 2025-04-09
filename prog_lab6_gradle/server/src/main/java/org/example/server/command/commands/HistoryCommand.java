package org.example.server.command.commands;

import org.example.common.dtp.RequestCommand;
import org.example.common.dtp.Response;
import org.example.common.dtp.ResponseStatus;
import org.example.server.command.Command;
import org.example.server.managers.CommandManager;

import java.util.ArrayList;

public class HistoryCommand extends Command {
    private final CommandManager commandManager;

    public HistoryCommand(CommandManager commandManager) {
        super("history", "Выводит названия пяти последних выполненных команд");
        this.commandManager = commandManager;
    }
    @Override
    public Response execute(RequestCommand requestCommand) {
        if (requestCommand.getArgs() != null) {
            if (!requestCommand.getArgs().isEmpty()) throw new IllegalArgumentException();
        }
        ArrayList<Command> history = commandManager.getHistory();
        if (history.size() > 5) history = new ArrayList<>(history.subList(history.size() - 5, history.size()));
        if (history.isEmpty()) {
            return new Response(ResponseStatus.OK, "Похоже, это ваша первая команда за сессию");
        }

        StringBuilder res = new StringBuilder("Последние " + history.size() + " команд:\n");
        for (int i = 1; i <= history.size(); i++ ) {
            res.append(String.format("%d. %s\n", i, history.get(i - 1)));
        }

        return new Response(ResponseStatus.OK, res.toString());
    }
}
