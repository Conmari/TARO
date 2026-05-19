package scari.corp.taro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import scari.corp.taro.entity.TaroCards;

import java.util.List;

@Repository
public interface TaroCardsRepository extends JpaRepository<TaroCards, Long> {

    List<TaroCards> findAll();
}
