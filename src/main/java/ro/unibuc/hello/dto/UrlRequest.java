package ro.unibuc.hello.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class UrlRequest {
    private String originalUrl;

    private LocalDateTime expiresAt;
}
