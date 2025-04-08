package org.example.common.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.example.common.utils.Validatable;

/**
 * Модель координат
 * @author maxkarn
 */
@Getter
@Setter
public class Coordinates implements Validatable, Comparable {
    private float x;
    private Integer y; //Значение поля должно быть больше -471, Поле не может быть null

    @JsonCreator
    public Coordinates(@JsonProperty("x") float x, @JsonProperty("y") Integer y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean validate() {
        return y != null && y > -471;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    @Override
    public int compareTo(Object object) {
        if (!(object instanceof Coordinates)) {
            throw new IllegalArgumentException("Сравнивать можно только с Coordinates");
        }
        Coordinates other = (Coordinates) object;

        int xCompare = Float.compare(this.x, other.x);
        if (xCompare != 0) {
            return xCompare;
        }

        return Integer.compare(this.y, other.y);
    }
}
