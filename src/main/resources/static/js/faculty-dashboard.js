document.addEventListener("DOMContentLoaded", () => {

    if (!localStorage.getItem("token")) {
        window.location.replace("login.html");
        return;
    }

    showHome();
    updateSessionStatus(false);
    connectWebSocket();

    // ✅ FIX: now these functions exist
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
    .then(() => updateSessionStatus(true));
}

function stopSession() {
    fetch('/api/faculty/stop-session', {
        method: 'POST',
        headers: getAuthHeader()
    })
    .then(() => updateSessionStatus(false));
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

            stompClient.subscribe('/topic/session-end', () =>
                handleSessionEnd()
            );
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

            const li = document.createElement("li");

            li.innerHTML = `
                ${app}
                <button onclick="removeAllowedApp('${app}')">X</button>
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
        headers: getAuthHeader(),
        body: app
    })
    .then(() => {
        input.value = "";
        loadAllowedApps();
    });
}

function removeAllowedApp(app) {

    fetch(`/api/faculty/remove-app?appName=${app}`, {
        method: 'DELETE',
        headers: getAuthHeader()
    })
    .then(() => loadAllowedApps());
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

            const li = document.createElement("li");

            li.innerHTML = `
                ${url}
                <button onclick="removeAllowedUrl('${url}')">X</button>
            `;

            list.appendChild(li);
        });
    });
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
    });
}

function removeAllowedUrl(url) {

    fetch(`/api/faculty/remove-url?url=${url}`, {
        method: 'DELETE',
        headers: getAuthHeader()
    })
    .then(() => loadAllowedUrls());
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