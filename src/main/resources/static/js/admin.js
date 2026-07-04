document.getElementById('logoutBtn').addEventListener('click', async () => {
    try {

        const apiUrl = `${window.base}/physio/logout`;

        console.log('Logout API URL:', apiUrl); 

        const response = await fetch(apiUrl, {
            method: 'POST',
            credentials: 'include'
        });

        const data = await response.json();

        if (data.success) {
            // Clear any client-side stored data (e.g. JWT, user info)
            localStorage.removeItem('token');
            localStorage.removeItem('user');

            // Redirect to login page
            window.location.href = '/'; // adjust to your actual login route
        } else {
            alert(data.message || 'Logout failed. Please try again.');
        }
    } catch (error) {
        console.error('Logout error:', error);
    }
});