package scari.corp.taro.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import scari.corp.taro.entity.TaroLayout;
import scari.corp.taro.entity.User;

@Repository
public interface TaroLayoutRepository extends JpaRepository<TaroLayout, Long> {

    @EntityGraph(attributePaths = {"cards", "cards.card"})
    Page<TaroLayout> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    @EntityGraph(attributePaths = {"cards", "cards.card"})
    Page<TaroLayout> findBySessionIdOrderByCreatedAtDesc(String sessionId, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE TaroLayout l SET l.user = :user WHERE l.sessionId = :sessionId AND l.user IS NULL")
    void linkSessionToUser(@Param("user") User user, @Param("sessionId") String sessionId);
}
