package org.example.server.managers;

import lombok.AllArgsConstructor;
import org.example.common.utils.Printable;

@AllArgsConstructor
public class RuntimeManager implements Runnable {
    private final Printable consoleOutput;
    private final RequestCommandHandler requestCommandHandler;
    private final Server server;

    @Override
    public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            consoleOutput.println("Завершение работы программы. До свидания!!");
        }));

        consoleOutput.println("Сервер для управления коллекцией Ticket запущен");

    }

}
