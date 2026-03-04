package ru.practicum.ewm.event.service;

import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.UserEventParam;

import java.util.List;

public interface EventService {

    List<EventShortDto> getEvents(UserEventParam param);
}
