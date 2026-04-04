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
        alert(msg);
        updateSessionStatus(true);
    })
    .catch(() => alert("Failed"));
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
    });
}

function updateSessionStatus(active) {

    const badge = document.getElementById("sessionStatus");

    if (!badge) return;

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

function loadAllowedApps() {

    fetch('/api/faculty/allowed-apps', {
        headers: getAuthHeader()
    })
    .then(res => res.ok ? res.json() : [])
    .then(data => {

        const list = document.getElementById("allowedAppsList");
        list.innerHTML = "";

        if (!Array.isArray(data)) return;

        data.forEach(app => {

            const li = document.createElement("li");

            li.innerHTML = `
                <span>${app}</span>
                <button class="remove-btn" onclick="removeAllowedApp('${app}')">X</button>
            `;

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

function loadAllowedUrls() {

    fetch('/api/faculty/allowed-urls', {
        headers: getAuthHeader()
    })
    .then(res => res.ok ? res.json() : [])
    .then(data => {

        const list = document.getElementById("allowedUrlsList");
        list.innerHTML = "";

        if (!Array.isArray(data)) return;

        data.forEach(url => {

            const li = document.createElement("li");

            li.innerHTML = `
                <span>${url}</span>
                <button class="remove-btn" onclick="removeAllowedUrl('${url}')">X</button>
            `;

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