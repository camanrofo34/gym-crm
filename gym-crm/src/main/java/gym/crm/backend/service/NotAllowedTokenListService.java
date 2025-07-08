package gym.crm.backend.service;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component
public class NotAllowedTokenListService {
    private final Set<String> blacklist = Collections.synchronizedSet(new HashSet<>());

    public void notAllowToken(String token) {
        blacklist.add(token);
    }

    public boolean isTokenBlacklisted(String token) {
        return blacklist.contains(token);
    }
}
