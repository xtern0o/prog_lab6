package org.example.server.managers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;
import org.example.common.entity.Ticket;
import org.example.common.utils.Printable;
import org.example.common.utils.Validatable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Objects;
import java.util.PriorityQueue;

/**
 * Менеджер для управления файлами
 * @author maxkarn
 */
public class FileManager implements Validatable {
    @Getter
    private final File file;
    private final Printable consoleOutput;

    public static final Logger logger = LoggerFactory.getLogger(FileManager.class);

    public FileManager(File file, Printable consoleOutput) {
        this.file = file;
        this.consoleOutput = consoleOutput;
    }

    /**
     * Статическая функция для получения расширения файла
     * @param file файл
     * @return расширение файла или null если он не имеет расширения
     */
    public static String getFileFormat(File file) {
        String name = file.getName();
        if (!name.contains(".")) return null;
        return name.substring(name.lastIndexOf('.') + 1);
    }

    @Override
    public boolean validate() {
        if (!file.exists()) {
            logger.error("Файла \"{}\" не существует", file.getName());
            return false;
        }
        if (!file.canRead() || !file.canWrite()) {
            logger.error("Недостаточно прав: файл \"{}\" недоступен для чтения и/или записи.", file.getName());
            return false;
        }
        if (!Objects.equals(getFileFormat(file), "json")) {
            logger.error("Программа работает только с файлами .json");
            return false;
        }
        return true;
    }

    /**
     * сериализация коллекции в json с помощью PrintWriter
     * @param collection коллекция
     * @throws FileNotFoundException если файл не найден (программа гарантирует наличие файла)
     */
    public void serializeCollectionToJSON(Collection<Ticket> collection) throws FileNotFoundException {
        try (PrintWriter printWriter = new PrintWriter(file)) {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            printWriter.print(objectMapper.writeValueAsString(collection));

        } catch (JsonProcessingException | NoClassDefFoundError e) {
            // TODO: понять откуда берется NoClassDefFoundError
            // UPD: maybe fixed
            throw new RuntimeException(e);
        }
    }

    /**
     * десериализация коллекции из json с помощью InputStreamReader
     * коллекция сохраняется в статической переменной CollectionManager.collection
     */
    public void deserializeCollectionFromJSON() {
        try (FileInputStream fileInputStream = new FileInputStream(file);
             InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

            String json = "";
            String line = bufferedReader.readLine();
            while (line != null) {
                json += line;
                line = bufferedReader.readLine();
            }

            // Проверяем, пустой ли JSON
            if (json.isEmpty() || json.equals("{}")) {
                logger.warn("Файл JSON пустой. Используется пустая коллекция");
                CollectionManager.setCollection(new PriorityQueue<>());
                return;
            }

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            PriorityQueue<Ticket> jsonCollection = objectMapper.readValue(
                    json,
                    objectMapper
                            .getTypeFactory()
                            .constructCollectionType(PriorityQueue.class, Ticket.class)
            );

            if (!CollectionManager.setCollection(jsonCollection)) {
                throw new IOException("Одно или несколько полей не прошли валидацию");
            }

        } catch (IOException e) {
            logger.error("Проверьте корректность json файла. Подробности: {}", e.getMessage());
            System.exit(-1);
        }
    }

}
