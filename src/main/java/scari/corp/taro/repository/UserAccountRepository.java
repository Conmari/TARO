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
}
