package org.example.server.command;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@AllArgsConstructor
@Getter
public abstract class Command implements CommandInterface {
    private String name;
    private String description;

    @Override
    public int hashCode() {
        return Objects.hash(name, description);
    }

    @Override
    public String toString() {
        return String.format(": %-30s | %s", name, description);
    }

}
