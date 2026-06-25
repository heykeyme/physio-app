(function() {
    const toggleBtn = document.getElementById('password-toggle');
    const passwordInput = document.getElementById('password');
    const eyeIcon = document.getElementById('eye-icon');
    const eyeOffIcon = document.getElementById('eye-off-icon');

    if (!toggleBtn || !passwordInput || !eyeIcon || !eyeOffIcon) return;

    toggleBtn.addEventListener('click', function(e) {
        e.preventDefault();
        const isPassword = passwordInput.type === 'password';

        // Toggle input type
        passwordInput.type = isPassword ? 'text' : 'password';

        // Toggle icon visibility
        eyeIcon.classList.toggle('hidden', isPassword);
        eyeOffIcon.classList.toggle('hidden', !isPassword);

        // Update aria-label
        toggleBtn.setAttribute(
            'aria-label',
            isPassword ? 'Hide password' : 'Show password'
        );
    });

    // Prevent form submission on Enter key when toggle is focused
    toggleBtn.addEventListener('keydown', function(e) {
        if (e.key === 'Enter') {
            e.preventDefault();
            toggleBtn.click();
        }
    });
})();