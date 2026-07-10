/**
 * participant.js
 * Standalone script for the AlproPhysio Participant Dashboard.
 *
 * Assumes:
 * - `window.base` is defined elsewhere (API base URL)
 * - Auth token is stored in sessionStorage under 'token'
 * - A global showToast(message, type) function exists for user-facing errors
 */

document.addEventListener('DOMContentLoaded', () => {
    const userToken = sessionStorage.getItem('token');

    if (!userToken) {
        console.warn('No authorization token found. Redirecting to login...');
        window.location.href = '/physio'; // adjust to your actual login route
        return;
    }

    loadCourseCatalog(userToken);

    const params = new URLSearchParams(window.location.search);
    const paymentStatus = params.get('paymentStatus');

    if (paymentStatus === '1') {
        Swal.fire({
            title: 'Payment received!',
            text: 'Your enrollment is being processed and should appear shortly.',
            icon: 'success'
        });
    } else if (paymentStatus === '3') {
        Swal.fire({
            title: 'Payment failed',
            text: 'Your payment was not completed. Please try again.',
            icon: 'error'
        });
    } else if (paymentStatus === '2') {
        Swal.fire({
            title: 'Payment pending',
            text: 'Your payment is still being processed.',
            icon: 'info'
        });
    }

    // Clean the query params out of the URL so a page refresh doesn't
    // re-trigger the same alert
    if (paymentStatus) {
        window.history.replaceState({}, document.title, window.location.pathname);
    }
});

// Logout button handler, matching the pattern used on the trainer/admin dashboards.
document.getElementById('logoutBtn')?.addEventListener('click', async () => {
    try {
        const apiUrl = `${window.base}/physio/logout`;

        const response = await fetch(apiUrl, {
            method: 'POST',
            credentials: 'include'
        });

        const data = await response.json();

        if (data.success) {
            sessionStorage.removeItem('token');
            sessionStorage.removeItem('user');
            window.location.href = '/physio';
        } else {
            showToast(data.message || 'Logout failed. Please try again.', 'error');
        }
    } catch (error) {
        console.error('Logout error:', error);
        showToast('Logout failed. Please try again.', 'error');
    }
});

/* ============================================
   COURSE CATALOG
   ============================================ */

/**
 * Fetches the course catalog and renders it into #catalogListContainer.
 */
async function loadCourseCatalog(token) {
    const endpoint = `${window.base}/physio/participant/courses/catalog`;
    const containerElement = document.getElementById('catalogListContainer');

    if (!containerElement) {
        console.warn('Catalog list container (#catalogListContainer) not found in DOM.');
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
                containerElement.innerHTML = `<p class="text-sm text-slate-500">No courses available right now.</p>`;
                return;
            }

            const iconStyles = [
                { icon: 'sports_gymnastics', bg: 'bg-blue-50', text: 'text-blue-600' },
                { icon: 'child_care', bg: 'bg-purple-50', text: 'text-purple-600' },
                { icon: 'fitness_center', bg: 'bg-emerald-50', text: 'text-emerald-600' },
                { icon: 'healing', bg: 'bg-amber-50', text: 'text-amber-600' }
            ];

            result.data.forEach((course, index) => {
                const style = iconStyles[index % iconStyles.length];
                const card = buildCatalogCard(course, style);
                containerElement.appendChild(card);
            });

        } else {
            console.error('API returned an unexpected structure or error message:', result.message);
            showToast('Failed to load course catalog.', 'error');
        }

    } catch (error) {
        console.error('Failed to fetch course catalog:', error);
        containerElement.innerHTML = `<p class="text-sm text-rose-600">Failed to load course catalog.</p>`;
    }
}

/**
 * Builds a single catalog card.
 * Expects a course object shaped like:
 * { courseId, courseName, trainerName, totalModule, ... }
 */
function buildCatalogCard(course, style) {
    const card = document.createElement('div');
    card.className = 'bg-white rounded-xl shadow-sm border border-slate-200 p-5 flex flex-col h-full';
    card.dataset.courseId = course.courseId;

    card.innerHTML = `
        <div class="w-12 h-12 ${style.bg} ${style.text} rounded-xl flex items-center justify-center mb-4">
            <span class="material-symbols-outlined">${style.icon}</span>
        </div>
        <h3 class="font-bold text-slate-800 text-lg">${course.courseName}</h3>
        <p class="text-sm text-slate-500 mt-1">Instructor: ${course.trainerName}</p>
        <p class="text-xs font-medium text-slate-400 mt-3 flex items-center gap-1">
            <span class="material-symbols-outlined text-[14px]">timer</span> ${course.totalModule} module${course.totalModule === 1 ? '' : 's'}
        </p>
        <button type="button" class="enroll-btn mt-auto pt-5 w-full">
            <div class="w-full py-2.5 bg-slate-800 text-white rounded-lg text-sm font-semibold hover:bg-slate-700 transition-colors">Enroll Now</div>
        </button>
    `;

    const enrollBtn = card.querySelector('.enroll-btn');
    enrollBtn.addEventListener('click', () => {
        Swal.fire({
            title: 'Enroll in this course?',
            html: `You will be redirected to pay <strong>RM ${course.coursePrice?.toFixed(2) ?? ''}</strong> for <strong>${course.courseName}</strong>.`,
            icon: 'question',
            showCancelButton: true,
            confirmButtonText: 'Yes, proceed to payment',
            cancelButtonText: 'Cancel'
        }).then((result) => {
            if (result.isConfirmed) {
                initiateEnrollmentPayment(course.courseId);
            }
        });
    });

    return card;
}

async function initiateEnrollmentPayment(courseId) {
    const token = sessionStorage.getItem('token');
    const endpoint = `${window.base}/physio/participant/payment/initiate`;

    try {
        const response = await fetch(endpoint, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            credentials: 'include',
            body: JSON.stringify({ courseId })
        });

        const result = await response.json();

        if (response.ok && result.status === 'success' && result.paymentUrl) {
            window.location.href = result.paymentUrl;
        } else {
            showToast(result.message || 'Failed to start payment.', 'error');
        }

    } catch (error) {
        console.error('Error initiating payment:', error);
        showToast('Failed to start payment.', 'error');
    }
}