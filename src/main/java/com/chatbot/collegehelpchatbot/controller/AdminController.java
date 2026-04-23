package com.chatbot.collegehelpchatbot.controller;

import com.chatbot.collegehelpchatbot.model.ChatbotData;
import com.chatbot.collegehelpchatbot.model.ChatHistory;
import com.chatbot.collegehelpchatbot.model.Student;
import com.chatbot.collegehelpchatbot.repository.ChatHistoryRepository;
import com.chatbot.collegehelpchatbot.repository.ChatbotDataRepository;
import com.chatbot.collegehelpchatbot.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired private ChatbotDataRepository chatbotDataRepository;
    @Autowired private ChatHistoryRepository chatHistoryRepository;
    @Autowired private StudentRepository studentRepository;

    // ── FAQ CRUD ──────────────────────────────────────────────
    @GetMapping("/faqs")
    public List<ChatbotData> getAllFaqs() { return chatbotDataRepository.findAll(); }

    @PostMapping("/faqs")
    public ChatbotData addFaq(@RequestBody ChatbotData faq) { return chatbotDataRepository.save(faq); }

    @PutMapping("/faqs/{id}")
    public ChatbotData updateFaq(@PathVariable int id, @RequestBody ChatbotData updated) {
        updated.setId(id); return chatbotDataRepository.save(updated);
    }

    @DeleteMapping("/faqs/{id}")
    public Map<String, String> deleteFaq(@PathVariable int id) {
        chatbotDataRepository.deleteById(id); return Map.of("status", "deleted");
    }

    // ── Students ──────────────────────────────────────────────
    @GetMapping("/students")
    public List<Student> getAllStudents() { return studentRepository.findAll(); }

    // Make a student admin
    @PutMapping("/students/{id}/promote")
    public Map<String, String> promote(@PathVariable Long id) {
        studentRepository.findById(id).ifPresent(s -> { s.setRole("ADMIN"); studentRepository.save(s); });
        return Map.of("status", "promoted");
    }

    @DeleteMapping("/students/{id}")
    public Map<String, String> deleteStudent(@PathVariable Long id) {
        studentRepository.deleteById(id); return Map.of("status", "deleted");
    }

    // ── Chat History ──────────────────────────────────────────
    @GetMapping("/history/all")
    public List<ChatHistory> getAllHistory() { return chatHistoryRepository.findAll(); }

    @GetMapping("/history/top")
    public List<Object[]> getTopQuestions() { return chatHistoryRepository.findTopAskedQuestions(); }

    @GetMapping("/history/feedback")
    public List<Object[]> getNotHelpful() { return chatHistoryRepository.findNotHelpfulQuestions(); }

    @GetMapping("/history/stats")
    public List<Object[]> getFeedbackStats() { return chatHistoryRepository.getFeedbackStats(); }

    @DeleteMapping("/history")
    public Map<String, String> clearAllHistory() {
        chatHistoryRepository.deleteAll(); return Map.of("status", "cleared");
    }
}