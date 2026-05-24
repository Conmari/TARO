package scari.corp.taro.processor;

import org.springframework.stereotype.Component;
import scari.corp.taro.entity.TaroCards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component("THREE_CARDS")
public class ThreeCardsProcessor implements TaroLayoutProcessor {
    @Override
    public List<TaroCards> process(List<TaroCards> deck) {
        List<TaroCards> shuffledDeck = new ArrayList<>(deck);

        Collections.shuffle(shuffledDeck);

        return shuffledDeck.subList(0, 3);
    }
}
