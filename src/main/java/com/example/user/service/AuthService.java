package com.example.user.service;

import com.example.db.entity.User;
import com.example.db.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder passwords;

    @Transactional
    public User signUp(String email, String password, String name) {
        String normalized = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        if (normalized.isBlank() || password == null || password.length() < 8 ||
                name == null || name.isBlank()) {
            throw new IllegalArgumentException("이메일, 이름과 8자 이상 비밀번호를 입력해 주세요.");
        }
        if (users.findByEmail(normalized).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        User user = User.builder().email(normalized).passwordHash(passwords.encode(password)).name(name.trim()).build();
        users.save(user);
        return user;
    }

    @Transactional(readOnly = true)
    public User signIn(String email, String password) {
        String normalized = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        User user = users.findByEmail(normalized)
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));
        if (user.getPasswordHash() == null || !passwords.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        return user;
    }
}
