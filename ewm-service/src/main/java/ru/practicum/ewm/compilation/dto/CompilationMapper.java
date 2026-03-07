package ru.practicum.ewm.compilation.dto;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.model.Event;

import java.util.ArrayList;
import java.util.List;

public class CompilationMapper {

    public static Compilation toCompilation(NewCompilationDto dto) {
        return Compilation.builder()
                .title(dto.getTitle())
                .pinned(dto.getPinned() != null && dto.getPinned())
                .events(dto.getEvents() != null ?
                        dto.getEvents().stream()
                                .map(id -> Event.builder()
                                        .id(id)
                                        .build())
                                .toList() : new ArrayList<>())
                .build();
    }

    public static Compilation toCompilation(UpdateCompilationRequest request) {
        List<Event> events = null;
        if (request.getEvents() != null) {
            events = request.getEvents().stream()
                    .map(id -> Event.builder()
                            .id(id)
                            .build())
                    .toList();
        }

        return Compilation.builder()
                .title(request.getTitle())
                .pinned(request.getPinned())
                .events(events) // Теперь здесь List<Event>, как и ждет модель
                .build();
    }


    public static CompilationDto toCompilationDto(Compilation compilation, List<EventShortDto> events) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .title(compilation.getTitle())
                .pinned(compilation.getPinned())
                .events(events)
                .build();
    }
}
