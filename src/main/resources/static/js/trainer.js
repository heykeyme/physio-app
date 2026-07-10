/**
 * my-classes.js
 * Standalone script for the trainer "My Classes" page.
 * Fetches the trainer's classes and renders them as cards.
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
    loadWeeklySchedule(userToken); 
    loadMyClasses(userToken);
    loadAssessmentClasses(userToken);
});

/**
 * Fetches the trainer's classes and renders them into #classListContainer.
 */
async function loadMyClasses(token) {
    const endpoint = `${window.base}/physio/trainer/classes/list`;
    const containerElement = document.getElementById('classListContainer');

    if (!containerElement) {
        console.warn('Class list container (#classListContainer) not found in DOM.');
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
                containerElement.innerHTML = `<p class="text-sm text-slate-500">No classes found.</p>`;
                return;
            }

            result.data.forEach(course => {
                const card = buildClassCard(course);
                containerElement.appendChild(card);
            });

        } else {
            console.error('API returned an unexpected structure or error message:', result.message);
            showToast('Failed to load classes.', 'error');
        }

    } catch (error) {
        console.error('Failed to fetch classes:', error);
        containerElement.innerHTML = `<p class="text-sm text-rose-600">Failed to load classes.</p>`;
    }
}

/**
 * Builds a single class card element.
 */
function buildClassCard(course) {
    const card = document.createElement('div');
    card.className = 'bg-white p-6 rounded-xl shadow-sm border border-slate-200';
    card.dataset.courseId = course.courseId;

    card.innerHTML = `
        <h3 class="font-bold text-slate-800 text-lg">${course.courseName}</h3>
        <p class="text-sm text-slate-500 mt-1 flex items-center gap-4">
            <span class="flex items-center gap-1">
                <span class="material-symbols-outlined text-[16px]">groups</span>
                ${course.totalParticipant} participants
            </span>
            <span class="flex items-center gap-1">
                <span class="material-symbols-outlined text-[16px]">view_agenda</span>
                ${course.totalModule} module
            </span>
        </p>
        <div class="mt-5 flex gap-2" data-actions></div>
    `;

    const manageBtn = document.createElement('button');
    manageBtn.type = 'button';
    manageBtn.className = 'flex-1 text-center text-sm font-semibold px-4 py-2 bg-brand-50 text-brand-700 rounded-lg cursor-pointer hover:bg-brand-100 transition-colors border border-brand-100';
    manageBtn.textContent = 'Manage';
    manageBtn.addEventListener('click', () => {
        const token = sessionStorage.getItem('token');
        openManageModal(course.courseId, course.courseName, token);
    });

    const attendanceBtn = document.createElement('button');
    attendanceBtn.type = 'button';
    attendanceBtn.className = 'flex-1 text-center text-sm font-semibold px-4 py-2 bg-amber-50 text-amber-700 rounded-lg cursor-pointer hover:bg-amber-100 transition-colors border border-amber-100';
    attendanceBtn.textContent = 'Attendance';
    attendanceBtn.addEventListener('click', () => {
        const token = sessionStorage.getItem('token');
        openAttendanceModal(course.courseId, course.courseName, token);
    });

    const actionsContainer = card.querySelector('[data-actions]');
    actionsContainer.appendChild(manageBtn);
    actionsContainer.appendChild(attendanceBtn);

    return card;
}

let currentManageCourseId = null;

function openManageModal(courseId, courseName, token) {
    const toggle = document.getElementById('manage-course');
    const titleEl = document.getElementById('manageCourseName');

    if (!toggle) {
        console.warn('Manage course toggle checkbox (#manage-course) not found in DOM.');
        return;
    }

    currentManageCourseId = courseId;
    if (titleEl) titleEl.textContent = courseName ?? '';

    toggle.checked = true;

    loadModulesForCourse(courseId, token);
}

function closeManageModal() {
    const toggle = document.getElementById('manage-course');
    if (toggle) toggle.checked = false;
    currentManageCourseId = null;
}

function getBasename(filepath) {
    if (!filepath) return '';
    return filepath.split(/[/\\]/).pop();
}

async function loadModulesForCourse(courseId, token) {
    const endpoint = `${window.base}/physio/trainer/classes/manage/modules?courseId=${courseId}`;
    const container = document.getElementById('modules-container');
    const totalEl = document.getElementById('module-total');

    if (!container) {
        console.warn('Modules container (#modules-container) not found in DOM.');
        return;
    }

    const addModuleBtn = document.getElementById('add-module-btn');
    container.innerHTML = '<p class="text-sm text-slate-500">Loading modules...</p>';

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

        container.innerHTML = '';

        if (result.status === 'success' && Array.isArray(result.data)) {
            result.data.forEach((module, index) => {
                const moduleEl = buildModuleElement(module, index + 1, token);
                container.appendChild(moduleEl);
            });

            if (totalEl) {
                const n = result.data.length;
                totalEl.textContent = n + (n === 1 ? ' module' : ' modules');
            }
        } else {
            console.error('API returned an unexpected structure or error message:', result.message);
            showToast('Failed to load modules.', 'error');
        }

    } catch (error) {
        console.error('Failed to fetch modules:', error);
        container.innerHTML = `<p class="text-sm text-rose-600">Failed to load modules.</p>`;
    }

    if (addModuleBtn) container.appendChild(addModuleBtn);
}

