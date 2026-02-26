package ru.practicum.stats.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViewStatsDto {

    private String app;                       // Название сервиса

    private String uri;                        // URI сервиса

    private Long hits;                          // Количество просмотров
}