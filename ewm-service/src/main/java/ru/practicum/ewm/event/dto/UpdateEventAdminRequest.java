package ru.practicum.ewm.event.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.event.model.AdminStateAction;
import ru.practicum.ewm.event.model.Location;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventAdminRequest {
    @Size(min = 20, max = 2000)
    private String annotation;

    private Long category;  // id категории

    @Size(min = 20, max = 7000)
    private String description;

    private String eventDate;  // "yyyy-MM-dd HH:mm:ss"

    private Location location;

    private Boolean paid;

    private Integer participantLimit;

    private Boolean requestModeration;

    private AdminStateAction stateAction;  // PUBLISH_EVENT / REJECT_EVENT

    @Size(min = 3, max = 120)
    private String title;
}