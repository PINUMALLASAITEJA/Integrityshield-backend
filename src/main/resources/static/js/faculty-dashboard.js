document.addEventListener("DOMContentLoaded", () => {

    if (!localStorage.getItem("token")) {
        window.location.replace("login.html");
        return;
    }

    showHome();
    updateSessionStatus(false);
    connectWebSocket();
    loadAllowedApps();
    loadAllowedUrls();
});

let stompClient = null;
let studentViolations = {};

/* ---------------- SESSION ---------------- */

function startSession() {
    fetch('/api/faculty/start-session?allowedApps=', {
        method: 'POST',
        headers: getAuthHeader()
    })
    .then(res => res.text())
    .then(msg => {
        updateSessionStatus(true);
    });
}

function stopSession() {
    fetch('/api/faculty/stop-session', {
        method: 'POST',
        headers: getAuthHeader()
    })
    .then(res => res.text())
    .then(() => {
        updateSessionStatus(false);
    });
}

function updateSessionStatus(active) {

    const badge = document.getElementById("sessionStatus");

    badge.innerText = active ? "Active" : "Inactive";
    badge.classList.remove("active", "inactive");
    badge.classList.add(active ? "active" : "inactive");
}

/* ---------------- VIEW ---------------- */

function showHome() {
    document.getElementById("homeSection").style.display = "block";
    document.getElementById("accessPanel").style.display = "none";
}

function showAccess() {
    document.getElementById("homeSection").style.display = "none";
    document.getElementById("accessPanel").style.display = "block";
}

/* ---------------- SOCKET ---------------- */

function connectWebSocket() {

    const socket = new WebSocket("wss://integrityshield-backend-2.onrender.com/ws");
    stompClient = Stomp.over(socket);

    stompClient.connect(
        { Authorization: "Bearer " + localStorage.getItem("token") },
        () => {

            stompClient.subscribe('/topic/faculty-alerts', msg =>
                handleEscalation(JSON.parse(msg.body))
            );

            stompClient.subscribe('/topic/student-join', msg =>
                addStudent(msg.body)
            );

            // 🔥 SESSION END LISTENER
            stompClient.subscribe('/topic/session-end', () =>
                handleSessionEnd()
            );
        }
    );
}

/* ---------------- SESSION END FIX ---------------- */

function handleSessionEnd() {

    const list = document.getElementById("studentsList");
    list.innerHTML = "";
    studentViolations = {};

    showSessionMessage("Session Ended");

    setTimeout(() => {
        document.getElementById("sessionMsg").style.display = "none";
    }, 3000);
}

function showSessionMessage(text) {
    const msg = document.getElementById("sessionMsg");
    msg.innerText = text;
    msg.style.display = "block";
}

/* ---------------- STUDENTS ---------------- */

function addStudent(roll) {

    if (!roll || document.getElementById("student-" + roll)) return;

    studentViolations[roll] = { count: 0, history: [] };

    const li = document.createElement("li");
    li.id = "student-" + roll;

    li.innerHTML = `
        <span>${roll}</span>
        <span class="count-badge">violations: 0</span>
    `;

    li.style.color = "green";
    li.onclick = () => showOverlay(roll);

    document.getElementById("studentsList").appendChild(li);
}

/* ---------------- VIOLATIONS ---------------- */

function handleEscalation(alert) {

    const roll = alert.studentRoll;

    if (!studentViolations[roll]) {
        studentViolations[roll] = { count: 0, history: [] };
    }

    studentViolations[roll].count++;
    studentViolations[roll].history.push(alert.message);

    const li = document.getElementById("student-" + roll);

    if (li) {
        li.style.color = "red";
        li.querySelector(".count-badge").innerText =
            "violations: " + studentViolations[roll].count;

        sortStudents();
    }
}

/* ---------------- SORT ---------------- */

function sortStudents() {

    const list = document.getElementById("studentsList");
    const items = Array.from(list.children);

    items.sort((a, b) => {
        const ra = a.id.replace("student-", "");
        const rb = b.id.replace("student-", "");
        return studentViolations[rb].count - studentViolations[ra].count;
    });

    items.forEach(i => list.appendChild(i));
}

/* ---------------- OVERLAY (NEW) ---------------- */

function showOverlay(roll) {

    const overlay = document.getElementById("overlay");
    const content = document.getElementById("overlayContent");

    const data = studentViolations[roll];
    if (!data) return;

    let html = `<h3>${roll}</h3>`;

    data.history.forEach((v, i) => {
        html += `<p>${i + 1}. ${v}</p>`;
    });

    content.innerHTML = html;
    overlay.style.display = "flex";
}

function closeOverlay() {
    document.getElementById("overlay").style.display = "none";
}

/* ---------------- HELPERS ---------------- */

function getAuthHeader() {
    return {
        "Authorization": "Bearer " + localStorage.getItem("token"),
        "Content-Type": "application/json"
    };
}

function logout() {
    localStorage.removeItem("token");
    window.location.replace("login.html");
}