package org.example.server.command.commands;

import org.example.common.dtp.RequestCommand;
import org.example.common.dtp.Response;
import org.example.common.dtp.ResponseStatus;
import org.example.common.entity.Ticket;
import org.example.common.exceptions.ValidationError;
import org.example.server.command.Command;
import org.example.server.managers.CollectionManager;

public class UpdateCommand extends Command {
    private final CollectionManager collectionManager;

    public UpdateCommand(CollectionManager collectionManager) {
        super("update", "обновить элемент с введенным id");
        this.collectionManager = collectionManager;
    }

    @Override
    public Response execute(RequestCommand requestCommand) {
        if (requestCommand.getArgs().size() != 1) throw new IllegalArgumentException();

        try {
            int id = Integer.parseInt(requestCommand.getArgs().get(0));
            if (collectionManager.getElementById(id) == null) {
                return new Response(ResponseStatus.ARGS_ERROR, String.format("Объекта с id=%d не существует", id));
            }

            if (requestCommand.getTicketObject() == null) {
                return new Response(ResponseStatus.OBJECT_REQUIRED, "Для выполнения команды нужно создать элемент коллекции");
            }
            if (collectionManager.removeById(id)) {
                Ticket newTicket = requestCommand.getTicketObject();
                newTicket.setId(id);
                try {
                    collectionManager.addElement(newTicket);
                } catch (ValidationError validationError) {
                    return new Response(ResponseStatus.VALIDATION_ERROR, "Одно или несколько полей нового объекта не соответствуют требованиям");
                }
                return new Response(ResponseStatus.OK, String.format("Объект с id=%d был учпешно изменен", id));
            }
            else {
                return new Response(ResponseStatus.ARGS_ERROR, String.format("Объекта с id=%d не существует", id));
            }
        } catch (NumberFormatException numberFormatException) {
            throw new IllegalArgumentException("Id - целое число");
        }
    }
}
