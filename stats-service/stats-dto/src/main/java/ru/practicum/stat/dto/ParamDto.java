package ru.practicum.stat.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParamDto {
    private LocalDateTime start;
    private LocalDateTime end;
    private String[] uris;
    private Boolean unique;
}
