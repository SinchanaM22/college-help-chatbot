package com.chatbot.collegehelpchatbot.controller;

import com.chatbot.collegehelpchatbot.model.Student;
import com.chatbot.collegehelpchatbot.repository.StudentRepository;
import com.chatbot.collegehelpchatbot.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired private StudentRepository studentRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;

    // Register new student
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody Map<String, String> req) {
        String username = req.get("username").trim();
        String password = req.get("password").trim();

        if (username.isEmpty() || password.isEmpty())
            return ResponseEntity.badRequest().body(Map.of("error", "Username and password required."));

        if (password.length() < 6)
            return ResponseEntity.badRequest().body(Map.of("error", "Password must be at least 6 characters."));

        if (studentRepository.existsByUsername(username))
            return ResponseEntity.badRequest().body(Map.of("error", "Username already taken."));

        Student student = new Student(username, passwordEncoder.encode(password));
        studentRepository.save(student);

        String token = jwtUtil.generateToken(username);
        return ResponseEntity.ok(Map.of("token", token, "username", username, "message", "Registered successfully!"));
    }

    // Login
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> req) {
        String username = req.get("username").trim();
        String password = req.get("password").trim();

        return studentRepository.findByUsername(username)
            .filter(s -> passwordEncoder.matches(password, s.getPassword()))
            .map(s -> {
                String token = jwtUtil.generateToken(username);
                return ResponseEntity.ok(Map.of(
                    "token", token,
                    "username", username,
                    "role", s.getRole(),
                    "message", "Login successful!"
                ));
            })
            .orElse(ResponseEntity.badRequest().body(Map.of("error", "Invalid username or password.")));
    }
}