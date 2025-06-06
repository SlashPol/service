package ro.unibuc.hello.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ro.unibuc.hello.data.ShortUrlEntity;
import ro.unibuc.hello.data.ShortUrlRepository;
import ro.unibuc.hello.dto.UrlRequest;
import ro.unibuc.hello.dto.UrlStats;
import ro.unibuc.hello.exception.NoPermissionException;
import ro.unibuc.hello.exception.ShortUrlNotFoundException;
import ro.unibuc.hello.utils.ShortUrlGenerator;
import ro.unibuc.hello.utils.Tracking;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Service
@EnableScheduling
public class UrlShortenerService {
    @Autowired
    private ShortUrlRepository shortUrlRepository;
    @Autowired
    private ShortUrlGenerator shortUrlGenerator;
    @Autowired
    private Tracking tracking;

    private final MeterRegistry registry;
    private final Counter shortUrlCreationCounter;
    private final Counter shortUrlAccessCounter;
    private final DistributionSummary deletedUrlSummary;

    public UrlShortenerService(MeterRegistry meterRegistry) {
        this.registry = meterRegistry;
        this.shortUrlCreationCounter = meterRegistry.counter("short.urls.created");
        this.shortUrlAccessCounter = meterRegistry.counter("short.urls.accessed");
        this.deletedUrlSummary = DistributionSummary
                .builder("expired.urls.deleted")
                .description("Amount of expired short URLs")
                .baseUnit("urls")
                .register(meterRegistry);
    }

    public String createShortUrl(UrlRequest urlRequest, String userId, boolean withMonitoring){
        Timer.Sample sample = null;
        if(withMonitoring) {
            sample = Timer.start(registry);
        }
        try {
            String originalUrl = urlRequest.getOriginalUrl();
            LocalDateTime expiresAt = urlRequest.getExpiresAt();

            if (originalUrl == null) {
                throw new IllegalArgumentException("Url must not be empty");
            }

            Optional<ShortUrlEntity> existingLink = Optional.ofNullable(shortUrlRepository.findByOriginalUrl(originalUrl));
            if (existingLink.isPresent()) {
                return existingLink.get().getShortenedUrl();
            }

            if (expiresAt != null && expiresAt.isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Expiration date must be in the future");
            }

            ShortUrlEntity newShortUrl = new ShortUrlEntity();
            newShortUrl.setOriginalUrl(originalUrl);
            newShortUrl.setExpirationDate(Objects.requireNonNullElseGet(expiresAt, () -> LocalDateTime.now().plusMonths(1L)));
            newShortUrl.setCreatorUserId(userId);
            newShortUrl = shortUrlRepository.save(newShortUrl);

            String shortUrl = shortUrlGenerator.getShortUrl();

            newShortUrl.setShortenedUrl(shortUrl);

            shortUrlRepository.save(newShortUrl);
            if(withMonitoring) shortUrlCreationCounter.increment();
            return shortUrl;
        }
        finally {
            if(withMonitoring) sample.stop(registry.timer("create.url.duration", "operation", "createShortUrl"));
        }
    }

    public String getOriginalUrl(String shortUrl, boolean withMonitoring){
        Timer.Sample sample = null;
        if(withMonitoring) {
            sample = Timer.start(registry);
        }
        try {
            String originalUrl = findShortUrl(shortUrl).getOriginalUrl();
            tracking.incrementVisits(shortUrl);
            if (withMonitoring) shortUrlAccessCounter.increment();
            return originalUrl;
        }
        finally {
            if(withMonitoring) sample.stop(registry.timer("get.url.duration", "operation", "getOriginalUrl"));
        }
    }

    public void deleteShortUrl(String shortUrl, String userId){
        ShortUrlEntity shortUrlEntity = findShortUrl(shortUrl);

        if(!shortUrlEntity.getCreatorUserId().equals(userId)){
            throw new NoPermissionException("You are not allowed to delete this URL");
        }
        shortUrlRepository.delete(shortUrlEntity);
    }

    public UrlStats getUrlStats(String shortUrl, String userId){
        ShortUrlEntity shortUrlEntity = findShortUrl(shortUrl);
        if(!shortUrlEntity.getCreatorUserId().equals(userId)){
            throw new NoPermissionException("You are not allowed to view this URL's stats");
        }
        return UrlStats.builder()
                .shortUrl(shortUrl)
                .totalVisits(shortUrlEntity.getTotalVisits())
                .build();
    }

    @Scheduled(fixedRate = 60000)
    public void deleteExpiredUrls(){
        int deletedAmount = shortUrlRepository.deleteByExpirationDateBefore(LocalDateTime.now());
        deletedUrlSummary.record(deletedAmount);
    }

    private ShortUrlEntity findShortUrl(String shortUrl){
        return Optional.ofNullable(shortUrlRepository.findByShortenedUrl(shortUrl))
                .orElseThrow(() -> new ShortUrlNotFoundException(shortUrl));
    }

}
