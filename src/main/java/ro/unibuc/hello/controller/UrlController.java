package ro.unibuc.hello.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ro.unibuc.hello.data.ShortUrlEntity;
import ro.unibuc.hello.service.UrlShortenerService;

@Controller
public class UrlController {

    @Autowired
    private UrlShortenerService urlShortenerService;

    @PostMapping("/create-short-url")
    @ResponseBody
    public ResponseEntity<String> createShortUrl(@RequestBody String originalUrl){
        return ResponseEntity.ok(urlShortenerService.createShortUrl(originalUrl));
    }

    @GetMapping("/{shortUrl}")
    @ResponseBody
    public ResponseEntity<String> getOriginalUrl(@PathVariable String shortUrl){
        try {
            return ResponseEntity.ok(urlShortenerService.getOriginalUrl(shortUrl));
        }
        catch (RuntimeException ex){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }
}
