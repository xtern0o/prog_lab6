package org.example.client.cli;

import org.example.common.utils.Printable;

import java.io.PrintStream;

/**
 * Класс для контроля потока вывода
 */
public class ConsoleOutput implements Printable {
    /**
     * PrintStream для вывода информации
     */
    private final PrintStream printStream = System.out;

    @Override
    public void print(String s) {
        printStream.print(s);
    }

    @Override
    public void println(String s) {
        printStream.println(s);
    }

    @Override
    public void printError(String s) {
        printStream.print("[Error]: " + s + "\n");
    }

}
