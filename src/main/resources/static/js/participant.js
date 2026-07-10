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
    loadMyCourses(userToken);
    loadParticipantSchedule(userToken);
    loadParticipantMaterials(userToken);
    loadDashboardSummary(userToken);

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

/**
 * Fetches the participant's enrolled courses and renders them into #myCoursesListContainer.
 */
async function loadMyCourses(token) {
    const endpoint = `${window.base}/physio/participant/courses/my-courses`;
    const containerElement = document.getElementById('myCoursesListContainer');

    if (!containerElement) {
        console.warn('My courses container (#myCoursesListContainer) not found in DOM.');
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
                containerElement.innerHTML = `<p class="text-sm text-slate-500">You haven't enrolled in any courses yet.</p>`;
                return;
            }

            result.data.forEach(course => {
                containerElement.appendChild(buildMyCourseCard(course));
            });

        } else {
            console.error('API returned an unexpected structure or error message:', result.message);
            showToast('Failed to load your courses.', 'error');
        }

    } catch (error) {
        console.error('Failed to fetch enrolled courses:', error);
        containerElement.innerHTML = `<p class="text-sm text-rose-600">Failed to load your courses.</p>`;
    }
}

/**
 * Builds a single enrolled-course card.
 * courseStatus: 1 = Complete, anything else = In Progress.
 */
function buildMyCourseCard(course) {
    const isComplete = course.courseStatus === 1;

    const card = document.createElement('div');
    card.className = 'bg-white rounded-xl shadow-sm border border-slate-200 p-6 hover:shadow-md transition-shadow';
    card.dataset.courseId = course.courseId;
    card.dataset.enrollmentId = course.enrollmentId;

    const badgeHtml = isComplete
        ? `<span class="text-[10px] font-bold uppercase tracking-wider px-2.5 py-1 bg-emerald-100 text-emerald-700 rounded-md">Completed</span>`
        : `<span class="text-[10px] font-bold uppercase tracking-wider px-2.5 py-1 bg-amber-100 text-amber-700 rounded-md">In Progress</span>`;

    card.innerHTML = `
        <div class="flex justify-between items-start mb-4">
            <div>
                <h3 class="font-bold text-slate-800 text-lg">${course.courseName}</h3>
                <p class="text-sm text-slate-500 mt-1">Instructor: ${course.trainerName}</p>
            </div>
            ${badgeHtml}
        </div>
        <div class="flex gap-2 mt-6" data-actions></div>
    `;

    const actionsContainer = card.querySelector('[data-actions]');

    if (isComplete) {
        const certBtn = document.createElement('button');
        certBtn.type = 'button';
        certBtn.className = 'w-full text-center text-sm font-semibold px-4 py-2 bg-emerald-50 text-emerald-700 rounded-lg cursor-pointer hover:bg-emerald-100 transition-colors flex justify-center items-center gap-2';
        certBtn.innerHTML = `<span class="material-symbols-outlined text-[18px]">workspace_premium</span> View Certificate`;
        certBtn.addEventListener('click', () => {
            const token = sessionStorage.getItem('token');
            viewCertificate(course.courseId, token);
        });
        actionsContainer.appendChild(certBtn);
    } else {
        const materialsBtn = document.createElement('button');
        materialsBtn.type = 'button';
        materialsBtn.className = 'flex-1 text-center text-sm font-semibold px-4 py-2 bg-brand-50 text-brand-700 rounded-lg cursor-pointer hover:bg-brand-100 transition-colors';
        materialsBtn.textContent = 'Materials';
        materialsBtn.addEventListener('click', () => {
            const token = sessionStorage.getItem('token');
            navigateToMaterials(course.courseId, token);
        });

        const assessmentBtn = document.createElement('button');
        assessmentBtn.type = 'button';
        assessmentBtn.className = 'flex-1 text-center text-sm font-semibold px-4 py-2 bg-white border border-slate-200 text-slate-700 rounded-lg cursor-pointer hover:bg-slate-50 transition-colors';
        assessmentBtn.textContent = 'Assessment';
        // TODO: same courseId-passing treatment needed for Assessment once that flow is built

        actionsContainer.appendChild(materialsBtn);
        actionsContainer.appendChild(assessmentBtn);
    }

    return card;
}

/**
 * Switches to the Materials page AND loads only the clicked course's
 * modules — this is what makes each course card's button actually
 * distinct, instead of every card showing the same full list.
 */
