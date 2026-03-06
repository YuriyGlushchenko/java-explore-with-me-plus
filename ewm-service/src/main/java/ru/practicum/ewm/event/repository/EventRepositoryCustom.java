package ru.practicum.ewm.event.repository;

import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.UserEventParam;

import java.util.List;
import java.util.Optional;

public interface EventRepositoryCustom {

    List<EventShortDto> findEvents(UserEventParam param);

    Optional<EventFullDto> findEventFullDtoById(Long id);
}
