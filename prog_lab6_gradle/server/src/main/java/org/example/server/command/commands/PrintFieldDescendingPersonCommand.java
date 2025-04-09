package org.example.server.command.commands;

import org.example.common.dtp.RequestCommand;
import org.example.common.dtp.Response;
import org.example.common.dtp.ResponseStatus;
import org.example.common.entity.Ticket;
import org.example.server.command.Command;
import org.example.server.managers.CollectionManager;

import java.util.PriorityQueue;

public class PrintFieldDescendingPersonCommand extends Command {
    public PrintFieldDescendingPersonCommand() {
        super("print_field_descending_person", "вывести значения поля person всех элементов в порядке убывания");
    }

    @Override
    public Response execute(RequestCommand requestCommand) {
        if (requestCommand.getArgs() != null) {
            if (!requestCommand.getArgs().isEmpty()) throw new IllegalArgumentException();
        }
        if (CollectionManager.getCollection().isEmpty()) {
            return new Response(ResponseStatus.OK, "Коллекция пуста");
        }
        StringBuilder res = new StringBuilder("Поля person элементов коллекции в порядке убывания приоритета:\n");
        PriorityQueue<Ticket> collectionCopy = new PriorityQueue<>(CollectionManager.getCollection());
        while (!collectionCopy.isEmpty()) {
            res.append(collectionCopy.poll().getPerson().toString()).append("\n");
        }
        return new Response(ResponseStatus.OK, res.toString());
    }
}
