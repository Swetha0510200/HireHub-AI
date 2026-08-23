/**
 * HireHub Admin JavaScript
 */
document.addEventListener('DOMContentLoaded', () => {
    // Confirmation on user/job deletion
    document.querySelectorAll('.btn-confirm-delete').forEach(btn => {
        btn.addEventListener('click', (e) => {
            if (!confirm('Are you sure you want to permanently delete this item? This action cannot be undone.')) {
                e.preventDefault();
            }
        });
    });
});
