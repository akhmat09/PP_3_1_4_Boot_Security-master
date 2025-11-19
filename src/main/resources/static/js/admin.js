console.log('=== ADMIN JS LOADED ===');

let currentEditingUserId = null;
let allRoles = [];

document.addEventListener('DOMContentLoaded', function() {
    console.log('DOM loaded - starting initialization');
    initializeApp();
    setupEventListeners();
});

function setupEventListeners() {
    document.getElementById('newUserBtn').addEventListener('click', openCreateUserForm);
    document.getElementById('newUserHeaderBtn').addEventListener('click', openCreateUserForm);
    document.getElementById('reloadUsersBtn').addEventListener('click', loadAllUsers);
    document.getElementById('saveUserBtn').addEventListener('click', saveUser);
    document.getElementById('confirmDeleteBtn').addEventListener('click', function() {
        if (window.userToDelete) {
            deleteUser(window.userToDelete);
        }
    });
}

async function initializeApp() {
    console.log('Initializing app...');
    await loadCurrentUser();
    await loadAllRoles();
    await loadAllUsers();
}

async function loadCurrentUser() {
    try {
        const response = await fetch('/api/current-user');
        if (response.ok) {
            const user = await response.json();
            document.getElementById('currentUserInfo').textContent =
                `${user.email} with roles: ${user.roles.map(role => role.name.replace('ROLE_', '')).join(' ')}`;
        }
    } catch (error) {
        console.error('Error loading current user:', error);
    }
}

async function loadAllUsers() {
    showLoading(true);
    try {
        const response = await fetch('/api/users');
        if (response.ok) {
            const users = await response.json();
            renderUsersTable(users);
        }
    } catch (error) {
        console.error('Error loading users:', error);
        alert('Error loading users');
    } finally {
        showLoading(false);
    }
}

async function loadAllRoles() {
    console.log('Loading roles from API...');
    try {
        const response = await fetch('/api/roles');
        console.log('Roles response status:', response.status);

        if (response.ok) {
            allRoles = await response.json();
            console.log('Roles loaded:', allRoles);
            renderRolesCheckboxes();
        } else {
            console.error('Failed to load roles');
            useDefaultRoles();
        }
    } catch (error) {
        console.error('Error loading roles:', error);
        useDefaultRoles();
    }
}

function useDefaultRoles() {
    console.log('Using default roles');
    allRoles = [
        {id: 1, name: 'ROLE_ADMIN'},
        {id: 2, name: 'ROLE_USER'}
    ];
    renderRolesCheckboxes();
}

