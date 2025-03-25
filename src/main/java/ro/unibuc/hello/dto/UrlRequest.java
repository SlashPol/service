package ro.unibuc.hello.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class UrlRequest {
    private String originalUrl;

    private LocalDateTime expiresAt;
}
