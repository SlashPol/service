package ro.unibuc.hello.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ro.unibuc.hello.dto.UrlStats;
import ro.unibuc.hello.service.UrlShortenerService;
import ro.unibuc.hello.service.UserService;

import java.time.LocalDateTime;

@Controller
public class UrlController {

    @Autowired
    private UrlShortenerService urlShortenerService;

    @Autowired
    private UserService userService;

    @PostMapping("/api/urls/createShortUrl")
    @ResponseBody
    public ResponseEntity<String> createShortUrl(@RequestBody String originalUrl,
                                                 @RequestParam(required = false)LocalDateTime expiresAt){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        String userId = userService.getUserByUsername(username).getId();

        return ResponseEntity.ok(urlShortenerService.createShortUrl(originalUrl, expiresAt, userId));
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

    @DeleteMapping("/api/urls/delete/{shortUrl}")
    @ResponseBody
    public ResponseEntity<String> deleteShortUrl(@PathVariable String shortUrl){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            String userId = userService.getUserByUsername(username).getId();

            urlShortenerService.deleteShortUrl(shortUrl, userId);
            return ResponseEntity.status(HttpStatus.OK).body("Url deleted successfully");
        }
        catch (Exception ex){
            return  ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
        // TODO: custom exception for bad user @radubig
    }

    @GetMapping("/api/urls/getStats/{shortUrl}")
    @ResponseBody
    public ResponseEntity<UrlStats> getStats(@PathVariable String shortUrl){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            String userId = userService.getUserByUsername(username).getId();

            UrlStats urlStats = urlShortenerService.getUrlStats(shortUrl, userId);
            return ResponseEntity.ok(urlStats);
        }
        catch (Exception ex){
            return ResponseEntity.notFound().build();
        }
    }
}
