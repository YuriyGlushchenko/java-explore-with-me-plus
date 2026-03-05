package ru.practicum.ewm.event.repository.projections;

import ru.practicum.ewm.event.model.Event;

public interface EventWithRequests {
    Event getEvent();

    Long getConfirmedRequests();
}