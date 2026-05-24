package scari.corp.taro.processor;

import org.springframework.stereotype.Component;
import scari.corp.taro.entity.TaroCards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component("THREE_CARDS")
public class ThreeCardsProcessor implements TaroLayoutProcessor {
    @Override
    public List<SelectedCard> process(List<TaroCards> deck) {
        List<TaroCards> shuffledDeck = new ArrayList<>(deck);

        Collections.shuffle(shuffledDeck);

        List<SelectedCard> result = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            boolean isReversed = ThreadLocalRandom.current().nextBoolean();
            result.add(new SelectedCard(shuffledDeck.get(i), isReversed));
        }
        return result;
    }
}
