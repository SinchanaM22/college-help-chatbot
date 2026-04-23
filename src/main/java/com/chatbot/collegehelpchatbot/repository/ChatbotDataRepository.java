package com.chatbot.collegehelpchatbot.repository;

import com.chatbot.collegehelpchatbot.model.ChatbotData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatbotDataRepository extends JpaRepository<ChatbotData, Integer> {

    // Searches if the stored question keyword is contained in the user's message
    @Query(value = "SELECT * FROM chatbot_data WHERE LOWER(:message) LIKE CONCAT('%', LOWER(question), '%') LIMIT 1", nativeQuery = true)
    List<ChatbotData> searchByKeyword(@Param("message") String message);
}