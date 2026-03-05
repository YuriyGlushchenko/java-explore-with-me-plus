package ru.practicum.ewm.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.UserEventParam;
import ru.practicum.ewm.event.model.EventSort;
import ru.practicum.ewm.event.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/events")
public class PublicEventController {
    private final EventService eventService;


    @GetMapping
    public List<EventShortDto> getEvents(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "false") boolean onlyAvailable,
            @RequestParam(defaultValue = "EVENT_DATE") EventSort sort,
            @RequestParam(defaultValue = "0") @Min(0) int from,
            @RequestParam(defaultValue = "10") @Positive int size,
            HttpServletRequest request
    ) {
        log.debug("Request to get events: uri={}, ip={}, text={}, categories={}, paid={}, rangeStart={}, rangeEnd={}, " +
                        "onlyAvailable={}, sort={}, from={}, size={}",
                request.getRequestURI(), request.getRemoteAddr(), text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);

        UserEventParam param = UserEventParam.builder()
                .text(text)
                .categories(categories)
                .paid(paid)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .onlyAvailable(onlyAvailable)
                .sort(sort)
                .from(from)
                .size(size)
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .build();

        return eventService.getEvents(param);
    }

    @GetMapping("/{id}")
    public EventFullDto getEvent(@PathVariable long id, HttpServletRequest request){
        log.debug("Request to get event: uri={}, ip={}, id={}", request.getRequestURI(), request.getRemoteAddr(), id);

        return  eventService.findEventById( request.getRequestURI(), request.getRemoteAddr(), id);
    }


}
