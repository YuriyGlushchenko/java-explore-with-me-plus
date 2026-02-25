package ru.practicum.stat.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ParamHitDto {
    String app;
    String uri;
    String ip;
    LocalDateTime timestamp;
}
