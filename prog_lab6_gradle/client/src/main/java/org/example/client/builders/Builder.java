package org.example.client.builders;

import org.example.client.cli.ConsoleInput;
import org.example.client.managers.RunnableScriptsManager;
import org.example.client.utils.InputReader;
import org.example.common.utils.Printable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Predicate;

/**
 * Базовый абстрактный класс Builder
 * Предназначен для создания сложных объектов поэтапно
 * @param <T> Класс объекта, который мы строим
 */
public abstract class Builder<T> {
    protected final Printable consoleOutput;
    protected final InputReader consoleInput;

    /**
     * Статический массив для хранения слов которые следует распознавать как значение true
     */
    public static final ArrayList<String> trueWords = new ArrayList<>(Arrays.asList("1", "+", "on", "y", "yes", "t", "true"));
    /**
     * Статический массив для хранения слов которые следует распознавать как значение false
     */
    public static final ArrayList<String> falseWords = new ArrayList<>(Arrays.asList("0", "-", "off", "n", "no", "not", "f", "false"));

    public Builder(Printable consoleOutput, InputReader consoleInput) {
        this.consoleOutput = consoleOutput;
        this.consoleInput = ConsoleInput.isFileMode() ? new RunnableScriptsManager() : consoleInput;
    }

    /**
     * Построение исходного объекта
     * @return объект, построенный на основе полученных данных
     */
    public abstract T build();

    /**
     * Получение строкового значения. До тех пор, пока не получится валидное.
     * Для дальнейших аналогичных типов данных сигнатура будет той же, поэтому описание параметров я напишу только в этом методе
     * @param valueName название поля для заполнения
     * @param valueInfo дополнительная информация, требования для поля и тд
     * @param validateRule предикат (лямбда), описывающий правило валидации значения
     * @param errorMessage сообщение об ошибке, которое получит пользователь при провале валидации
     * @return полученная валидная строка
     */
    public String askString(String valueName, String valueInfo, Predicate<String> validateRule, String errorMessage) {
        while (true) {
            consoleOutput.print(String.format("%s (%s)\n> ", valueName, valueInfo));
            String value = consoleInput.readLine();
            if (value != null) value = value.trim();
            if (validateRule.test(value)) return value;
            consoleOutput.printError("Введенное значение не удовлетворяет одному или нескольким условиям валидации поля \"" + valueName + "\". " + errorMessage);
            dropIfFileMode();
        }
    }

    public Integer askInteger(String valueName, String valueInfo, Predicate<Integer> validateRule, String errorMessage) {
        while (true) {
            consoleOutput.print(String.format("%s (%s)\n> ", valueName, valueInfo));
            try {
                Integer value = Integer.parseInt(consoleInput.readLine());
                if (validateRule.test(value)) return value;
                consoleOutput.printError("Введенное значение не удовлетворяет одному или нескольким условиям валидации поля \"" + valueName + "\". " + errorMessage);
            }
            catch (NumberFormatException e) {
                consoleOutput.printError("Что непонятного?! Введите число!!");
                dropIfFileMode();
            }
        }
    }

    public <T extends Enum<T>> T askEnum(String valueName, String valueInfo, Class<T> enumClass, Predicate<T> validateRule, String errorMessage) {
        while (true) {
            consoleOutput.print(String.format("%s (%s)\n%s\n> ", valueName, valueInfo, Arrays.toString(enumClass.getEnumConstants())));
            try {
                String input = consoleInput.readLine();
                if (input == null) throw new IllegalArgumentException();
                input = input.trim();
                if (input.isBlank()) return null;
                T value = Enum.valueOf(enumClass, input.toUpperCase());
                if (validateRule.test(value)) return value;
                consoleOutput.printError("Введенное значение не удовлетворяет одному или нескольким условиям валидации поля \"" + valueName + "\". " + errorMessage);

            }
            catch (IllegalArgumentException e) {
                consoleOutput.printError("Что непонятного?! Выберите корректное значение из доступных!!");
                dropIfFileMode();
            }
        }
    }

    public Float askFloat(String valueName, String valueInfo, Predicate<Float> validateRule, String errorMessage) {
        while (true) {
            consoleOutput.print(String.format("%s (%s)\n> ", valueName, valueInfo));
            try {
                String input = consoleInput.readLine();
                if (input != null) input = input.trim();
                if (input.isBlank()) throw new NumberFormatException();
                Float value = Float.parseFloat(input);
                if (validateRule.test(value)) return value;
                consoleOutput.printError("Введенное значение не удовлетворяет одному или нескольким условиям валидации поля \"" + valueName + "\". " + errorMessage);
                dropIfFileMode();
            }
            catch (NumberFormatException e) {
                consoleOutput.printError("Что непонятного?! Введите корректное дробное число Float!!");
                dropIfFileMode();
            }
        }
    }

    public Double askDouble(String valueName, String valueInfo, Predicate<Double> validateRule, String errorMessage) {
        while (true) {
            consoleOutput.print(String.format("%s (%s)\n> ", valueName, valueInfo));
            try {
                String input = consoleInput.readLine();
                if (input != null) input = input.trim();
                if (input.isBlank()) throw new NumberFormatException();
                Double value = Double.parseDouble(input);
                if (validateRule.test(value)) return value;
                consoleOutput.printError("Введенное значение не удовлетворяет одному или нескольким условиям валидации поля \"" + valueName + "\". " + errorMessage);
            }
            catch (NumberFormatException e) {
                consoleOutput.printError("Что непонятного?! Введите корректное дробное число Double!!");
                dropIfFileMode();
            }
        }
    }

    public Long askLong(String valueName, String valueInfo, Predicate<Long> validateRule, String errorMessage) {
        while (true) {
            consoleOutput.print(String.format("%s (%s)\n> ", valueName, valueInfo));
            try {
                String input = consoleInput.readLine();
                if (input != null) input = input.trim();
                if (input.isBlank()) throw new NumberFormatException();
                Long value = Long.parseLong(input);
                if (validateRule.test(value)) return value;
                consoleOutput.printError("Введенное значение не удовлетворяет одному или нескольким условиям валидации поля \"" + valueName + "\". " + errorMessage);
            }
            catch (NumberFormatException e) {
                consoleOutput.printError("Что непонятного?! Введите корректное целое число типа Long!!");
                dropIfFileMode();
            }
        }
    }

    public boolean askBoolean(String valueName) {
        while (true) {
            consoleOutput.print(String.format("%s?\nДА=(\"1\", \"+\", \"on\", \"y\", \"yes\", \"t\", \"true\"); \nНЕТ=(\"0\", \"-\", \"off\", \"n\", \"no\", \"not\", \"f\", \"false\")\n> ", valueName));
            String input = consoleInput.readLine();
            if (input != null) input = input.trim().toLowerCase();
            if (Builder.trueWords.contains(input)) {
                return true;
            }
            else if (Builder.falseWords.contains(input)) {
                return false;
            }
            consoleOutput.printError("Что непонятного?! Скажите ДА или НЕТ одним из разрешенных способов!");
            dropIfFileMode();
        }
    }

    private void dropIfFileMode() {
        if (ConsoleInput.isFileMode()) {
            consoleOutput.printError("Неверный ввод. Завершение программы");
            System.exit(-1);
        }
    }

}