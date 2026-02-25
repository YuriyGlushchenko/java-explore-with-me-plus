package ru.practicum.stat.dto;

import lombok.*;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class StatDto {
    String app;
    String uri;
    Integer hits;
}
