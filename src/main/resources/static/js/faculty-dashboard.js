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
        headers: getAuthHeader()
    })
    .then(() => {
        updateSessionStatus(true);

        // 🔥 reload permissions after session start
        loadAllowedApps();
        loadAllowedUrls();
    })
    .catch(err => console.error("Start session failed:", err));
}

function stopSession() {
    fetch('/api/faculty/stop-session', {
        method: 'POST',
        headers: getAuthHeader()
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

    if (socketConnected) return; // ✅ prevent duplicate connections

    const socket = new WebSocket("wss://integrityshield-backend-2.onrender.com/ws");
    stompClient = Stomp.over(socket);

    stompClient.connect(
        { Authorization: "Bearer " + localStorage.getItem("token") },

        () => {
            socketConnected = true;
            console.log("✅ WebSocket Connected");

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

        (error) => {
            console.error("❌ WebSocket error:", error);
            socketConnected = false;

            // 🔥 auto reconnect
            setTimeout(connectWebSocket, 3000);
        }
    );
}

/* ---------------- SESSION END ---------------- */

function handleSessionEnd() {

    document.getElementById("studentsList").innerHTML = "";
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

/* ---------------- OVERLAY ---------------- */

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

/* ---------------- ALLOWED APPS ---------------- */

function loadAllowedApps() {

    fetch('/api/faculty/allowed-apps', {
        headers: getAuthHeader()
    })
    .then(res => res.json())
    .then(data => {

        const list = document.getElementById("allowedAppsList");
        list.innerHTML = "";

        data.forEach(app => {

            const safeApp = encodeURIComponent(app);

            const li = document.createElement("li");

            li.innerHTML = `
                ${app}
                <button onclick="removeAllowedApp('${safeApp}')">X</button>
            `;

            list.appendChild(li);
        });
    })
    .catch(err => console.error("Load apps error:", err));
}

function addAllowedApp() {

    const input = document.getElementById("appInput");
    const app = input.value.trim();

    if (!app) return;

    fetch('/api/faculty/allow-app', {
        method: 'POST',
        headers: getAuthHeader(),
        body: app
    })
    .then(() => {
        input.value = "";
        loadAllowedApps();
    })
    .catch(err => console.error("Add app error:", err));
}

function removeAllowedApp(app) {

    fetch(`/api/faculty/remove-app?appName=${decodeURIComponent(app)}`, {
        method: 'DELETE',
        headers: getAuthHeader()
    })
    .then(() => loadAllowedApps())
    .catch(err => console.error("Remove app error:", err));
}

/* ---------------- ALLOWED URLS ---------------- */

function loadAllowedUrls() {

    fetch('/api/faculty/allowed-urls', {
        headers: getAuthHeader()
    })
    .then(res => res.json())
    .then(data => {

        const list = document.getElementById("allowedUrlsList");
        list.innerHTML = "";

        data.forEach(url => {

            const safeUrl = encodeURIComponent(url);

            const li = document.createElement("li");

            li.innerHTML = `
                ${url}
                <button onclick="removeAllowedUrl('${safeUrl}')">X</button>
            `;

            list.appendChild(li);
        });
    })
    .catch(err => console.error("Load URLs error:", err));
}

function addAllowedUrl() {

    const input = document.getElementById("urlInput");
    const url = input.value.trim();

    if (!url) return;

    fetch('/api/faculty/allow-url', {
        method: 'POST',
        headers: getAuthHeader(),
        body: url
    })
    .then(() => {
        input.value = "";
        loadAllowedUrls();
    })
    .catch(err => console.error("Add URL error:", err));
}

function removeAllowedUrl(url) {

    fetch(`/api/faculty/remove-url?url=${decodeURIComponent(url)}`, {
        method: 'DELETE',
        headers: getAuthHeader()
    })
    .then(() => loadAllowedUrls())
    .catch(err => console.error("Remove URL error:", err));
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