/**
 * Global Toast Notification Utility
 * @param {string} message - The text message to display
 * @param {'success'|'error'} type - The type of notification
 */
window.showToast = function(message, type = "success") {
    let toast = document.getElementById("appToast");

    if (!toast) {
        toast = document.createElement("div");
        toast.id = "appToast";
        toast.className = "fixed top-6 right-6 z-50 px-5 py-4 rounded-xl shadow-lg text-white font-medium transition-all duration-300 translate-x-[120%]";
        document.body.appendChild(toast);
    }

    toast.textContent = message;
    
    // Clean up previous background classes
    toast.classList.remove("bg-green-600", "bg-red-600");

    // Add appropriate background color based on type
    toast.classList.add(type === "success" ? "bg-green-600" : "bg-red-600");

    // Animate in
    requestAnimationFrame(() => {
        toast.classList.remove("translate-x-[120%]");
    });

    // Animate out after 3 seconds
    setTimeout(() => {
        toast.classList.add("translate-x-[120%]");
    }, 3000);
};