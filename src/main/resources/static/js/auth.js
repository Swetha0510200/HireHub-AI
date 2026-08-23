/**
 * HireHub Authentication Form Helpers
 */
document.addEventListener('DOMContentLoaded', () => {
    // Password Confirmation Validation
    const regForm = document.querySelector('form[action="/register"]');
    if (regForm) {
        regForm.addEventListener('submit', (e) => {
            const pwd = document.getElementById('password') || document.querySelector('input[name="password"]');
            const confirm = document.getElementById('confirmPassword') || document.querySelector('input[name="confirmPassword"]');
            
            if (pwd && confirm && pwd.value !== confirm.value) {
                e.preventDefault();
                alert('Passwords do not match! Please check and try again.');
                confirm.focus();
            }
        });
    }

    // Role Tab Switcher in Registration
    const roleTabs = document.querySelectorAll('.role-tab-btn');
    const roleInput = document.getElementById('selectedRoleInput') || document.querySelector('input[name="role"]');
    const studentFields = document.querySelectorAll('.student-field');
    const recruiterFields = document.querySelectorAll('.recruiter-field');

    if (roleTabs.length > 0) {
        roleTabs.forEach(btn => {
            btn.addEventListener('click', () => {
                roleTabs.forEach(b => b.classList.remove('active', 'btn-primary'));
                roleTabs.forEach(b => b.classList.add('btn-outline-primary'));
                btn.classList.add('active', 'btn-primary');
                btn.classList.remove('btn-outline-primary');

                const role = btn.getAttribute('data-role');
                if (roleInput) roleInput.value = role;

                if (role === 'recruiter') {
                    studentFields.forEach(el => el.style.display = 'none');
                    recruiterFields.forEach(el => el.style.display = 'block');
                } else {
                    studentFields.forEach(el => el.style.display = 'block');
                    recruiterFields.forEach(el => el.style.display = 'none');
                }
            });
        });
    }
});
