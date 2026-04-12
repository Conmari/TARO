package scari.corp.taro.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import scari.corp.taro.dto.auth.LoginRequest;
import scari.corp.taro.dto.auth.RegisterRequest;
import scari.corp.taro.entity.User;
import scari.corp.taro.exception.UserAlreadyExistsException;
import scari.corp.taro.repository.TaroHistoryRepository;
import scari.corp.taro.repository.UserRepository;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final TaroHistoryRepository historyRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public void register(RegisterRequest request, String sessionId) {
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new UserAlreadyExistsException("Пользователь уже существует");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        userRepository.save(user);

        historyRepository.linkSessionToUser(user, sessionId);
        log.info("Пользователь зарегистрирован: {}", request.username());
    }

    @Transactional
    public void login(LoginRequest request, String sessionId, HttpServletRequest servletRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        servletRequest.getSession().setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                context
        );

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new NoSuchElementException("Пользователь не найден"));
        historyRepository.linkSessionToUser(user, sessionId);
        log.info("Пользователь вошёл в систему: {}", request.username());
    }
}
