package org.example.server.command.commands;

import org.example.common.dtp.RequestCommand;
import org.example.common.dtp.Response;
import org.example.common.dtp.ResponseStatus;
import org.example.server.command.Command;
import org.example.server.managers.CommandManager;

import java.util.stream.Collectors;

public class HelpCommand extends Command {
    private final CommandManager commandManager;

    public HelpCommand(CommandManager commandManager) {
        super("help", "Вывод справки о доступных командах");
        this.commandManager = commandManager;
    }
    
    @Override
    public Response execute(RequestCommand requestCommand) {
        if (requestCommand.getArgs() != null) {
            if (!requestCommand.getArgs().isEmpty()) throw new IllegalArgumentException();
        }
        return new Response(
                ResponseStatus.OK,
                "Краткая справка по всем командам: \n" +
                        commandManager.getCommands().values().stream()
                                .map(Command::toString)
                                .collect(Collectors.joining("\n"))
        );
    }
}
