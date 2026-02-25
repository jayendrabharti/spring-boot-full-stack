package com.jayendrabharti.springbootfullstack.services;

import com.jayendrabharti.springbootfullstack.dto.AuthResponse;
import com.jayendrabharti.springbootfullstack.dto.LoginRequest;
import com.jayendrabharti.springbootfullstack.dto.SignupRequest;
import com.jayendrabharti.springbootfullstack.models.RefreshToken;
import com.jayendrabharti.springbootfullstack.models.User;
import com.jayendrabharti.springbootfullstack.repositories.RefreshTokenRepository;
import com.jayendrabharti.springbootfullstack.repositories.UserRepository;
import jakarta.servlet.http.Cookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuthService {

	private final UserRepository userRepository;

	private final RefreshTokenRepository refreshTokenRepository;

	private final JwtService jwtService;

	private final PasswordEncoder passwordEncoder;

	private final AuthenticationManager authenticationManager;

	public AuthService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository,
			JwtService jwtService, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
		this.userRepository = userRepository;
		this.refreshTokenRepository = refreshTokenRepository;
		this.jwtService = jwtService;
		this.passwordEncoder = passwordEncoder;
		this.authenticationManager = authenticationManager;
	}

	public record AuthResult(AuthResponse response, Cookie accessTokenCookie, Cookie refreshTokenCookie) {
	}

	public AuthResult signup(SignupRequest request) {
		if (userRepository.existsByEmail(request.email())) {
			throw new RuntimeException("Email already registered");
		}

		User user = new User(request.email(), passwordEncoder.encode(request.password()));
		userRepository.save(user);

		return generateTokensAndCookies(user);
	}

	public AuthResult login(LoginRequest request) {
		authenticationManager
			.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));

		User user = userRepository.findByEmail(request.email())
			.orElseThrow(() -> new RuntimeException("User not found"));

		return generateTokensAndCookies(user);
	}

	public Cookie[] logout(String userId) {
		refreshTokenRepository.deleteByUserId(userId);
		return new Cookie[] { createClearedCookie("access_token", "/"),
				createClearedCookie("refresh_token", "/api/auth/refresh") };
	}

	public AuthResult refreshTokens(String refreshTokenValue) {
		RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
			.orElseThrow(() -> new RuntimeException("Invalid refresh token"));

		if (refreshToken.isExpired()) {
			refreshTokenRepository.delete(refreshToken);
			throw new RuntimeException("Refresh token expired");
		}

		// Delete old refresh token (rotation)
		refreshTokenRepository.delete(refreshToken);

		User user = userRepository.findById(refreshToken.getUserId())
			.orElseThrow(() -> new RuntimeException("User not found"));

		return generateTokensAndCookies(user);
	}

	private AuthResult generateTokensAndCookies(User user) {
		String accessToken = jwtService.generateAccessToken(user);
		String refreshToken = jwtService.generateRefreshToken();

		// Save refresh token to DB
		RefreshToken refreshTokenEntity = new RefreshToken(refreshToken, user.getId(),
				Instant.now().plusMillis(jwtService.getRefreshTokenExpiration()));
		refreshTokenRepository.save(refreshTokenEntity);

		Cookie accessCookie = createCookie("access_token", accessToken, 900, "/");
		Cookie refreshCookie = createCookie("refresh_token", refreshToken,
				(int) (jwtService.getRefreshTokenExpiration() / 1000), "/api/auth/refresh");

		return new AuthResult(new AuthResponse("Success", user.getEmail()), accessCookie, refreshCookie);
	}

	private Cookie createCookie(String name, String value, int maxAgeSeconds, String path) {
		Cookie cookie = new Cookie(name, value);
		cookie.setHttpOnly(true);
		cookie.setPath(path);
		cookie.setMaxAge(maxAgeSeconds);
		// cookie.setSecure(true); // uncomment for HTTPS in production
		return cookie;
	}

	private Cookie createClearedCookie(String name, String path) {
		Cookie cookie = new Cookie(name, "");
		cookie.setHttpOnly(true);
		cookie.setPath(path);
		cookie.setMaxAge(0);
		return cookie;
	}

}
