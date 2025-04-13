package org.example.server.managers;

import org.example.common.dtp.RequestCommand;
import org.example.common.dtp.Response;
import org.example.common.dtp.ResponseStatus;
import org.example.common.exceptions.NoSuchCommand;

/**
 * Класс для обработки запросов с командами
 * @author maxkarn
 */
public class RequestCommandHandler {
    private final CommandManager commandManager;

    public RequestCommandHandler(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    /**
     * Метод обработки запросов
     * Если все эщкере то делаем
     * Если нет команды или там с аргументами не то чето, то говорим
     * @param requestCommand request from lovely loved user
     * @return response
     */
    public Response handleRequestCommand(RequestCommand requestCommand) {
        try {
            return commandManager.execute(requestCommand);
        } catch (NoSuchCommand noSuchCommand) {
            return new Response(ResponseStatus.NO_SUCH_COMMAND, "Команда \"" + requestCommand.getCommandName() + "\" не найдена");
        } catch (IllegalArgumentException illegalArgumentException) {
            return new Response(ResponseStatus.ARGS_ERROR, "Неверное использование аргументов. " + illegalArgumentException.getMessage());
        }
    }
}
