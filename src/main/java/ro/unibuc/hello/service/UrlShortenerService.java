package ro.unibuc.hello.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.unibuc.hello.data.ShortUrlEntity;
import ro.unibuc.hello.data.ShortUrlRepository;

import java.util.Optional;

@Service
public class UrlShortenerService {
    @Autowired
    private ShortUrlRepository shortUrlRepository;

    private static final String Base62 = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int shortUrlLength = 6;

    // private static final String baseSiteUrl = "http://localhost:8080/";

    public String createShortUrl(String originalUrl){
        Optional<ShortUrlEntity> existingLink = Optional.ofNullable(shortUrlRepository.findByOriginalUrl(originalUrl));
        if(existingLink.isPresent()){
            return existingLink.get().getShortenedUrl();
        }
        ShortUrlEntity newShortUrl = new ShortUrlEntity();
        newShortUrl.setOriginalUrl(originalUrl);

        String shortUrl = shortenUrl(newShortUrl.getId());

        newShortUrl.setShortenedUrl(shortUrl);

        // TBA: creator user id
        shortUrlRepository.save(newShortUrl);
        return shortUrl;
    }

    public String getOriginalUrl(String shortUrl){
        return Optional.ofNullable(shortUrlRepository.findByShortenedUrl(shortUrl))
                .map(ShortUrlEntity::getOriginalUrl)
                .orElseThrow(() -> new RuntimeException("URL not found"));
    }

    private String shortenUrl(Long Id){
        StringBuilder shortUrl = new StringBuilder();
        // shortUrl.append(baseSiteUrl);
        for(int urlCharIndex = 0; urlCharIndex < shortUrlLength; urlCharIndex++){
            shortUrl.append(Base62.charAt((int)(Id % 62)));
            Id /= 62;
        }
        return shortUrl.toString();
    }

}
