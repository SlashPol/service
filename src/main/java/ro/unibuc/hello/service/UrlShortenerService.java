package ro.unibuc.hello.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.unibuc.hello.data.ShortUrlEntity;
import ro.unibuc.hello.data.ShortUrlRepository;
import ro.unibuc.hello.util.ShortUrlGenerator;
import ro.unibuc.hello.util.Tracking;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UrlShortenerService {
    @Autowired
    private ShortUrlRepository shortUrlRepository;
    @Autowired
    private ShortUrlGenerator shortUrlGenerator;
    @Autowired
    private Tracking tracking;

    public String createShortUrl(String originalUrl){
        Optional<ShortUrlEntity> existingLink = Optional.ofNullable(shortUrlRepository.findByOriginalUrl(originalUrl));
        if(existingLink.isPresent()){
            return existingLink.get().getShortenedUrl();
        }
        ShortUrlEntity newShortUrl = new ShortUrlEntity();
        newShortUrl.setOriginalUrl(originalUrl);
        newShortUrl.setExpirationDate(LocalDateTime.now().plusMonths(1L));
        newShortUrl = shortUrlRepository.save(newShortUrl);

        String shortUrl = shortUrlGenerator.getShortUrl();

        newShortUrl.setShortenedUrl(shortUrl);

        // TBA: creator user id
        shortUrlRepository.save(newShortUrl);
        return shortUrl;
    }

    public String getOriginalUrl(String shortUrl){
        String originalUrl = Optional.ofNullable(shortUrlRepository.findByShortenedUrl(shortUrl))
                .map(ShortUrlEntity::getOriginalUrl)
                .orElseThrow(() -> new RuntimeException("URL not found"));
        tracking.incrementVisits(shortUrl);
        return originalUrl;
    }

}
