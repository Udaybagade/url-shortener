
package com.urlshortener.controller;

import com.urlshortener.dto.CreateUrlRequest;
import com.urlshortener.dto.CreateUrlResponse;
import com.urlshortener.service.UrlService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;

    @PostMapping("/api/urls")
    public ResponseEntity<CreateUrlResponse> createShortUrl(
            @Valid @RequestBody CreateUrlRequest request) {

        CreateUrlResponse response =
                urlService.createShortUrl(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{shortCode:[a-zA-Z0-9_-]+}")
    public RedirectView redirect(
            @PathVariable String shortCode) {

        String originalUrl =
                urlService.getOriginalUrl(shortCode);

        RedirectView redirectView = new RedirectView();

        redirectView.setUrl(originalUrl);
        redirectView.setStatusCode(HttpStatus.FOUND);
        redirectView.setExposeModelAttributes(false);

        return redirectView;
    }
}
