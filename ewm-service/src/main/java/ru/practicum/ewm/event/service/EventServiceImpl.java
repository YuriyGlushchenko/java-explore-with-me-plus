package ru.practicum.ewm.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.UserEventParam;
import ru.practicum.ewm.event.model.EventSort;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.stat.client.StatsClient;
import ru.practicum.stat.dto.EndpointHitDto;
import ru.practicum.stat.dto.ParamDto;
import ru.practicum.stat.dto.ViewStatsDto;

import java.time.LocalDateTime;
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

    // начало отчета сбора статистики, можно вынести в properties
    private static final LocalDateTime STATS_START_DATE = LocalDateTime.of(2000, 1, 1, 0, 0, 0);

    @Override
    public List<EventShortDto> getEvents(UserEventParam param) {
        List<EventShortDto> events = eventRepository.findEvents(param);

        if (events.isEmpty()) {
            return events;
        }

        enrichEventsWithViews(events);

        if (param.getSortOrDefault() == EventSort.VIEWS) {  // из репозитория приходят отсортированными по дате
            events.sort(Comparator.comparing(EventShortDto::getViews).reversed());
        }

        EndpointHitDto hitDto = EndpointHitDto.builder()
                .app("ewm-main-service")
                .uri(param.getUri())
                .ip(param.getIp())
                .timestamp(LocalDateTime.now())
                .build();

        statsClient.hit(hitDto);

        return events;
    }

    private void enrichEventsWithViews(List<EventShortDto> events) {
        String[] uris = events.stream()
                .map(e -> "/events/" + e.getId())
                .toArray(String[]::new);

        ParamDto statRequestParam = ParamDto.builder()
                .start(STATS_START_DATE)
                .end(LocalDateTime.now())
                .uris(uris)
                .unique(false)
                .build();

        try {
            List<ViewStatsDto> stats = statsClient.get(statRequestParam);

            if (stats.size() == 1 && stats.getFirst().getHits() == -1) {
                log.error("Failed to get stats from stats-service, returned hits = -1");
                return;
            }

            Map<Long, Long> hits = stats.stream()
                    .filter(stat -> stat.getUri() != null && stat.getHits() != -1)
                    .collect(Collectors.toMap(
                            this::extractEventIdFromUri,
                            ViewStatsDto::getHits
                    ));

            events.forEach(event ->
                    event.setViews(hits.getOrDefault(event.getId(), 0L))
            );

        } catch (Exception e) {
            log.error("Failed to get stats from stats-service", e);
        }
    }

    private Long extractEventIdFromUri(ViewStatsDto stat) {
        String uri = stat.getUri(); // приходить должно в формате "/events/{id}"
        return Long.parseLong(uri.substring(uri.lastIndexOf('/') + 1));
    }
}
