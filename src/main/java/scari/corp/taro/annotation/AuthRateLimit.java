package scari.corp.taro.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация-маркер для защиты от перебора паролей на эндпоинтах авторизации.
 * <p>
 * Применяется к методам входа, чтобы автоматически проверять лимиты попыток по IP и логину.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthRateLimit {
}