function buildModuleElement(module, displayNumber, token) {
    const totalItems = (module.videos?.length ?? 0) + (module.pdfs?.length ?? 0);

    const details = document.createElement('details');
    details.className = 'module border border-slate-200 rounded-lg overflow-hidden';
    details.dataset.moduleId = module.moduleId;
    if (displayNumber === 1) details.setAttribute('open', '');

    details.innerHTML = `
        <summary class="flex items-center justify-between p-4 cursor-pointer bg-slate-50 hover:bg-slate-100 transition-colors">
            <div class="flex items-center gap-2.5 min-w-0">
                <span class="material-symbols-outlined text-[20px] text-brand-600">view_agenda</span>
                <span class="font-semibold text-slate-800 text-sm truncate">Module ${displayNumber} · ${module.moduleName}</span>
            </div>
            <div class="flex items-center gap-2 shrink-0">
                <span class="item-count text-xs text-slate-400">${totalItems} ${totalItems === 1 ? 'item' : 'items'}</span>
                <button type="button" class="delete-module-btn w-6 h-6 flex items-center justify-center text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-full transition-colors" title="Delete module">
                    <span class="material-symbols-outlined text-[16px]">delete</span>
                </button>
                <span class="material-symbols-outlined accordion-chevron text-[20px] text-slate-400">expand_more</span>
            </div>
        </summary>
        <div class="module-body p-4 border-t border-slate-100 space-y-2">
            <div class="module-items"></div>
            <p class="module-empty text-xs text-slate-400 text-center py-2" style="display:${totalItems === 0 ? '' : 'none'}">No materials yet. Add a video or PDF below.</p>
            <div class="module-actions flex gap-2 pt-1">
                <label class="flex-1 flex items-center justify-center gap-1.5 text-xs font-semibold px-3 py-2 bg-brand-50 text-brand-700 rounded-lg border border-brand-100 hover:bg-brand-100 cursor-pointer transition-colors">
                    <span class="material-symbols-outlined text-[16px]">video_call</span> Add Video
                    <input type="file" accept="video/*" class="hidden" data-upload-type="video">
                </label>
                <label class="flex-1 flex items-center justify-center gap-1.5 text-xs font-semibold px-3 py-2 bg-amber-50 text-amber-700 rounded-lg border border-amber-100 hover:bg-amber-100 cursor-pointer transition-colors">
                    <span class="material-symbols-outlined text-[16px]">upload_file</span> Add PDF
                    <input type="file" accept="application/pdf" class="hidden" data-upload-type="pdf">
                </label>
            </div>
        </div>
    `;

    const itemsContainer = details.querySelector('.module-items');

    (module.videos ?? []).forEach(video => {
        itemsContainer.appendChild(
            buildMaterialItem(video.id, video.videoFilename, getBasename(video.videoFilepath), true, module.courseId, token)
        );
    });

    (module.pdfs ?? []).forEach(pdf => {
        itemsContainer.appendChild(
            buildMaterialItem(pdf.id, pdf.uploadFilename, getBasename(pdf.uploadFilepath), false, module.courseId, token)
        );
    });

    const deleteModuleBtn = details.querySelector('.delete-module-btn');
    deleteModuleBtn.addEventListener('click', async (e) => {
        e.preventDefault();
        e.stopPropagation();

        const confirmed = confirm(`Delete "${module.moduleName}" and all its videos/PDFs? This cannot be undone.`);
        if (!confirmed) return;

        const currentToken = sessionStorage.getItem('token') || token;
        await deleteModule(module.moduleId, currentToken);

        if (currentManageCourseId !== null) {
            loadModulesForCourse(currentManageCourseId, currentToken);
        }
    });

    const fileInputs = details.querySelectorAll('input[type="file"]');
    fileInputs.forEach(input => {
        input.addEventListener('change', async () => {
            const file = input.files[0];
            if (!file) return;

            const uploadType = input.dataset.uploadType;
            const currentToken = sessionStorage.getItem('token') || token;

            await uploadMaterial(module.moduleId, uploadType, file, currentToken);
            input.value = '';

            if (currentManageCourseId !== null) {
                loadModulesForCourse(currentManageCourseId, currentToken);
            }
        });
    });

    return details;
}

