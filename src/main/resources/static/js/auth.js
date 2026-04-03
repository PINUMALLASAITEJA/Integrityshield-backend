function login() {

    const username = document.getElementById("username").value;
    const password = document.getElementById("password").value;

    fetch('/api/student/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            rollNumber: username,
            password: password
        })
    })
    .then(res => {
        if (!res.ok) {
            throw new Error("Invalid credentials");
        }
        return res.json();
    })
    .then(data => {

        localStorage.setItem("token", data.token);
        window.location.href = "faculty-dashboard.html";
    })
    .catch(() => {
        document.getElementById("error").innerText = "Invalid username or password";
    });
}

function register() {

    const username = document.getElementById("regUsername").value;
    const password = document.getElementById("regPassword").value;

    fetch('/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            userIdentifier: username,
            password: password,
            role: "FACULTY"
        })
    })
    .then(res => res.text())
    .then(response => {
        alert(response);
        window.location.href = "login.html";
    })
    .catch(() => {
        alert("Registration failed");
    });
}