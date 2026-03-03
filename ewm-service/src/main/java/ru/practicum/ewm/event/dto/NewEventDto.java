package ru.practicum.ewm.event.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.event.model.Location;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewEventDto {
    @NotBlank
    @Size(min = 20, max = 2000)
    private String annotation;

    @NotNull
    private Long category;  // id категории

    @NotBlank
    @Size(min = 20, max = 7000)
    private String description;

    @NotBlank
    private String eventDate;  // "yyyy-MM-dd HH:mm:ss"

    @NotNull
    private Location location;

    private Boolean paid;  // по умолчанию false

    private Integer participantLimit;  // по умолчанию 0

    private Boolean requestModeration;  // по умолчанию true

    @NotBlank
    @Size(min = 3, max = 120)
    private String title;
}
