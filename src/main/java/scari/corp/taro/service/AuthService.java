package scari.corp.taro.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import scari.corp.taro.entity.User;
import scari.corp.taro.exception.UserAlreadyExistsException;
import scari.corp.taro.repository.TaroLayoutRepository;
import scari.corp.taro.repository.UserRepository;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final TaroLayoutRepository layoutRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Создает нового пользователя в базе данных и привязывает к нему историю гостевой сессии.
     */
    @Transactional
    public User register(String username, String password, String sessionId) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new UserAlreadyExistsException("Пользователь уже существует");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        User savedUser = userRepository.save(user);

        layoutRepository.linkSessionToUser(savedUser, sessionId);
        log.info("Пользователь зарегистрирован в БД: {}", username);

        return savedUser;
    }

    /**
     * Привязывает накопленную за сессию историю к существующему пользователю после успешного входа.
     */
    @Transactional
    public void processPostLoginHistory(String username, String sessionId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не найден: " + username));

        layoutRepository.linkSessionToUser(user, sessionId);
        log.info("История сессии {} успешно привязана к пользователю: {}", sessionId, username);
    }
}
