package com.ucb.sakila.apigateway.auth;

import com.ucb.sakila.apigateway.shared.BusinessServiceGraphQlClient;
import jakarta.validation.Valid;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String STAFF_LOGIN_QUERY = """
            mutation($input: StaffLoginInput!) {
                staffLogin(input: $input) { id username }
            }
            """;

    private static final String CREATE_STAFF_MUTATION = """
            mutation($input: CreateStaffInput!) {
                createStaff(input: $input) { id username }
            }
            """;

    private final BusinessServiceGraphQlClient businessServiceClient;
    private final JwtEncoder jwtEncoder;

    @Value("${jwt.expiration-minutes}")
    private long expirationMinutes;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        Map<String, Object> data = businessServiceClient.query(STAFF_LOGIN_QUERY,
                Map.of("input", Map.of("username", request.username(), "password", request.password())));
        return toLoginResponse(data, "staffLogin");
    }

    @PostMapping("/register")
    public LoginResponse register(@Valid @RequestBody RegisterRequest request) {
        Map<String, Object> input = new HashMap<>();
        input.put("firstName", request.firstName());
        input.put("lastName", request.lastName());
        input.put("addressId", request.addressId());
        input.put("storeId", request.storeId());
        input.put("username", request.username());
        input.put("password", request.password());
        input.put("email", request.email());

        Map<String, Object> data = businessServiceClient.query(CREATE_STAFF_MUTATION, Map.of("input", input));
        return toLoginResponse(data, "createStaff");
    }

    @SuppressWarnings("unchecked")
    private LoginResponse toLoginResponse(Map<String, Object> data, String field) {
        Map<String, Object> staff = (Map<String, Object>) data.get(field);
        Integer id = Integer.valueOf(staff.get("id").toString());
        String username = (String) staff.get("username");
        return new LoginResponse(signToken(id, username), new StaffDto(id, username));
    }

    private String signToken(Integer staffId, String username) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(staffId.toString())
                .claim("username", username)
                .issuedAt(now)
                .expiresAt(now.plus(expirationMinutes, ChronoUnit.MINUTES))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
