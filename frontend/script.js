const chatMessages    = document.getElementById("chatMessages");
const userInput       = document.getElementById("userInput");
const typingIndicator = document.getElementById("typingIndicator");
const sendBtn         = document.getElementById("sendBtn");
const micBtn          = document.getElementById("micBtn");
const BASE            = "http://localhost:8081";

// ── Auth check — redirect to login if no token ───────────────
const token    = localStorage.getItem("token");
const username = localStorage.getItem("username");
const role     = localStorage.getItem("role");

if (!token) window.location.href = "login.html";

// Show username in header
document.getElementById("usernameLabel").textContent = "👤 " + username;

// Show admin button only for admins
if (role === "ADMIN") document.getElementById("adminBtn").style.display = "flex";

// Auth headers for every request
function authHeaders() {
    return { "Content-Type": "application/json", "Authorization": "Bearer " + token };
}

const SUGGESTIONS = {
    fee:         ["Hostel fee?", "Scholarship details?", "MBA fee?"],
    hostel:      ["Boys hostel fee?", "Girls hostel fee?", "Hostel rules?"],
    placement:   ["Top companies?", "Average package?", "Internship process?"],
    exam:        ["Result dates?", "Exam timetable?", "Hall ticket?"],
    scholarship: ["Merit scholarship?", "Government schemes?", "How to apply?"],
    admission:   ["Documents needed?", "Fee structure?", "Last date?"],
    library:     ["Library timings?", "How to borrow books?", "E-library access?"],
    default:     ["CSE fee?", "Hostel details?", "Placement info?"]
};

// ── Send Message ─────────────────────────────────────────────
function sendMessage() {
    const message = userInput.value.trim();
    if (!message) return;

    document.getElementById("suggestions").style.display = "none";
    appendMessage(message, "user");
    userInput.value = "";
    sendBtn.disabled = true;
    showTyping();

    fetch(`${BASE}/chat`, {
        method: "POST",
        headers: authHeaders(),
        credentials: "include",
        body: JSON.stringify({ message })
    })
    .then(r => {
        if (r.status === 403 || r.status === 401) {
            localStorage.clear();
            window.location.href = "login.html";
        }
        return r.json();
    })
    .then(data => {
        hideTyping();
        typeMessage(data.reply, data.historyId, getSuggestions(message));
        sendBtn.disabled = false;
    })
    .catch(() => {
        hideTyping();
        appendMessage("Sorry, couldn't connect to the server. Please try again.", "bot");
        sendBtn.disabled = false;
    });
}

// ── Typing Effect + Feedback Buttons ─────────────────────────
function typeMessage(text, historyId, suggestions) {
    const messageDiv = document.createElement("div");
    messageDiv.className = "message bot-message";

    const avatar = document.createElement("div");
    avatar.className = "avatar";
    avatar.textContent = "🤖";

    const wrapper = document.createElement("div");
    wrapper.className = "bot-wrapper";

    const bubble = document.createElement("div");
    bubble.className = "bubble typing-effect";

    const feedback = document.createElement("div");
    feedback.className = "feedback-row";
    feedback.style.display = "none";
    feedback.innerHTML = `
        <button class="fb-btn" onclick="sendFeedback(${historyId}, true, this)">👍</button>
        <button class="fb-btn" onclick="sendFeedback(${historyId}, false, this)">👎</button>
        <span class="fb-label">Was this helpful?</span>`;

    wrapper.appendChild(bubble);
    wrapper.appendChild(feedback);
    messageDiv.appendChild(avatar);
    messageDiv.appendChild(wrapper);
    chatMessages.appendChild(messageDiv);
    scrollToBottom();

    let i = 0;
    const interval = setInterval(() => {
        bubble.innerHTML = text.slice(0, i + 1).replace(/\n/g, "<br>");
        i++; scrollToBottom();
        if (i >= text.length) {
            clearInterval(interval);
            bubble.classList.remove("typing-effect");
            feedback.style.display = "flex";
            showSuggestions(suggestions);
        }
    }, 18);
}

// ── Feedback ─────────────────────────────────────────────────
function sendFeedback(historyId, helpful, btn) {
    if (historyId === -1) return;
    fetch(`${BASE}/chat/feedback`, {
        method: "POST", headers: authHeaders(),
        body: JSON.stringify({ historyId, helpful })
    });
    const row = btn.closest(".feedback-row");
    row.innerHTML = helpful
        ? `<span class="fb-done fb-good">👍 Thanks for your feedback!</span>`
        : `<span class="fb-done fb-bad">👎 We'll improve this answer.</span>`;
}

// ── Suggested Questions ───────────────────────────────────────
function getSuggestions(userMessage) {
    const lower = userMessage.toLowerCase();
    for (const key of Object.keys(SUGGESTIONS)) {
        if (key !== "default" && lower.includes(key)) return SUGGESTIONS[key];
    }
    return SUGGESTIONS.default;
}

