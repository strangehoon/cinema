package com.example.user;

import com.example.db.repository.UserRepository;
import com.example.user.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

class AuthServiceTest {
    private final UserRepository users = mock(UserRepository.class);
    private final AuthService service = new AuthService(users, new BCryptPasswordEncoder());

    @Nested
    @DisplayName("회원가입")
    class SignUp {
        @Test
        @DisplayName("비밀번호를 해시하여 저장한다")
        void success() {
            // given
            when(users.findByEmail("guest@example.com")).thenReturn(Optional.empty());

            // when
            var user = service.signUp("guest@example.com", "password123", "관객");

            // then
            assertThat(user.getEmail()).isEqualTo("guest@example.com");
            assertThat(user.getPasswordHash()).isNotEqualTo("password123");
            assertThat(new BCryptPasswordEncoder().matches("password123", user.getPasswordHash())).isTrue();
        }

        @Test
        @DisplayName("이미 등록된 이메일은 거부한다")
        void fail_duplicate_email() {
            // given
            when(users.findByEmail("guest@example.com")).thenReturn(Optional.of(mock(com.example.db.entity.User.class)));

            // when & then
            assertThatThrownBy(() -> service.signUp("guest@example.com", "password123", "관객"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
