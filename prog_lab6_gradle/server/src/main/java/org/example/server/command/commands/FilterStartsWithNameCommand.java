package org.example.server.command.commands;

import org.example.common.dtp.RequestCommand;
import org.example.common.dtp.Response;
import org.example.common.dtp.ResponseStatus;
import org.example.common.entity.Ticket;
import org.example.server.command.Command;
import org.example.server.managers.CollectionManager;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

public class FilterStartsWithNameCommand extends Command {
    private final CollectionManager collectionManager;

    public FilterStartsWithNameCommand(CollectionManager collectionManager) {
        super("filter_starts_with_name", "вывести элементы, значение поля name которых начинается с заданной подстроки");
        this.collectionManager = collectionManager;
    }


    @Override
    public Response execute(RequestCommand requestCommand) {
        if (requestCommand.getArgs().size() != 1) throw new IllegalArgumentException();

        PriorityQueue<Ticket> collection = CollectionManager.getCollection().stream()
                .filter(ticket -> ticket.getName().startsWith(requestCommand.getArgs().get(0)))
                .sorted(Comparator.comparing(Ticket::getCoordinates))
                .collect(Collectors.toCollection(PriorityQueue::new));

        if (collection.isEmpty()) {
            return new Response(ResponseStatus.OK, "Не найдено билетов, название которых начинается на \"" + requestCommand.getArgs().get(0) + "\"");
        }
        return new Response(ResponseStatus.OK, "Найдено " + collection.size() + " билетов, название которых начинается на \"" + requestCommand.getArgs().get(0) + "\"", collection);
    }
}
