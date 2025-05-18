package br.com.md.controllers;

import br.com.md.config.JwtUtil;
import br.com.md.domain.User;
import br.com.md.services.implementations.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/oauth2/callback")
public class GoogleOAuthCallbackController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @Value("${google.client-id}")
    private String clientId;

    @Value("${google.client-secret}")
    private String clientSecret;

    @Value("${google.redirect-uri}")
    private String redirectUri;

    public GoogleOAuthCallbackController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/google")
    public void handleGoogleCallback(@RequestParam("code") String code,
                                     HttpServletResponse response) throws IOException {

        // Troca o código de autorização pelo access_token
        String tokenEndpoint = "https://oauth2.googleapis.com/token";
        RestTemplate restTemplate = new RestTemplate();

        Map<String, String> params = Map.of(
                "code", code,
                "client_id", clientId,
                "client_secret", clientSecret,
                "redirect_uri", redirectUri,
                "grant_type", "authorization_code"
        );

        Map<String, Object> tokenResponse = restTemplate.postForObject(tokenEndpoint, params, Map.class);
        String accessToken = (String) tokenResponse.get("access_token");

        // Requisição para obter dados do usuário com o access token
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> userInfoResponse = restTemplate.exchange(
                "https://www.googleapis.com/oauth2/v3/userinfo",
                HttpMethod.GET,
                entity,
                Map.class
        );

        Map<String, Object> userInfo = userInfoResponse.getBody();
        String email = (String) userInfo.get("email");
        String name = (String) userInfo.get("name");

        // Registro ou login do usuário
        Optional<User> userOptional = authService.authenticate(email, null); // auth adaptado
        User user = userOptional.orElseGet(() -> authService.registerOAuthUser(email, name));

        String jwt = jwtUtil.generateToken(email);

        // Redirecionamento para o frontend com token JWT
        response.sendRedirect("https://seufrontend.com/logged?token=Bearer " + jwt);
    }
}
