package ru.practicum.ewm.event.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.categories.dto.CategoryDto;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.model.Location;
import ru.practicum.ewm.user.dto.UserShortDto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventFullDto {
    private Long id;

    @NotBlank
    @Size(min = 20, max = 2000)
    private String annotation;

    private CategoryDto category;

    private Long confirmedRequests;  // из запросов на участие

    private String createdOn;  // "yyyy-MM-dd HH:mm:ss"

    @NotBlank
    @Size(min = 20, max = 7000)
    private String description;

    private String eventDate;  // "yyyy-MM-dd HH:mm:ss"

    private UserShortDto initiator;

    private Location location;

    private Boolean paid;

    private Integer participantLimit;  // по умолчанию 0

    private String publishedOn;  // "yyyy-MM-dd HH:mm:ss"

    private Boolean requestModeration;  // по умолчанию true

    private EventState state;

    @NotBlank
    @Size(min = 3, max = 120)
    private String title;

    private Long views;  // из сервиса статистики
}