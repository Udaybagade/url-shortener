
package com.urlshortener.controller;

import com.urlshortener.dto.CreateUrlResponse;
import com.urlshortener.service.UrlService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@WebMvcTest(UrlController.class)
class UrlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UrlService urlService;

    @Test
    void createShortUrlShouldReturnCreatedResponse() throws Exception {

        CreateUrlResponse response = new CreateUrlResponse(
                "abc12345",
                "http://localhost:8080/abc12345",
                "https://www.example.com",
                null
        );

        when(urlService.createShortUrl(
                org.mockito.ArgumentMatchers.any(
                        com.urlshortener.dto.CreateUrlRequest.class
                )
        )).thenReturn(response);

        mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "originalUrl": "https://www.example.com"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortCode").value("abc12345"))
                .andExpect(jsonPath("$.shortUrl")
                        .value("http://localhost:8080/abc12345"))
                .andExpect(jsonPath("$.originalUrl")
                        .value("https://www.example.com"));
    }

    @Test
    void redirectShouldSendUserToOriginalUrl() throws Exception {

        when(urlService.getOriginalUrl("abc12345"))
                .thenReturn("https://www.example.com");

        mockMvc.perform(get("/abc12345"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("https://www.example.com"));
    }
}
