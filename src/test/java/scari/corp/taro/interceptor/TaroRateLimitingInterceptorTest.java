package scari.corp.taro.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import scari.corp.taro.service.TaroService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
class TaroRateLimitingInterceptorTest {

    private static final String RANDOM_CARD_URL = "/api/v1/taro/random";
    private static final String TOO_MANY_REQUESTS_MSG = "Вы совершаете запросы слишком часто. Пожалуйста, подождите 3 секунды.";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaroService taroService;

    @BeforeEach
    void setUp() {
        reset(taroService);
    }

    @Test
    @WithMockUser(username = "lucky_user")
    @DisplayName("Блокировка частых кликов по картам (429) для авторизованного пользователя")
    void shouldBlockFrequentRequestsWithStatus429() throws Exception {
        log.info("Старт теста: проверка лимитов для авторизованного пользователя...");
        when(taroService.generateLayout(any(), any(), any())).thenReturn(List.of());
        MockHttpSession session = new MockHttpSession();

        log.info("Пользователь делает первый клик на /random.");
        mockMvc.perform(get(RANDOM_CARD_URL)
                        .session(session)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        log.info("Пользователь делает мгновенный второй клик на /random");
        mockMvc.perform(get(RANDOM_CARD_URL)
                        .session(session)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message")
                        .value(TOO_MANY_REQUESTS_MSG));

        verify(taroService, times(1)
                .description("Интерцептор пропустил спам-запрос от авторизованного пользователя"))
                .generateLayout(any(), any(), any());
    }

    @Test
    @DisplayName("Блокировка частых кликов по картам (429) для Гостя (Анонимная сессия)")
    void shouldBlockFrequentRequestsForGuestSession() throws Exception {
        log.info("Старт теста: проверка лимитов для неавторизованного пользователя...");
        when(taroService.generateLayout(any(), any(), any())).thenReturn(List.of());
        MockHttpSession guestSession = new MockHttpSession();

        log.info("Гость делает первый клик на /random.");
        mockMvc.perform(get(RANDOM_CARD_URL).session(guestSession).contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        log.info("Гость делает мгновенный второй клик на /random");
        mockMvc.perform(get(RANDOM_CARD_URL).session(guestSession).contentType(APPLICATION_JSON))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message")
                        .value(TOO_MANY_REQUESTS_MSG));

        verify(taroService, times(1)
                .description("Интерцептор пропустил спам-запрос от гостя"))
                .generateLayout(any(), any(), any());
    }
}
