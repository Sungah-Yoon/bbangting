package com.khu.bbangting.domain.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.khu.bbangting.domain.bread.service.BreadService;
import com.khu.bbangting.domain.user.model.TokenType;
import com.khu.bbangting.domain.user.model.Tokens;
import com.khu.bbangting.domain.user.repository.TokenRepository;
import com.khu.bbangting.error.CustomException;
import com.khu.bbangting.error.ErrorCode;
import com.khu.bbangting.error.UserException;
import com.khu.bbangting.domain.user.dto.*;
import com.khu.bbangting.domain.user.model.User;
import com.khu.bbangting.domain.user.repository.UserRepository;
import com.khu.bbangting.config.jwt.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Duration;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final BreadService breadService;


    // 회원가입
    @Transactional
    public UserResponseDto join(JoinRequestDto requestDto) {
        isExistUserEmail(requestDto.getEmail());

        String rawPassword = requestDto.getPassword(); // encoding 전 비밀번호
        String encPassword = passwordEncoder.encode(rawPassword);
        requestDto.setPassword(encPassword);

        User saveUser = userRepository.save(JoinRequestDto.ofEntity(requestDto));

        return UserResponseDto.fromUser(saveUser);
    }

    // 로그인
    @Transactional
    public LoginResponseDto login(User user, HttpServletResponse response) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));

        String jwtToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserTokens(user);
        saveToken(user, refreshToken);

        long now = (new Date().getTime());
        Date accessTokenExpiresIn = new Date(now + Duration.ofMinutes(30).toMillis());
        UserTokenDto tokenDto = UserTokenDto.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();

        response.addHeader("Authorization", "BEARER" + " " + tokenDto.getAccessToken());
        response.addHeader("RefreshToken", tokenDto.getRefreshToken());
        response.addHeader("Access-Token-Expire-Time", String.valueOf(accessTokenExpiresIn));

        // 로그인 시 오늘의 빵팅 알림 전송
        User userDetail = userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        breadService.loginNotification(userDetail);

        // 로그인 시 userId, username 반환
        return LoginResponseDto.builder()
                .userId(userDetail.getId())
                .username(userDetail.getUsername())
                .build();

    }

    private void revokeAllUserTokens(User user) {
        List<Tokens> validTokens = tokenRepository.findAllValidTokenByEmail(user.getEmail());
        if (!validTokens.isEmpty()) {
            validTokens.forEach( t-> {
                t.setExpired(true);
                t.setRevoked(true);
            });
            tokenRepository.saveAll(validTokens);
        }
    }

    private void saveToken (User user, String jwtToken) {
        Tokens token = Tokens.builder()
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .email(user.getEmail())
                .build();

        tokenRepository.save(token);
    }

    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail; // username
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }
        refreshToken = authHeader.substring(7);
        userEmail = jwtService.extractUsername(refreshToken);
        if (userEmail != null) {
            User user= this.userRepository.findByEmail(userEmail).get();
            if (jwtService.isTokenValid(refreshToken, user)) {
                String accessToken = jwtService.generateToken(user);
                UserTokenDto userTokenDto = new UserTokenDto(accessToken, refreshToken);
                new ObjectMapper().writeValue(response.getOutputStream(), userTokenDto);
            }
        }
    }

    private void isExistUserEmail(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new UserException("이미 사용 중인 이메일입니다.", HttpStatus.BAD_REQUEST);
        }
    }

}