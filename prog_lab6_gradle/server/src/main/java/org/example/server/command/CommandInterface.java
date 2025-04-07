package org.example.server.command;

import org.example.common.dtp.RequestCommand;
import org.example.common.dtp.Response;

/**
 * Базовый интерфейс для всех команд
 * @author maxkarn
 */
public interface CommandInterface {
    /**
     * Метод для запуска команды
     * @param requestCommand сериализованный формат, содержащий название команды, аргументы и объект коллекции
     */
    Response execute(RequestCommand requestCommand);
}
