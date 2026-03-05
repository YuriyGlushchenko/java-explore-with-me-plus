package ru.practicum.ewm.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.projections.EventWithRequests;

import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, EventRepositoryCustom {

    @Query("SELECT e as event, COUNT(r) as confirmedRequests " +
            "FROM Event e " +
            "LEFT JOIN ParticipationRequest r ON r.event = e AND r.status = 'CONFIRMED' " +
            "WHERE e.id = :id " +
            "GROUP BY e")
    Optional<EventWithRequests> findEventWithConfirmedRequests(@Param("id") Long id);
}