function renderUsersTable(users) {
    const tbody = document.getElementById('usersTableBody');
    if (!tbody) {
        console.error('usersTableBody not found');
        return;
    }

    tbody.innerHTML = '';

    users.forEach(user => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${user.id}</td>
            <td>${user.firstName}</td>
            <td>${user.lastName}</td>
            <td>${user.age}</td>
            <td>${user.email}</td>
            <td>
                ${user.roles.map(role =>
            `<span class="badge bg-primary me-1">${role.name.replace('ROLE_', '')}</span>`
        ).join('')}
            </td>
            <td>
                <button class="btn btn-outline-primary btn-sm edit-user-btn" data-user-id="${user.id}">
                    Edit
                </button>
            </td>
            <td>
                <button class="btn btn-outline-danger btn-sm delete-user-btn" data-user-id="${user.id}" data-user-name="${user.firstName} ${user.lastName}">
                    Delete
                </button>
            </td>
        `;
        tbody.appendChild(row);
    });

    // Add event listeners to dynamically created buttons
    document.querySelectorAll('.edit-user-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            const userId = this.getAttribute('data-user-id');
            openEditUserForm(userId);
        });
    });

    document.querySelectorAll('.delete-user-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            const userId = this.getAttribute('data-user-id');
            const userName = this.getAttribute('data-user-name');
            openDeleteUserModal(userId, userName);
        });
    });
}

function renderRolesCheckboxes() {
    const container = document.getElementById('rolesContainer');
    if (!container) {
        console.error('rolesContainer not found!');
        return;
    }

    console.log('Rendering roles checkboxes for:', allRoles);

    let html = '';
    allRoles.forEach(role => {
        html += `
            <div class="form-check">
                <input class="form-check-input" type="checkbox" 
                       id="role_${role.id}" value="${role.id}">
                <label class="form-check-label" for="role_${role.id}">
                    ${role.name.replace('ROLE_', '')}
                </label>
            </div>
        `;
    });

    container.innerHTML = html;
    console.log('Roles checkboxes rendered successfully');
}

function openCreateUserForm() {
    console.log('Opening create user form');
    currentEditingUserId = null;
    document.getElementById('userModalLabel').textContent = 'Add new user';
    document.getElementById('saveUserBtn').textContent = 'Add new user';
    document.getElementById('passwordHelp').classList.add('d-none');
    document.getElementById('password').required = true;

    resetUserForm();

    if (!allRoles || allRoles.length === 0) {
        loadAllRoles();
    }
}

async function openEditUserForm(userId) {
    try {
        const response = await fetch(`/api/users/${userId}`);
        if (response.ok) {
            const user = await response.json();

            currentEditingUserId = user.id;
            document.getElementById('userModalLabel').textContent = 'Edit user';
            document.getElementById('saveUserBtn').textContent = 'Edit';
            document.getElementById('passwordHelp').classList.remove('d-none');
            document.getElementById('password').required = false;

            document.getElementById('userId').value = user.id;
            document.getElementById('firstName').value = user.firstName || '';
            document.getElementById('lastName').value = user.lastName || '';
            document.getElementById('age').value = user.age || '';
            document.getElementById('email').value = user.email || '';
            document.getElementById('username').value = user.username || '';
            document.getElementById('password').value = '';

            const roleCheckboxes = document.querySelectorAll('#rolesContainer input[type="checkbox"]');
            roleCheckboxes.forEach(checkbox => {
                const isChecked = user.roles.some(role => role.id == checkbox.value);
                checkbox.checked = isChecked;
            });

            // Show modal
            const modal = new bootstrap.Modal(document.getElementById('userModal'));
            modal.show();
        }
    } catch (error) {
        console.error('Error loading user for edit:', error);
        alert('Error loading user data');
    }
}

function openDeleteUserModal(userId, userName) {
    document.getElementById('deleteUserName').textContent = userName;
    window.userToDelete = userId;

    // Show modal
    const modal = new bootstrap.Modal(document.getElementById('deleteModal'));
    modal.show();
}

async function saveUser() {
    const selectedRoleIds = getSelectedRoleIds();

    const userData = {
        firstName: document.getElementById('firstName').value,
        lastName: document.getElementById('lastName').value,
        age: parseInt(document.getElementById('age').value),
        email: document.getElementById('email').value,
        username: document.getElementById('username').value,
        password: document.getElementById('password').value,
        roleIds: selectedRoleIds
    };

    console.log('Saving user with data:', userData);

    if (!userData.firstName || !userData.lastName || !userData.email || !userData.username) {
        alert('Please fill all required fields');
        return;
    }

    if (!currentEditingUserId && !userData.password) {
        alert('Password is required for new users');
        return;
    }

    try {
        let response;
        if (currentEditingUserId) {
            userData.id = currentEditingUserId;
            response = await fetch(`/api/users/${currentEditingUserId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(userData)
            });
        } else {
            response = await fetch('/api/users', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(userData)
            });
        }

        if (response.ok) {
            const modal = bootstrap.Modal.getInstance(document.getElementById('userModal'));
            modal.hide();
            await loadAllUsers();
            alert(`User ${currentEditingUserId ? 'updated' : 'created'} successfully`);
        } else {
            const errorText = await response.text();
            alert('Failed to save user: ' + errorText);
        }
    } catch (error) {
        console.error('Error saving user:', error);
        alert('Error saving user: ' + error.message);
    }
}

function getSelectedRoleIds() {
    const selectedRoleIds = [];
    document.querySelectorAll('#rolesContainer input[type="checkbox"]:checked').forEach(checkbox => {
        selectedRoleIds.push(parseInt(checkbox.value));
    });
    return selectedRoleIds;
}

function resetUserForm() {
    document.getElementById('userForm').reset();
    document.querySelectorAll('#rolesContainer input[type="checkbox"]').forEach(checkbox => {
        checkbox.checked = false;
    });
}

function showLoading(show) {
    const spinner = document.getElementById('loadingSpinner');
    if (spinner) {
        spinner.classList.toggle('d-none', !show);
    }
}

async function deleteUser(userId) {
    try {
        const response = await fetch(`/api/users/${userId}`, {
            method: 'DELETE'
        });

        if (response.ok) {
            const modal = bootstrap.Modal.getInstance(document.getElementById('deleteModal'));
            modal.hide();
            await loadAllUsers();
            alert('User deleted successfully');
            window.userToDelete = null;
        } else {
            alert('Failed to delete user');
        }
    } catch (error) {
        console.error('Error deleting user:', error);
        alert('Error deleting user');
    }
}