package org.example.client.cli;


import lombok.Getter;
import lombok.Setter;
import org.example.client.managers.InputManager;
import org.example.client.utils.InputReader;

import java.util.Scanner;

/**
 * Класс для контроля пользовательского ввода
 */
public class ConsoleInput implements InputReader {
    private static final Scanner scanner = InputManager.scanner;
    @Getter
    @Setter
    private static boolean fileMode = false;

    @Override
    public String readLine() {
        return scanner.nextLine();
    }

}