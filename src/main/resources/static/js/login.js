    document.getElementById("login-submit").addEventListener("click", function (e) {
    e.preventDefault();

    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;

    const apiUrl = `${window.base}/physio/login`;

    fetch(apiUrl, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        credentials: "include",
        body: JSON.stringify({ email, password })
    })
        .then(response => response.json().then(data => ({ status: response.status, body: data })))
        .then(result => {
            const { body } = result;

            if (body.success) {
                saveSession(body);

                showToast(body.message, "success");

                setTimeout(() => {
                    redirectByRole(body.role);
                }, 1500);
            } else {
                showToast(body.message, "error");
            }
        })
        .catch(() => {
            showToast("Something went wrong. Please try again.", "error");
        });
});

function saveSession(data) {
    // Store token separately since it's read most often (e.g. every fetch header)
    sessionStorage.setItem("token", data.token);

    // Store the rest of the user info as a single JSON object
    const user = {
        userId: data.userId,
        fullname: data.fullname,
        role: data.role
    };
    sessionStorage.setItem("user", JSON.stringify(user));
}

function redirectByRole(role) {
    const roleRoutes = {
        1: "/physio/admin",
        2: "/physio/management",
        3: "/physio/participant",
        4: "/physio/trainer"
    };

    const destination = roleRoutes[role];

    if (destination) {
        window.location.href = destination;
    } else {
        showToast("Unrecognized user role. Please contact support.", "error");
    }
}

function showToast(message, type) {
    let toast = document.getElementById("appToast");

    if (!toast) {
        toast = document.createElement("div");
        toast.id = "appToast";
        toast.className = "fixed top-6 right-6 z-50 px-5 py-4 rounded-xl shadow-lg text-white font-medium transition-all duration-300 translate-x-[120%]";
        document.body.appendChild(toast);
    }

    toast.textContent = message;
    toast.className = toast.className
        .replace("bg-green-600", "")
        .replace("bg-red-600", "");

    toast.classList.add(type === "success" ? "bg-green-600" : "bg-red-600");

    requestAnimationFrame(() => {
        toast.classList.remove("translate-x-[120%]");
    });

    setTimeout(() => {
        toast.classList.add("translate-x-[120%]");
    }, 3000);
}