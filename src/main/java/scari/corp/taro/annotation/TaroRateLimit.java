package scari.corp.taro.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация-маркер для включения лимита частоты запросов к картам Таро.
 * <p>
 * Применяется к методам контроллеров, чтобы автоматически отсекать частые клики пользователей и гостей.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TaroRateLimit {
}
