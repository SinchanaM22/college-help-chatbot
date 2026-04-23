package com.chatbot.collegehelpchatbot.repository;

import com.chatbot.collegehelpchatbot.model.ChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {

    List<ChatHistory> findBySessionIdOrderByTimestampAsc(String sessionId);

    // Top 10 most asked questions
    @Query(value = """
        SELECT user_message, COUNT(*) as count
        FROM chat_history
        GROUP BY user_message
        ORDER BY count DESC
        LIMIT 10
        """, nativeQuery = true)
    List<Object[]> findTopAskedQuestions();

    // Questions marked as not helpful (👎) — needs improvement
    @Query(value = """
        SELECT user_message, bot_reply, COUNT(*) as count
        FROM chat_history
        WHERE helpful = false
        GROUP BY user_message, bot_reply
        ORDER BY count DESC
        LIMIT 10
        """, nativeQuery = true)
    List<Object[]> findNotHelpfulQuestions();

    // Feedback stats: total, helpful, not helpful
    @Query(value = """
        SELECT
            COUNT(*) as total,
            SUM(CASE WHEN helpful = true  THEN 1 ELSE 0 END) as thumbsUp,
            SUM(CASE WHEN helpful = false THEN 1 ELSE 0 END) as thumbsDown
        FROM chat_history
        WHERE helpful IS NOT NULL
        """, nativeQuery = true)
    List<Object[]> getFeedbackStats();
}