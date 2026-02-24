package ru.practicum.stat.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatDto {
    String app;
    String uri;
    Integer hits;
}
