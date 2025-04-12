package org.example.server.managers;

import lombok.AllArgsConstructor;
import org.example.common.utils.Printable;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Класс для управления жизненным циклом сервера (запуск, завершение работы)
 * @author maxkarn
 */
@AllArgsConstructor
public class RuntimeManager implements Runnable {
    private final Printable consoleOutput;
    private final Server server;
    private final FileManager fileManager;

    @Override
    public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            saveCollection();
            try {
                server.stop();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            consoleOutput.println("⚡ Сервер - В С Ё.");
            consoleOutput.println("Карнажицкий. Подписаться.");
        }));

        try {
            consoleOutput.println("* Ticket collection managing server have been started");
            consoleOutput.println("> [Ctrl + C] to close server");
            server.start();
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }

    public void saveCollection() {
        try {
            fileManager.serializeCollectionToJSON(CollectionManager.getCollection());
            if (CollectionManager.getCollection().isEmpty()) consoleOutput.println("Внимание: вы записали в файл пустую коллекцию");
            else consoleOutput.println("Коллекция успешно сохранена в файле " + fileManager.getFile().getName());
        } catch (FileNotFoundException fileNotFoundException) {
            // программа гарантирует, что мы сюда не попадем
            consoleOutput.printError("Файл для сохранения не найден");
            throw new RuntimeException(fileNotFoundException);
        }
    }
}
