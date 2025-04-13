package org.example.common.exceptions;

import lombok.Getter;

/**
 * Неудачный запуск сервера
 */
@Getter
public class OpenServerException extends RuntimeException {
    private int port;
    public OpenServerException(int port) {
        super("Ошибка открытия сервера на порту " + port);
    }
}
