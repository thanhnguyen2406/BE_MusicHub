package MusicHub.service.implement;

import MusicHub.configuration.JwtProperties;
import MusicHub.configuration.OauthGoogleProperties;
import MusicHub.dto.AuthenticateDTO.AuthenticateDTO;
import MusicHub.dto.AuthenticateDTO.IntrospectDTO;
import MusicHub.dto.ResponseAPI;
import MusicHub.dto.UserDTO.UserDTO;
import MusicHub.exception.AppException;
import MusicHub.exception.ErrorCode;
import MusicHub.mapper.UserMapper;
import MusicHub.model.User;
import MusicHub.repository.UserRepository;
import MusicHub.service.interf.IAuthenticateService;
import MusicHub.service.interf.IUserService;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticateService implements IAuthenticateService {
    IUserService userService;
    UserRepository userRepository;
    UserMapper userMapper;

    JwtProperties jwtProperties;
    OauthGoogleProperties oauthGoogleProperties;

    public Mono<ResponseAPI<Void>> authenticate(AuthenticateDTO request, Boolean isGoogleLogin) {
        String sanitizedUsername = request.getEmail().trim();

        if (!sanitizedUsername.endsWith("@gmail.com")) {
            return Mono.error(new AppException(ErrorCode.UNAUTHENTICATED_USERNAME_DOMAIN));
        }

        return userRepository.findByEmail(sanitizedUsername)
                .flatMap(user -> {
                    if (!isGoogleLogin) {
                        if (user.getIsGoogleAccount()) {
                            return Mono.error(new AppException(ErrorCode.UNAUTHENTICATED_LOGIN));
                        }

                        return Mono.fromCallable(() -> {
                            PasswordEncoder encoder = new BCryptPasswordEncoder(5);
                            boolean authenticated = encoder.matches(request.getPassword(), user.getPassword());
                            if (!authenticated) {
                                throw new AppException(ErrorCode.UNAUTHENTICATED_USERNAME_PASSWORD);
                            }
                            return user;
                        }).subscribeOn(Schedulers.boundedElastic());
                    }
                    return Mono.just(user);
                })
                .map(user -> {
                    String token = generateToken(user);

                    return ResponseAPI.<Void>builder()
                            .code(200)
                            .message("Login successfully")
                            .token(token)
                            .expiration(new Date(Instant.now().plus(2, ChronoUnit.HOURS).toEpochMilli()))
                            .build();
                })
                .switchIfEmpty(Mono.error(new AppException(ErrorCode.USER_NOT_FOUND)))
                .onErrorResume(AppException.class, e -> Mono.just(
                        ResponseAPI.<Void>builder()
                                .code(e.getErrorCode().getCode())
                                .message(e.getErrorCode().getMessage())
                                .build()))
                .onErrorResume(e -> Mono.just(
                        ResponseAPI.<Void>builder()
                                .code(500)
                                .message("Error Occurs During User Login: " + e.getMessage())
                                .build()));
    }

    @Override
    public Mono<ResponseAPI<Void>> introspect(IntrospectDTO request){
        try {
            String token = request.getToken();
            JWSVerifier verifier = new MACVerifier(jwtProperties.getSignerKey().getBytes());
            SignedJWT signedJWT = SignedJWT.parse(token);

            Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            boolean verified = signedJWT.verify(verifier);

            String message = (verified && expirationTime.after(new Date())) ? "Token is valid" : "Token is invalid";

            return Mono.just(ResponseAPI.<Void>builder()
                    .code(200)
                    .message(message)
                    .expiration(expirationTime)
                    .build());
        } catch (ParseException | JOSEException e) {
            return Mono.just(ResponseAPI.<Void>builder()
                    .code(500)
                    .message("Error while introspecting: " + e.getMessage())
                    .build());
        }
    }

    @Override
    public Mono<ResponseAPI<String>> generateAuthUrl(ServerHttpRequest request, String state) {
        String url =  oauthGoogleProperties.getProvider().getAuthorizationUri()
                + "?client_id=" + oauthGoogleProperties.getRegistration().getClientId()
                + "&redirect_uri=" + oauthGoogleProperties.getRegistration().getRedirectUri()
                + "&response_type=code";

        if (state.equals("login")) {
            url += "&scope=email" + "&state=" + state;
        } else {
            url += "&scope=email+profile" + "&state=" + state;
        }

        return Mono.just(ResponseAPI.<String>builder()
                .code(200)
                .message("Url generated successfully")
                .data(url)
                .build());
    }

    @Override
    public Mono<ResponseAPI<Void>> registerUser(UserDTO request) {
        String sanitizedUsername = request.getEmail().trim();

        if (!sanitizedUsername.endsWith("@gmail.com")) {
            return Mono.error(new AppException(ErrorCode.UNAUTHENTICATED_USERNAME_DOMAIN));
        }

        return userRepository.existsByEmail(request.getEmail())
                .flatMap(existed -> {
                    if (existed) {
                        return Mono.error(new AppException(ErrorCode.USER_EXISTED));
                    }
                    User user = userMapper.toUser(request);
                    user.setRole("USER");
                    user.setIsGoogleAccount(false);
                    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(5);
                    user.setPassword(passwordEncoder.encode(user.getPassword()));

                    return userRepository.save(user)
                            .thenReturn(ResponseAPI.<Void>builder()
                                    .code(200)
                                    .message("User register successfully")
                                    .build());
                })
                .onErrorResume(AppException.class, e -> Mono.just(
                        ResponseAPI.<Void>builder()
                                .code(e.getErrorCode().getCode())
                                .message(e.getErrorCode().getMessage())
                                .build()))
                .onErrorResume(e -> Mono.just(
                        ResponseAPI.<Void>builder()
                                .code(500)
                                .message("Error Occurs During User Login: " + e.getMessage())
                                .build()));
    }

    @Override
    public Mono<ResponseAPI<Void>> getAccessToken(String code, String state) {
        return WebClient.builder()
                .baseUrl(oauthGoogleProperties.getProvider().getTokenUri())
                .build()
                .post()
                .uri("")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("client_id=" + oauthGoogleProperties.getRegistration().getClientId()
                        + "&client_secret=" + oauthGoogleProperties.getRegistration().getClientSecret()
                        + "&code=" + code
                        + "&grant_type=authorization_code"
                        + "&redirect_uri=" + oauthGoogleProperties.getRegistration().getRedirectUri())
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    Object token = response != null ? response.get("access_token") : null;
                    String accessToken = token instanceof String ? (String) token : null;

                    if (accessToken == null) {
                        return Mono.error(new AppException(ErrorCode.UNAUTHENTICATED_LOGIN));
                    }

                    return accessToken;
                })
                .flatMap(accessToken -> getUserInfo((String) accessToken, state));
    }

    public Mono<ResponseAPI<Void>> getUserInfo(String accessToken, String state) {
        return WebClient.builder()
                .baseUrl(oauthGoogleProperties.getProvider().getUserInfoUri())
                .build()
                .get()
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(Map.class)
                .flatMap(userInfo -> {
                    String email = userInfo != null && userInfo.get("email") != null ? userInfo.get("email").toString() : "Unknown";
                    String name = userInfo != null && userInfo.get("name") != null ? userInfo.get("name").toString() : "Unknown";

                    if (state.equals("login")) {
                        AuthenticateDTO authenticateDTO = new AuthenticateDTO();
                        authenticateDTO.setEmail(email);
                        return authenticate(authenticateDTO, true);
                    } else if (state.equals("register")) {
                        return userService.createUser(email, name);
                    } else {
                        return Mono.just(ResponseAPI.<Void>builder()
                                .code(200)
                                .message("User info fetched successfully")
                                .build());
                    }
                })
                .onErrorResume(e -> Mono.just(
                        ResponseAPI.<Void>builder()
                                .code(500)
                                .message("Error fetching user info: " + e.getMessage())
                                .build()));
    }


    private String generateToken(User user) {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS256);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issuer("MyApp")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plusSeconds(jwtProperties.getExpiration()).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(jwsHeader, payload);

        try {
            jwsObject.sign(new MACSigner(jwtProperties.getSignerKey().getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }
    }

    private String buildScope(User user){
        return "ROLE_" + user.getRole();
    }
}
