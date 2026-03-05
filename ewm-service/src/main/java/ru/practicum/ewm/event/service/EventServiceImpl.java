package ru.practicum.ewm.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.categories.Category;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.dto.paramDto.EventRepositoryParam;
import ru.practicum.ewm.event.dto.paramDto.UserEventParam;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventSort;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exceptions.exceptions.ConditionsNotMetException;
import ru.practicum.ewm.exceptions.exceptions.NotFoundException;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final StatsClient statsClient;
    private final EventMapper eventMapper;


    // константа - начало отчета времени сбора статистики, можно вынести в properties, если нужно будет
    private static final LocalDateTime STATS_START_DATE = LocalDateTime.of(2000, 1, 1, 0, 0, 0);

    @Transactional
    @Override
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        LocalDateTime minEventDate = LocalDateTime.now().plusHours(2);
        if (newEventDto.getEventDate().isBefore(minEventDate)) {
            throw new ConditionsNotMetException("Unable to update event at last 2 hours before event date");
        }

        // или не надо проверять, раз по условию он аутентифицирован и авторизован, значит точно есть
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + userId + " was not found"));

        // Todo загружаем категорию из репозитория категорий, пока заглушка
        Category cat = new Category(-1L, "222222");

        Event event = eventMapper.toEvent(newEventDto, cat, user);

        event = eventRepository.save(event);

        return eventMapper.toEventFullDto(event, 0L, 0L);
    }

    @Override
    public List<EventShortDto> getUserEvents(Long userId, int from, int size) {
        EventRepositoryParam param = EventRepositoryParam.builder()
                .initiator(userId)
                .from(from)
                .size(size)
                .build();

        List<EventShortDto> events = eventRepository.findEvents(param);
        if (events.isEmpty()) {
            return events;
        }

        enrichEventsWithViews(events);

        return events;
    }

    @Override
    public List<EventShortDto> getEvents(UserEventParam userEventParam) {
        EventRepositoryParam param = EventRepositoryParam.fromUserEventParam(userEventParam);

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

    @Override
    public EventFullDto findUserEventByEventId(Long userId, Long eventId) {

        EventFullDto event = eventRepository.findEventFullDtoById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Event with id=" + eventId + " not found for user with id=" + userId);
        }

        enrichEventWithViews(event);

        return event;
    }

    @Override
    @Transactional
    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest body) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Event with id=" + eventId + " not found for user with id=" + userId);
        }

        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new ConditionsNotMetException("Only events with CANCELED or PENDING state can be updated");
        }

        // в спецификации не сказано четко, какую дату проверять, которая уже в событии или новую(если есть), проверим обе
        LocalDateTime minEventDateForUpdating = LocalDateTime.now().plusHours(2);
        if (event.getEventDate().isBefore(minEventDateForUpdating)) {
            throw new ConditionsNotMetException("Unable to update event at last 2 hours before event date");
        }

        if (body.getEventDate() != null) {
            LocalDateTime newEventDate = body.getEventDate();
            if (newEventDate.isBefore(minEventDateForUpdating)) {
                throw new ConditionsNotMetException("Unable to update event at last 2 hours before event date");
            }
        }

        Category cat = null;
        if (body.getCategory() != null) {
            // Todo загружаем категорию из репозитория категорий, пока заглушка
            cat = new Category(-1L, "222222");
        }
        eventMapper.updateEventFromUserRequest(body, event, cat);

        event = eventRepository.save(event);

        String[] uris = {"/events/" + event.getId()};
        Map<Long, Long> hits = fetchViews(uris);
        Long views = hits.getOrDefault(event.getId(), 0L);

        // Todo загружаем запросы из репозитория запросов, пока заглушка
        Long confirmedRequests = -1L;

        return eventMapper.toEventFullDto(event, confirmedRequests, views);
    }

    public EventFullDto findEventById(String uri, String ip, Long id) {

        EventFullDto event = eventRepository.findEventFullDtoById(id)
                .orElseThrow(() -> new NotFoundException("Event with id=" + id + " was not found"));

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new NotFoundException("Published Event with id=" + id + " was not found");
        }

        sendHit(uri, ip, LocalDateTime.now());
        enrichEventWithViews(event);

        return event;
    }

    @Override
    public List<ParticipationRequestDto> getParticipationRequests(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Event with id=" + eventId + " not found for user with id=" + userId);
        }

        // ToDo запрашиваем в репозитории ParticipationRequest заявки с таким eventId.В полученных ParticipationRequest
        // можно проверить сразу что initiator.id == userId, тогда и отдельный  запрос не нужен?


        return List.of();
    }

    @Override
    public EventRequestStatusUpdateResult updateRequestStatuses(Long userId, Long eventId, EventRequestStatusUpdateRequest request) {

        // ToDo реализация метода

        return null;
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
