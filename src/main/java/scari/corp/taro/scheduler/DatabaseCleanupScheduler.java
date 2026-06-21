package scari.corp.taro.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import scari.corp.taro.repository.TaroLayoutRepository;

import java.time.LocalDateTime;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class DatabaseCleanupScheduler {

    private final TaroLayoutRepository taroLayoutRepository;

    /**
     * Очистка неактивных гостевых сессий.
     * Запускается автоматически каждую ночь в 03:00:00.
     * <p>
     * Удаляет расклады гостей {@code user_id IS NULL}, созданные более 7 дней назад.
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanExpiredGuestLayouts() {
        log.info("[Фоновый клинер БД] Старт очистки протухших гостевых раскладов...");

        long startTime = System.currentTimeMillis();
        LocalDateTime thresholdDate = LocalDateTime.now().minusDays(7);

        try {
            taroLayoutRepository.deleteExpiredGuestLayouts(thresholdDate);

            long duration = System.currentTimeMillis() - startTime;
            log.info("[Фоновый клинер БД] Процедура успешно завершена за {} мс. База очищена от старого мусора.", duration);
        } catch (Exception e) {
            log.error("[Фоновый клинер БД] ОШИБКА при выполнении очистки: ", e);
        }
    }
}
