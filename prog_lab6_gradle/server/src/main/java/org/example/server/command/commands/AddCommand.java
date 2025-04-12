package org.example.server.command.commands;

import org.example.common.dtp.RequestCommand;
import org.example.common.dtp.Response;
import org.example.common.dtp.ResponseStatus;
import org.example.common.entity.Ticket;
import org.example.common.exceptions.ValidationError;
import org.example.server.command.Command;
import org.example.server.managers.CollectionManager;

public class AddCommand extends Command {
    private final CollectionManager collectionManager;

    public AddCommand(CollectionManager collectionManager) {
        super("add", "add {element} - добавить новый элемент в коллекцию");
        this.collectionManager = collectionManager;
    }


    @Override
    public Response execute(RequestCommand requestCommand) {
        if (requestCommand.getArgs() != null) {
            if (!requestCommand.getArgs().isEmpty()) throw new IllegalArgumentException();
        }

        if (requestCommand.getTicketObject() == null) {
            return new Response(ResponseStatus.OBJECT_REQUIRED, "Для выполнения команды нужно создать элемент коллекции");
        } else {
            Ticket newTicket = requestCommand.getTicketObject();
            newTicket.setId(CollectionManager.generateFreeId());
            try {
                collectionManager.addElement(newTicket);
            } catch (ValidationError validationError) {
                return new Response(ResponseStatus.VALIDATION_ERROR, "Одно или несколько полей созданного объекта не соответствуют требованиям");
            }
            return new Response(ResponseStatus.OK, "Объект успешно добавлен в коллекцию");
        }
    }
}
