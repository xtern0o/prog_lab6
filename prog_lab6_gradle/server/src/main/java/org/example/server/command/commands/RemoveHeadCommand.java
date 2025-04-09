package org.example.server.command.commands;

import org.example.common.dtp.RequestCommand;
import org.example.common.dtp.Response;
import org.example.common.dtp.ResponseStatus;
import org.example.server.command.Command;
import org.example.server.managers.CollectionManager;

public class RemoveHeadCommand extends Command {
    private final CollectionManager collectionManager;

    public RemoveHeadCommand(CollectionManager collectionManager) {
        super("remove_head", "выводит первый элемент коллекции и удаляет его");
        this.collectionManager = collectionManager;
    }


    @Override
    public Response execute(RequestCommand requestCommand) {
        if (requestCommand.getArgs() != null) {
            if (!requestCommand.getArgs().isEmpty()) throw new IllegalArgumentException();
        }
        if (collectionManager.getCollectionSize() == 0) {
            return new Response(ResponseStatus.OK, "Коллекция пуста");
        }

        return new Response(
                ResponseStatus.OK,
                "Эта запись была удалена:\n" + CollectionManager.getCollection().poll()
        );
    }
}
