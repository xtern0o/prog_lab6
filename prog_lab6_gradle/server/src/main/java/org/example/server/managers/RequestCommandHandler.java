package org.example.server.managers;

import org.example.common.dtp.RequestCommand;
import org.example.common.dtp.Response;
import org.example.common.dtp.ResponseStatus;
import org.example.common.exceptions.NoSuchCommand;

public class RequestCommandHandler {
    private final CommandManager commandManager;

    public RequestCommandHandler(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    public Response handleRequestCommand(RequestCommand requestCommand) {
        try {
            return commandManager.execute(requestCommand);
        } catch (NoSuchCommand noSuchCommand) {
            return new Response(ResponseStatus.COMMAND_ERROR, "Такой команды нет. Воспользуйтесь help для того, чтобы узнать больше о доступных командах");
        } catch (IllegalArgumentException illegalArgumentException) {
            return new Response(ResponseStatus.COMMAND_ERROR, "Неверное использование аргументов. " + illegalArgumentException.getMessage());
        }
    }
}