function buildMaterialItem(id, displayName, storedFilename, isVideo, courseId, token) {
    const row = document.createElement('div');
    row.className = 'module-item flex items-center justify-between gap-3 p-2.5 rounded-lg border border-slate-100 hover:bg-slate-50 transition-colors';

    const fileUrl = isVideo
        ? `${window.base}/physio/video/${storedFilename}`
        : `${window.base}/physio/pdf/${storedFilename}`;

    row.innerHTML = `
        <a href="${fileUrl}" target="_blank" rel="noopener" class="flex items-center gap-3 min-w-0 hover:opacity-80 transition-opacity">
            <div class="w-8 h-8 shrink-0 ${isVideo ? 'bg-blue-50 text-blue-500' : 'bg-red-50 text-red-500'} rounded-lg flex items-center justify-center">
                <span class="material-symbols-outlined text-[18px]">${isVideo ? 'play_circle' : 'picture_as_pdf'}</span>
            </div>
            <p class="text-sm font-medium text-slate-700 truncate">${displayName}</p>
        </a>
        <button type="button" class="remove-item w-7 h-7 shrink-0 flex items-center justify-center text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-full transition-colors" title="Delete">
            <span class="material-symbols-outlined text-[18px]">close</span>
        </button>
    `;

    const removeBtn = row.querySelector('.remove-item');
    removeBtn.addEventListener('click', async () => {
        const confirmed = confirm(`Delete "${displayName}"? This cannot be undone.`);
        if (!confirmed) return;

        const currentToken = sessionStorage.getItem('token') || token;

        if (isVideo) {
            await deleteVideo(id, currentToken);
        } else {
            await deletePdf(id, currentToken);
        }

        if (currentManageCourseId !== null) {
            loadModulesForCourse(currentManageCourseId, currentToken);
        }
    });

    return row;
}

async function deleteVideo(id, token) {
    const endpoint = `${window.base}/physio/trainer/classes/manage/video?id=${id}`;

    try {
        const response = await fetch(endpoint, {
            method: 'DELETE',
            headers: { 'Authorization': `Bearer ${token}` },
            credentials: 'include'
        });

        const result = await response.json();

        if (response.ok && result.status === 'success') {
            showToast(result.message || 'Video deleted.', 'success');
        } else {
            showToast(result.message || 'Failed to delete video.', 'error');
        }

    } catch (error) {
        console.error('Error deleting video:', error);
        showToast('Failed to delete video.', 'error');
    }
}

async function deletePdf(id, token) {
    const endpoint = `${window.base}/physio/trainer/classes/manage/pdf?id=${id}`;

    try {
        const response = await fetch(endpoint, {
            method: 'DELETE',
            headers: { 'Authorization': `Bearer ${token}` },
            credentials: 'include'
        });

        const result = await response.json();

        if (response.ok && result.status === 'success') {
            showToast(result.message || 'PDF deleted.', 'success');
        } else {
            showToast(result.message || 'Failed to delete PDF.', 'error');
        }

    } catch (error) {
        console.error('Error deleting PDF:', error);
        showToast('Failed to delete PDF.', 'error');
    }
}

async function deleteModule(id, token) {
    const endpoint = `${window.base}/physio/trainer/classes/manage/module?id=${id}`;

    try {
        const response = await fetch(endpoint, {
            method: 'DELETE',
            headers: { 'Authorization': `Bearer ${token}` },
            credentials: 'include'
        });

        const result = await response.json();

        if (response.ok && result.status === 'success') {
            showToast(result.message || 'Module deleted.', 'success');
        } else {
            showToast(result.message || 'Failed to delete module.', 'error');
        }

    } catch (error) {
        console.error('Error deleting module:', error);
        showToast('Failed to delete module.', 'error');
    }
}

async function uploadMaterial(moduleId, type, file, token) {
    const endpoint = type === 'video'
        ? `${window.base}/physio/trainer/classes/manage/video`
        : `${window.base}/physio/trainer/classes/manage/pdf`;

    const formData = new FormData();
    formData.append('moduleId', moduleId);
    formData.append('file', file);

    try {
        const response = await fetch(endpoint, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${token}` },
            credentials: 'include',
            body: formData
        });

        const result = await response.json();

        if (response.ok && result.status === 'success') {
            showToast(`${type === 'video' ? 'Video' : 'PDF'} uploaded successfully.`, 'success');
        } else {
            showToast(result.message || `Failed to upload ${type}.`, 'error');
        }

    } catch (error) {
        console.error(`Error uploading ${type}:`, error);
        showToast(`Failed to upload ${type}.`, 'error');
    }
}

function promptForModuleName() {
    return new Promise((resolve) => {
        const modal = document.getElementById('module-name-modal');
        const backdrop = document.getElementById('module-name-modal-backdrop');
        const input = document.getElementById('moduleNameInput');
        const confirmBtn = document.getElementById('moduleNameConfirmBtn');
        const cancelBtn = document.getElementById('moduleNameCancelBtn');

        if (!modal || !input || !confirmBtn || !cancelBtn) {
            console.warn('Module name prompt modal elements not found in DOM.');
            resolve(null);
            return;
        }

        input.value = '';
        modal.classList.remove('hidden');
        modal.classList.add('flex');
        input.focus();

        function cleanup(result) {
            modal.classList.add('hidden');
            modal.classList.remove('flex');
            confirmBtn.removeEventListener('click', onConfirm);
            cancelBtn.removeEventListener('click', onCancel);
            backdrop.removeEventListener('click', onCancel);
            input.removeEventListener('keydown', onKeydown);
            resolve(result);
        }

        function onConfirm() {
            const value = input.value.trim();
            cleanup(value.length > 0 ? value : null);
        }

        function onCancel() {
            cleanup(null);
        }

        function onKeydown(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                onConfirm();
            } else if (e.key === 'Escape') {
                onCancel();
            }
        }

        confirmBtn.addEventListener('click', onConfirm);
        cancelBtn.addEventListener('click', onCancel);
        backdrop.addEventListener('click', onCancel);
        input.addEventListener('keydown', onKeydown);
    });
}

async function createNewModule(courseId, token) {
    const moduleName = await promptForModuleName();
    if (!moduleName) return;

    const endpoint = `${window.base}/physio/trainer/classes/manage/module`;

    const formData = new FormData();
    formData.append('courseId', courseId);
    formData.append('title', moduleName.trim());

    try {
        const response = await fetch(endpoint, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${token}` },
            credentials: 'include',
            body: formData
        });

        const result = await response.json();

        if (response.ok && result.status === 'success') {
            showToast('Module created successfully.', 'success');
            loadModulesForCourse(courseId, token);
        } else {
            showToast(result.message || 'Failed to create module.', 'error');
        }

    } catch (error) {
        console.error('Error creating module:', error);
        showToast('Failed to create module.', 'error');
    }
}

