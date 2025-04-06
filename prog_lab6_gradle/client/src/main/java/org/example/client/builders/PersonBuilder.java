package org.example.client.builders;

import org.example.client.utils.InputReader;
import org.example.common.entity.Country;
import org.example.common.entity.Person;
import org.example.common.utils.Printable;

import java.util.function.Predicate;

/**
 * Класс билдера объектов класса Person
 */
public class PersonBuilder extends Builder<Person> {
    public PersonBuilder(Printable consoleOutput, InputReader consoleInput) {
        super(consoleOutput, consoleInput);
    }

    @Override
    public Person build() {
        Predicate<Long> validateHeight = (height) -> (height > 0);
        Predicate<Country> validateCountry = (country) -> true;

        consoleOutput.println("Создание нового объекта Person");

        long height = askLong("рост", "целое число типа Long", validateHeight, "Неверный формат ввода: целое число должно удовлетворять условиям");
        Country country = askEnum("страна", "выберите доступную страну из списка (или не выбирайте, оставив поле пустым)", Country.class, validateCountry, "Мы сотрудничаем только со странами из списка!! Ну или просто скип");

        return new Person(height, country);

    }
}
