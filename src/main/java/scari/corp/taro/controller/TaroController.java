package scari.corp.taro.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import scari.corp.taro.dto.CardResponseDto;
import scari.corp.taro.dto.taro.TaroHistoryResponseDto;
import scari.corp.taro.service.TaroService;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/taro")
@RequiredArgsConstructor
public class TaroController {

    private final TaroService taroService;

    /**
     * Возвращает случайную карту Таро.
     *
     * @return DTO {@link CardResponseDto}  с данными карты
     */
    @GetMapping("/random")
    public CardResponseDto randomCard(Principal principal,
                                      HttpServletRequest req) {
        return taroService.getRandomCard(principal, req);
    }

    @GetMapping("/history")
    public List<TaroHistoryResponseDto> history(@RequestParam(defaultValue = "10") int limit, Principal principal,
                                                HttpServletRequest req) {
        return taroService.getLastReadings(principal, req, limit);
    }

}
