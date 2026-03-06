package ru.practicum.ewm.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.UserEventParam;
import ru.practicum.ewm.event.model.EventSort;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exceptions.exceptions.NotFoundException;
import ru.practicum.stat.client.StatsClient;
import ru.practicum.stat.dto.EndpointHitDto;
import ru.practicum.stat.dto.ParamDto;
import ru.practicum.stat.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final StatsClient statsClient;

    // константа - начало отчета времени сбора статистики, можно вынести в properties, если нужно будет
    private static final LocalDateTime STATS_START_DATE = LocalDateTime.of(2000, 1, 1, 0, 0, 0);

    @Override
    public List<EventShortDto> getEvents(UserEventParam param) {
        List<EventShortDto> events = eventRepository.findEvents(param);

        if (events.isEmpty()) {
            return events;
        }

        enrichEventsWithViews(events);

        if (param.getSortOrDefault() == EventSort.VIEWS) {  // из репозитория приходят уже отсортированными по дате
            events.sort(Comparator.comparing(EventShortDto::getViews).reversed());
        }

        sendHit(param.getUri(), param.getIp(), LocalDateTime.now());

        return events;
    }

    public EventFullDto findEventById(String uri, String ip, Long id) {

        //  Через QDsl, избыточно возможно, но кастомный репозиторий уже есть, QDSL уже подключен, с ним можно всё одним
        // запросом сделать и получить ответ сразу в EventFullDto, не надо мапить отдельно.

        EventFullDto event = eventRepository.findEventFullDtoById(id)
                .orElseThrow(() -> new NotFoundException("Event with id=" + id + " was not found"));

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new NotFoundException("Published Event with id=" + id + " was not found");
        }

        sendHit(uri, ip, LocalDateTime.now());
        enrichEventWithViews(event);

        return event;
    }

    private void enrichEventsWithViews(List<EventShortDto> events) {
        String[] uris = events.stream()
                .map(e -> "/events/" + e.getId())
                .toArray(String[]::new);

        Map<Long, Long> hits = fetchViews(uris);

        events.forEach(event ->
                event.setViews(hits.getOrDefault(event.getId(), 0L))
        );
    }

    private Map<Long, Long> fetchViews(String[] uris) {
        ParamDto statRequestParam = ParamDto.builder()
                .start(STATS_START_DATE)
                .end(LocalDateTime.now())
                .uris(uris)
                .unique(false)
                .build();

        try {
            List<ViewStatsDto> stats = statsClient.get(statRequestParam);

            if (stats.size() == 1 && stats.getFirst().getHits() == -1) {
                log.error("Failed to fetch views from stats-service, returned hits = -1 (Fail marker)");
                return Collections.emptyMap();
            }

            return stats.stream()
                    .filter(stat -> stat.getUri() != null && stat.getHits() != -1)
                    .collect(Collectors.toMap(
                            this::extractEventIdFromUri,
                            ViewStatsDto::getHits
                    ));
        } catch (Exception e) {
            log.error("Failed to fetch views from stats-service", e);
            return Collections.emptyMap();
        }
    }

    private void enrichEventWithViews(EventFullDto event) {
        String[] uris = {"/events/" + event.getId()};
        Map<Long, Long> hits = fetchViews(uris);
        event.setViews(hits.getOrDefault(event.getId(), 0L));
    }

    private Long extractEventIdFromUri(ViewStatsDto stat) {
        String uri = stat.getUri(); // приходить должно в формате "/events/{id}"
        return Long.parseLong(uri.substring(uri.lastIndexOf('/') + 1));
    }

    private void sendHit(String uri, String ip, LocalDateTime time) {
        EndpointHitDto hitDto = EndpointHitDto.builder()
                .app("ewm-main-service")
                .uri(uri)
                .ip(ip)
                .timestamp(time)
                .build();

        statsClient.hit(hitDto);
    }
}
