package ro.unibuc.hello.data;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ShortUrlRepository extends MongoRepository<ShortUrlEntity, String> {
    ShortUrlEntity findByOriginalUrl(String originalUrl);

    ShortUrlEntity findByShortenedUrl(String shortenedUrl);

    void deleteByExpirationDateBefore(LocalDateTime expirationDate);
}
