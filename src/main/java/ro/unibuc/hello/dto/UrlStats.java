package ro.unibuc.hello.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UrlStats {
    private String shortUrl;
    private Long totalVisits;
}