function showSuggestions(suggestions) {
    const container = document.getElementById("suggestions");
    container.innerHTML = "";
    suggestions.forEach(q => {
        const btn = document.createElement("button");
        btn.textContent = q; btn.onclick = () => quickAsk(q);
        container.appendChild(btn);
    });
    container.style.display = "flex";
}

// ── Voice Input ──────────────────────────────────────────────
let recognition = null, isRecording = false;

function toggleVoice() {
    if (!('webkitSpeechRecognition' in window) && !('SpeechRecognition' in window))
        return alert("Voice input is only supported in Chrome.");
    if (isRecording) { recognition.stop(); return; }

    const SR = window.SpeechRecognition || window.webkitSpeechRecognition;
    recognition = new SR();
    recognition.lang = "en-IN"; recognition.interimResults = true; recognition.continuous = false;

    recognition.onstart = () => {
        isRecording = true; micBtn.classList.add("recording");
        micBtn.textContent = "🔴"; userInput.placeholder = "Listening...";
    };
    recognition.onresult = (e) => {
        userInput.value = Array.from(e.results).map(r => r[0].transcript).join("");
        if (e.results[e.results.length - 1].isFinal) setTimeout(() => sendMessage(), 500);
    };
    recognition.onend = recognition.onerror = () => {
        isRecording = false; micBtn.classList.remove("recording");
        micBtn.textContent = "🎤"; userInput.placeholder = "Type or speak your question...";
    };
    recognition.start();
}

// ── Dark Mode ────────────────────────────────────────────────
function toggleDark() {
    const html = document.documentElement;
    const isDark = html.getAttribute("data-theme") === "dark";
    html.setAttribute("data-theme", isDark ? "light" : "dark");
    document.getElementById("darkBtn").textContent = isDark ? "🌙" : "☀️";
    localStorage.setItem("theme", isDark ? "light" : "dark");
}
(function() {
    const saved = localStorage.getItem("theme") || "light";
    document.documentElement.setAttribute("data-theme", saved);
    document.getElementById("darkBtn").textContent = saved === "dark" ? "☀️" : "🌙";
})();

// ── Logout ───────────────────────────────────────────────────
function logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("username");
    localStorage.removeItem("role");
    window.location.href = "login.html";
}

// ── Helpers ──────────────────────────────────────────────────
function quickAsk(q) { userInput.value = q; sendMessage(); }

function appendMessage(text, sender) {
    const div = document.createElement("div");
    div.className = `message ${sender}-message`;
    const avatar = document.createElement("div");
    avatar.className = "avatar";
    avatar.textContent = sender === "bot" ? "🤖" : "🧑";
    const bubble = document.createElement("div");
    bubble.className = "bubble";
    bubble.innerHTML = text.replace(/\n/g, "<br>");
    div.appendChild(avatar); div.appendChild(bubble);
    chatMessages.appendChild(div); scrollToBottom();
}

function showTyping()     { typingIndicator.style.display = "flex"; scrollToBottom(); }
function hideTyping()     { typingIndicator.style.display = "none"; }
function scrollToBottom() { chatMessages.scrollTop = chatMessages.scrollHeight; }

function clearChat() {
    chatMessages.innerHTML = `
        <div class="message bot-message">
            <div class="avatar">🤖</div>
            <div class="bubble">Hi <strong>${username}</strong>! 👋 Ask me about
            <strong>fees, hostel, placement, exams, scholarships</strong>!</div>
        </div>`;
    showSuggestions(SUGGESTIONS.default);
    fetch(`${BASE}/chat/clear`, { method: "POST", headers: authHeaders(), credentials: "include" });
}

// ── Admin Panel ──────────────────────────────────────────────
function openAdmin()  { document.getElementById("adminModal").style.display = "flex"; loadFaqs(); }
function closeAdmin() { document.getElementById("adminModal").style.display = "none"; }

function switchTab(tab) {
    document.querySelectorAll(".tab-btn").forEach(b => b.classList.remove("active"));
    event.target.classList.add("active");
    document.getElementById("tab-faqs").style.display     = tab === "faqs"     ? "block" : "none";
    document.getElementById("tab-history").style.display  = tab === "history"  ? "block" : "none";
    document.getElementById("tab-students").style.display = tab === "students" ? "block" : "none";
    if (tab === "history")  loadHistory();
    if (tab === "students") loadStudents();
}

function loadFaqs() {
    const list = document.getElementById("faqList");
    list.innerHTML = "<p class='loading-text'>Loading...</p>";
    fetch(`${BASE}/admin/faqs`, { headers: authHeaders() }).then(r => r.json()).then(faqs => {
        list.innerHTML = "";
        faqs.forEach(faq => {
            const row = document.createElement("div");
            row.className = "faq-row"; row.id = `faq-${faq.id}`;
            row.innerHTML = `
                <div class="faq-fields">
                    <input type="text" value="${faq.question}" id="q-${faq.id}">
                    <input type="text" value="${faq.answer}"   id="a-${faq.id}">
                </div>
                <div class="faq-actions">
                    <button class="btn-save"   onclick="updateFaq(${faq.id})">💾</button>
                    <button class="btn-delete" onclick="deleteFaq(${faq.id})">🗑️</button>
                </div>`;
            list.appendChild(row);
        });
    });
}

