package ru.practicum.ewm.event.repository;

import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.paramDto.EventRepositoryParam;

import java.util.List;
import java.util.Optional;

public interface EventRepositoryCustom {

    List<EventShortDto> findEvents(EventRepositoryParam param);

    Optional<EventFullDto> findEventFullDtoById(Long id);

    List<EventFullDto> findFullDtoEvents(EventRepositoryParam param);
}
