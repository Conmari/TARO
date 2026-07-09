package scari.corp.taro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import scari.corp.taro.entity.User;
import scari.corp.taro.entity.UserAccount;

import java.util.Optional;

/**
 * Репозиторий для работы со связанными аккаунтами на внешних платформах.
 */
@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    /**
     * Находит основного пользователя сайта, к которому привязан аккаунт мессенджера.
     *
     * @param provider       тип чат-платформы
     * @param providerUserId id пользователя внутри этой платформы
     * @return {@link Optional} с сущностью пользователя, если привязка существует
     */
    @Query("SELECT a.user FROM UserAccount a JOIN a.user WHERE a.provider = :provider AND a.providerUserId = :providerUserId")
    Optional<User> findUserByAccountFields(@Param("provider") String provider, @Param("providerUserId") String providerUserId);

    /**
     * Проверяет, привязан ли уже данный аккаунт мессенджера к какому-либо пользователю.
     *
     * @param provider       тип чат-платформы
     * @param providerUserId id пользователя на платформе
     * @return {@code true}, если привязка существует в базе данных
     */
    boolean existsByProviderAndProviderUserId(String provider, String providerUserId);

    /**
     * Проверяет, подключен ли уже к данному пользователю сайта аккаунт конкретной платформы.
     * Используется для соблюдения бизнес-логики: "Один профиль сайта — один бот внешней платформы".
     *
     * @param user     сущность авторизованного пользователя сайта
     * @param provider строковое имя платформы {@code BotProvider.name()}
     * @return {@code true}, если у пользователя уже есть интеграция с этой платформой
     */
    boolean existsByUserAndProvider(User user, String provider);

    /**
     * Удаляет связь между пользователем сайта и конкретной чат-платформой.
     *
     * @param user     сущность пользователя сайта
     * @param provider строковое имя платформы (например, "TELEGRAM")
     * @return количество удаленных записей (0 или 1)
     */
    int deleteByUserAndProvider(User user, String provider);
}