function addFaq() {
    const q = document.getElementById("newQuestion").value.trim();
    const a = document.getElementById("newAnswer").value.trim();
    if (!q || !a) return alert("Please fill both fields.");
    fetch(`${BASE}/admin/faqs`, { method:"POST", headers:authHeaders(), body:JSON.stringify({question:q,answer:a}) })
        .then(() => { document.getElementById("newQuestion").value=""; document.getElementById("newAnswer").value=""; loadFaqs(); });
}

function updateFaq(id) {
    const q = document.getElementById(`q-${id}`).value.trim();
    const a = document.getElementById(`a-${id}`).value.trim();
    fetch(`${BASE}/admin/faqs/${id}`, { method:"PUT", headers:authHeaders(), body:JSON.stringify({question:q,answer:a}) })
        .then(() => { const r=document.getElementById(`faq-${id}`); r.style.background="#d1fae5"; setTimeout(()=>r.style.background="",1000); });
}

function deleteFaq(id) {
    if (!confirm("Delete this FAQ?")) return;
    fetch(`${BASE}/admin/faqs/${id}`, { method:"DELETE", headers:authHeaders() })
        .then(() => document.getElementById(`faq-${id}`).remove());
}

function loadStudents() {
    const list = document.getElementById("studentList");
    list.innerHTML = "<p class='loading-text'>Loading...</p>";
    fetch(`${BASE}/admin/students`, { headers: authHeaders() }).then(r => r.json()).then(students => {
        list.innerHTML = "";
        students.forEach(s => {
            const row = document.createElement("div");
            row.className = "history-row"; row.id = `stu-${s.id}`;
            row.innerHTML = `
                <span class="history-question">👤 ${s.username} <small style="color:var(--muted)">(${s.role})</small></span>
                <div style="display:flex;gap:6px">
                    ${s.role !== "ADMIN" ? `<button class="btn-save" onclick="promoteStudent(${s.id})">⬆️ Make Admin</button>` : ""}
                    <button class="btn-delete" onclick="deleteStudent(${s.id})">🗑️</button>
                </div>`;
            list.appendChild(row);
        });
    });
}

function promoteStudent(id) {
    if (!confirm("Make this student an admin?")) return;
    fetch(`${BASE}/admin/students/${id}/promote`, { method:"PUT", headers:authHeaders() }).then(() => loadStudents());
}

function deleteStudent(id) {
    if (!confirm("Delete this student?")) return;
    fetch(`${BASE}/admin/students/${id}`, { method:"DELETE", headers:authHeaders() })
        .then(() => document.getElementById(`stu-${id}`).remove());
}

function loadHistory() {
    const list = document.getElementById("historyList");
    list.innerHTML = "<p class='loading-text'>Loading...</p>";
    Promise.all([
        fetch(`${BASE}/admin/history/stats`,    { headers: authHeaders() }).then(r => r.json()),
        fetch(`${BASE}/admin/history/top`,      { headers: authHeaders() }).then(r => r.json()),
        fetch(`${BASE}/admin/history/feedback`, { headers: authHeaders() }).then(r => r.json())
    ]).then(([stats, top, notHelpful]) => {
        list.innerHTML = "";
        if (stats.length > 0) {
            const s = stats[0];
            list.innerHTML += `<div class="stats-bar">
                <span>📊 Total: <strong>${s[0]}</strong></span>
                <span class="fb-good">👍 ${s[1]}</span>
                <span class="fb-bad">👎 ${s[2]}</span></div>`;
        }
        if (top.length > 0) {
            list.innerHTML += `<p class="section-label">🔥 Most Asked</p>`;
            top.forEach(r => { list.innerHTML += `<div class="history-row"><span class="history-question">❓ ${r[0]}</span><span class="history-count">${r[1]}x</span></div>`; });
        }
        if (notHelpful.length > 0) {
            list.innerHTML += `<p class="section-label">⚠️ Needs Improvement</p>`;
            notHelpful.forEach(r => { list.innerHTML += `<div class="history-row bad-row"><span class="history-question">❓ ${r[0]}</span><span class="history-count">${r[2]}x 👎</span></div>`; });
        }
        if (top.length === 0) list.innerHTML += "<p class='loading-text'>No history yet.</p>";
    });
}

function clearHistory() {
    if (!confirm("Clear all chat history?")) return;
    fetch(`${BASE}/admin/history`, { method:"DELETE", headers:authHeaders() }).then(() => loadHistory());
}