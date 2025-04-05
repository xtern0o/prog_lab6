package org.example.common.exceptions;

/**
 * Ошибка валидации
 * @author maxkarn
 */
public class ValidationError extends RuntimeException {
    /**
     * Объект виновник ошибки
     */
    public Object o;

    public ValidationError(Object o) {
        super();
        this.o = o;
    }

    @Override
    public String getMessage() {
        return "Ошибка при валидации объекта " + this.o.getClass().getName() + ".";
    }
}
