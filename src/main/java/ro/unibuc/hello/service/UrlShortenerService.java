package ro.unibuc.hello.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ro.unibuc.hello.data.ShortUrlEntity;
import ro.unibuc.hello.data.ShortUrlRepository;
import ro.unibuc.hello.dto.UrlStats;
import ro.unibuc.hello.util.ShortUrlGenerator;
import ro.unibuc.hello.util.Tracking;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Service
public class UrlShortenerService {
    @Autowired
    private ShortUrlRepository shortUrlRepository;
    @Autowired
    private ShortUrlGenerator shortUrlGenerator;
    @Autowired
    private Tracking tracking;

    public String createShortUrl(String originalUrl, LocalDateTime expiresAt){
        Optional<ShortUrlEntity> existingLink = Optional.ofNullable(shortUrlRepository.findByOriginalUrl(originalUrl));
        if(existingLink.isPresent()){
            return existingLink.get().getShortenedUrl();
        }

        if(expiresAt != null && expiresAt.isBefore(LocalDateTime.now())){
            throw new IllegalArgumentException("Expiration date must be in the future");
        }

        ShortUrlEntity newShortUrl = new ShortUrlEntity();
        newShortUrl.setOriginalUrl(originalUrl);
        newShortUrl.setExpirationDate(Objects.requireNonNullElseGet(expiresAt, () -> LocalDateTime.now().plusMonths(1L)));
        newShortUrl = shortUrlRepository.save(newShortUrl);

        String shortUrl = shortUrlGenerator.getShortUrl();

        newShortUrl.setShortenedUrl(shortUrl);

        // TBA: creator user id
        shortUrlRepository.save(newShortUrl);
        return shortUrl;
    }

    public String getOriginalUrl(String shortUrl){
        String originalUrl = findShortUrl(shortUrl).getOriginalUrl();
        tracking.incrementVisits(shortUrl);
        return originalUrl;
    }

    public void deleteShortUrl(String shortUrl){
        ShortUrlEntity shortUrlEntity = findShortUrl(shortUrl);
        shortUrlRepository.delete(shortUrlEntity);
    }

    public UrlStats getUrlStats(String shortUrl){
        ShortUrlEntity shortUrlEntity = findShortUrl(shortUrl);
        return UrlStats.builder()
                .shortUrl(shortUrl)
                .totalVisits(shortUrlEntity.getTotalVisits())
                .build();
    }

    @Scheduled(fixedRate = 60000)
    public void deleteExpiredUrls(){
        shortUrlRepository.deleteByExpirationDateBefore(LocalDateTime.now());
    }

    private ShortUrlEntity findShortUrl(String shortUrl){
        return Optional.ofNullable(shortUrlRepository.findByShortenedUrl(shortUrl))
                .orElseThrow(() -> new RuntimeException("URL not found"));
    }

}