document.addEventListener('DOMContentLoaded', () => {
    const addModuleBtn = document.getElementById('add-module-btn');

    if (addModuleBtn) {
        addModuleBtn.addEventListener('click', () => {
            const token = sessionStorage.getItem('token');
            if (currentManageCourseId !== null) {
                createNewModule(currentManageCourseId, token);
            }
        });
    }
});

let currentAttendanceCourseId = null;

function openAttendanceModal(courseId, courseName, token) {
    const toggle = document.getElementById('page-attendance');
    const titleEl = document.getElementById('attendanceCourseName');

    if (!toggle) {
        console.warn('Attendance toggle checkbox (#page-attendance) not found in DOM.');
        return;
    }

    currentAttendanceCourseId = courseId;
    if (titleEl) titleEl.textContent = courseName ?? '';

    toggle.checked = true;

    loadAttendanceList(courseId, token);
}

function closeAttendanceModal() {
    const toggle = document.getElementById('page-attendance');
    if (toggle) toggle.checked = false;
    currentAttendanceCourseId = null;
}

async function loadAttendanceList(courseId, token) {
    const endpoint = `${window.base}/physio/trainer/classes/attendance?courseId=${courseId}`;
    const listElement = document.getElementById('attendanceList');

    if (!listElement) {
        console.warn('Attendance list (#attendanceList) not found in DOM.');
        return;
    }

    listElement.innerHTML = `<li class="py-3 text-sm text-slate-500">Loading attendees...</li>`;

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
            listElement.innerHTML = '';

            if (result.data.length === 0) {
                listElement.innerHTML = `<li class="py-3 text-sm text-slate-500">No participants enrolled in this course.</li>`;
                return;
            }

            result.data.forEach(attendee => {
                const row = buildAttendeeRow(attendee);
                listElement.appendChild(row);
            });

        } else {
            console.error('API returned an unexpected structure or error message:', result.message);
            listElement.innerHTML = `<li class="py-3 text-sm text-rose-600">Failed to load attendees.</li>`;
        }

    } catch (error) {
        console.error('Failed to fetch attendees:', error);
        listElement.innerHTML = `<li class="py-3 text-sm text-rose-600">Failed to load attendees.</li>`;
    }
}

function buildAttendeeRow(attendee) {
    const li = document.createElement('li');
    li.className = 'flex items-center justify-between py-3';
    li.dataset.userId = attendee.userId;

    const isPresent = attendee.attendanceStatus === 1;

    li.innerHTML = `
        <p class="text-sm font-semibold text-slate-800">${attendee.participantName}</p>
        <label class="inline-flex items-center gap-2 cursor-pointer select-none">
            <span class="text-xs font-medium text-slate-500">Present</span>
            <input type="checkbox" class="w-4 h-4 rounded border-slate-300 text-brand-700 focus:ring-brand-700" ${isPresent ? 'checked' : ''}>
        </label>
    `;

    return li;
}

async function saveAttendance(token) {
    const listElement = document.getElementById('attendanceList');
    const saveBtn = document.getElementById('saveAttendanceBtn');

    if (!listElement || currentAttendanceCourseId === null) {
        console.warn('No active course context for saving attendance.');
        return;
    }

    const rows = listElement.querySelectorAll('li[data-user-id]');
    const items = Array.from(rows).map(row => {
        const checkbox = row.querySelector('input[type="checkbox"]');
        return {
            userId: row.dataset.userId,
            courseId: currentAttendanceCourseId,
            attendanceStatus: checkbox.checked ? 1 : 0
        };
    });

    if (items.length === 0) {
        console.warn('No attendees to save.');
        return;
    }

    const originalText = saveBtn.textContent;
    saveBtn.disabled = true;
    saveBtn.textContent = 'Saving...';

    try {
        const endpoint = `${window.base}/physio/trainer/classes/attendance/update`;

        const response = await fetch(endpoint, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            credentials: 'include',
            body: JSON.stringify(items)
        });

        const result = await response.json();

        if (response.ok && (result.status === 'success' || result.status === 'partial')) {
            showToast(result.message || 'Attendance saved.', result.status === 'success' ? 'success' : 'error');
            if (result.status === 'success') {
                closeAttendanceModal();
            }
        } else {
            showToast(result.message || 'Failed to save attendance.', 'error');
        }

    } catch (error) {
        console.error('Error saving attendance:', error);
        showToast('Failed to save attendance.', 'error');
    } finally {
        saveBtn.disabled = false;
        saveBtn.textContent = originalText;
    }
}

