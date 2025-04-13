package org.example.common.dtp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.common.entity.Ticket;

import java.io.Serializable;
import java.util.Collection;

/**
 * Ответ сервера
 */
@Getter
@AllArgsConstructor
public class Response implements Serializable {
    /**
     * Енам статуса для категоризации ответа
     */
    private final ResponseStatus responseStatus;

    /**
     * Сообщение сервера
     */
    private final String message;

    /**
     * Коллекция Ticket при запросе коллекции
     */
    private final Collection<Ticket> collection;

    public Response(ResponseStatus responseStatus, String message) {
        this(responseStatus, message, null);
    }
}
