package org.example.client.builders;

import org.example.client.cli.ConsoleInput;
import org.example.common.entity.Coordinates;
import org.example.common.entity.Person;
import org.example.common.entity.Ticket;
import org.example.common.entity.TicketType;
import org.example.common.utils.Printable;

import java.util.function.Predicate;

/**
 * Билдер для объектов класса Ticket
 */
public class TicketBuilder extends Builder<Ticket>{
    public TicketBuilder(Printable consoleOutput, ConsoleInput consoleInput) {
        super(consoleOutput, consoleInput);
    }

    @Override
    public Ticket build() {
        Predicate<String> validateName = (name) -> (name != null && !name.isBlank());
        Predicate<Double> validatePrice = (price) -> (price > 0);
        Predicate<Float> validateDiscount = (discount) -> (discount != null && discount > 0 && discount < 100);

        consoleOutput.println("Создание нового объекта Ticket");

        String name = askString("имя", "непустая строка", validateName, "Неверный формат ввода: строка должна быть непустой");
        Coordinates coordinates = new CoordinatesBuilder(consoleOutput, consoleInput).build();
        Double price = askDouble("цена", "дробное число типа Double > 0", validatePrice, "Неверный формат ввода: дробное число должно быть больше нуля");
        Float discount = askFloat("скидка", "дробное число типа Float > 0 и < 100", validateDiscount, "Неверный формат ввода: дробное число должно лежать в промежутке (0; 100)");
        TicketType type = askEnum("тип билета", "выберите тип из доступных (или оставьте поле пустым)", TicketType.class, (t) -> (true), "Неверный формат ввода: выберите из доступных или оставьте поле пустым!!");
        boolean refundable = askBoolean("возвратный");
        Person person = new PersonBuilder(consoleOutput, consoleInput).build();

        return new Ticket(name, coordinates, price, discount, type, refundable, person);
    }

}