document.getElementById("registrationForm").addEventListener("submit", function (e) {
    e.preventDefault();

    const fullname = document.getElementById("fullnameInput").value;
    const email = document.getElementById("emailInput").value;
    const password = document.getElementById("passwordInput").value;


    const apiUrl = `${window.base}/physio/register`;
    console.log("API URL:", apiUrl); // Log the API URL for debugging

    fetch(apiUrl, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ fullname, email, password })
    })
        .then(response => response.json().then(data => ({ status: response.status, body: data })))
        .then(result => {
            const { body } = result;

            if (body.success) {
                showToast(body.message, "success");

                setTimeout(() => {
                    window.location.href = document.getElementById("loginLink").href;
                }, 1500);
            } else {
                showToast(body.message, "error");
            }
        })
        .catch(() => {
            showToast("Something went wrong. Please try again.", "error");
        });
});

function togglePassword() {
    const passwordInput = document.getElementById("passwordInput");
    const passwordIcon = document.getElementById("passwordIcon");

    if (passwordInput.type === "password") {
        passwordInput.type = "text";
        passwordIcon.textContent = "visibility_off";
    } else {
        passwordInput.type = "password";
        passwordIcon.textContent = "visibility";
    }
}

function showToast(message, type) {
    let toast = document.getElementById("appToast");

    if (!toast) {
        toast = document.createElement("div");
        toast.id = "appToast";
        toast.className = "fixed top-6 right-6 z-50 px-5 py-4 rounded-xl shadow-lg text-white font-body-md transition-all duration-300 translate-x-[120%]";
        document.body.appendChild(toast);
    }

    toast.textContent = message;
    toast.className = toast.className
        .replace("bg-primary", "")
        .replace("bg-error", "");

    if (type === "success") {
        toast.classList.add("bg-primary");
    } else {
        toast.classList.add("bg-error");
    }

    requestAnimationFrame(() => {
        toast.classList.remove("translate-x-[120%]");
    });

    setTimeout(() => {
        toast.classList.add("translate-x-[120%]");
    }, 3000);
}