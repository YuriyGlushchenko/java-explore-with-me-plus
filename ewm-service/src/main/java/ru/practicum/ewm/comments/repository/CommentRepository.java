package ru.practicum.ewm.comments.repository;


import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.comments.model.Comment;
import ru.practicum.ewm.comments.model.CommentStatus;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByEventIdAndStatusOrderByCreatedDesc(Long eventId, CommentStatus status, Pageable pageable);

    Optional<Comment> findByIdAndStatus(Long id, CommentStatus status);

    List<Comment> findByStatusOrderByCreatedAsc(CommentStatus status, Pageable pageable);

    Optional<Comment> findByIdAndAuthorId(Long id, Long authorId);

    boolean existsByIdAndAuthorId(Long id, Long authorId);
}