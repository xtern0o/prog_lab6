package org.example.server.command.commands;

import org.example.common.dtp.RequestCommand;
import org.example.common.dtp.Response;
import org.example.common.dtp.ResponseStatus;
import org.example.server.command.Command;
import org.example.server.managers.CollectionManager;

public class RemoveByIdCommand extends Command {
    private final CollectionManager collectionManager;

    public RemoveByIdCommand(CollectionManager collectionManager) {
        super("remove_by_id", "удаляет элемент из коллекции по его id");
        this.collectionManager = collectionManager;
    }

    @Override
    public Response execute(RequestCommand requestCommand) {
        if (requestCommand.getArgs().size() != 1) throw new IllegalArgumentException();

        try {
            int id = Integer.parseInt(requestCommand.getArgs().get(0));
            if (collectionManager.removeById(id)) {
                return new Response(ResponseStatus.OK, String.format("Элемент с id=%d был успешно удален", id));
            }
            return new Response(ResponseStatus.OK, String.format("Элемент с id=%d не найден", id));
        } catch (NumberFormatException numberFormatException) {
            throw new IllegalArgumentException("Команда принимает целочисленный аргумент");
        }

    }
}
