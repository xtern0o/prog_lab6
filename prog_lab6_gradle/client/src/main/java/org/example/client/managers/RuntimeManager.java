package org.example.client.managers;

import lombok.AllArgsConstructor;
import org.example.client.utils.InputReader;
import org.example.common.utils.Printable;
import org.example.client.managers.Client;

@AllArgsConstructor
public class RuntimeManager implements Runnable {
    private final Printable consoleOutput;
    private final InputReader consoleInput;
    private final Client client;
    private final RunnableScriptsManager runnableScriptsManager;

    @Override
    public void run() {
        while (true) {

        }
    }
}
