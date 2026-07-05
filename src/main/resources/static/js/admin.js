// 1. Run initialization logic when the webpage finishes loading
document.addEventListener('DOMContentLoaded', () => {
    // Look inside sessionStorage to match your login script
    const userToken = sessionStorage.getItem('token'); 
    
    if (userToken) {
        loadTrainers(userToken);
        setupCourseFormSubmission(userToken); // Initialize form handler
    } else {
        console.warn('No authorization token found. Redirecting to login...');
        showToast('Your session has expired or you are not logged in. Please log in again.');
        window.location.href = '/physio'; // Adjust to your actual login route
    }
});

// 2. Logout Button Click Handler
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
            sessionStorage.removeItem('token');
            sessionStorage.removeItem('user');
            window.location.href = '/'; 
            return; 
        } else {
            showToast(data.message || 'Logout failed. Please try again.');
        }
    } catch (error) {
        console.error('Logout error:', error);
    }
});

/**
 * Fetches trainers from the API and populates the #trainerName select dropdown.
 */
async function loadTrainers(token) {
    const endpoint = `${window.base}/physio/admin/courses/trainers`;
    const selectElement = document.getElementById('trainerName');

    if (!selectElement) {
        console.warn('Trainer select element (#trainerName) not found in DOM.');
        return;
    }

    try {
        const response = await fetch(endpoint, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }

        const result = await response.json();

        if (result.status === 'success' && Array.isArray(result.data)) {
            selectElement.length = 1; // Keep default placeholder

            result.data.forEach(trainer => {
                const option = document.createElement('option');
                option.value = trainer.userId;     
                option.textContent = trainer.fullname; 
                selectElement.appendChild(option);
            });
        } else {
            console.error('API returned an unexpected structure or error message:', result.message);
        }

    } catch (error) {
        console.error('Failed to fetch trainers:', error);
        const errorOption = document.createElement('option');
        errorOption.textContent = 'Failed to load trainers';
        errorOption.disabled = true;
        selectElement.appendChild(errorOption);
    }
}

/**
 * 3. Handles Course Form Submission via Fetch (JSON payload)
 */
function setupCourseFormSubmission(token) {
    const form = document.getElementById('createCourseForm');
    const saveBtn = document.getElementById('saveCourseBtn');

    if (!form) return;

    form.addEventListener('submit', async (e) => {
        e.preventDefault(); // Prevent default HTML page reload/submit

        // Disable button & show loading state
        const originalBtnText = saveBtn.innerHTML;
        saveBtn.disabled = true;
        saveBtn.innerHTML = `<span class="material-symbols-outlined animate-spin text-[18px]">progress_activity</span> Saving...`;

        try {
            // Extract values and map them to your CourseCreateRequestDTO fields
            const payload = {
                courseName: document.getElementById('courseName').value.trim(),
                staffId: document.getElementById('trainerName').value,
                courseDate: document.getElementById('courseDate').value,
                courseStartTime: document.getElementById('startTime').value,        // Mapped from startTime
                courseEndTime: document.getElementById('endTime').value,            // Mapped from endTime
                coursePrice: parseFloat(document.getElementById('coursePrice').value) || 0.00
            };

            const endpoint = `${window.base}/physio/admin/courses/add`; 

            const response = await fetch(endpoint, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                },
                credentials: 'include', 
                body: JSON.stringify(payload)
            });

            const result = await response.json();

            if (response.ok && result.status === 'success') {
                showToast("Course created successfully!", "success");
                form.reset(); 

                const modalToggle = document.getElementById('create-course-modal-toggle');
                if (modalToggle) modalToggle.checked = false;

            } else {
                showToast('Failed to create course. Please try again.', "error");
            }

        } catch (error) {
            console.error('Error adding course:', error);
        } finally {
            // Restore button state
            saveBtn.disabled = false;
            saveBtn.innerHTML = originalBtnText;
        }
    });
}