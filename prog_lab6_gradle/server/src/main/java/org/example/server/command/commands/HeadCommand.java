package org.example.server.command.commands;

import org.example.common.dtp.RequestCommand;
import org.example.common.dtp.Response;
import org.example.common.dtp.ResponseStatus;
import org.example.server.command.Command;
import org.example.server.managers.CollectionManager;

public class HeadCommand extends Command {
    private final CollectionManager collectionManager;

    public HeadCommand(CollectionManager collectionManager) {
        super("head", "выводит первый элемент коллекции");
        this.collectionManager = collectionManager;
    }

    @Override
    public Response execute(RequestCommand requestCommand) {
        if (requestCommand.getArgs() != null) {
            if (!requestCommand.getArgs().isEmpty()) throw new IllegalArgumentException();
        }
        if (CollectionManager.getCollection().isEmpty()) {
            return new Response(ResponseStatus.OK, "Коллекция пуста");
        }
        return new Response(ResponseStatus.OK, CollectionManager.getCollection().peek().toString());
    }
}
