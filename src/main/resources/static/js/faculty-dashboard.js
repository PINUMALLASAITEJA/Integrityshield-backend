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

/* ---------------- SESSION CONTROL ---------------- */

function startSession() {

    fetch('/api/faculty/start-session?allowedApps=', {
        method: 'POST',
        headers: getAuthHeader()
    })
    .then(res => res.text())
    .then(msg => {
        alert(msg);
        updateSessionStatus(true);
    })
    .catch(() => alert("Failed to start session"));
}

function stopSession() {

    fetch('/api/faculty/stop-session', {
        method: 'POST',
        headers: getAuthHeader()
    })
    .then(res => res.text())
    .then(msg => {
        alert(msg);
        updateSessionStatus(false);
    })
    .catch(() => alert("Failed to stop session"));
}

function updateSessionStatus(active) {

    const status = document.getElementById("sessionStatus");
    if (!status) return;

    status.innerText = active ? "Session Active" : "No Active Session";
    status.style.color = active ? "green" : "red";
}

/* ---------------- VIEW SWITCH ---------------- */

function showHome() {
    document.getElementById("homeSection").style.display = "block";
    document.getElementById("accessPanel").style.display = "none";
}

function showAccess() {
    document.getElementById("homeSection").style.display = "none";
    document.getElementById("accessPanel").style.display = "block";
}

/* ---------------- WEBSOCKET ---------------- */

function connectWebSocket() {

    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect(
        { Authorization: "Bearer " + localStorage.getItem("token") },
        function () {

            stompClient.subscribe('/topic/faculty-alerts', function (msg) {
                handleEscalation(JSON.parse(msg.body));
            });

            stompClient.subscribe('/topic/student-join', function (msg) {
                addStudent(msg.body);
            });

            loadExistingStudents();
        }
    );
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
    li.onclick = () => showStudentDetails(roll);

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

/* ---------------- DETAILS ---------------- */

function showStudentDetails(roll) {

    const data = studentViolations[roll];
    if (!data) return;

    let text = `Violations:\n\n`;

    data.history.forEach((v, i) => {
        text += `${i + 1}. ${v}\n`;
    });

    alert(text);
}

/* ---------------- PERMISSIONS ---------------- */

function addAllowedApp() {
    const app = document.getElementById("appInput").value;

    fetch('/api/faculty/allow-app', {
        method: 'POST',
        headers: getAuthHeader(),
        body: JSON.stringify(app)
    }).then(loadAllowedApps);
}

function loadAllowedApps() {

    const list = document.getElementById("allowedAppsList");
    list.innerHTML = "";

    fetch('/api/faculty/allowed-apps', {
        headers: getAuthHeader()
    })
    .then(res => res.json())
    .then(data => {

        data.forEach(app => {

            const li = document.createElement("li");

            const span = document.createElement("span");
            span.innerText = app;

            const btn = document.createElement("button");
            btn.innerText = "X";
            btn.className = "remove-btn";
            btn.onclick = () => removeAllowedApp(app);

            li.appendChild(span);
            li.appendChild(btn);

            list.appendChild(li);
        });
    });
}

function removeAllowedApp(app) {
    fetch(`/api/faculty/remove-app?appName=${app}`, {
        method: 'DELETE',
        headers: getAuthHeader()
    }).then(loadAllowedApps);
}

/* ---------------- URLS ---------------- */

function addAllowedUrl() {
    const url = document.getElementById("urlInput").value;

    fetch('/api/faculty/allow-url', {
        method: 'POST',
        headers: getAuthHeader(),
        body: JSON.stringify(url)
    }).then(loadAllowedUrls);
}

function loadAllowedUrls() {

    const list = document.getElementById("allowedUrlsList");
    list.innerHTML = "";

    fetch('/api/faculty/allowed-urls', {
        headers: getAuthHeader()
    })
    .then(res => res.json())
    .then(data => {

        data.forEach(url => {

            const li = document.createElement("li");

            const span = document.createElement("span");
            span.innerText = url;

            const btn = document.createElement("button");
            btn.innerText = "X";
            btn.className = "remove-btn";
            btn.onclick = () => removeAllowedUrl(url);

            li.appendChild(span);
            li.appendChild(btn);

            list.appendChild(li);
        });
    });
}

function removeAllowedUrl(url) {
    fetch(`/api/faculty/remove-url?url=${url}`, {
        method: 'DELETE',
        headers: getAuthHeader()
    }).then(loadAllowedUrls);
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