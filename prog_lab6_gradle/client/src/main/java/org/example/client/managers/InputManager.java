package org.example.client.managers;

import java.io.InputStream;
import java.util.Scanner;

public class InputManager {
    public static final InputStream inputStream = System.in;
    public static final Scanner scanner = new Scanner(inputStream);
}