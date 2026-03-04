package ru.practicum.ewm.event.repository;


import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.UserEventParam;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.model.QEvent;
import ru.practicum.ewm.request.model.QParticipationRequest;
import ru.practicum.ewm.request.model.RequestStatus;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class EventRepositoryCustomImpl implements EventRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    public List<EventShortDto> findEvents(UserEventParam param) {

        QEvent event = QEvent.event;
        QParticipationRequest request = QParticipationRequest.participationRequest;

        BooleanBuilder predicate = new BooleanBuilder();

        predicate.and(event.state.eq(EventState.PUBLISHED));

        if (param.hasTextSearchRequest()) {
            predicate.and(
                    event.annotation.containsIgnoreCase(param.getText())
                            .or(event.description.containsIgnoreCase(param.getText())));
        }

        if (param.hasCategories()) {
            predicate.and(event.category.id.in(param.getCategories()));
        }

        if (param.hasPaidParam()) {
            predicate.and(event.paid.eq(param.getPaid()));
        }

        if (param.hasDateRange()) {
            predicate.and(event.eventDate.between(param.getRangeStart(), param.getRangeEnd()));
        } else if (param.hasRangeStart()) { // прямо не указано как обрабатывать, когда одна граница
            predicate.and(event.eventDate.after(param.getRangeStart()));
        } else if (param.hasRangeEnd()) {
            predicate.and(event.eventDate.before(param.getRangeEnd()));
        } else {
            predicate.and(event.eventDate.after(LocalDateTime.now()));
        }

        // начинаем составлять запрос (но не отправляем!)
        var query = queryFactory  // var - компилятор сам определяет тип (JPAQuery<EventShortDto> в данном случае)
                .select(Projections.constructor(EventShortDto.class,
                        event.id,
                        event.annotation,
                        event.category,
                        request.count().as("confirmedRequests"),
                        event.eventDate,
                        event.initiator,
                        event.paid,
                        event.title,
                        Expressions.asNumber(0L).as("views")))
                .from(event)
                .leftJoin(request).on(
                        request.event.eq(event)
                                .and(request.status.eq(RequestStatus.CONFIRMED))
                )
                .where(predicate)
                .groupBy(event.id);

        // добавляем фильтрацию в запрос, если требуются только доступные события
        if (param.isOnlyAvailable()) {
            query.having(
                    event.participantLimit.eq(0)
                            .or(request.count().lt(event.participantLimit))
            );
        }

        return query
                .orderBy(event.eventDate.asc()) // сортируем сразу по дате, если нужна по views, то потом в сервисе переделаем
                .offset(param.getFrom())
                .limit(param.getSize())
                .fetch(); // вот только тут отправляется запрос

    }
}
