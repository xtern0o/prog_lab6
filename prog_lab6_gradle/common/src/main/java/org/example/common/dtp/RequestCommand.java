package org.example.common.dtp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.common.entity.Ticket;

import java.io.Serializable;
import java.util.ArrayList;

@AllArgsConstructor
@Getter
public class RequestCommand implements Serializable {
    private final String commandName;
    private final ArrayList<String> args;
    private final Ticket ticketObject;

    public RequestCommand(String commandName, ArrayList<String> args) {
        this(commandName, args, null);
    }

    public RequestCommand(String commandName) {
        this(commandName, null, null);
    }

}
