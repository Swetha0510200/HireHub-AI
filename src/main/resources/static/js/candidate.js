/**
 * HireHub Candidate Portal JavaScript
 */
document.addEventListener('DOMContentLoaded', () => {
    // Save / Bookmark Toggle via AJAX or form
    document.querySelectorAll('.btn-bookmark').forEach(btn => {
        btn.addEventListener('click', async (e) => {
            const jobId = btn.getAttribute('data-job-id');
            const isSaved = btn.getAttribute('data-saved') === 'true';
            const endpoint = isSaved ? `/jobs/${jobId}/unsave` : `/jobs/${jobId}/save`;

            try {
                const response = await fetch(endpoint, {
                    method: 'POST',
                    headers: { 'X-Requested-With': 'XMLHttpRequest' }
                });
                if (response.ok) {
                    btn.setAttribute('data-saved', (!isSaved).toString());
                    btn.innerHTML = !isSaved ? '★ Bookmarked' : '☆ Save';
                    btn.classList.toggle('btn-primary');
                    btn.classList.toggle('btn-outline-primary');
                }
            } catch (err) {
                console.error('Bookmark toggle error:', err);
            }
        });
    });

    // Quick Apply Modal Opening
    const applyModal = document.getElementById('applyModal');
    if (applyModal) {
        document.querySelectorAll('.btn-open-apply-modal').forEach(btn => {
            btn.addEventListener('click', () => {
                const jobId = btn.getAttribute('data-job-id');
                const jobTitle = btn.getAttribute('data-job-title');
                const form = document.getElementById('modalApplyForm');
                if (form && jobId) {
                    form.action = `/jobs/${jobId}/apply`;
                }
                const titleElem = document.getElementById('modalJobTitle');
                if (titleElem && jobTitle) {
                    titleElem.textContent = jobTitle;
                }
            });
        });
    }
});
