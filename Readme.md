# College Help Chatbot

A full-stack AI-powered chatbot for college students built with Java Spring Boot and HTML/CSS/JS. Students can ask questions about fees, hostel, placement, exams, scholarships, and campus life. The chatbot searches a MySQL FAQ database first, then falls back to Groq AI for questions not in the database.

---

## Features

- AI-powered responses using Groq API (Llama 3.3 70B model)
- MySQL FAQ database with keyword search
- Session-based conversation memory with context awareness
- Student login and registration with JWT authentication
- Admin panel to manage FAQs, students, and chat history
- Voice input using Web Speech API (Chrome only)
- Dark mode toggle with preference saved in localStorage
- Typing effect on bot replies
- Suggested questions after every bot reply
- Feedback buttons (thumbs up/down) on bot replies
- Chat history saved to MySQL with analytics
- Feedback analytics showing most asked and poorly rated answers

---

## Tech Stack

**Backend**
- Java 17+
- Spring Boot 4.x
- Spring Security with JWT
- Spring Data JPA with Hibernate
- MySQL 8+
- Groq API (free tier)

**Frontend**
- HTML, CSS, JavaScript (vanilla)
- Web Speech API for voice input
- Fetch API for HTTP requests

---

## Project Structure

```
college-help-chatbot/
├── src/
│   └── main/
│       ├── java/com/chatbot/collegehelpchatbot/
│       │   ├── config/
│       │   │   └── SecurityConfig.java
│       │   ├── controller/
│       │   │   ├── AuthController.java
│       │   │   ├── ChatController.java
│       │   │   └── AdminController.java
│       │   ├── model/
│       │   │   ├── Student.java
│       │   │   ├── ChatbotData.java
│       │   │   └── ChatHistory.java
│       │   ├── repository/
│       │   │   ├── StudentRepository.java
│       │   │   ├── ChatbotDataRepository.java
│       │   │   └── ChatHistoryRepository.java
│       │   ├── security/
│       │   │   ├── JwtUtil.java
│       │   │   └── JwtFilter.java
│       │   └── service/
│       │       └── ChatService.java
│       └── resources/
│           └── application.properties
├── frontend/
│   ├── index.html
│   ├── login.html
│   ├── style.css
│   └── script.js
├── .gitignore
└── pom.xml
```

---

## Prerequisites

- Java 17 or higher
- Maven
- MySQL 8+
- Groq API key (free at console.groq.com)
- Chrome browser (for voice input)

---

## Setup Instructions

### 1. Clone the repository

```bash
git clone https://github.com/YOUR_USERNAME/college-help-chatbot.git
cd college-help-chatbot
```

### 2. Create MySQL database

```sql
CREATE DATABASE college_chatbot;
USE college_chatbot;

CREATE TABLE chatbot_data (
    id INT PRIMARY KEY AUTO_INCREMENT,
    question VARCHAR(255),
    answer VARCHAR(500)
);

INSERT INTO chatbot_data (question, answer) VALUES
('cse fee', 'CSE course fee is Rs. 75,000 per year.'),
('ece fee', 'ECE course fee is Rs. 70,000 per year.'),
('hostel fee', 'Hostel fee: Boys Rs. 45,000 | Girls Rs. 50,000 per year including food.'),
('library', 'Library is open Monday to Saturday, 9:00 AM to 5:00 PM.'),
('placement', 'Top recruiters: TCS, Infosys, Wipro, Accenture. Average package Rs. 4.5 LPA.'),
('scholarship', 'Scholarships available for merit, sports quota, and SC/ST/OBC schemes.'),
('admission', 'Admission needs application form, mark sheets, TC, and fee payment.'),
('exam', 'Semester exams in November (odd sem) and April (even sem).');
```

### 3. Create application.properties

Create the file at `src/main/resources/application.properties`:

```properties
spring.application.name=college-help-chatbot
server.port=8081

spring.datasource.url=jdbc:mysql://localhost:3306/college_chatbot
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

groq.api.key=YOUR_GROQ_API_KEY
college.name=Your College Name
jwt.secret=college-chatbot-super-secret-key-2024
```

### 4. Run the backend

```bash
mvn spring-boot:run
```

Server starts at `http://localhost:8081`

### 5. Open the frontend

Open `frontend/login.html` using Live Server in VS Code.

---

## Creating an Admin Account

1. Register a student account through the login page
2. Open MySQL CLI and run:

```sql
USE college_chatbot;
UPDATE students SET role='ADMIN' WHERE username='your_username';
```

3. Log in again — the admin panel button will appear in the chat header

After the first admin is created, new admins can be promoted from inside the admin panel without touching MySQL.



## How It Works

1. Student opens `login.html`, registers or logs in
2. On successful login, a JWT token is stored in localStorage
3. Student is redirected to `index.html`
4. Every chat message is sent with the JWT token in the Authorization header
5. Backend searches MySQL FAQ table first using keyword matching
6. If no match found, the question is sent to Groq AI with the full conversation history for context
7. The reply is saved to the `chat_history` table with the student's username
8. Student can give thumbs up or down feedback on each reply
9. Admin can view FAQ management, student list, and analytics from the admin panel

## Dependencies

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
