package ru.practicum.ewm.event.dto;

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
public class UserEventParam {
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