function navigateToMaterials(courseId, token) {
    const materialsRadio = document.getElementById('page-materials');
    if (materialsRadio) {
        materialsRadio.checked = true; // triggers the existing CSS/nav page-switch, same as clicking the sidebar link
    }
    loadParticipantMaterials(token, courseId);
}

/**
 * Fetches the participant's schedule and renders it into #participantScheduleContainer.
 */
async function loadParticipantSchedule(token) {
    const endpoint = `${window.base}/physio/participant/schedule/list`;
    const containerElement = document.getElementById('participantScheduleContainer');

    if (!containerElement) {
        console.warn('Schedule container (#participantScheduleContainer) not found in DOM.');
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
                containerElement.innerHTML = `<p class="text-sm text-slate-500">No upcoming sessions scheduled.</p>`;
                return;
            }

            const dateBoxColors = [
                { bg: 'bg-brand-50', border: 'border-brand-100', label: 'text-brand-600', day: 'text-brand-800' },
                { bg: 'bg-amber-50', border: 'border-amber-100', label: 'text-amber-600', day: 'text-amber-800' },
                { bg: 'bg-blue-50', border: 'border-blue-100', label: 'text-blue-600', day: 'text-blue-800' },
                { bg: 'bg-emerald-50', border: 'border-emerald-100', label: 'text-emerald-600', day: 'text-emerald-800' }
            ];

            result.data.forEach((entry, index) => {
                const color = dateBoxColors[index % dateBoxColors.length];
                containerElement.appendChild(buildScheduleEntry(entry, color));
            });

        } else {
            console.error('API returned an unexpected structure or error message:', result.message);
            showToast('Failed to load schedule.', 'error');
        }

    } catch (error) {
        console.error('Failed to fetch schedule:', error);
        containerElement.innerHTML = `<p class="text-sm text-rose-600">Failed to load schedule.</p>`;
    }
}

/**
 * Formats "HH:mm:ss" into "h:mm AM/PM".
 */
function formatTime12Hour(timeStr) {
    if (!timeStr) return '';
    const [hourStr, minute] = timeStr.split(':');
    let hour = parseInt(hourStr, 10);
    const period = hour >= 12 ? 'PM' : 'AM';
    hour = hour % 12 || 12;
    return `${hour}:${minute} ${period}`;
}

/**
 * Builds a single schedule entry card.
 * Expects an entry shaped like:
 * { courseName, courseDate, courseStartTime, courseEndTime, trainerName }
 */
function buildScheduleEntry(entry, color) {
    const date = new Date(entry.courseDate + 'T00:00:00'); // avoid timezone shift
    const monthAbbrev = date.toLocaleDateString('en-US', { month: 'short' });
    const day = date.getDate();

    const startTime = formatTime12Hour(entry.courseStartTime);
    const endTime = formatTime12Hour(entry.courseEndTime);

    const card = document.createElement('div');
    card.className = 'bg-white p-5 rounded-xl shadow-sm border border-slate-200 flex flex-col sm:flex-row sm:items-center gap-4';

    card.innerHTML = `
        <div class="w-14 h-14 ${color.bg} rounded-xl flex flex-col items-center justify-center shrink-0 border ${color.border}">
            <span class="text-[10px] font-bold ${color.label} uppercase">${monthAbbrev}</span>
            <span class="text-xl font-bold ${color.day} leading-none">${day}</span>
        </div>
        <div class="flex-1">
            <p class="font-bold text-slate-800 text-lg">${entry.courseName}</p>
            <p class="text-sm text-slate-500 flex items-center gap-2 mt-1">
                <span class="material-symbols-outlined text-[16px]">schedule</span> ${startTime} - ${endTime}
                <span class="text-slate-300">|</span>
                <span class="material-symbols-outlined text-[16px]">person</span> ${entry.trainerName}
            </p>
        </div>
    `;

    return card;
}

/**
 * Fetches materials for all enrolled courses and renders into #materialsListContainer.
 */
async function loadParticipantMaterials(token, courseId = null) {
    const endpoint = courseId
        ? `${window.base}/physio/participant/materials/list?courseId=${courseId}`
        : `${window.base}/physio/participant/materials/list`;

    const containerElement = document.getElementById('materialsListContainer');

    if (!containerElement) {
        console.warn('Materials container (#materialsListContainer) not found in DOM.');
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
                containerElement.innerHTML = `<p class="text-sm text-slate-500">No materials found.</p>`;
                return;
            }

            result.data.forEach((course, index) => {
                containerElement.appendChild(buildCourseMaterialsGroup(course, index === 0));
            });

        } else {
            console.error('API returned an unexpected structure or error message:', result.message);
            showToast('Failed to load materials.', 'error');
        }

    } catch (error) {
        console.error('Failed to fetch materials:', error);
        containerElement.innerHTML = `<p class="text-sm text-rose-600">Failed to load materials.</p>`;
    }
}

