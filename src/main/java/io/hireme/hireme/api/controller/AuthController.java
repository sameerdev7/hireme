package io.hireme.hireme.api.controller;

import io.hireme.hireme.api.dto.response.UserDto;
import io.hireme.hireme.user.User;
import io.hireme.hireme.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.hireme.hireme.security.JwtUtil;
import io.hireme.hireme.api.dto.request.LoginRequest;
import io.hireme.hireme.api.dto.response.LoginResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@Profile("api")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;

    private final JwtUtil jwtUtil;

    private final UserRepository  userRepository;

    @PostMapping("/login")
  public ResponseEntity<LoginResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
    Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(), loginRequest.getPassword()));

    SecurityContextHolder.getContext().setAuthentication(authentication);
    String jwt = jwtUtil.generateJwtToken(authentication);
    User userDetails = userRepository.findByUsername(loginRequest.getUsername()).orElseThrow();
    UserDto user = new UserDto(userDetails.getUsername(), userDetails.getRole());
    return ResponseEntity.ok(new LoginResponse(jwt, user));
  }
}
