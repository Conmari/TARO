package scari.corp.taro.bot.dto;

import lombok.Builder;

import java.util.List;

/**
 * Универсальный DTO для отправки ответов чат-команд.
 * <p>
 * Используется для передачи текста и структуры экранного меню
 * из бизнес-логики в классы-адаптеры мессенджеров.
 */
@Builder
public record BotResponse(
        String destinationId,
        String text,
        List<List<String>> buttons,
        List<String> imageUrls
) {}
