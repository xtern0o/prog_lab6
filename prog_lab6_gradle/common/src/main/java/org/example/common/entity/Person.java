package org.example.common.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.example.common.utils.Validatable;

import java.io.Serializable;

/**
 * Модель Person
 * @author maxkarn
 */
@Getter
@Setter
public class Person implements Validatable, Serializable {
    private long height; //Значение поля должно быть больше 0
    private Country nationality; //Поле может быть null

    @JsonCreator
    public Person(@JsonProperty("height") long height, @JsonProperty("nationality") Country nationality) {
        this.height = height;
        this.nationality = nationality;
    }

    @Override
    public boolean validate() {
        return height > 0;
    }

    @Override
    public String toString() {
        return String.format("Person (height: %s; nationality: %s)", height, nationality != null ? nationality : "?");
    }

}
