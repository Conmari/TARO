package scari.corp.taro.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import scari.corp.taro.entity.TaroHistory;
import scari.corp.taro.entity.User;

@Repository
public interface TaroHistoryRepository extends JpaRepository<TaroHistory, Long> {

    Page<TaroHistory> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    Page<TaroHistory> findBySessionIdOrderByCreatedAtDesc(String sessionId, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE TaroHistory h SET h.user = :user WHERE h.sessionId = :sessionId AND h.user IS NULL")
    void linkSessionToUser(@Param("user") User user, @Param("sessionId") String sessionId);

}
