package scari.corp.taro.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import scari.corp.taro.entity.TaroCards;
import scari.corp.taro.repository.TaroCardsRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaroCacheService {

    private final TaroCardsRepository cardsRepository;

    @Cacheable(value = "allCards", unless = "#result == null || #result.isEmpty()")
    public List<TaroCards> getAllCards() {
        return cardsRepository.findAll();
    }
}
