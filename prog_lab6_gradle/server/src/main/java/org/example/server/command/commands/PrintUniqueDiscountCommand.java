package org.example.server.command.commands;

import org.example.common.dtp.RequestCommand;
import org.example.common.dtp.Response;
import org.example.common.dtp.ResponseStatus;
import org.example.common.entity.Ticket;
import org.example.server.command.Command;
import org.example.server.managers.CollectionManager;

import java.util.List;

public class PrintUniqueDiscountCommand extends Command {
    public PrintUniqueDiscountCommand() {
        super("print_unique_discount", "вывести уникальные значения поля discount всех элементов в коллекции");
    }

    @Override
    public Response execute(RequestCommand requestCommand) {
        if (requestCommand.getArgs() != null) {
            if (!requestCommand.getArgs().isEmpty()) throw new IllegalArgumentException();
        }
        List<Float> uniqueDiscounts = CollectionManager.getCollection()
                .stream()
                .map(Ticket::getDiscount)
                .distinct()
                .toList();
        if (uniqueDiscounts.isEmpty()) {
            return new Response(ResponseStatus.OK, "Коллекция пуста");
        }
        StringBuilder res = new StringBuilder("Уникальных значений discount: " + uniqueDiscounts.size() + "\n");
        for (Float d : uniqueDiscounts) {
            res.append(": ").append(d).append("\n");
        }
        return new Response(ResponseStatus.OK, res.toString());
    }
}
