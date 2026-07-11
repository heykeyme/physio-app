// 1. Run initialization logic when the webpage finishes loading
document.addEventListener('DOMContentLoaded', () => {
    // Look inside sessionStorage to match your login script
    const userToken = sessionStorage.getItem('token'); 
    
    if (userToken) {
        loadTrainers(userToken);
        loadTrainers(userToken, 'editTrainerName');
        setupCourseFormSubmission(userToken); 
        setupEditCourseFormSubmission(userToken);
        loadCourses(userToken); 
        loadFeedback(userToken);
        loadUsers(userToken);
        setupRegisterStaffFormSubmission(userToken);
        setupUserSearch(userToken);
        loadHeaderUserInfo(userToken);
        loadHeaderDate();
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
            window.location.href = '/physio'; 
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
/**
 * Fetches trainers from the API and populates a given select dropdown.
 */
async function loadTrainers(token, targetSelectId = 'trainerName') {
    const endpoint = `${window.base}/physio/admin/courses/trainer`;
    const selectElement = document.getElementById(targetSelectId);

    if (!selectElement) {
        console.warn(`Trainer select element (#${targetSelectId}) not found in DOM.`);
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
                option.textContent = trainer.fullName;
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

            const endpoint = `${window.base}/physio/admin/courses/create`; 

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

/**
 * Fetches courses from the API and renders them into #courseListContainer.
 */
async function loadCourses(token) {
    const endpoint = `${window.base}/physio/admin/courses/list`;
    const containerElement = document.getElementById('courseListContainer');

    if (!containerElement) {
        console.warn('Course list container (#courseListContainer) not found in DOM.');
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
            containerElement.innerHTML = '';

            if (result.data.length === 0) {
                containerElement.innerHTML = `<p class="text-sm text-slate-500">No courses found.</p>`;
                return;
            }

            result.data.forEach(course => {
                const isActive = course.courseStatus === 1;

                const badgeClass = isActive
                    ? 'bg-green-100 text-green-700'
                    : 'bg-rose-100 text-rose-700';
                const badgeText = isActive ? 'Active' : 'Inactive';

                const actionButtonClass = isActive
                    ? 'bg-rose-50 text-rose-700 hover:bg-rose-100'
                    : 'bg-emerald-50 text-emerald-700 hover:bg-emerald-100';
                const actionButtonText = isActive ? 'Deactivate' : 'Activate';

                const card = document.createElement('div');
                card.className = 'bg-white rounded-xl shadow-sm border border-slate-200 p-5 hover:shadow-md transition-shadow';
                card.dataset.courseId = course.courseId;

                card.innerHTML = `
                    <div class="flex justify-between items-start">
                        <h3 class="font-semibold text-slate-800">${course.courseName}</h3>
                        <span class="px-2 py-0.5 ${badgeClass} rounded-full text-[10px] font-bold uppercase tracking-wide">${badgeText}</span>
                    </div>
                    <p class="text-sm text-slate-500 mt-2">Trainer: ${course.trainerName}</p>
                    <p class="text-sm text-slate-500">Enrolled: ${course.participant} participants</p>
                    <div class="mt-4 flex gap-2" data-actions></div>
                `;

                const editBtn = document.createElement('button');
                editBtn.type = 'button';
                editBtn.className = 'text-xs font-medium px-3 py-1.5 bg-brand-50 text-brand-700 rounded-lg hover:bg-brand-100 transition-colors';
                editBtn.textContent = 'Edit';
                editBtn.addEventListener('click', () => {
                    const currentToken = sessionStorage.getItem('token');
                    loadCourseForEdit(course.courseId, currentToken);

                    const editModalToggle = document.getElementById('edit-course-modal-toggle');
                    if (editModalToggle) editModalToggle.checked = true;
                });

                const actionBtn = document.createElement('button');
                actionBtn.type = 'button';
                actionBtn.className = `text-xs font-medium px-3 py-1.5 ${actionButtonClass} rounded-lg transition-colors`;
                actionBtn.textContent = actionButtonText;
                actionBtn.addEventListener('click', async () => {
                    const currentToken = sessionStorage.getItem('token');
                    const newStatus = isActive ? 0 : 1;

                    // Prevent double-clicks while request is in flight
                    actionBtn.disabled = true;
                    const originalText = actionBtn.textContent;
                    actionBtn.textContent = 'Updating...';

                    const success = await changeCourseStatus(course.courseId, newStatus, currentToken);

                    if (success) {
                        loadCourses(currentToken); // refresh list to reflect new status
                    } else {
                        actionBtn.disabled = false;
                        actionBtn.textContent = originalText;
                    }
                });

                const actionsContainer = card.querySelector('[data-actions]');
                actionsContainer.appendChild(editBtn);
                actionsContainer.appendChild(actionBtn);

                containerElement.appendChild(card);
            });
        } else {
            console.error('API returned an unexpected structure or error message:', result.message);
        }

    } catch (error) {
        console.error('Failed to fetch courses:', error);
        containerElement.innerHTML = `<p class="text-sm text-rose-600">Failed to load courses.</p>`;
    }
}

/**
 * Fetches a single course's data and populates the EDIT form fields.
 */
async function loadCourseForEdit(courseId, token) {
    const endpoint = `${window.base}/physio/admin/courses/edit?courseId=${courseId}`;
    const form = document.getElementById('editCourseForm');

    if (!form) {
        console.warn('Edit course form (#editCourseForm) not found in DOM.');
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

        if (result.status === 'success' && result.data) {
            const course = result.data;

            document.getElementById('editCourseId').value = course.id ?? '';
            document.getElementById('editCourseName').value = course.courseName ?? '';
            document.getElementById('editCourseDate').value = course.courseDate ?? '';
            document.getElementById('editStartTime').value = course.courseStartTime ? course.courseStartTime.slice(0, 5) : '';
            document.getElementById('editEndTime').value = course.courseEndTime ? course.courseEndTime.slice(0, 5) : '';
            document.getElementById('editCoursePrice').value = course.coursePrice ?? '';

            // Direct ID match now that backend returns trainerid — no more fragile name matching.
            const trainerSelect = document.getElementById('editTrainerName');
            trainerSelect.value = course.trainerid ?? '';

            if (trainerSelect.value !== (course.trainerid ?? '')) {
                // value didn't actually get set — means no matching <option> exists yet
                console.warn('Trainer option not found for trainerid:', course.trainerid, '— is #editTrainerName populated yet?');
            }

        } else {
            console.error('API returned an unexpected structure or error message:', result.message);
            showToast('Failed to load course details.', 'error');
        }

    } catch (error) {
        console.error('Failed to fetch course details:', error);
        showToast('Failed to load course details.', 'error');
    }
}

/**
 * Handles Edit Course Form Submission via Fetch (JSON payload, PUT).
 */
function setupEditCourseFormSubmission(token) {
    const form = document.getElementById('editCourseForm');
    const updateBtn = document.getElementById('updateCourseBtn');

    if (!form) return;

    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        const originalBtnText = updateBtn.innerHTML;
        updateBtn.disabled = true;
        updateBtn.innerHTML = `<span class="material-symbols-outlined animate-spin text-[18px]">progress_activity</span> Updating...`;

        try {
            const payload = {
                id: parseInt(document.getElementById('editCourseId').value, 10),
                courseName: document.getElementById('editCourseName').value.trim(),
                staffId: document.getElementById('editTrainerName').value,
                courseDate: document.getElementById('editCourseDate').value,
                courseStartTime: document.getElementById('editStartTime').value,
                courseEndTime: document.getElementById('editEndTime').value,
                coursePrice: parseFloat(document.getElementById('editCoursePrice').value) || 0.00
            };

            const currentToken = sessionStorage.getItem('token') || token;
            const endpoint = `${window.base}/physio/admin/courses/update`;

            const response = await fetch(endpoint, {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${currentToken}`,
                    'Content-Type': 'application/json'
                },
                credentials: 'include',
                body: JSON.stringify(payload)
            });

            const result = await response.json();

            if (response.ok && result.status === 'success') {
                showToast("Course updated successfully!", "success");
                form.reset();

                const modalToggle = document.getElementById('edit-course-modal-toggle');
                if (modalToggle) modalToggle.checked = false;

                loadCourses(currentToken); // refresh the list to reflect the update

            } else {
                showToast(result.message || 'Failed to update course. Please try again.', "error");
            }

        } catch (error) {
            console.error('Error updating course:', error);
            showToast('Failed to update course. Please try again.', "error");
        } finally {
            updateBtn.disabled = false;
            updateBtn.innerHTML = originalBtnText;
        }
    });
}

/**
 * Toggles a course's active/inactive status via PATCH.
 */
async function changeCourseStatus(courseId, newStatus, token) {
    const endpoint = `${window.base}/physio/admin/courses/change-status?courseId=${courseId}&status=${newStatus}`;

    try {
        const response = await fetch(endpoint, {
            method: 'PATCH',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            credentials: 'include'
        });

        const result = await response.json();

        if (response.ok && result.status === 'success') {
            showToast('Course status updated successfully.', 'success');
            return true;
        } else {
            showToast(result.message || 'Failed to update course status.', 'error');
            return false;
        }

    } catch (error) {
        console.error('Error changing course status:', error);
        showToast('Failed to update course status.', 'error');
        return false;
    }
}

/**
 * Fetches feedback from the API and renders them into #feedbackListContainer.
 */
async function loadFeedback(token) {
    const endpoint = `${window.base}/physio/admin/feedback/list`;
    const containerElement = document.getElementById('feedbackListContainer');

    if (!containerElement) {
        console.warn('Feedback list container (#feedbackListContainer) not found in DOM.');
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
            containerElement.innerHTML = '';

            if (result.data.length === 0) {
                containerElement.innerHTML = `<p class="text-sm text-slate-500">No feedback found.</p>`;
                return;
            }

            result.data.forEach(feedback => {
                const stars = renderStars(feedback.rate);
                const formattedDate = formatFeedbackDate(feedback.feedbackDate);

                const card = document.createElement('div');
                card.className = 'bg-white p-5 rounded-xl shadow-sm border border-slate-200';
                card.dataset.feedbackId = feedback.feedbackId;

                card.innerHTML = `
                    <div class="flex justify-between items-center mb-2">
                        <span class="font-bold text-slate-800">${feedback.courseName}</span>
                        <span class="text-amber-400 text-lg tracking-widest">${stars}</span>
                    </div>
                    <p class="text-sm text-slate-600 italic">"${feedback.review}"</p>
                    <p class="text-[11px] font-medium uppercase tracking-wider text-slate-400 mt-3">
                        <span>— ${feedback.participantName}</span> • <span>${formattedDate}</span>
                    </p>
                `;

                containerElement.appendChild(card);
            });
        } else {
            console.error('API returned an unexpected structure or error message:', result.message);
        }

    } catch (error) {
        console.error('Failed to fetch feedback:', error);
        containerElement.innerHTML = `<p class="text-sm text-rose-600">Failed to load feedback.</p>`;
    }
}

/**
 * Converts a numeric rating (0-5) into a filled/empty star string.
 */
function renderStars(rate) {
    const safeRate = Math.max(0, Math.min(5, Number(rate) || 0));
    const filled = '★'.repeat(safeRate);
    const empty = '☆'.repeat(5 - safeRate);
    return filled + empty;
}

/**
 * Formats an ISO date string (YYYY-MM-DD) into "Month Day" (e.g. "July 8").
 */
function formatFeedbackDate(dateStr) {
    if (!dateStr) return '';
    const date = new Date(dateStr + 'T00:00:00'); // avoid timezone shift issues
    if (isNaN(date.getTime())) return dateStr; // fallback if parsing fails

    return date.toLocaleDateString('en-US', { month: 'long', day: 'numeric' });
}

let currentUserPage = 1;
let currentSearchTerm = '';
let searchDebounceTimer = null;

/**
 * Fetches a page of users (optionally filtered by fullname) and renders the table + pagination.
 * Routes through /search since that endpoint already falls back to the full list when blank.
 */
async function loadUsers(token, page = 1, searchTerm = '') {
    const endpoint = `${window.base}/physio/admin/manage-users/search?fullname=${encodeURIComponent(searchTerm)}&page=${page}`;
    const tbody = document.getElementById('userListBody');
    const paginationContainer = document.getElementById('userPagination');

    if (!tbody) {
        console.warn('User list body (#userListBody) not found in DOM.');
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
            tbody.innerHTML = '';
            currentUserPage = result.currentPage ?? page;
            currentSearchTerm = searchTerm;

            if (result.data.length === 0) {
                tbody.innerHTML = `<tr><td colspan="5" class="px-4 py-6 text-center text-slate-500">No users found.</td></tr>`;
            } else {
                result.data.forEach(user => {
                    const row = buildUserRow(user);
                    tbody.appendChild(row);
                });
            }

            renderPagination(paginationContainer, result.currentPage, result.totalPages, token);

        } else {
            console.error('API returned an unexpected structure or error message:', result.message);
        }

    } catch (error) {
        console.error('Failed to fetch users:', error);
        tbody.innerHTML = `<tr><td colspan="5" class="px-4 py-6 text-center text-rose-600">Failed to load users.</td></tr>`;
    }
}

/**
 * Wires up the search input with debounce — fires the API call
 * only after the user stops typing for 500ms.
 */
function setupUserSearch(token) {
    const searchInput = document.getElementById('userSearchInput');

    if (!searchInput) {
        console.warn('Search input (#userSearchInput) not found in DOM.');
        return;
    }

    searchInput.addEventListener('input', () => {
        clearTimeout(searchDebounceTimer);

        searchDebounceTimer = setTimeout(() => {
            const term = searchInput.value.trim();
            const currentToken = sessionStorage.getItem('token') || token;
            loadUsers(currentToken, 1, term); // reset to page 1 on every new search
        }, 500);
    });
}

/**
 * Builds a single <tr> for a user row.
 */
function buildUserRow(user) {
    const isActive = user.status === 1;

    const statusBadgeClass = isActive
        ? 'bg-green-100 text-green-700'
        : 'bg-rose-100 text-rose-700';
    const statusText = isActive ? 'Active' : 'Inactive';

    const roleBadgeClass = getRoleBadgeClass(user.role);
    const roleText = user.role ? user.role.charAt(0).toUpperCase() + user.role.slice(1) : 'Unknown';

    const actionText = isActive ? 'Deactivate' : 'Activate';
    const actionColorClass = isActive ? 'text-rose-600' : 'text-emerald-600';

    const row = document.createElement('tr');
    row.className = 'hover:bg-slate-50 transition-colors';
    row.dataset.userId = user.id;

    row.innerHTML = `
        <td class="px-4 py-3 font-medium text-slate-800">${user.fullname}</td>
        <td class="px-4 py-3 text-slate-600">${user.email}</td>
        <td class="px-4 py-3"><span class="px-2 py-1 ${roleBadgeClass} rounded-full text-xs font-medium">${roleText}</span></td>
        <td class="px-4 py-3"><span class="px-2 py-1 ${statusBadgeClass} rounded-full text-xs font-medium">${statusText}</span></td>
        <td class="px-4 py-3 text-right">
            <button class="${actionColorClass} font-medium hover:underline text-xs" data-action-btn>${actionText}</button>
        </td>
    `;

    const actionBtn = row.querySelector('[data-action-btn]');
    actionBtn.addEventListener('click', async () => {
        const token = sessionStorage.getItem('token');
        const newStatus = isActive ? 0 : 1;

        actionBtn.disabled = true;
        const originalText = actionBtn.textContent;
        actionBtn.textContent = 'Updating...';

        const success = await changeUserStatus(user.id, newStatus, token);

        if (success) {
            loadUsers(token, currentUserPage); // refresh current page to reflect new status
        } else {
            actionBtn.disabled = false;
            actionBtn.textContent = originalText;
        }
    });

    return row;
}

/**
 * Maps a role string to a Tailwind badge color class.
 * Placeholder colors — adjust to your preferred palette.
 */
function getRoleBadgeClass(role) {
    switch ((role || '').toLowerCase()) {
        case 'admin':
            return 'bg-amber-100 text-amber-700';
        case 'trainer':
            return 'bg-purple-100 text-purple-700';
        case 'management':
            return 'bg-indigo-100 text-indigo-700';
        case 'participant':
            return 'bg-blue-100 text-blue-700';
        default:
            return 'bg-slate-100 text-slate-700';
    }
}

/**
 * Renders Prev/Next + page number controls.
 */
function renderPagination(container, currentPage, totalPages, token) {
    if (!container) return;

    container.innerHTML = '';

    if (!totalPages || totalPages <= 1) {
        return;
    }

    const info = document.createElement('span');
    info.textContent = `Page ${currentPage} of ${totalPages}`;
    container.appendChild(info);

    const controls = document.createElement('div');
    controls.className = 'flex gap-2';

    const prevBtn = document.createElement('button');
    prevBtn.textContent = 'Previous';
    prevBtn.className = 'px-3 py-1.5 border border-slate-200 rounded-lg text-xs font-medium hover:bg-slate-100 disabled:opacity-40 disabled:cursor-not-allowed';
    prevBtn.disabled = currentPage <= 1;
    prevBtn.addEventListener('click', () => loadUsers(token, currentPage - 1, currentSearchTerm));

    const nextBtn = document.createElement('button');
    nextBtn.textContent = 'Next';
    nextBtn.className = 'px-3 py-1.5 border border-slate-200 rounded-lg text-xs font-medium hover:bg-slate-100 disabled:opacity-40 disabled:cursor-not-allowed';
    nextBtn.disabled = currentPage >= totalPages;
    nextBtn.addEventListener('click', () => loadUsers(token, currentPage + 1, currentSearchTerm));

    controls.appendChild(prevBtn);
    controls.appendChild(nextBtn);
    container.appendChild(controls);
}

/**
 * Toggles a user's active/inactive status via PATCH.
 */
async function changeUserStatus(id, newStatus, token) {
    const endpoint = `${window.base}/physio/admin/manage-users/change-status?id=${id}&status=${newStatus}`;

    try {
        const response = await fetch(endpoint, {
            method: 'PATCH',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            credentials: 'include'
        });

        const result = await response.json();

        if (response.ok && result.status === 'success') {
            showToast('User status updated successfully.', 'success');
            return true;
        } else {
            showToast(result.message || 'Failed to update user status.', 'error');
            return false;
        }

    } catch (error) {
        console.error('Error changing user status:', error);
        showToast('Failed to update user status.', 'error');
        return false;
    }
}

/**
 * Handles Register Staff Form Submission via Fetch (JSON payload).
 */
function setupRegisterStaffFormSubmission(token) {
    const form = document.getElementById('registerStaffForm');
    const createBtn = document.getElementById('createStaffBtn');

    if (!form) return;

    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        const originalBtnText = createBtn.innerHTML;
        createBtn.disabled = true;
        createBtn.innerHTML = `<span class="material-symbols-outlined animate-spin text-[18px]">progress_activity</span> Creating...`;

        try {
            const selectedRole = form.querySelector('input[name="roleId"]:checked');

            if (!selectedRole) {
                showToast('Please select a role.', 'error');
                return;
            }

            const payload = {
                fullname: document.getElementById('staffFullname').value.trim(),
                email: document.getElementById('staffEmail').value.trim(),
                password: document.getElementById('staffPassword').value,
                roleId: parseInt(selectedRole.value, 10)
            };

            const currentToken = sessionStorage.getItem('token') || token;
            const endpoint = `${window.base}/physio/admin/manage-users/register`;

            const response = await fetch(endpoint, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${currentToken}`,
                    'Content-Type': 'application/json'
                },
                credentials: 'include',
                body: JSON.stringify(payload)
            });

            const result = await response.json();

            if (response.ok && result.success) {
                showToast('Staff account created successfully!', 'success');
                form.reset();

                const modalToggle = document.getElementById('register-staff-modal-toggle');
                if (modalToggle) modalToggle.checked = false;

                loadUsers(currentToken, currentUserPage); // refresh list to show new staff member

            } else {
                showToast(result.message || 'Failed to create staff account.', 'error');
            }

        } catch (error) {
            console.error('Error creating staff account:', error);
            showToast('Failed to create staff account.', 'error');
        } finally {
            createBtn.disabled = false;
            createBtn.innerHTML = originalBtnText;
        }
    });
}

let reportsCache = null;

function statRow(label, value, colorClass = 'text-slate-700') {
    return `<div class="flex justify-between border-b border-slate-50 pb-1"><span>${label}</span><span class="font-bold ${colorClass}">${value}</span></div>`;
}

async function loadAdminReports(token) {
    const endpoint = `${window.base}/physio/admin/reports/summary`;

    try {
        const response = await fetch(endpoint, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) throw new Error(`HTTP error! Status: ${response.status}`);

        const result = await response.json();
        if (result.status !== 'success') {
            console.error('API returned an unexpected structure or error message:', result.message);
            return;
        }

        reportsCache = result.data;
        const d = result.data;

        // Enrollment
        const et = d.enrollmentTotals;
        document.getElementById('enrollmentReportStats').innerHTML =
            statRow('Total Enrolments', et.total) +
            statRow('Ongoing', et.ongoing) +
            statRow('Completed', et.completed, 'text-emerald-600');

        // Attendance
        const at = d.attendanceTotals;
        const attendancePct = at.total > 0 ? ((at.present / at.total) * 100).toFixed(1) : '0.0';
        document.getElementById('attendanceReportStats').innerHTML =
            statRow('Average Attendance', `${attendancePct}%`) +
            statRow('Present', at.present, 'text-emerald-600') +
            statRow('Absentees', at.absent, 'text-rose-600');

        // Revenue
        const rt = d.revenueTotals;
        document.getElementById('revenueReportStats').innerHTML =
            statRow('Total Revenue', `RM ${Number(rt.total_revenue).toFixed(2)}`) +
            statRow('Successful Payments', rt.success_count, 'text-emerald-600') +
            statRow('Pending / Failed', `${rt.pending_count} / ${rt.failed_count}`, 'text-amber-600');

        // Feedback
        const ft = d.feedbackTotals;
        document.getElementById('feedbackReportStats').innerHTML =
            statRow('Total Reviews', ft.total) +
            statRow('Average Rating', `${Number(ft.avg_rating).toFixed(1)} / 5`);

    } catch (error) {
        console.error('Failed to fetch reports:', error);
    }
}

function downloadReportPdf(title, totalsHtml, byCourseRows, courseColumnLabels) {
    if (!reportsCache) {
        showToast('Reports have not loaded yet.', 'error');
        return;
    }

    const { jsPDF } = window.jspdf;
    const doc = new jsPDF({ orientation: 'portrait', unit: 'mm', format: 'a4' });

    doc.setFont('helvetica', 'bold');
    doc.setFontSize(18);
    doc.setTextColor(15, 118, 110);
    doc.text(title, 20, 20);

    doc.setFont('helvetica', 'normal');
    doc.setFontSize(10);
    doc.setTextColor(120, 120, 120);
    doc.text(`Generated on ${new Date().toLocaleDateString('en-GB', { day: 'numeric', month: 'long', year: 'numeric' })}`, 20, 27);

    doc.setDrawColor(230, 230, 230);
    doc.line(20, 32, 190, 32);

    let y = 42;
    doc.setFont('helvetica', 'bold');
    doc.setFontSize(12);
    doc.setTextColor(30, 41, 59);
    doc.text('Summary', 20, y);
    y += 8;

    doc.setFont('helvetica', 'normal');
    doc.setFontSize(11);
    totalsHtml.forEach(line => {
        doc.text(line, 20, y);
        y += 7;
    });

    y += 8;
    doc.setFont('helvetica', 'bold');
    doc.setFontSize(12);
    doc.text('By Course (Top 5)', 20, y);
    y += 8;

    doc.setFontSize(10);
    doc.setFont('helvetica', 'bold');
    doc.text(courseColumnLabels[0], 20, y);
    doc.text(courseColumnLabels[1], 150, y);
    y += 6;
    doc.setDrawColor(200, 200, 200);
    doc.line(20, y - 2, 190, y - 2);

    doc.setFont('helvetica', 'normal');
    byCourseRows.forEach(row => {
        doc.text(String(row.name), 20, y);
        doc.text(String(row.value), 150, y);
        y += 7;
    });

    const blobUrl = doc.output('bloburl');
    window.open(blobUrl, '_blank');
}

document.addEventListener('DOMContentLoaded', () => {
    const token = sessionStorage.getItem('token');
    if (!token) return;

    loadAdminReports(token);

    document.getElementById('downloadEnrollmentReportBtn')?.addEventListener('click', () => {
        const et = reportsCache.enrollmentTotals;
        downloadReportPdf(
            'Enrolment Report',
            [`Total Enrolments: ${et.total}`, `Ongoing: ${et.ongoing}`, `Completed: ${et.completed}`],
            reportsCache.enrollmentByCourse.map(r => ({ name: r.course_name, value: r.enrollment_count })),
            ['Course', 'Enrolments']
        );
    });

    document.getElementById('downloadAttendanceReportBtn')?.addEventListener('click', () => {
        const at = reportsCache.attendanceTotals;
        const pct = at.total > 0 ? ((at.present / at.total) * 100).toFixed(1) : '0.0';
        downloadReportPdf(
            'Attendance Report',
            [`Average Attendance: ${pct}%`, `Present: ${at.present}`, `Absentees: ${at.absent}`],
            reportsCache.attendanceByCourse.map(r => ({
                name: r.course_name,
                value: r.total > 0 ? `${((r.present / r.total) * 100).toFixed(0)}%` : '0%'
            })),
            ['Course', 'Attendance %']
        );
    });

    document.getElementById('downloadRevenueReportBtn')?.addEventListener('click', () => {
        const rt = reportsCache.revenueTotals;
        downloadReportPdf(
            'Revenue Report',
            [`Total Revenue: RM ${Number(rt.total_revenue).toFixed(2)}`, `Successful: ${rt.success_count}`, `Pending/Failed: ${rt.pending_count}/${rt.failed_count}`],
            reportsCache.revenueByCourse.map(r => ({ name: r.course_name, value: `RM ${Number(r.revenue).toFixed(2)}` })),
            ['Course', 'Revenue']
        );
    });

    document.getElementById('downloadFeedbackReportBtn')?.addEventListener('click', () => {
        const ft = reportsCache.feedbackTotals;
        downloadReportPdf(
            'Feedback Report',
            [`Total Reviews: ${ft.total}`, `Average Rating: ${Number(ft.avg_rating).toFixed(1)} / 5`],
            reportsCache.feedbackByCourse.map(r => ({ name: r.course_name, value: `${Number(r.avg_rating).toFixed(1)} / 5 (${r.review_count})` })),
            ['Course', 'Avg Rating']
        );
    });
});

async function loadAdminDashboard(token) {
    const endpoint = `${window.base}/physio/admin/dashboard/summary`;

    try {
        const response = await fetch(endpoint, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) throw new Error(`HTTP error! Status: ${response.status}`);

        const result = await response.json();
        if (result.status !== 'success') {
            console.error('API returned an unexpected structure or error message:', result.message);
            return;
        }

        const d = result.data;

        document.getElementById('statTotalUsers').textContent = d.totalUsers;
        document.getElementById('statActiveCourses').textContent = d.activeCourses;
        document.getElementById('statTotalRevenue').textContent = `RM ${Number(d.totalRevenue).toFixed(2)}`;

        const container = document.getElementById('recentActivityContainer');
        container.innerHTML = '';

        if (!d.recentActivity || d.recentActivity.length === 0) {
            container.innerHTML = `<p class="text-sm text-slate-500">No recent enrolments yet.</p>`;
            return;
        }

        d.recentActivity.forEach(entry => {
            const row = document.createElement('div');
            row.className = 'flex justify-between';
            row.innerHTML = `
                <span class="text-slate-600">${entry.fullname} enrolled in "${entry.course_name}" (RM ${Number(entry.amount).toFixed(2)})</span>
                <span class="text-slate-400">${formatRelativeTime(entry.created_at)}</span>
            `;
            container.appendChild(row);
        });

    } catch (error) {
        console.error('Failed to fetch admin dashboard:', error);
    }
}

/**
 * Converts an ISO timestamp into "X minutes/hours/days ago".
 */
function formatRelativeTime(timestamp) {
    const then = new Date(timestamp);
    const now = new Date();
    const diffMs = now - then;
    const diffMins = Math.floor(diffMs / 60000);

    if (diffMins < 1) return 'Just now';
    if (diffMins < 60) return `${diffMins} min${diffMins === 1 ? '' : 's'} ago`;

    const diffHours = Math.floor(diffMins / 60);
    if (diffHours < 24) return `${diffHours} hour${diffHours === 1 ? '' : 's'} ago`;

    const diffDays = Math.floor(diffHours / 24);
    return `${diffDays} day${diffDays === 1 ? '' : 's'} ago`;
}

/**
 * Sets today's date in the header (independent of any API call).
 */
function loadHeaderDate() {
    const dateEl = document.getElementById('headerCurrentDate');
    if (!dateEl) return;

    const today = new Date();
    dateEl.textContent = today.toLocaleDateString('en-US', {
        month: 'long',
        day: 'numeric',
        year: 'numeric'
    });
}

/**
 * Fetches the logged-in user's name and updates the header, reusing
 * the dashboard summary endpoint since it already returns fullName.
 */
function loadHeaderUserInfo() {
    const nameEl = document.getElementById('headerUserName');
    const initialsEl = document.getElementById('headerUserInitials');

    if (!nameEl || !initialsEl) return;

    try {
        const userJson = sessionStorage.getItem('user');
        if (!userJson) {
            nameEl.textContent = 'User';
            initialsEl.textContent = '--';
            return;
        }

        const user = JSON.parse(userJson);
        const fullName = user.fullname || 'User';

        nameEl.textContent = fullName;
        initialsEl.textContent = getInitials(fullName);

    } catch (error) {
        console.error('Failed to parse user session data:', error);
        nameEl.textContent = 'User';
        initialsEl.textContent = '--';
    }
}

/**
 * Extracts up to 2 initials from a full name, e.g. "John Doe" -> "JD".
 */
function getInitials(fullName) {
    const parts = fullName.trim().split(/\s+/);
    if (parts.length === 1) return parts[0].charAt(0).toUpperCase();
    return (parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
}

document.addEventListener('DOMContentLoaded', () => {
    const token = sessionStorage.getItem('token');
    if (!token) return;
    loadAdminDashboard(token);
});