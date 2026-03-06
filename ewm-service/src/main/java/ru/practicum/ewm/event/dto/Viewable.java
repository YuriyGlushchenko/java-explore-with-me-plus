package ru.practicum.ewm.event.dto;


// этот интерфейс мы добавляем для полиморфизма, чтобы метод enrichEventsWithViews в сервисе мог принимать и ShortDto и FullDto, которе реализуют этот интерфейс.
public interface Viewable {
    Long getId();

    void setViews(Long views);
}