package org.example.common.utils;

/**
 * Интерфейс реализующий методы вывода информации
 * @author maxkarn
 */
public interface Printable {
    /**
     * Метод для вывода текста
     * @param s строка
     */
    void print(String s);

    /**
     * Метод для вывода текста в одну строку
     * @param s строка
     */
    void println(String s);

    /**
     * Метод для вывода ошибки в строку
     * @param s строка
     */
    void printError(String s);
}