package com.example.user.controller;

import com.example.db.entity.User;
import com.example.db.repository.UserRepository;
import com.example.user.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService auth;
    private final UserRepository users;
    private final SecurityContextRepository contexts;

    public record Credentials(@Email @NotBlank String email, @NotBlank String password) {}
    public record SignUp(@Email @NotBlank String email, @Size(min = 8) String password, @NotBlank String name) {}
    public record Profile(Long id, String email, String name) {
        public static Profile from(User user) { return new Profile(user.getId(), user.getEmail(), user.getName()); }
    }

    @GetMapping("/csrf")
    public String csrf(CsrfToken token) { return token.getToken(); }

    @PostMapping("/signup")
    public Profile signUp(@Valid @RequestBody SignUp body, HttpServletRequest request, HttpServletResponse response) {
        User user = auth.signUp(body.email(), body.password(), body.name());
        signInSession(user, request, response);
        return Profile.from(user);
    }

    @PostMapping("/login")
    public Profile login(@Valid @RequestBody Credentials body, HttpServletRequest request, HttpServletResponse response) {
        User user = auth.signIn(body.email(), body.password());
        signInSession(user, request, response);
        return Profile.from(user);
    }

    @GetMapping("/me")
    public Profile me(Authentication authentication) {
        Long id = (Long) authentication.getPrincipal();
        return Profile.from(users.findById(id).orElseThrow());
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        if (request.getSession(false) != null) request.getSession(false).invalidate();
    }

    private void signInSession(User user, HttpServletRequest request, HttpServletResponse response) {
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(user.getId(), null, List.of()));
        SecurityContextHolder.setContext(context);
        contexts.saveContext(context, request, response);
    }
}
