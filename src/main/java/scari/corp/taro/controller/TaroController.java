package scari.corp.taro.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import scari.corp.taro.dto.taro.CardResponseDto;
import scari.corp.taro.dto.taro.TaroHistoryResponseDto;
import scari.corp.taro.service.TaroService;

import java.security.Principal;

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
        String username = (principal != null) ? principal.getName() : null;
        String sessionId = req.getSession().getId();
        return taroService.getRandomCard(username, sessionId);
    }

    @GetMapping("/history")
    public Page<TaroHistoryResponseDto> history(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal,
            HttpServletRequest req) {

        String username = (principal != null) ? principal.getName() : null;
        String sessionId = req.getSession().getId();
        return taroService.getLastReadings(username, sessionId, page, size);
    }
}
