package ru.practicum.ewm.event.dto.paramDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.event.model.EventSort;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRepositoryParam {
    private String text;
    private List<Long> categories;
    private Boolean paid;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private Boolean onlyAvailable;
    private EventSort sort;
    private Integer from;
    private Integer size;
    private String uri;
    private String ip;
    private Long initiator;

    public static EventRepositoryParam fromUserEventParam(UserEventParam userParam) {
        return EventRepositoryParam.builder()
                .text(userParam.getText())
                .categories(userParam.getCategories())
                .paid(userParam.getPaid())
                .rangeStart(userParam.getRangeStart())
                .rangeEnd(userParam.getRangeEnd())
                .onlyAvailable(userParam.getOnlyAvailable())
                .sort(userParam.getSort())
                .from(userParam.getFrom())
                .size(userParam.getSize())
                .uri(userParam.getUri())
                .ip(userParam.getIp())
                .build();
    }

    public boolean hasInitiatorParam() {
        return initiator != null;
    }

    public boolean hasPaidParam() {
        return paid != null;
    }

    public boolean isOnlyAvailable() {
        return onlyAvailable != null ? onlyAvailable : false;
    }

    public EventSort getSortOrDefault() {
        return sort != null ? sort : EventSort.EVENT_DATE;
    }

    public boolean hasDateRange() {
        return rangeStart != null && rangeEnd != null;
    }

    public boolean hasRangeStart() {
        return rangeStart != null;
    }

    public boolean hasRangeEnd() {
        return rangeEnd != null;
    }

    public boolean hasTextSearchRequest() {
        return text != null && !text.isBlank();
    }

    public boolean hasCategories() {
        return categories != null && !categories.isEmpty();
    }
}
