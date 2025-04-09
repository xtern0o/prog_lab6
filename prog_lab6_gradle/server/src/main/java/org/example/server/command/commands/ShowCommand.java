package org.example.server.command.commands;

import org.example.common.dtp.RequestCommand;
import org.example.common.dtp.Response;
import org.example.common.dtp.ResponseStatus;
import org.example.common.entity.Ticket;
import org.example.server.command.Command;
import org.example.server.managers.CollectionManager;

import java.util.PriorityQueue;

public class ShowCommand extends Command {
    public ShowCommand() {
        super("show", "выводит в стандартный поток вывода все элементы коллекции в строковом представлении");
    }


    @Override
    public Response execute(RequestCommand requestCommand) {
        if (requestCommand.getArgs() != null) {
            if (!requestCommand.getArgs().isEmpty()) throw new IllegalArgumentException();
        }
        PriorityQueue<Ticket> collection = new PriorityQueue<>(CollectionManager.getCollection());
        if (collection.isEmpty()) {
            return new Response(ResponseStatus.OK, "Коллекция пуста");
        }

        return new Response(
                ResponseStatus.OK,
                "Всего элементов в коллекции: " + + collection.size() + ".\nЭлементы коллекции в порядке возростания приоритета:",
                collection
        );
    }
}
