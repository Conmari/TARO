package scari.corp.taro.processor;

import org.springframework.stereotype.Component;
import scari.corp.taro.entity.TaroCards;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component("ONE_CARD")
public class OneCardProcessor implements TaroLayoutProcessor {

    @Override
    public List<TaroCards> process(List<TaroCards> deck) {
        int randomIndex = ThreadLocalRandom.current().nextInt(deck.size());
        return List.of(deck.get(randomIndex));
    }
}
