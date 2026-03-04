package ru.practicum.ewm.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.UserEventParam;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventService eventService;

    @Override
    public List<EventShortDto> getEvents(UserEventParam param) {
        List<EventShortDto> events = eventService.getEvents(param);


        return List.of();
    }
}