document.addEventListener('DOMContentLoaded', () => {
    const closeBtn = document.getElementById('closeAttendanceModalBtn');
    const cancelBtn = document.getElementById('cancelAttendanceBtn');
    const saveBtn = document.getElementById('saveAttendanceBtn');
    const backdrop = document.getElementById('attendance-modal-backdrop');

    if (closeBtn) closeBtn.addEventListener('click', closeAttendanceModal);
    if (cancelBtn) cancelBtn.addEventListener('click', closeAttendanceModal);
    if (backdrop) backdrop.addEventListener('click', closeAttendanceModal);
    if (saveBtn) {
        saveBtn.addEventListener('click', () => {
            const token = sessionStorage.getItem('token');
            saveAttendance(token);
        });
    }
});

async function loadWeeklySchedule(token) {
    const endpoint = `${window.base}/physio/trainer/schedule/weekly`;
    const containerElement = document.getElementById('scheduleListContainer');

    if (!containerElement) {
        console.warn('Schedule list container (#scheduleListContainer) not found in DOM.');
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
                containerElement.innerHTML = `<p class="text-sm text-slate-500">No classes scheduled this week.</p>`;
                return;
            }

            const borderColors = ['border-l-brand-500', 'border-l-amber-500', 'border-l-blue-500', 'border-l-rose-500'];

            result.data.forEach((entry, index) => {
                const card = buildScheduleCard(entry, borderColors[index % borderColors.length]);
                containerElement.appendChild(card);
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

function buildScheduleCard(entry, borderColorClass) {
    const card = document.createElement('div');
    card.className = `bg-white p-5 rounded-xl shadow-sm border border-slate-200 flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-l-4 ${borderColorClass}`;

    card.innerHTML = `
        <div>
            <p class="font-bold text-slate-800 text-lg">${entry.courseName}</p>
            <p class="text-sm text-slate-500 mt-1">${entry.day}, ${entry.date}</p>
        </div>
        <div class="px-4 py-2 bg-slate-50 rounded-lg font-bold text-slate-700 border border-slate-100 text-center">
            ${entry.time}
        </div>
    `;

    return card;
}

async function loadAssessmentClasses(token) {
    const endpoint = `${window.base}/physio/trainer/classes/list`;
    const containerElement = document.getElementById('assessmentClassListContainer');

    if (!containerElement) {
        console.warn('Assessment class list container (#assessmentClassListContainer) not found in DOM.');
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
                containerElement.innerHTML = `<p class="text-sm text-slate-500">No assigned classes found.</p>`;
                return;
            }

            result.data.forEach(course => {
                const card = buildAssessmentCourseCard(course);
                containerElement.appendChild(card);
            });

        } else {
            console.error('API returned an unexpected structure or error message:', result.message);
            showToast('Failed to load classes.', 'error');
        }

    } catch (error) {
        console.error('Failed to fetch classes:', error);
        containerElement.innerHTML = `<p class="text-sm text-rose-600">Failed to load classes.</p>`;
    }
}

function buildAssessmentCourseCard(course) {
    const isActive = course.courseStatus === 1;
    const statusBadgeClass = isActive
        ? 'bg-green-100 text-green-700'
        : 'bg-rose-100 text-rose-700';
    const statusText = isActive ? 'Active' : 'Inactive';

    const card = document.createElement('div');
    card.className = 'bg-white p-5 rounded-xl shadow-sm border border-slate-200 flex flex-col sm:flex-row sm:items-center justify-between gap-4';
    card.dataset.courseId = course.courseId;

    card.innerHTML = `
        <div>
            <div class="flex items-center gap-2">
                <p class="font-bold text-slate-800 text-lg">${course.courseName}</p>
                <span class="px-2 py-0.5 ${statusBadgeClass} rounded-full text-[10px] font-bold uppercase tracking-wide">${statusText}</span>
            </div>
            <p class="text-sm text-slate-500 mt-1 flex items-center gap-4">
                <span class="flex items-center gap-1">
                    <span class="material-symbols-outlined text-[16px]">groups</span>
                    ${course.totalParticipant} participants
                </span>
            </p>
        </div>
    `;

    const manageBtn = document.createElement('button');
    manageBtn.type = 'button';
    manageBtn.className = 'px-5 py-2 bg-brand-50 text-brand-700 rounded-lg text-sm font-semibold border border-brand-100 hover:bg-brand-100 transition-colors w-full sm:w-auto';
    manageBtn.textContent = 'Manage Assessment';
    manageBtn.addEventListener('click', () => {
        const token = sessionStorage.getItem('token');
        showAssessmentManagementPage(course.courseId, course.courseName, token);
    });

    card.appendChild(manageBtn);

    return card;
}

let currentAssessmentCourseId = null;

function showAssessmentManagementPage(courseId, courseName, token) {
    const listSection = document.getElementById('content-assessments');
    const manageSection = document.getElementById('content-manage-assessment');
    const titleEl = document.getElementById('manageAssessmentCourseName');

    if (!manageSection) {
        console.warn('Assessment management section (#content-manage-assessment) not found in DOM.');
        return;
    }

    currentAssessmentCourseId = courseId;
    if (titleEl) titleEl.textContent = courseName ?? '';

    if (listSection) listSection.style.display = 'none';
    manageSection.classList.add('active');

    loadAssessmentsForCourse(courseId, token);
}

function hideAssessmentManagementPage() {
    const listSection = document.getElementById('content-assessments');
    const manageSection = document.getElementById('content-manage-assessment');

    if (manageSection) manageSection.classList.remove('active');
    if (listSection) listSection.style.display = '';

    currentAssessmentCourseId = null;
}

async function loadAssessmentsForCourse(courseId, token) {
    const endpoint = `${window.base}/physio/trainer/assessment/list?courseId=${courseId}`;
    const container = document.getElementById('assessmentListContainer');

    if (!container) {
        console.warn('Assessment list container (#assessmentListContainer) not found in DOM.');
        return;
    }

    container.innerHTML = `<p class="text-sm text-slate-500">Loading assessments...</p>`;

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

        container.innerHTML = '';

        if (result.status === 'success' && Array.isArray(result.data)) {
            if (result.data.length === 0) {
                container.innerHTML = `<p class="text-sm text-slate-500">No assessments yet. Create one below.</p>`;
                return;
            }

            result.data.forEach(assessment => {
                container.appendChild(buildAssessmentItem(assessment));
            });

        } else {
            console.error('API returned an unexpected structure or error message:', result.message);
            showToast('Failed to load assessments.', 'error');
        }

    } catch (error) {
        console.error('Failed to fetch assessments:', error);
        container.innerHTML = `<p class="text-sm text-rose-600">Failed to load assessments.</p>`;
    }
}

