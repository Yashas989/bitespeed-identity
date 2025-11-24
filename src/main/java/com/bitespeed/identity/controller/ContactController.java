package com.bitespeed.identity.controller;

import com.bitespeed.identity.service.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/identify")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;


    @PostMapping
    public ResponseEntity<Map<String, Object>> identify(@RequestBody Map<String, Object> request) {
        String email = (String) request.get("email");
        String phone = request.get("phoneNumber") != null ? request.get("phoneNumber").toString() : null;
        return ResponseEntity.ok(contactService.identify(email, phone));
    }
}
