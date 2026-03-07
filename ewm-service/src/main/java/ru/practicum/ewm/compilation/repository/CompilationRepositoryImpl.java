package ru.practicum.ewm.compilation.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.compilation.model.Compilation;

import java.sql.PreparedStatement;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class CompilationRepositoryImpl implements CompilationRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Compilation postCompilation(Compilation compilation) {
        String sql = "INSERT INTO compilations (title, pinned) VALUES (?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, compilation.getTitle());
            ps.setBoolean(2, compilation.getPinned());
            return ps;
        }, keyHolder);

        Long compId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        compilation.setId(compId);

        if (compilation.getEvents() != null && !compilation.getEvents().isEmpty()) {
            saveEventsRelations(compId, compilation.getEvents());
        }

        return compilation;
    }

    @Override
    public void deleteCompilation(Long comId) {
        String sql = "DELETE FROM compilations WHERE id = ?";
        jdbcTemplate.update(sql, comId);
    }

    @Override
    public Compilation patchCompilation(Long compId, Compilation compilation) {
        if (compilation.getTitle() != null) {
            jdbcTemplate.update("UPDATE compilations SET title = ? WHERE id = ?", compilation.getTitle(), compId);
        }
        if (compilation.getPinned() != null) {
            jdbcTemplate.update("UPDATE compilations SET pinned = ? WHERE id = ?", compilation.getPinned(), compId);
        }

        if (compilation.getEvents() != null) {
            jdbcTemplate.update("DELETE FROM compilation_events WHERE compilation_id = ?", compId);
            if (!compilation.getEvents().isEmpty()) {
                saveEventsRelations(compId, compilation.getEvents());
            }
        }
        compilation.setId(compId);
        return compilation;
    }

    @Override
    public List<Compilation> getCompilations (Boolean pinned, int from, int size) {
        String sql = "SELECT * FROM compilations ";
        List<Object> params = new ArrayList<>();

        if (pinned != null) {
            sql += "WHERE pinned = ? ";
            params.add(pinned);
        }
        sql += "ORDER BY id LIMIT ? OFFSET ?";
        params.add(size);
        params.add(from);

        List<Compilation> compilations = jdbcTemplate.query(sql, (rs, rowNum) ->
                        Compilation.builder()
                                .id(rs.getLong("id"))
                                .title(rs.getString("title"))
                                .pinned(rs.getBoolean("pinned"))
                                .build(),
                params.toArray()
        );
        for (Compilation comp : compilations) {
            List<Long> eventIds = jdbcTemplate.queryForList(
                    "SELECT event_id FROM compilation_events WHERE compilation_id = ?",
                    Long.class,
                    comp.getId()
            );
            comp.setEvents(eventIds);
        }

        return compilations;
    }

    @Override
    public Compilation getCompilationById(Long compId) {
        String sqlComp = "SELECT * FROM compilations WHERE id = ?";
        String sqlEvents = "SELECT event_id FROM compilation_events WHERE compilation_id = ?";
        Compilation comp = Objects.requireNonNull(
                jdbcTemplate.queryForObject(sqlComp, (rs, rowNum) ->
                                Compilation.builder()
                                        .id(rs.getLong("id"))
                                        .title(rs.getString("title"))
                                        .pinned(rs.getBoolean("pinned"))
                                        .build(),
                        compId)
        );

        List<Long> eventIds = jdbcTemplate.queryForList(sqlEvents, Long.class, compId);
        comp.setEvents(eventIds);

        return comp;
    }

    @Override
    public boolean existsById(Long comId) {
        String sql = "SELECT COUNT(*) FROM compilations WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, comId);
        return count > 0;
    }

    private void saveEventsRelations(Long compId, List<Long> eventIds) {
        String sql = "INSERT INTO compilation_events (compilation_id, event_id) VALUES (?, ?)";

        for (Long eventId : eventIds) {
            jdbcTemplate.update(sql, compId, eventId);
        }
    }
}
