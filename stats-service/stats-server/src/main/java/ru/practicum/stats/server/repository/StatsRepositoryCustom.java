package ru.practicum.stats.server.repository;

import ru.practicum.stats.server.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepositoryCustom {
    List<ViewStatsDto> getStats(
            LocalDateTime start,
            LocalDateTime end,
            List<String> uris,
            boolean unique
    );
}
