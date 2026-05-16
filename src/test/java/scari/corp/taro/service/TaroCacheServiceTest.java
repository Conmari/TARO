package scari.corp.taro.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import scari.corp.taro.entity.TaroCards;
import scari.corp.taro.repository.TaroCardsRepository;

import java.util.List;

import static org.mockito.Mockito.*;

@Slf4j
@SpringBootTest
class TaroCacheServiceTest {

    @Autowired
    private TaroCacheService taroCacheService;

    @MockitoBean
    private TaroCardsRepository cardsRepository;

    @Test
    @DisplayName("Проверка кэширования, получение всех карт таро")
    void shouldCacheAllCards() {
        log.info("Запуск теста: проверка кэширования...");

        List<TaroCards> cards = List.of(new TaroCards(), new TaroCards());
        when(cardsRepository.findAll()).thenReturn(cards);

        log.info("Первый вызов из БД");
        taroCacheService.getAllCards();

        log.info("Второй вызов из БД, должен быть из кэша");
        taroCacheService.getAllCards();

        verify(cardsRepository, times(1)
                .description("Метод findAll() был вызван более одного раза"))
                .findAll();
    }
}
