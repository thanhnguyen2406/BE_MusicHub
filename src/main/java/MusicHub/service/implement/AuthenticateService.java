package MusicHub.service.implement;

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
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticateService implements IAuthenticateService {
    private final IUserService userService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Value("${JWT_SIGNER_KEY}")
    private String signerKey;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.provider.google.authorization-uri}")
    private String authUrl;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    @Value("${spring.security.oauth2.client.provider.google.token-uri}")
    private String tokenUri;

    @Value("${spring.security.oauth2.client.provider.google.user-info-uri}")
    private String userInfoUri;

    @Override
    public ResponseAPI<Void> authenticate(AuthenticateDTO request, Boolean isGoogleLogin) {
        try{
            String sanitizedUsername = request.getEmail().trim();
            if (!sanitizedUsername.endsWith("@gmail.com")) {
                throw new AppException(ErrorCode.UNAUTHENTICATED_USERNAME_DOMAIN);
            }

            User user = userRepository.findByEmail(sanitizedUsername)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            if (!isGoogleLogin) {
                if (user.getIsGoogleAccount()) {
                    throw new AppException(ErrorCode.UNAUTHENTICATED_LOGIN);
                }
                PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(5);
                boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());
                if (!authenticated) {
                    throw new AppException(ErrorCode.UNAUTHENTICATED);
                }
            }

            String token = generateToken(user);

            return ResponseAPI.<Void>builder()
                    .code(200)
                    .message("Login successfully")
                    .token(token)
                    .expiration(new Date(Instant.now().plus(2, ChronoUnit.HOURS).toEpochMilli()))
                    .build();
        } catch (AppException e) {
            return ResponseAPI.<Void>builder()
                    .code(e.getErrorCode().getCode())
                    .message(e.getErrorCode().getMessage())
                    .build();
        } catch (Exception e) {
            return ResponseAPI.<Void>builder()
                    .code(500)
                    .message("Error Occurs During User Login: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public ResponseAPI<Void> introspect(IntrospectDTO request) throws ParseException, JOSEException {
        String token = request.getToken();
        JWSVerifier verifier = new MACVerifier(signerKey.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        boolean verified = signedJWT.verify(verifier);

        String message = (verified && expirationTime.after(new Date())) ? "Token is valid" : "Token is invalid";

        return ResponseAPI.<Void>builder()
                .code(200)
                .message(message)
                .token(null)
                .expiration(expirationTime)
                .build();
    }

    private String generateToken(User user) {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS256);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issuer("MyApp")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(2, ChronoUnit.HOURS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(jwsHeader, payload);

        try {
            jwsObject.sign(new MACSigner(signerKey.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }
    }

    private String buildScope(User user){
        return "ROLE_" + user.getRole();
    }

    @Override
    public ResponseAPI<Void> registerUser(UserDTO request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        String sanitizedUsername = request.getEmail().trim();
        if (!sanitizedUsername.endsWith("@gmail.com")) {
            throw new AppException(ErrorCode.UNAUTHENTICATED_USERNAME_DOMAIN);
        }

        User user = userMapper.toUser(request);
        user.setRole("PATIENT");
        user.setIsGoogleAccount(false);
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(5);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        return ResponseAPI.<Void>builder()
                .code(200)
                .message("Patient register successfully")
                .build();
    }

    @Override
    public ResponseAPI<String> generateAuthUrl(HttpServletRequest request, String state) {
        String url;

        if (state.equals("login")) {
            url = authUrl + "?client_id=" + clientId
                    + "&redirect_uri=" + redirectUri
                    + "&response_type=code"
                    + "&scope=email"
                    + "&state=" + state;
        } else {
            url = authUrl + "?client_id=" + clientId
                    + "&redirect_uri=" + redirectUri
                    + "&response_type=code"
                    + "&scope=email+profile"
                    + "&state=" + state;
        }

        return ResponseAPI.<String>builder()
                .code(200)
                .message("Url generated successful")
                .data(url)
                .build();
    }

    @Override
    public ResponseAPI<Void> getAccessToken(String code, String state) {
        RestTemplate restTemplate = new RestTemplate();
        String tokenUrl = tokenUri;

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("client_id", clientId);
        requestBody.add("client_secret", clientSecret);
        requestBody.add("code", code);
        requestBody.add("grant_type", "authorization_code");
        requestBody.add("redirect_uri", redirectUri);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, requestEntity, Map.class);

        Object token = response.getBody() != null ? response.getBody().get("access_token") : null;
        String accessToken = token instanceof String ? (String) token : null;

        if (!response.getStatusCode().is2xxSuccessful() || accessToken == null) {
            throw new AppException(ErrorCode.TOKEN_FETCHED_FAIL);
        }

        return getUserInfo(accessToken, state);
    }

    public ResponseAPI<Void> getUserInfo(String accessToken, String state) {
        System.out.println(state);
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<Void> userRequest = new HttpEntity<>(headers);

        ResponseEntity<Map> userResponse = restTemplate.exchange(
                userInfoUri,
                HttpMethod.GET,
                userRequest,
                Map.class
        );

        Map userInfo = userResponse.getBody();

        if (!userResponse.getStatusCode().is2xxSuccessful() || userInfo == null) {
            throw new AppException(ErrorCode.USERINFO_FETCHED_FAIL);
        }

        String email = userInfo.get("email") != null ? userInfo.get("email").toString() : "Unknown";
        String name = userInfo.get("name") != null ? userInfo.get("name").toString() : "Unknown";

        if (state.equals("login")) {
            AuthenticateDTO authenticateDTO = new AuthenticateDTO();
            authenticateDTO.setEmail(email);
            return authenticate(authenticateDTO, true);
        } else if (state.equals("register")) {
            return userService.createPatient(email, name);
        } else {
            return ResponseAPI.<Void>builder()
                    .code(200)
                    .message("User info fetched successfully")
                    .build();
        }
    }
}
