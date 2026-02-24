package ru.practicum.stat.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ParamDto {
    LocalDateTime start;
    LocalDateTime end;
    String[] uris;
    Boolean unique;
}
