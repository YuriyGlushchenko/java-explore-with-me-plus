package ru.practicum.ewm.event.repository;

import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.UserEventParam;

import java.util.List;

public interface EventRepositoryCustom {

    List<EventShortDto> findEvents(UserEventParam param);
}
