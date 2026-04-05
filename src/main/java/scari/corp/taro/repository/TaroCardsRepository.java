package scari.corp.taro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import scari.corp.taro.entity.TaroCards;

@Repository
public interface TaroCardsRepository extends JpaRepository<TaroCards, Long> {

    @Query(value = "SELECT * FROM taro_cards ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    TaroCards findRandomCard();
}
