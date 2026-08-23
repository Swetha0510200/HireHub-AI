/**
 * HireHub Recruiter JavaScript
 */
document.addEventListener('DOMContentLoaded', () => {
    // Application filter by status
    const statusFilter = document.getElementById('applicantStatusFilter');
    if (statusFilter) {
        statusFilter.addEventListener('change', () => {
            const val = statusFilter.value.toLowerCase();
            const rows = document.querySelectorAll('.applicant-row');
            rows.forEach(row => {
                const status = row.getAttribute('data-status').toLowerCase();
                if (val === 'all' || status === val) {
                    row.style.display = '';
                } else {
                    row.style.display = 'none';
                }
            });
        });
    }

    // Confirmation for rejecting candidates
    document.querySelectorAll('.btn-confirm-reject').forEach(btn => {
        btn.addEventListener('click', (e) => {
            if (!confirm('Are you sure you want to mark this candidate as Rejected?')) {
                e.preventDefault();
            }
        });
    });
});