function buildAssessmentItem(assessment) {
    const row = document.createElement('div');
    row.className = 'bg-white p-5 rounded-xl shadow-sm border border-slate-200 flex flex-col sm:flex-row sm:items-center justify-between gap-4';
    row.dataset.assessmentId = assessment.assessmentId;

    row.innerHTML = `
        <p class="font-bold text-slate-800 text-lg">${assessment.title}</p>
    `;

    const manageQuestionsBtn = document.createElement('button');
    manageQuestionsBtn.type = 'button';
    manageQuestionsBtn.className = 'px-5 py-2 bg-brand-50 text-brand-700 rounded-lg text-sm font-semibold border border-brand-100 hover:bg-brand-100 transition-colors w-full sm:w-auto';
    manageQuestionsBtn.textContent = 'Manage Questions';
    manageQuestionsBtn.addEventListener('click', () => {
        const token = sessionStorage.getItem('token');
        showManageQuestionsPage(assessment.assessmentId, assessment.title, token);
    });

    row.appendChild(manageQuestionsBtn);

    return row;
}

/* ============================================
   MANAGE QUESTIONS PAGE
   ============================================ */

let currentQuestionsAssessmentId = null;

/**
 * Shows the per-assessment question management section, hiding the
 * assessment management section. Same self-contained visibility
 * pattern as content-manage-assessment.
 */
function showManageQuestionsPage(assessmentId, assessmentTitle, token) {
    const assessmentSection = document.getElementById('content-manage-assessment');
    const questionsSection = document.getElementById('content-manage-questions');
    const titleEl = document.getElementById('manageQuestionsAssessmentTitle');

    if (!questionsSection) {
        console.warn('Manage questions section (#content-manage-questions) not found in DOM.');
        return;
    }

    currentQuestionsAssessmentId = assessmentId;
    if (titleEl) titleEl.textContent = assessmentTitle ?? '';

    if (assessmentSection) assessmentSection.classList.remove('active');
    questionsSection.classList.add('active');

    loadQuestionsForAssessment(assessmentId, token);
}

/**
 * Hides the question management section and returns to the
 * assessment management section (not the top-level class list).
 */
function hideManageQuestionsPage() {
    const assessmentSection = document.getElementById('content-manage-assessment');
    const questionsSection = document.getElementById('content-manage-questions');

    if (questionsSection) questionsSection.classList.remove('active');
    if (assessmentSection) assessmentSection.classList.add('active');

    currentQuestionsAssessmentId = null;
}

/**
 * Fetches questions (with options) for an assessment and renders
 * them into #questionListContainer.
 */
