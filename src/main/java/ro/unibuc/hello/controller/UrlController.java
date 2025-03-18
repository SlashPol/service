package ro.unibuc.hello.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ro.unibuc.hello.data.ShortUrlEntity;
import ro.unibuc.hello.dto.UrlStats;
import ro.unibuc.hello.service.UrlShortenerService;

import java.time.LocalDateTime;

@Controller
public class UrlController {

    @Autowired
    private UrlShortenerService urlShortenerService;

    @PostMapping("/create-short-url")
    @ResponseBody
    public ResponseEntity<String> createShortUrl(@RequestBody String originalUrl,
                                                 @RequestParam(required = false)LocalDateTime expiresAt){
        return ResponseEntity.ok(urlShortenerService.createShortUrl(originalUrl, expiresAt));
    }

    @GetMapping("/{shortUrl}")
    @ResponseBody
    public ResponseEntity<String> getOriginalUrl(@PathVariable String shortUrl){
        try {
            return ResponseEntity.ok(urlShortenerService.getOriginalUrl(shortUrl));
        }
        catch (Exception ex){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

    @PostMapping("/delete/{shortUrl}")
    @ResponseBody
    public ResponseEntity<String> deleteShortUrl(@PathVariable String shortUrl){
        try{
            urlShortenerService.deleteShortUrl(shortUrl);
            return ResponseEntity.status(HttpStatus.OK).body("Url deleted successfully");
        }
        catch (Exception ex){
            return  ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

    @GetMapping("/getVisits/{shortUrl}")
    @ResponseBody
    public ResponseEntity<UrlStats> getStats(@PathVariable String shortUrl){
        try{
            UrlStats urlStats = urlShortenerService.getUrlStats(shortUrl);
            return ResponseEntity.ok(urlStats);
        }
        catch (Exception ex){
            return ResponseEntity.notFound().build();
        }
    }
}
