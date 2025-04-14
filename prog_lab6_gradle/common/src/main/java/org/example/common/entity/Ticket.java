package org.example.common.entity;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.example.common.utils.Validatable;

/**
 * Модель Ticket
 * @author maxkarn
 */
@Getter
@Setter
public class Ticket implements Validatable, Comparable<Ticket>, Serializable {
    private Integer id; //Поле не может быть null, Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически

    private String name; //Поле не может быть null, Строка не может быть пустой

    private Coordinates coordinates; //Поле не может быть null

    private ZonedDateTime creationDate; //Поле не может быть null, Значение этого поля должно генерироваться автоматически

    private double price; //Значение поля должно быть больше 0

    private Float discount; //Поле не может быть null, Значение поля должно быть больше 0, Максимальное значение поля: 100

    private boolean refundable;

    private TicketType type; //Поле может быть null

    private Person person; //Поле не может быть null

    public Ticket() {

    }

    public Ticket(String name, Coordinates coordinates, double price, Float discount, TicketType type, boolean refundable, Person person) {
        this.name = name;
        this.coordinates = coordinates;
        this.price = price;
        this.discount = discount;
        this.type = type;
        this.refundable = refundable;
        this.person = person;

//        this.id = CollectionManager.generateFreeId();
        this.id = 0;
        this.creationDate = ZonedDateTime.now();
    }

    @JsonCreator
    Ticket(
            @JsonProperty("name") String name,
            @JsonProperty("coordinates") Coordinates coordinates,
            @JsonProperty("price") double price,
            @JsonProperty("discount") Float discount,
            @JsonProperty("type") TicketType type,
            @JsonProperty("refundable") boolean refundable,
            @JsonProperty("person") Person person,
            @JsonProperty("id") Integer id,
            @JsonProperty("creationDate") ZonedDateTime creationDate
    ) {
        this(name, coordinates, price, discount, type, refundable, person);
        this.id = id;
        this.creationDate = creationDate;
    }

    @Override
    public boolean validate() {
        if (id == null || id <= 0) return false;
        if (name == null || name.isEmpty()) return false;
        if (creationDate == null) return false;
        if (price <= 0) return false;
        if (discount == null || discount <= 0 || discount > 100) return false;
        if (person == null) return false;

        return person.validate() && coordinates.validate();
    }

    @Override
    public String toString() {
        return String.format(
                "Ticket:\n" +
                        ": id           | %d\n" +
                        ": name         | %s\n" +
                        ": coordinates  | %s\n" +
                        ": creationDate | %s\n" +
                        ": price        | %.2f\n" +
                        ": discount     | %.2f\n" +
                        ": refundable   | %s\n" +
                        ": type         | %s\n" +
                        ": person       | %s",
                id,
                name,
                coordinates,
                this.creationDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")),
                price,
                discount,
                refundable ? "Yes" : "No",
                type != null ? type.name() : "?",
                person
        );
    }

    @Override
    public int compareTo(Ticket ticket) {
        return this.coordinates.compareTo(ticket.coordinates);
    }
}