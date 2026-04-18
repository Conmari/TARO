package scari.corp.taro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import scari.corp.taro.entity.TaroHistory;

import java.util.List;

@Repository
public interface TaroHistoryRepository extends JpaRepository<TaroHistory, Long> {
    List<TaroHistory> findAllByOrderByCreatedAtDesc();
}
