package scari.corp.taro.config;


import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import scari.corp.taro.entity.TaroCards;
import scari.corp.taro.repository.TaroCardsRepository;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.io.InputStream;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TaroDataLoader implements CommandLineRunner {

    private final TaroCardsRepository repository;
    private final JsonMapper jsonMapper;

    @Override
    public void run(String... args) throws Exception {
        if (repository.count() == 0) {
            System.out.println("Загрузка карт Таро из JSON...");

            InputStream inputStream = new ClassPathResource("data/taro_cards.json").getInputStream();
            List<TaroCards> cards = jsonMapper.readValue(inputStream, new TypeReference<>() {});
            repository.saveAll(cards);
            System.out.println("Загружено карт: " + repository.count());
        } else {
            System.out.println("Карты уже есть в БД, пропускаем загрузку.");
        }
    }

}
