package com.example.DronLocalizationBackend.security.auth.service;

import com.example.DronLocalizationBackend.security.auth.entity.AuthenticationRequest;
import com.example.DronLocalizationBackend.security.auth.entity.AuthenticationResponse;
import com.example.DronLocalizationBackend.security.auth.entity.RegisterRequest;
import com.example.DronLocalizationBackend.security.auth.exception.UserAlreadyExistAuthenticationException;
import com.example.DronLocalizationBackend.security.service.JwtService;
import com.example.DronLocalizationBackend.user.Role;
import com.example.DronLocalizationBackend.user.UserEntity;
import com.example.DronLocalizationBackend.user.repository.UserEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserEntityRepository userEntityRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationResponse register(@RequestBody RegisterRequest request) {

        Optional<UserEntity> userEntityOptional = userEntityRepository.findByEmailIgnoreCase(request.getEmail());

        if(userEntityOptional.isPresent()) {
            throw new UserAlreadyExistAuthenticationException("User with email " + request.getEmail() + " already exists");
        }

        UserEntity user = UserEntity.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        userEntityRepository.save(user);
        String jwtToken = jwtService.generateToken(user);

        return AuthenticationResponse.builder().token(jwtToken).build();
    }

    public AuthenticationResponse authenticate(@RequestBody AuthenticationRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        UserEntity user = userEntityRepository.findByEmailIgnoreCase(request.getEmail()).orElseThrow();

        String jwtToken = jwtService.generateToken(user);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }
}
