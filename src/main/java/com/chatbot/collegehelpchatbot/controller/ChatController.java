package com.chatbot.collegehelpchatbot.controller;

import com.chatbot.collegehelpchatbot.repository.ChatHistoryRepository;
import com.chatbot.collegehelpchatbot.service.ChatService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired private ChatService chatService;
    @Autowired private ChatHistoryRepository chatHistoryRepository;

    @PostMapping
    public Map<String, Object> chat(@RequestBody Map<String, String> request,
                                     HttpSession session,
                                     Principal principal) {
        String sessionId = session.getId();
        String username  = principal.getName(); // from JWT
        String message   = request.get("message");
        String reply     = chatService.getReply(sessionId, username, message);

        // Get ID of saved history for feedback
        var history = chatHistoryRepository.findBySessionIdOrderByTimestampAsc(sessionId);
        Long historyId = history.isEmpty() ? -1L : history.get(history.size() - 1).getId();

        return Map.of("reply", reply, "historyId", historyId, "username", username);
    }

    @PostMapping("/feedback")
    public Map<String, String> feedback(@RequestBody Map<String, Object> req) {
        Long id      = Long.valueOf(req.get("historyId").toString());
        Boolean helpful = (Boolean) req.get("helpful");
        chatHistoryRepository.findById(id).ifPresent(h -> {
            h.setHelpful(helpful);
            chatHistoryRepository.save(h);
        });
        return Map.of("status", "saved");
    }

    @PostMapping("/clear")
    public Map<String, String> clear(HttpSession session) {
        chatService.clearHistory(session.getId());
        return Map.of("status", "cleared");
    }
}