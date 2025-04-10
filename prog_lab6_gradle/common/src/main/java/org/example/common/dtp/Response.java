package org.example.common.dtp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.common.entity.Ticket;

import java.io.Serializable;
import java.util.Collection;

@Getter
@AllArgsConstructor
public class Response implements Serializable {
    private final ResponseStatus responseStatus;
    private final String message;
    private final Collection<Ticket> collection;

    public Response(ResponseStatus responseStatus, String message) {
        this(responseStatus, message, null);
    }
}
