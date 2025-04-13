package org.example.common.dtp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.common.entity.Ticket;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Класс запроса с клиента на сервер
 */
@AllArgsConstructor
@Getter
public class RequestCommand implements Serializable {
    /**
     * Название команды
     */
    private final String commandName;

    /**
     * Аргументы в строчном формате
     */
    private final ArrayList<String> args;

    /**
     * Объект Ticket (например при передаче add или update)
     */
    private final Ticket ticketObject;

    public RequestCommand(String commandName, ArrayList<String> args) {
        this(commandName, args, null);
    }

    public RequestCommand(String commandName) {
        this(commandName, null, null);
    }

    public RequestCommand(String commandName, Ticket ticketObject) {
        this(commandName, null, ticketObject);
    }
    
    public boolean isEmpty() {
        return commandName.isBlank() && args.isEmpty() && ticketObject == null;
    }

}
