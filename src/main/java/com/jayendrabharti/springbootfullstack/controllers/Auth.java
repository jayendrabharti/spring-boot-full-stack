package com.jayendrabharti.springbootfullstack.controllers;

import com.jayendrabharti.springbootfullstack.dto.AuthResponse;
import com.jayendrabharti.springbootfullstack.dto.LoginRequest;
import com.jayendrabharti.springbootfullstack.dto.SignupRequest;
import com.jayendrabharti.springbootfullstack.models.User;
import com.jayendrabharti.springbootfullstack.services.AuthService;
import com.jayendrabharti.springbootfullstack.services.AuthService.AuthResult;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class Auth {

	private final AuthService authService;

	public Auth(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/signup")
	public ResponseEntity<AuthResponse> signup(@RequestBody SignupRequest request, HttpServletResponse response) {
		AuthResult result = authService.signup(request);
		response.addCookie(result.accessTokenCookie());
		response.addCookie(result.refreshTokenCookie());
		return ResponseEntity.ok(result.response());
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request, HttpServletResponse response) {
		AuthResult result = authService.login(request);
		response.addCookie(result.accessTokenCookie());
		response.addCookie(result.refreshTokenCookie());
		return ResponseEntity.ok(result.response());
	}

	@PostMapping("/logout")
	public ResponseEntity<AuthResponse> logout(@AuthenticationPrincipal User user, HttpServletResponse response) {
		Cookie[] cookies = authService.logout(user.getId());
		for (Cookie cookie : cookies) {
			response.addCookie(cookie);
		}
		return ResponseEntity.ok(new AuthResponse("Logged out successfully", user.getEmail()));
	}

	@PostMapping("/refresh")
	public ResponseEntity<AuthResponse> refresh(HttpServletRequest request, HttpServletResponse response) {
		String refreshToken = extractCookieValue(request, "refresh_token");
		if (refreshToken == null) {
			return ResponseEntity.status(401).body(new AuthResponse("No refresh token provided", null));
		}

		AuthResult result = authService.refreshTokens(refreshToken);
		response.addCookie(result.accessTokenCookie());
		response.addCookie(result.refreshTokenCookie());
		return ResponseEntity.ok(result.response());
	}

	@GetMapping("/me")
	public ResponseEntity<AuthResponse> me(@AuthenticationPrincipal User user) {
		return ResponseEntity.ok(new AuthResponse("Authenticated", user.getEmail()));
	}

	private String extractCookieValue(HttpServletRequest request, String cookieName) {
		if (request.getCookies() == null)
			return null;
		for (Cookie cookie : request.getCookies()) {
			if (cookieName.equals(cookie.getName())) {
				return cookie.getValue();
			}
		}
		return null;
	}

}
