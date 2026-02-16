package com.et.SudburyCityPlatform.controller;

import com.et.SudburyCityPlatform.models.ChatRequest;
import com.et.SudburyCityPlatform.service.ChatServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatServiceImpl chatService;
    @Autowired

    public ChatController(ChatServiceImpl chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('STUDENT','EMPLOYER','ADMIN')")
    public String chat(@RequestBody ChatRequest request) {
        return chatService.chat(request.getMessage());
    }
}