async function loadQuestionsForAssessment(assessmentId, token) {
    const endpoint = `${window.base}/physio/trainer/question/list?assessmentId=${assessmentId}`;
    const container = document.getElementById('questionListContainer');

    if (!container) {
        console.warn('Question list container (#questionListContainer) not found in DOM.');
        return;
    }

    container.innerHTML = `<p class="text-sm text-slate-500">Loading questions...</p>`;

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

        container.innerHTML = '';

        if (result.status === 'success' && Array.isArray(result.data)) {
            if (result.data.length === 0) {
                container.innerHTML = `<p class="text-sm text-slate-500">No questions yet. Create one below.</p>`;
                return;
            }

            result.data.forEach((question, index) => {
                container.appendChild(buildQuestionItem(question, index + 1));
            });

        } else {
            console.error('API returned an unexpected structure or error message:', result.message);
            showToast('Failed to load questions.', 'error');
        }

    } catch (error) {
        console.error('Failed to fetch questions:', error);
        container.innerHTML = `<p class="text-sm text-rose-600">Failed to load questions.</p>`;
    }
}

/**
 * Builds a single question card, showing all 4 options with the
 * correct one highlighted. No edit/delete yet — display only.
 */
function buildQuestionItem(question, displayNumber) {
    const card = document.createElement('div');
    card.className = 'bg-white p-5 rounded-xl shadow-sm border border-slate-200';
    card.dataset.questionId = question.questionId;

    const optionsHtml = (question.options ?? []).map(opt => {
        const isCorrect = opt.isCorrect === true;
        return `
            <div class="flex items-center gap-2 px-3 py-2 rounded-lg ${isCorrect ? 'bg-green-50 border border-green-200' : 'bg-slate-50 border border-slate-100'}">
                <span class="text-xs font-bold ${isCorrect ? 'text-green-700' : 'text-slate-500'}">${opt.optionLabel}</span>
                <span class="text-sm ${isCorrect ? 'text-green-800 font-medium' : 'text-slate-700'}">${opt.optionText}</span>
                ${isCorrect ? '<span class="material-symbols-outlined text-[16px] text-green-600 ml-auto">check_circle</span>' : ''}
            </div>
        `;
    }).join('');

    card.innerHTML = `
        <p class="font-semibold text-slate-800 mb-3">${displayNumber}. ${question.questionText}</p>
        <div class="space-y-1.5">${optionsHtml}</div>
    `;

    return card;
}

/**
 * Builds one option input row (radio + label + text input) for the
 * question creation modal.
 */
function buildOptionInputRow(label) {
    const row = document.createElement('div');
    row.className = 'flex items-center gap-2';
    row.innerHTML = `
        <input type="radio" name="correctOption" value="${label}" class="w-4 h-4 accent-brand-600" />
        <span class="text-sm font-semibold text-slate-600 w-5">${label}</span>
        <input type="text" data-option-label="${label}" placeholder="Option ${label} text"
            class="flex-1 border border-slate-200 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-brand-500 focus:border-brand-500 outline-none" />
    `;
    return row;
}

/**
 * Opens the question creation modal, resetting all fields.
 */
function openQuestionCreateModal() {
    const modal = document.getElementById('question-create-modal');
    const questionTextInput = document.getElementById('questionTextInput');
    const optionsContainer = document.getElementById('optionInputsContainer');

    if (!modal || !questionTextInput || !optionsContainer) {
        console.warn('Question create modal elements not found in DOM.');
        return;
    }

    questionTextInput.value = '';
    optionsContainer.innerHTML = '';
    ['A', 'B', 'C', 'D'].forEach(label => {
        optionsContainer.appendChild(buildOptionInputRow(label));
    });

    modal.classList.remove('hidden');
    modal.classList.add('flex');
}

/**
 * Closes the question creation modal.
 */
function closeQuestionCreateModal() {
    const modal = document.getElementById('question-create-modal');
    if (modal) {
        modal.classList.add('hidden');
        modal.classList.remove('flex');
    }
}

/**
 * Collects the question form fields, validates them, and submits
 * to POST /trainer/question/create.
 */
async function saveNewQuestion(token) {
    if (currentQuestionsAssessmentId === null) {
        console.warn('No active assessment context for saving a question.');
        return;
    }

    const questionText = document.getElementById('questionTextInput').value.trim();
    const optionInputs = document.querySelectorAll('#optionInputsContainer input[type="text"]');
    const correctRadio = document.querySelector('input[name="correctOption"]:checked');

    if (!questionText) {
        showToast('Question text is required.', 'error');
        return;
    }

    const options = Array.from(optionInputs).map(input => ({
        label: input.dataset.optionLabel,
        text: input.value.trim(),
        isCorrect: correctRadio ? correctRadio.value === input.dataset.optionLabel : false
    }));

    if (options.some(opt => !opt.text)) {
        showToast('All 4 options must have text.', 'error');
        return;
    }

    if (!correctRadio) {
        showToast('Select which option is correct.', 'error');
        return;
    }

    const saveBtn = document.getElementById('saveQuestionBtn');
    const originalText = saveBtn.textContent;
    saveBtn.disabled = true;
    saveBtn.textContent = 'Saving...';

    try {
        const endpoint = `${window.base}/physio/trainer/question/create`;

        const response = await fetch(endpoint, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            credentials: 'include',
            body: JSON.stringify({
                assessmentId: currentQuestionsAssessmentId,
                questionText: questionText,
                options: options
            })
        });

        const result = await response.json();

        if (response.ok && result.status === 'success') {
            showToast('Question created successfully.', 'success');
            closeQuestionCreateModal();
            loadQuestionsForAssessment(currentQuestionsAssessmentId, token);
        } else {
            showToast(result.message || 'Failed to create question.', 'error');
        }

    } catch (error) {
        console.error('Error creating question:', error);
        showToast('Failed to create question.', 'error');
    } finally {
        saveBtn.disabled = false;
        saveBtn.textContent = originalText;
    }
}