/**
 * Extracts just the filename from a filepath, handling both
 * forward slashes and Windows backslashes.
 */
function getMaterialBasename(filepath) {
    if (!filepath) return '';
    return filepath.split(/[/\\]/).pop();
}

/**
 * Builds a single course's <details> group with its materials list.
 */
/**
 * Builds a single course block: course name heading, followed by
 * one collapsible <details> per module.
 */
function buildCourseMaterialsGroup(course, openFirstModule) {
    const wrapper = document.createElement('div');
    wrapper.className = 'space-y-2';

    const heading = document.createElement('h2');
    heading.className = 'text-sm font-bold text-slate-500 uppercase tracking-wide px-1';
    heading.textContent = course.courseName;
    wrapper.appendChild(heading);

    if (!course.modules || course.modules.length === 0) {
        const empty = document.createElement('p');
        empty.className = 'text-xs text-slate-400 px-1';
        empty.textContent = 'No modules available yet.';
        wrapper.appendChild(empty);
        return wrapper;
    }

    course.modules.forEach((module, index) => {
        const details = document.createElement('details');
        details.className = 'bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden group';
        if (openFirstModule && index === 0) details.setAttribute('open', '');

        details.innerHTML = `
            <summary class="font-bold text-slate-800 cursor-pointer p-5 bg-slate-50 border-b border-slate-100 flex justify-between items-center list-none">
                ${module.moduleName}
                <span class="material-symbols-outlined text-slate-400 group-open:-scale-y-100 transition-transform">expand_more</span>
            </summary>
            <div class="p-3 space-y-1"></div>
        `;

        const itemsContainer = details.querySelector('.p-3');

        if (!module.materials || module.materials.length === 0) {
            itemsContainer.innerHTML = `<p class="text-xs text-slate-400 text-center py-3">No materials uploaded yet.</p>`;
        } else {
            module.materials.forEach(material => {
                const isVideo = material.type === 'video';
                const filename = getMaterialBasename(material.filepath);
                const fileUrl = isVideo
                    ? `${window.base}/physio/video/${filename}`
                    : `${window.base}/physio/pdf/${filename}`;

                const row = document.createElement('a');
                row.href = fileUrl;
                row.target = '_blank';
                row.rel = 'noopener';
                row.className = 'flex items-center justify-between p-3 hover:bg-slate-50 rounded-lg cursor-pointer transition-colors group/item';

                row.innerHTML = `
                    <div class="flex items-center gap-3">
                        <span class="material-symbols-outlined ${isVideo ? 'text-blue-500' : 'text-red-400'}">${isVideo ? 'play_circle' : 'picture_as_pdf'}</span>
                        <span class="text-sm font-medium text-slate-700 group-hover/item:text-brand-600 transition-colors">${material.filename}</span>
                    </div>
                    <span class="material-symbols-outlined text-slate-300 opacity-0 group-hover/item:opacity-100 transition-opacity">${isVideo ? 'play_arrow' : 'download'}</span>
                `;

                itemsContainer.appendChild(row);
            });
        }

        wrapper.appendChild(details);
    });

    return wrapper;
}

