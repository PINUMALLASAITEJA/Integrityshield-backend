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
let socketConnected = false;

/* ---------------- SESSION ---------------- */

function startSession() {
    fetch('/api/faculty/start-session?allowedApps=', {
        method: 'POST',
        headers: authHeaderWithJSON()
    })
    .then(() => {
        updateSessionStatus(true);
        loadAllowedApps();
        loadAllowedUrls();
    })
    .catch(err => console.error("Start session failed:", err));
}

function stopSession() {
    fetch('/api/faculty/stop-session', {
        method: 'POST',
        headers: authHeaderWithJSON()
    })
    .then(() => updateSessionStatus(false))
    .catch(err => console.error("Stop session failed:", err));
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

    if (socketConnected) return;

    const socket = new WebSocket("wss://integrityshield-backend-2.onrender.com/ws");
    stompClient = Stomp.over(socket);

    stompClient.connect(
        { Authorization: "Bearer " + localStorage.getItem("token") },
        () => {
            socketConnected = true;

            stompClient.subscribe('/topic/faculty-alerts', msg =>
                handleEscalation(JSON.parse(msg.body))
            );

            stompClient.subscribe('/topic/student-join', msg =>
                addStudent(msg.body)
            );

            stompClient.subscribe('/topic/session-end', () =>
                handleSessionEnd()
            );
        },
        () => {
            socketConnected = false;
            setTimeout(connectWebSocket, 3000);
        }
    );
}

/* ---------------- SESSION END ---------------- */

function handleSessionEnd() {
    document.getElementById("studentsList").innerHTML = "";
    studentViolations = {};
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
        li.querySelector(".count-badge").innerText =
            "violations: " + studentViolations[roll].count;
    }
}

/* ---------------- ALLOWED APPS ---------------- */

function loadAllowedApps() {

    fetch('/api/faculty/allowed-apps', {
        headers: authHeader()
    })
    .then(res => res.json())
    .then(data => {

        const list = document.getElementById("allowedAppsList");
        list.innerHTML = "";

        data.forEach(app => {

            const li = document.createElement("li");

            li.innerHTML = `
                ${app}
                <button onclick="removeAllowedApp('${encodeURIComponent(app)}')">X</button>
            `;

            list.appendChild(li);
        });
    });
}

function addAllowedApp() {

    const input = document.getElementById("appInput");
    const app = input.value.trim();

    if (!app) return;

    fetch('/api/faculty/allow-app', {
        method: 'POST',
        headers: authHeaderWithJSON(),
        body: app
    })
    .then(() => {
        input.value = "";
        loadAllowedApps();
    });
}

function removeAllowedApp(app) {

    fetch(`/api/faculty/remove-app?appName=${decodeURIComponent(app)}`, {
        method: 'DELETE',
        headers: authHeader() // 🔥 FIX (NO JSON HEADER)
    })
    .then(() => loadAllowedApps());
}

/* ---------------- HELPERS ---------------- */

function authHeader() {
    return {
        "Authorization": "Bearer " + localStorage.getItem("token")
    };
}

function authHeaderWithJSON() {
    return {
        "Authorization": "Bearer " + localStorage.getItem("token"),
        "Content-Type": "application/json"
    };
}

function logout() {
    localStorage.removeItem("token");
    window.location.replace("login.html");
}