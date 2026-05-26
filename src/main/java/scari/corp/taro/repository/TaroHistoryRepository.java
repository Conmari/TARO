package scari.corp.taro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import scari.corp.taro.entity.TaroHistory;

@Repository
public interface TaroHistoryRepository extends JpaRepository<TaroHistory, Long> {
}
