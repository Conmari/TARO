package scari.corp.taro.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import scari.corp.taro.dto.CardResponseDto;
import scari.corp.taro.service.TaroService;

@RestController
@RequestMapping("/api/v1/taro")
@RequiredArgsConstructor
public class TaroController {

    private final TaroService tarotService;

    /**
     * Возвращает случайную карту Таро.
     *
     * @return DTO {@link CardResponseDto}  с данными карты
     */
    @GetMapping("/random")
    public CardResponseDto randomCard() {
        return tarotService.getRandomCard();
    }

}
