document.addEventListener('DOMContentLoaded', () => {
    const liveStatus = document.getElementById('liveStatus');
    const atsScoreValue = document.getElementById('atsScoreValue');
    const applicationsValue = document.getElementById('applicationsValue');
    const interviewValue = document.getElementById('interviewValue');

    if (!liveStatus) {
        return;
    }

    const updateDashboard = (payload) => {
        if (payload && payload.status === 'online') {
            liveStatus.classList.remove('disconnected');
            liveStatus.classList.add('connected');
            liveStatus.innerHTML = '<span class="live-dot"></span> Live updates online';
        }

        if (atsScoreValue && payload && payload.atsScore !== undefined) {
            atsScoreValue.textContent = payload.atsScore;
        }

        if (applicationsValue && payload && payload.applicationsCount !== undefined) {
            applicationsValue.textContent = payload.applicationsCount;
        }

        if (interviewValue && payload && payload.interviewCount !== undefined) {
            interviewValue.textContent = payload.interviewCount;
        }
    };

    fetch('/api/dashboard/summary')
        .then((response) => response.json())
        .then(updateDashboard)
        .catch(() => {
            liveStatus.classList.add('connected');
            liveStatus.innerHTML = '<span class="live-dot"></span> Live updates online';
        });
});

