package ro.unibuc.hello.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.unibuc.hello.data.ShortUrlEntity;
import ro.unibuc.hello.data.ShortUrlRepository;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Optional;

@Service
public class UrlShortenerService {
    @Autowired
    private ShortUrlRepository shortUrlRepository;

    private static final String Base62 = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int shortUrlLength = 6;

    private static final SecureRandom Random = new SecureRandom();


    public String createShortUrl(String originalUrl){
        Optional<ShortUrlEntity> existingLink = Optional.ofNullable(shortUrlRepository.findByOriginalUrl(originalUrl));
        if(existingLink.isPresent()){
            return existingLink.get().getShortenedUrl();
        }
        ShortUrlEntity newShortUrl = new ShortUrlEntity();
        newShortUrl.setOriginalUrl(originalUrl);
        newShortUrl = shortUrlRepository.save(newShortUrl);

        String shortUrl = shortenUrl();

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

    private String shortenUrl(){
        StringBuilder shortUrl = new StringBuilder();
        boolean shortUrlReady = false;
        while(!shortUrlReady) {
            for (int urlCharIndex = 0; urlCharIndex < shortUrlLength; urlCharIndex++) {
                shortUrl.append(Base62.charAt(Random.nextInt(62)));
            }
            if(Optional.ofNullable(shortUrlRepository.findByShortenedUrl(shortUrl.toString())).isEmpty())
                shortUrlReady = true;
        }
        return shortUrl.toString();
    }

}