async function viewCertificate(courseId, token) {
    const endpoint = `${window.base}/physio/participant/certificate/${courseId}`;

    try {
        const response = await fetch(endpoint, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`
            },
            credentials: 'include'
        });

        if (!response.ok) {
            const errorText = await response.text();
            showToast(errorText || 'Failed to load certificate.', 'error');
            return;
        }

        const blob = await response.blob();
        const blobUrl = URL.createObjectURL(blob);
        window.open(blobUrl, '_blank');

    } catch (error) {
        console.error('Error loading certificate:', error);
        showToast('Failed to load certificate.', 'error');
    }
}

async function viewCertificate(courseId, token) {
    const endpoint = `${window.base}/physio/participant/certificate/data/${courseId}`;

    try {
        const response = await fetch(endpoint, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            credentials: 'include'
        });

        const result = await response.json();

        if (!response.ok || result.status !== 'success') {
            showToast(result.message || 'Failed to load certificate.', 'error');
            return;
        }

        generateCertificatePdf(result.data);

    } catch (error) {
        console.error('Error loading certificate:', error);
        showToast('Failed to load certificate.', 'error');
    }
}

/**
 * Draws the certificate directly onto a jsPDF canvas (A4 landscape)
 * and opens it in a new tab.
 */
function generateCertificatePdf(data) {
    const { jsPDF } = window.jspdf;
    const doc = new jsPDF({ orientation: 'landscape', unit: 'mm', format: 'a4' });

    const pageWidth = doc.internal.pageSize.getWidth();
    const pageHeight = doc.internal.pageSize.getHeight();
    const centerX = pageWidth / 2;

    // Outer double border
    doc.setDrawColor(15, 118, 110); // teal, matches brand color
    doc.setLineWidth(1.2);
    doc.rect(12, 12, pageWidth - 24, pageHeight - 24);
    doc.setLineWidth(0.4);
    doc.rect(15, 15, pageWidth - 30, pageHeight - 30);

    // Title
    doc.setFont('times', 'bold');
    doc.setFontSize(38);
    doc.setTextColor(15, 118, 110);
    doc.text('CERTIFICATE', centerX, 45, { align: 'center' });

    doc.setFontSize(14);
    doc.setTextColor(85, 85, 85);
    doc.setFont('times', 'normal');
    doc.text('OF COMPLETION', centerX, 55, { align: 'center', charSpace: 2 });

    // Presented-to line
    doc.setFontSize(12);
    doc.text('This certificate is proudly presented to', centerX, 75, { align: 'center' });

    // Recipient name
    doc.setFont('times', 'bold');
    doc.setFontSize(30);
    doc.setTextColor(30, 41, 59);
    doc.text(data.fullName, centerX, 92, { align: 'center' });

    // Underline beneath name
    const nameWidth = doc.getTextWidth(data.fullName);
    doc.setDrawColor(203, 213, 225);
    doc.setLineWidth(0.5);
    doc.line(centerX - nameWidth / 2 - 10, 96, centerX + nameWidth / 2 + 10, 96);

    // Description line
    doc.setFont('times', 'normal');
    doc.setFontSize(12);
    doc.setTextColor(85, 85, 85);
    doc.text('for successfully completing the course', centerX, 108, { align: 'center' });

    // Course name
    doc.setFont('times', 'bold');
    doc.setFontSize(20);
    doc.setTextColor(15, 118, 110);
    doc.text(data.courseName, centerX, 120, { align: 'center' });

    // Footer: cert number + date
    doc.setFont('times', 'normal');
    doc.setFontSize(10);
    doc.setTextColor(136, 136, 136);
    doc.text(`Certificate No: ALPRO-${data.courseId}`, 30, pageHeight - 25);
    doc.text(`Date of Completion: ${data.completionDate}`, pageWidth - 30, pageHeight - 25, { align: 'right' });

    // Open in a new tab rather than force-downloading
    const blobUrl = doc.output('bloburl');
    window.open(blobUrl, '_blank');
}

let selectedFeedbackRating = 0;

/**
 * Loads completed courses without existing feedback into the dropdown.
 */
async function loadFeedbackEligibleCourses(token) {
    const endpoint = `${window.base}/physio/participant/feedback/eligible-courses`;
    const select = document.getElementById('feedbackCourseSelect');

    if (!select) {
        console.warn('Feedback course select (#feedbackCourseSelect) not found in DOM.');
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
            select.length = 1; // keep placeholder

            if (result.data.length === 0) {
                const opt = document.createElement('option');
                opt.disabled = true;
                opt.textContent = 'No completed courses available for feedback';
                select.appendChild(opt);
                return;
            }

            result.data.forEach(item => {
                const opt = document.createElement('option');
                opt.value = item.enrollmentId;
                opt.textContent = item.courseName;
                select.appendChild(opt);
            });

        } else {
            console.error('API returned an unexpected structure or error message:', result.message);
        }

    } catch (error) {
        console.error('Failed to fetch eligible courses:', error);
    }
}

/**
 * Wires up the clickable star rating, tracking the selected value
 * and visually filling stars up to that point.
 */
function setupStarRating() {
    const container = document.getElementById('feedbackStarRating');
    if (!container) return;

    const stars = container.querySelectorAll('[data-star]');

    stars.forEach(star => {
        star.addEventListener('click', () => {
            selectedFeedbackRating = parseInt(star.dataset.star, 10);
            updateStarDisplay(stars, selectedFeedbackRating);
        });
    });
}

function updateStarDisplay(stars, rating) {
    stars.forEach(star => {
        const value = parseInt(star.dataset.star, 10);
        star.classList.toggle('fill-icon', value <= rating);
    });
}

/**
 * Submits the feedback form.
 */
async function submitFeedback(token) {
    const select = document.getElementById('feedbackCourseSelect');
    const reviewInput = document.getElementById('feedbackReviewInput');
    const submitBtn = document.getElementById('submitFeedbackBtn');

    const enrollmentId = select.value;
    const review = reviewInput.value.trim();

    if (!enrollmentId) {
        showToast('Please select a course.', 'error');
        return;
    }

    if (selectedFeedbackRating < 1) {
        showToast('Please select a star rating.', 'error');
        return;
    }

    const originalText = submitBtn.textContent;
    submitBtn.disabled = true;
    submitBtn.textContent = 'Submitting...';

    try {
        const endpoint = `${window.base}/physio/participant/feedback/submit`;

        const response = await fetch(endpoint, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            credentials: 'include',
            body: JSON.stringify({
                enrollmentId: parseInt(enrollmentId, 10),
                rate: selectedFeedbackRating,
                review: review || null
            })
        });

        const result = await response.json();

        if (response.ok && result.status === 'success') {
            showToast('Feedback submitted successfully!', 'success');
            select.value = '';
            reviewInput.value = '';
            selectedFeedbackRating = 0;
            updateStarDisplay(document.querySelectorAll('#feedbackStarRating [data-star]'), 0);
            loadFeedbackEligibleCourses(token); // refresh — this course should now disappear from the list
        } else {
            showToast(result.message || 'Failed to submit feedback.', 'error');
        }

    } catch (error) {
        console.error('Error submitting feedback:', error);
        showToast('Failed to submit feedback.', 'error');
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = originalText;
    }
}

document.addEventListener('DOMContentLoaded', () => {
    const token = sessionStorage.getItem('token');
    if (!token) return;

    loadFeedbackEligibleCourses(token);
    setupStarRating();

    const submitBtn = document.getElementById('submitFeedbackBtn');
    if (submitBtn) {
        submitBtn.addEventListener('click', () => submitFeedback(token));
    }
});

async function loadDashboardSummary(token) {
    const endpoint = `${window.base}/physio/participant/dashboard/summary`;

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
            const data = result.data;

            const welcomeHeading = document.querySelector('#content-dashboard h1');
            if (welcomeHeading) welcomeHeading.textContent = `Welcome back, ${data.fullName}`;

            const statValues = document.querySelectorAll('#content-dashboard .grid.grid-cols-2 > div .text-2xl');
            if (statValues.length >= 4) {
                statValues[0].textContent = data.enrolledCoursesCount;
                statValues[1].textContent = data.upcomingClassesCount;
                statValues[2].textContent = data.pendingTasksCount ?? '—'; // not implemented yet
                statValues[3].textContent = data.certificatesCount;
            }

            const scheduleContainer = document.querySelector('#content-dashboard .space-y-3');
            if (scheduleContainer) {
                scheduleContainer.innerHTML = '';

                if (!data.todaySchedule || data.todaySchedule.length === 0) {
                    scheduleContainer.innerHTML = `<p class="text-sm text-slate-500">No classes scheduled for today.</p>`;
                } else {
                    data.todaySchedule.forEach(entry => {
                        const row = document.createElement('div');
                        row.className = 'flex items-center justify-between p-4 bg-slate-50 hover:bg-slate-100 transition-colors rounded-xl border border-slate-100';
                        row.innerHTML = `
                            <div class="flex items-center gap-4">
                                <div class="w-1.5 h-10 bg-brand-500 rounded-full"></div>
                                <div>
                                    <p class="font-bold text-slate-800">${entry.courseName}</p>
                                    <p class="text-sm text-slate-500 mt-0.5 flex items-center gap-1">
                                        <span class="material-symbols-outlined text-[14px]">schedule</span> ${entry.startTime} — ${entry.trainerName}
                                    </p>
                                </div>
                            </div>
                        `;
                        scheduleContainer.appendChild(row);
                    });
                }
            }

        } else {
            console.error('API returned an unexpected structure or error message:', result.message);
        }

    } catch (error) {
        console.error('Failed to fetch dashboard summary:', error);
    }
}