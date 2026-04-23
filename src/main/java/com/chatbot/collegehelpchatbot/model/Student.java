package com.chatbot.collegehelpchatbot.model;

import jakarta.persistence.*;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password; // stored as bcrypt hash

    private String role = "STUDENT"; // STUDENT or ADMIN

    public Student() {}
    public Student(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public Long getId()           { return id; }
    public String getUsername()   { return username; }
    public String getPassword()   { return password; }
    public String getRole()       { return role; }

    public void setId(Long id)           { this.id = id; }
    public void setUsername(String u)    { this.username = u; }
    public void setPassword(String p)    { this.password = p; }
    public void setRole(String r)        { this.role = r; }
}