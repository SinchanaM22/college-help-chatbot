package com.chatbot.collegehelpchatbot.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_history")
public class ChatHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sessionId;
    private String username;   // ← which student sent this

    @Column(columnDefinition = "TEXT")
    private String userMessage;

    @Column(columnDefinition = "TEXT")
    private String botReply;

    private Boolean helpful;

    private LocalDateTime timestamp;

    @PrePersist
    public void prePersist() { this.timestamp = LocalDateTime.now(); }

    public ChatHistory() {}
    public ChatHistory(String sessionId, String username, String userMessage, String botReply) {
        this.sessionId   = sessionId;
        this.username    = username;
        this.userMessage = userMessage;
        this.botReply    = botReply;
    }

    public Long getId()                { return id; }
    public String getSessionId()       { return sessionId; }
    public String getUsername()        { return username; }
    public String getUserMessage()     { return userMessage; }
    public String getBotReply()        { return botReply; }
    public Boolean getHelpful()        { return helpful; }
    public LocalDateTime getTimestamp(){ return timestamp; }

    public void setId(Long id)                 { this.id = id; }
    public void setSessionId(String s)         { this.sessionId = s; }
    public void setUsername(String u)          { this.username = u; }
    public void setUserMessage(String m)       { this.userMessage = m; }
    public void setBotReply(String r)          { this.botReply = r; }
    public void setHelpful(Boolean h)          { this.helpful = h; }
    public void setTimestamp(LocalDateTime t)  { this.timestamp = t; }
}