async function loadManagementDashboard(token) {
    const endpoint = `${window.base}/physio/management/dashboard/summary`;

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

        document.getElementById('statTrainingCompletion').textContent = `${d.trainingCompletionPct}%`;
        document.getElementById('statActiveParticipants').textContent = d.activeParticipants;
        document.getElementById('statMonthlyRevenue').textContent = `RM ${Number(d.monthlyRevenue).toFixed(2)}`;
        document.getElementById('statAvgFeedback').textContent = `${Number(d.avgFeedback).toFixed(1)}/5`;
        document.getElementById('statAttendanceRate').textContent = `${d.attendanceRatePct}%`;

        const changeEl = document.getElementById('statEnrolmentsChange');
        if (d.enrolmentsThisMonthChangePct === null) {
            changeEl.textContent = 'Not available';
            changeEl.className = 'text-lg font-bold text-slate-400 mt-1';
        } else {
            changeEl.textContent = `${d.enrolmentsThisMonthChangePct > 0 ? '+' : ''}${d.enrolmentsThisMonthChangePct}%`;
        }

    } catch (error) {
        console.error('Failed to fetch management dashboard:', error);
    }
}

async function loadParticipantStats(token) {
    const endpoint = `${window.base}/physio/management/participant-stats/summary`;

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

        document.getElementById('statTotalParticipants').textContent = d.totalParticipants;
        document.getElementById('statActiveParticipants').textContent = d.activeParticipants;
        document.getElementById('statInactiveParticipants').textContent = d.inactiveParticipants;
        document.getElementById('statCompletedAtLeastOne').textContent = d.completedAtLeastOneCourse;

        const container = document.getElementById('enrolmentByCourseContainer');
        container.innerHTML = '';

        if (!d.enrollmentByCourse || d.enrollmentByCourse.length === 0) {
            container.innerHTML = `<p class="text-sm text-slate-500">No enrolment data yet.</p>`;
            return;
        }

        d.enrollmentByCourse.forEach(row => {
            const item = document.createElement('div');
            item.className = 'flex justify-between items-center p-3 bg-slate-50 rounded-lg';
            item.innerHTML = `
                <span class="font-medium text-slate-700">${row.course_name}</span>
                <span class="font-bold text-slate-800 bg-white px-3 py-1 rounded-md shadow-sm">${row.enrollment_count}</span>
            `;
            container.appendChild(item);
        });

    } catch (error) {
        console.error('Failed to fetch participant stats:', error);
    }
}

document.addEventListener('DOMContentLoaded', () => {
    const token = sessionStorage.getItem('token');
    if (!token) return;
    loadManagementDashboard(token);
    loadParticipantStats(token);
});