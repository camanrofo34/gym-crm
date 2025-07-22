package gym.crm.hours_microservice.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class TransactionLoggingFilter extends OncePerRequestFilter {

    private static final String TRANSACTION_ID_HEADER = "Transaction-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String transactionId = request.getHeader(TRANSACTION_ID_HEADER);

            MDC.put("transactionId", transactionId);

            log.info("→ Transaction START | Method: {} | URI: {} | TxId: {}",
                    request.getMethod(), request.getRequestURI(), transactionId);

            filterChain.doFilter(request, response);

        } finally {
            log.info("← Transaction END | URI: {} | TxId: {}",
                    request.getRequestURI(), MDC.get("transactionId"));
            MDC.clear();
        }
    }
}

