package ru.practicum.ewm.compilation.repository;

import ru.practicum.ewm.compilation.model.Compilation;

import java.util.List;

public interface CompilationRepository {
    Compilation postCompilation(Compilation compilation);

    void deleteCompilation(Long comId);

    Compilation patchCompilation(Long comId, Compilation compilation);

    List<Compilation> getCompilations(Boolean pinned, int from, int size);

    Compilation getCompilationById(Long compId);

    boolean existsById(Long compId);

}
