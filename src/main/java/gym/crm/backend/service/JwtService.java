package gym.crm.backend.service;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey SECRET_KEY;
    private final long EXPIRATION_TIME;

    public JwtService(@Value("${JWT.SECRET}") String base64Secret,
                   @Value("${JWT.EXPIRATION}") long expirationTime) {
        this.SECRET_KEY = Keys.hmacShaKeyFor(Base64.getDecoder().decode(base64Secret));
        this.EXPIRATION_TIME = expirationTime;
    }


    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)  // Reemplaza setSubject()
                .issuedAt(new Date())  // Reemplaza setIssuedAt()
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))  // Reemplaza setExpiration()
                .signWith(SECRET_KEY, Jwts.SIG.HS256)  // Reemplaza signWith() y especifica el algoritmo
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(SECRET_KEY)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false; // Token inválido o expirado
        }
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public String getTokenFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        return (header != null && header.startsWith("Bearer ")) ? header.substring(7) : null;
    }

}
