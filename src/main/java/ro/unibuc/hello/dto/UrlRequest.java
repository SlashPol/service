package ro.unibuc.hello.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UrlRequest {
    private String originalUrl;

    private LocalDateTime expiresAt;
}
