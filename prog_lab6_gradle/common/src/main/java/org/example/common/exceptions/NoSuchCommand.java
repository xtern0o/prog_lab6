package org.example.common.exceptions;

import java.io.IOException;


public class NoSuchCommand extends IOException {
    String userInput;

    public NoSuchCommand(String userInput) {
        super(String.format("Команда \"%s\" не найдена", userInput));
    }
}
