package com.chatbot.collegehelpchatbot.service;

import com.chatbot.collegehelpchatbot.model.ChatHistory;
import com.chatbot.collegehelpchatbot.model.ChatbotData;
import com.chatbot.collegehelpchatbot.repository.ChatHistoryRepository;
import com.chatbot.collegehelpchatbot.repository.ChatbotDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatService {

    @Autowired private ChatbotDataRepository chatbotDataRepository;
    @Autowired private ChatHistoryRepository chatHistoryRepository;

    @Value("${groq.api.key}")   private String apiKey;
    @Value("${college.name}")   private String collegeName;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
    private final Map<String, List<Map<String, String>>> sessionHistory = new ConcurrentHashMap<>();

    public String getReply(String sessionId, String username, String message) {
        String lowerMessage = message.toLowerCase().trim();
        List<ChatbotData> matches = chatbotDataRepository.searchByKeyword(lowerMessage);

        String reply;
        if (!matches.isEmpty()) {
            reply = matches.get(0).getAnswer();
            addToHistory(sessionId, "user", message);
            addToHistory(sessionId, "assistant", reply);
        } else {
            reply = getGroqReply(sessionId, message);
        }

        // Save to DB with username
        chatHistoryRepository.save(new ChatHistory(sessionId, username, message, reply));
        return reply;
    }

    private String getGroqReply(String sessionId, String userMessage) {
        try {
            addToHistory(sessionId, "user", userMessage);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            String systemPrompt = String.format("""
                You are a helpful college assistant chatbot for %s, an Indian engineering college.
                Only answer questions related to college academics, admissions, exams,
                timetable, fees, hostel, placements, scholarships, and campus life.
                Keep responses concise, friendly, and under 3 sentences.
                Remember conversation context — if user says "what about ece?" after asking
                about CSE fee, understand they mean ECE fee.
                If unsure say: Please contact the %s office for accurate details.
                Do not answer questions unrelated to college life.
                """, collegeName, collegeName);

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));
            messages.addAll(sessionHistory.getOrDefault(sessionId, new ArrayList<>()));

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", "llama-3.3-70b-versatile");
            body.put("max_tokens", 300);
            body.put("messages", messages);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(GROQ_URL, request, Map.class);

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            Map<String, Object> messageMap = (Map<String, Object>) choices.get(0).get("message");
            String reply = (String) messageMap.get("content");

            addToHistory(sessionId, "assistant", reply);
            return reply;

        } catch (Exception e) {
            return "Sorry, I couldn't process that right now. Please contact the college office.";
        }
    }

    private void addToHistory(String sessionId, String role, String content) {
        sessionHistory.putIfAbsent(sessionId, new ArrayList<>());
        List<Map<String, String>> history = sessionHistory.get(sessionId);
        history.add(Map.of("role", role, "content", content));
        if (history.size() > 10) history.remove(0);
    }

    public void clearHistory(String sessionId) { sessionHistory.remove(sessionId); }
}