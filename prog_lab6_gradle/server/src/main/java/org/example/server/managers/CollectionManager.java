package org.example.server.managers;

import lombok.Getter;
import org.example.common.entity.Ticket;
import org.example.common.exceptions.ValidationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Класс для управления коллекцией
 * @author maxkarn
 */
public class CollectionManager {
    public final static Logger logger = LoggerFactory.getLogger(CollectionManager.class);
    /**
     * Коллекция билетов
     */
    @Getter
    private static PriorityQueue<Ticket> collection = new PriorityQueue<>();

    /**
     * Время инициализации коллекции
     * Время инициализации объекта CollectionManager
     */
    @Getter
    private final Date initDate = new Date();

    /**
     * Метод присваивает коллекции передаваемое значение, если элементы коллекции корректны
     * @param collection новая коллекция
     * @return true если успешно, false если не прошла валидация одного из элементов
     */
    public static boolean setCollection(PriorityQueue<Ticket> collection) {
        if (!CollectionManager.allIdsAreUnique(collection)) {
            return false;
        }

        if (!collection.stream().allMatch(Ticket::validate)) return false;

        CollectionManager.collection = collection;
        logger.info("Коллекция обновлена");
        return true;
    }

    /**
     * Статический метод для генерации нового id
     * @return минимальный несуществующий id
     */
    public static int generateFreeId() {
        if (collection.isEmpty()) return 1;

        HashSet<Integer> existIds = collection.stream()
                .map(Ticket::getId)
                .collect(Collectors.toCollection(HashSet::new));


        for (int i = 1; i < Collections.max(existIds); i++) {
            if (!existIds.contains(i)) return i;
        }
        return Collections.max(existIds) + 1;
    }

    /**
     * Получение типа коллекции
     * @return класс объекта коллекции
     */
    public String getTypeOfCollection() {
        return collection.getClass().getName();
    }

    /**
     * Возвращает размер коллекции
     * @return число элементов в коллекции
     */
    public int getCollectionSize() {
        return collection.size();
    }

    /**
     * Находит объект в коллекции по его id
     * @param id айди.
     * @return Объект из коллекции или null, если его не существует
     */
    public Ticket getElementById(Integer id) {
        return collection.stream()
                .filter(ticket -> Objects.equals(ticket.getId(), id))
                .findFirst()
                .orElse(null);
    }

    /**
     * Очищает коллекцию
     */
    public void clearCollection() {
        collection.clear();
    }

    /**
     * Удаляет элемент из коллекции по его id
     * @param id id элемента
     * @return true если элемент с таким id есть и удален, и false если элемент не найден
     */
    public boolean removeById(int id) {
        boolean deleted = collection.removeIf(ticket -> ticket.getId() == id);
        if (deleted) logger.info("Элемент с id=" + id + " был успешно удален");
        else logger.warn("Элемент с id={} не найден", id);
        return deleted;
    }

    /**
     * Проверка на уникальность всех id в коллекции билетов
     * @param collection коллекция
     * @return уникалны ли айдишники
     */
    public static boolean allIdsAreUnique(Collection<Ticket> collection) {
        return collection.stream()
                .map(Ticket::getId)
                .distinct()
                .count() == collection.size();
    }

    /**
     * Добавляет элемент в коллекцию предварительно проведя контрольную валидацию
     * @param ticket новый элемент
     * @throws ValidationError в случае неудачного прохождения валидации
     */
    public void addElement(Ticket ticket) throws ValidationError {
        if (ticket.validate()) {
            collection.add(ticket);
            logger.info("Добавлен новый элемент с id={}", ticket.getId());
            return;
        }
        throw new ValidationError(ticket);
    }
}