// Wire up question management controls once DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    const backBtn = document.getElementById('backToManageAssessmentBtn');
    const addQuestionBtn = document.getElementById('addQuestionBtn');
    const closeModalBtn = document.getElementById('closeQuestionCreateModalBtn');
    const cancelModalBtn = document.getElementById('cancelQuestionCreateBtn');
    const modalBackdrop = document.getElementById('question-create-modal-backdrop');
    const saveQuestionBtn = document.getElementById('saveQuestionBtn');

    if (backBtn) backBtn.addEventListener('click', hideManageQuestionsPage);
    if (addQuestionBtn) addQuestionBtn.addEventListener('click', openQuestionCreateModal);
    if (closeModalBtn) closeModalBtn.addEventListener('click', closeQuestionCreateModal);
    if (cancelModalBtn) cancelModalBtn.addEventListener('click', closeQuestionCreateModal);
    if (modalBackdrop) modalBackdrop.addEventListener('click', closeQuestionCreateModal);
    if (saveQuestionBtn) {
        saveQuestionBtn.addEventListener('click', () => {
            const token = sessionStorage.getItem('token');
            saveNewQuestion(token);
        });
    }
});

function promptForAssessmentTitle() {
    return new Promise((resolve) => {
        const modal = document.getElementById('assessment-name-modal');
        const backdrop = document.getElementById('assessment-name-modal-backdrop');
        const input = document.getElementById('assessmentNameInput');
        const confirmBtn = document.getElementById('assessmentNameConfirmBtn');
        const cancelBtn = document.getElementById('assessmentNameCancelBtn');

        if (!modal || !input || !confirmBtn || !cancelBtn) {
            console.warn('Assessment name prompt modal elements not found in DOM.');
            resolve(null);
            return;
        }

        input.value = '';
        modal.classList.remove('hidden');
        modal.classList.add('flex');
        input.focus();

        function cleanup(result) {
            modal.classList.add('hidden');
            modal.classList.remove('flex');
            confirmBtn.removeEventListener('click', onConfirm);
            cancelBtn.removeEventListener('click', onCancel);
            backdrop.removeEventListener('click', onCancel);
            input.removeEventListener('keydown', onKeydown);
            resolve(result);
        }

        function onConfirm() {
            const value = input.value.trim();
            cleanup(value.length > 0 ? value : null);
        }

        function onCancel() {
            cleanup(null);
        }

        function onKeydown(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                onConfirm();
            } else if (e.key === 'Escape') {
                onCancel();
            }
        }

        confirmBtn.addEventListener('click', onConfirm);
        cancelBtn.addEventListener('click', onCancel);
        backdrop.addEventListener('click', onCancel);
        input.addEventListener('keydown', onKeydown);
    });
}

async function createNewAssessment(courseId, token) {
    const title = await promptForAssessmentTitle();
    if (!title) return;

    const endpoint = `${window.base}/physio/trainer/assessment/create?courseId=${courseId}&title=${encodeURIComponent(title)}`;

    try {
        const response = await fetch(endpoint, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${token}` },
            credentials: 'include'
        });

        const result = await response.json();

        if (response.ok && result.status === 'success') {
            showToast('Assessment created successfully.', 'success');
            loadAssessmentsForCourse(courseId, token);
        } else {
            showToast(result.message || 'Failed to create assessment.', 'error');
        }

    } catch (error) {
        console.error('Error creating assessment:', error);
        showToast('Failed to create assessment.', 'error');
    }
}

document.addEventListener('DOMContentLoaded', () => {
    const backBtn = document.getElementById('backToAssessmentsBtn');
    const addBtn = document.getElementById('addAssessmentBtn');

    if (backBtn) {
        backBtn.addEventListener('click', hideAssessmentManagementPage);
    }

    if (addBtn) {
        addBtn.addEventListener('click', () => {
            const token = sessionStorage.getItem('token');
            if (currentAssessmentCourseId !== null) {
                createNewAssessment(currentAssessmentCourseId, token);
            }
        });
    }

    document.querySelectorAll('input[name="page"]').forEach(radio => {
        radio.addEventListener('change', () => {
            hideManageQuestionsPage();
            hideAssessmentManagementPage();
        });
    });
});