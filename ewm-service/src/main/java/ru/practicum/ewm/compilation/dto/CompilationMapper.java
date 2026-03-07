package ru.practicum.ewm.compilation.dto;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.event.dto.EventShortDto;

import java.util.ArrayList;
import java.util.List;

public class CompilationMapper {

    public static Compilation toCompilation(NewCompilationDto dto) {
        return Compilation.builder()
                .title(dto.getTitle())
                .pinned(dto.getPinned() != null && dto.getPinned())
                .events(dto.getEvents() != null ? dto.getEvents() : new ArrayList<>())
                .build();
    }

    public static Compilation toCompilation(UpdateCompilationRequest request) {
        return Compilation.builder()
                .title(request.getTitle())
                .pinned(request.getPinned())
                .events(request.getEvents())
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
