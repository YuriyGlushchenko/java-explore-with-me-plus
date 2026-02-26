package ru.practicum.stats.server.service;

import ru.practicum.stats.server.dto.HitDto;
import ru.practicum.stats.server.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {

    void saveHit(HitDto dto);

    List<ViewStatsDto> getStats(
            LocalDateTime start,
            LocalDateTime end,
            List<String> uris,
            boolean unique
    );
}