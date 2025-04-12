package org.example.common.dtp;

import java.io.*;

/**
 * Класс со статическими методами для сериализации объектов
 */
public class ObjectSerializer {
    /**
     * Сериализация объекта в поток байтов
     * @param obj объект для сериализации
     * @return байты
     * @throws IOException если чот не то)
     */
    public static byte[] serializeObject(Object obj) throws IOException {
        try (
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        ) {
            objectOutputStream.writeObject(obj);
            objectOutputStream.flush();
            return byteArrayOutputStream.toByteArray();
        }
    }

    /**
     * десериализация объекта из потока байтов
     * @param bytes поток байтов
     * @return объект
     * @throws IOException если в стримах чот не то)
     * @throws ClassNotFoundException если сервак послал не тот класс)
     */
    public static Object deserializeObject(byte[] bytes) throws IOException, ClassNotFoundException {
        try (
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)
        ) {
            return objectInputStream.readObject();
        }
    }
}
