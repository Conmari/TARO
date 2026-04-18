package scari.corp.taro.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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
    public void register(RegisterRequest request, HttpServletRequest servletRequest) {
        String sessionId = servletRequest.getSession().getId();

        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new UserAlreadyExistsException("Пользователь уже существует");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        userRepository.save(user);

        authenticateAndStoreSecurityContext(request.username(), request.password(), servletRequest);

        historyRepository.linkSessionToUser(user, sessionId);
        log.info("Пользователь зарегистрирован: {}", request.username());
    }

    @Transactional
    public void login(LoginRequest request, HttpServletRequest servletRequest) {
        String sessionId = servletRequest.getSession().getId();

        authenticateAndStoreSecurityContext(request.username(), request.password(), servletRequest);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new NoSuchElementException("Пользователь не найден"));

        historyRepository.linkSessionToUser(user, sessionId);
        log.info("Пользователь вошёл в систему: {}", request.username());
    }

    @Transactional
    public void logout(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null) ? auth.getName() : "unknown";

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();

        log.info("Пользователь вышел из системы: {}", username);
    }

    private void authenticateAndStoreSecurityContext(String username, String password, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(username, password);
        Authentication authentication = authenticationManager.authenticate(authToken);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);
        request.getSession().setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                context
        );
    }
}
