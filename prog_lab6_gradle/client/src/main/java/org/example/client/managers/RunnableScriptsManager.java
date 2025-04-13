package org.example.client.managers;

import org.example.client.utils.InputReader;

import java.io.*;
import java.util.ArrayDeque;

/**
 * Менеджер для контроля корректности выполнения исполняемых скриптов
 * @author maxkarn
 */
public class RunnableScriptsManager implements InputReader {
    /**
     * Хранение запущенных на данный моент файлов
     */
    private static final ArrayDeque<File> launchedFiles = new ArrayDeque<>();

    private static final ArrayDeque<BufferedReader> readers = new ArrayDeque<>();

    /**
     * Метод для проверки, был ли запущен файл повторно (рекурсивно)
     * @param file проверяемый файл
     * @return был или не был запущен
     */
    public static boolean checkIfLaunchedInStack(File file) {
        return launchedFiles.contains(file);
    }

    /**
     * Добавляет файл в список запущенных
     * @param file файл
     */
    public static void addFile(File file) throws FileNotFoundException {
        launchedFiles.add(file);
        readers.add(new BufferedReader(new InputStreamReader(new FileInputStream(file))));
    }

    /**
     * Удаляет файл из списка запущенных
     * @param file файл
     */
    public static void removeFile(File file) {
        launchedFiles.remove(file);
    }

    /**
     * Очищение списка запущенных файлов
     */
    public static void clear() {
        launchedFiles.clear();
        readers.clear();
    }

    /**
     * Метод для чтения перенаправленного потока ввода на файл
     */
    @Override
    public String readLine() {
        try {
            return readers.getLast().readLine();
        } catch (IOException ignored) {
            return "";
        }
    }
}