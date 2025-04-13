package org.example.common.dtp;

/**
 * Статусы ответа сервера
 */
public enum ResponseStatus {
    /**
     * Запрос и ответ успешно обработаны
     */
    OK,

    /**
     * Ошибка на стороне сервера
     */
    SERVER_ERROR,

    /**
     * Ошибка при выполнении команды
     */
    COMMAND_ERROR,

    /**
     * Некорректные аргументы команды
     */
    ARGS_ERROR,

    /**
     * Команда не найдена
     */
    NO_SUCH_COMMAND,

    /**
     * Запрос на создание объекта от сервера
     */
    OBJECT_REQUIRED,

    /**
     * Ошибка валидации данных запроса
     */
    VALIDATION_ERROR,

    /**
     * Выполнить скрипт на стороне клиента
     */
    EXECUTE_SCRIPT
}
