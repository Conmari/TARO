package scari.corp.taro.processor;

import org.springframework.stereotype.Component;
import scari.corp.taro.entity.TaroCards;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component("ONE_CARD")
public class OneCardProcessor implements TaroLayoutProcessor {

    @Override
    public List<SelectedCard> process(List<TaroCards> deck) {
        int randomIndex = ThreadLocalRandom.current().nextInt(deck.size());

        boolean isReversed = ThreadLocalRandom.current().nextBoolean();

        return List.of(new SelectedCard(deck.get(randomIndex), isReversed));
    }
}
