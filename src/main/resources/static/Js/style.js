// Book Form page JavaScript
const currentBookId = document.querySelector('input[name="id"]')?.value || null;

// Validation status tracker
const validationStatus = {
    title: true,
    isbn: true,
    libraryCode: true
};

// Function to validate field for duplicates
async function validateField(fieldType, value) {
    if (!value || value.trim() === '') {
        clearValidation(fieldType + 'Field');
        return;
    }

    const spinner = document.getElementById(fieldType + 'Spinner');
    const field = document.getElementById(fieldType + 'Field');

    // Show spinner
    spinner.style.display = 'inline-block';

    try {
        // Make API call to check for duplicates
        const response = await fetch(`/books/api/validate-${fieldType}?value=${encodeURIComponent(value)}&currentId=${currentBookId || ''}`);
        const isDuplicate = await response.json();

        // Hide spinner
        spinner.style.display = 'none';

        if (isDuplicate) {
            // Show duplicate error
            field.classList.add('duplicate');
            validationStatus[fieldType] = false;
        } else {
            // Clear duplicate error
            field.classList.remove('duplicate');
            validationStatus[fieldType] = true;
        }

        // Update save button state
        updateSaveButton();

    } catch (error) {
        console.error('Validation error:', error);
        // Hide spinner on error
        spinner.style.display = 'none';
        // Assume valid on error
        field.classList.remove('duplicate');
        validationStatus[fieldType] = true;
        updateSaveButton();
    }
}

// Function to clear validation styling
function clearValidation(fieldId) {
    const field = document.getElementById(fieldId);
    if (field) {
        field.classList.remove('duplicate');
        // Reset validation status when user starts typing
        const fieldType = fieldId.replace('Field', '');
        if (validationStatus.hasOwnProperty(fieldType)) {
            validationStatus[fieldType] = true;
            updateSaveButton();
        }
    }
}

// Function to update save button state
function updateSaveButton() {
    const saveBtn = document.getElementById('saveBtn');
    const hasErrors = !validationStatus.title || !validationStatus.isbn || !validationStatus.libraryCode;

    if (hasErrors) {
        saveBtn.disabled = true;
        saveBtn.style.backgroundColor = '#6c757d';
        saveBtn.style.cursor = 'not-allowed';
        saveBtn.title = 'Please fix duplicate data errors before saving';
    } else {
        saveBtn.disabled = false;
        saveBtn.style.backgroundColor = '';
        saveBtn.style.cursor = '';
        saveBtn.title = '';
    }
}

// Function to toggle custom category input
function toggleCustomCategory() {
    const select = document.getElementById('bookCategorySelect');
    const customDiv = document.getElementById('customCategoryDiv');
    const customInput = document.getElementById('customCategoryInput');

    if (select.value === 'ADD_NEW') {
        customDiv.style.display = 'block';
        customInput.focus();
        customInput.required = true;
    } else {
        customDiv.style.display = 'none';
        customInput.value = '';
        customInput.required = false;
    }
}

// Function to update category value when custom category is entered
function updateCategoryValue() {
    const customInput = document.getElementById('customCategoryInput');
    const select = document.getElementById('bookCategorySelect');

    if (customInput.value.trim()) {
        // Create new option with the custom category name
        const newOption = document.createElement('option');
        newOption.value = customInput.value.trim();
        newOption.text = customInput.value.trim();
        newOption.selected = true;

        // Insert before the "Add New Category" option
        const addNewOption = select.querySelector('option[value="ADD_NEW"]');
        select.insertBefore(newOption, addNewOption);

        // Hide the custom input
        document.getElementById('customCategoryDiv').style.display = 'none';
        customInput.required = false;
    }
}

// Function to update available copies based on total copies
function updateAvailableCopies() {
    const totalCopies = document.getElementById('totalCopies').value;
    const availableCopiesField = document.getElementById('availableCopies');

    if (totalCopies && parseInt(totalCopies) >= 0) {
        // For new books, available copies = total copies
        // For existing books, you might want to maintain the current available copies
        const currentAvailable = parseInt(availableCopiesField.value) || 0;
        const isNewBook = !availableCopiesField.value || currentAvailable === 0;

        if (isNewBook) {
            availableCopiesField.value = totalCopies;
        } else {
            // For existing books, ensure available copies don't exceed total copies
            if (currentAvailable > parseInt(totalCopies)) {
                availableCopiesField.value = totalCopies;
            }
        }
    }
}

// Prevent form submission if there are validation errors
document.getElementById('bookForm').addEventListener('submit', function(e) {
    const hasErrors = !validationStatus.title || !validationStatus.isbn || !validationStatus.libraryCode;
    if (hasErrors) {
        e.preventDefault();
        alert('Please fix duplicate data errors before saving the book.');
        return false;
    }
});

// Initialize form when page loads
document.addEventListener('DOMContentLoaded', function() {
    // Set initial available copies if total copies is already set
    updateAvailableCopies();

    // Initial validation for existing values (edit mode)
    const titleInput = document.querySelector('input[name="title"]');
    const isbnInput = document.querySelector('input[name="isbn"]');
    const libraryCodeInput = document.querySelector('input[name="libraryCode"]');
    const authorInput = document.querySelector('input[name="author"]');
    const publicationYearInput = document.querySelector('input[name="publicationYear"]');
    const bookCategorySelect = document.querySelector('select[name="bookCategory"]');
    const totalCopiesInput = document.querySelector('input[name="totalCopies"]');

    // Validate existing values for duplicates (edit mode)
    if (titleInput && titleInput.value) {
        validateField('title', titleInput.value);
    }
    if (isbnInput && isbnInput.value) {
        validateField('isbn', isbnInput.value);
    }
    if (libraryCodeInput && libraryCodeInput.value) {
        validateField('libraryCode', libraryCodeInput.value);
    }

    // Validate required fields on load
    validateRequired('title', titleInput ? titleInput.value : '');
    validateRequired('author', authorInput ? authorInput.value : '');
    validateRequired('isbn', isbnInput ? isbnInput.value : '');
    validateRequired('libraryCode', libraryCodeInput ? libraryCodeInput.value : '');
    validateRequired('publicationYear', publicationYearInput ? publicationYearInput.value : '');
    validateRequired('bookCategory', bookCategorySelect ? bookCategorySelect.value : '');
    validateRequired('totalCopies', totalCopiesInput ? totalCopiesInput.value : '');
});

// Member Form page JavaScript
document.addEventListener('DOMContentLoaded', function() {
    const nameInput = document.getElementById('name');
    const emailInput = document.getElementById('email');
    const phoneInput = document.getElementById('phone');
    const saveBtn = document.getElementById('saveBtn');
    const memberId = document.getElementById('memberId').value;

    let validationState = {
        name: true,
        email: true,
        phone: true
    };

    // Debounce function to limit API calls
    function debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    }

    // Generic validation function
    function validateField(field, endpoint, validationElementId, spinnerId) {
        const value = field.value.trim();
        const validationElement = document.getElementById(validationElementId);
        const spinner = document.getElementById(spinnerId);

        if (!value) {
            field.classList.remove('error', 'success');
            validationElement.style.display = 'none';
            validationState[field.id] = true;
            updateSaveButton();
            return;
        }

        // Show spinner
        spinner.style.display = 'block';

        // Make API call
        const url = `/members/api/validate-${endpoint}?value=${encodeURIComponent(value)}&currentId=${memberId}`;

        fetch(url)
            .then(response => response.json())
            .then(isDuplicate => {
                spinner.style.display = 'none';

                if (isDuplicate) {
                    field.classList.remove('success');
                    field.classList.add('error');
                    validationElement.className = 'validation-message error';
                    validationElement.textContent = `❌ This ${field.name} is already registered!`;
                    validationElement.style.display = 'block';
                    validationState[field.id] = false;
                } else {
                    field.classList.remove('error');
                    field.classList.add('success');
                    validationElement.className = 'validation-message success';
                    validationElement.textContent = `✅ This ${field.name} is available!`;
                    validationElement.style.display = 'block';
                    validationState[field.id] = true;
                }
                updateSaveButton();
            })
            .catch(error => {
                console.error('Validation error:', error);
                spinner.style.display = 'none';
                field.classList.remove('error', 'success');
                validationElement.style.display = 'none';
                validationState[field.id] = true;
                updateSaveButton();
            });
    }

    // Update save button state
    function updateSaveButton() {
        const isFormValid = Object.values(validationState).every(state => state === true);
        saveBtn.disabled = !isFormValid;
    }

    // Add event listeners with debouncing
    const debouncedValidateName = debounce((e) => {
        validateField(e.target, 'name', 'nameValidation', 'nameSpinner');
    }, 500);

    const debouncedValidateEmail = debounce((e) => {
        validateField(e.target, 'email', 'emailValidation', 'emailSpinner');
    }, 500);

    const debouncedValidatePhone = debounce((e) => {
        if (e.target.value.trim()) { // Only validate if phone has value (it's optional)
            validateField(e.target, 'phone', 'phoneValidation', 'phoneSpinner');
        } else {
            // Clear validation for empty phone field
            e.target.classList.remove('error', 'success');
            document.getElementById('phoneValidation').style.display = 'none';
            validationState.phone = true;
            updateSaveButton();
        }
    }, 500);

    nameInput.addEventListener('input', debouncedValidateName);
    emailInput.addEventListener('input', debouncedValidateEmail);
    phoneInput.addEventListener('input', debouncedValidatePhone);

    // Form submission validation
    document.getElementById('memberForm').addEventListener('submit', function(e) {
        const isFormValid = Object.values(validationState).every(state => state === true);
        if (!isFormValid) {
            e.preventDefault();
            alert('Please fix validation errors before submitting!');
        }
    });

    // Initial validation for edit mode
    if (memberId && nameInput.value.trim()) {
        validateField(nameInput, 'name', 'nameValidation', 'nameSpinner');
    }
    if (memberId && emailInput.value.trim()) {
        validateField(emailInput, 'email', 'emailValidation', 'emailSpinner');
    }
    if (memberId && phoneInput.value.trim()) {
        validateField(phoneInput, 'phone', 'phoneValidation', 'phoneSpinner');
    }
});
// Dismiss alert function
function dismissAlert(button) {
    const alert = button.closest('.alert');
    alert.classList.add('fade-out');
    setTimeout(() => {
        alert.remove();
    }, 300);
}

// Auto-dismiss alerts with progress bar after 4 seconds
document.querySelectorAll('.alert-success .alert-progress').forEach(progressBar => {
    const alert = progressBar.closest('.alert');
    setTimeout(() => {
        dismissAlert(alert.querySelector('.alert-close'));
    }, 4000);
});

// Function to create and show alerts dynamically
function showAlert(type, message) {
    const container = document.getElementById('alert-container');

    const icons = {
        success: '✓',
        error: '!',
        warning: '⚠',
        info: 'ℹ'
    };

    const titles = {
        success: 'Success!',
        error: 'Error!',
        warning: 'Warning!',
        info: 'Information'
    };

    const alertHTML = `
                <div class="alert alert-${type}">
                    <div class="icon">${icons[type]}</div>
                    <div class="alert-content">
                        <div class="alert-title">${titles[type]}</div>
                        <div class="alert-description">${message}</div>
                    </div>
                    <button class="alert-close" onclick="dismissAlert(this)">×</button>
                    ${type === 'success' ? '<div class="alert-progress"></div>' : ''}
                </div>
            `;

    container.insertAdjacentHTML('beforeend', alertHTML);

    // Auto-dismiss success alerts
    if (type === 'success') {
        const newAlert = container.lastElementChild;
        setTimeout(() => {
            dismissAlert(newAlert.querySelector('.alert-close'));
        }, 4000);
    }
}

// Close alerts when clicking outside
document.addEventListener('click', function(e) {
    if (e.target.classList.contains('alert')) {
        dismissAlert(e.target.querySelector('.alert-close'));
    }
});

// Index page JavaScript
    class SMSNotificationSystem {
    constructor() {
    this.container = document.getElementById('notificationContainer');
    this.init();
}

    init() {
    // Check for notifications every 30 seconds
    this.checkNotifications();
    setInterval(() => {
    this.checkNotifications();
}, 30000);
}

    async checkNotifications() {
    try {
    // Simulate API call - replace with actual API endpoint
    const response = await this.simulateAPICall();
    if (response.count > 0) {
    this.showNotification(response.type, response.message, response.count);
}
} catch (error) {
    console.error('Error checking notifications:', error);
}
}

    // Simulate API call - replace with actual fetch to /api/notifications
    simulateAPICall() {
    return new Promise((resolve) => {
    // Simulate random overdue loans for demo
    const overdueCount = Math.floor(Math.random() * 6); // 0-5 overdue loans
    setTimeout(() => {
    if (overdueCount > 0) {
    resolve({
    type: 'warning',
    message: `មានការខ្ចីសៀវភៅចំនួន ${overdueCount} ដែលហួសកំណត់ពេល!`,
    count: overdueCount
});
} else {
    resolve({
    type: 'success',
    message: 'មិនមានការខ្ចីហួសកំណត់ពេលទេ',
    count: 0
});
}
}, 500);
});
}

    showNotification(type, message, count) {
    const notification = document.createElement('div');
    notification.className = `sms-notification ${type}`;

    const icon = type === 'warning' ? '⚠️' : '✅';
    const title = type === 'warning' ? 'ការជូនដំណឹង' : 'ស្ថានភាពល្អ';

    notification.innerHTML = `
                    <button class="close-btn">&times;</button>
                    <div class="notification-header">
                        <div class="notification-icon ${type}">${icon}</div>
                        <div class="notification-title">${title}</div>
                    </div>
                    <div class="notification-message">${message}</div>
                `;

    this.container.appendChild(notification);

    // Show animation
    setTimeout(() => {
    notification.classList.add('show');
}, 100);

    // Auto hide after 5 seconds
    setTimeout(() => {
    this.hideNotification(notification);
}, 5000);

    // Close button functionality
    const closeBtn = notification.querySelector('.close-btn');
    closeBtn.addEventListener('click', () => {
    this.hideNotification(notification);
});
}

    hideNotification(notification) {
    notification.classList.add('hide');
    setTimeout(() => {
    if (notification.parentNode) {
    notification.parentNode.removeChild(notification);
}
}, 500);
}
}

    // Initialize the notification system when page loads
    document.addEventListener('DOMContentLoaded', () => {
    new SMSNotificationSystem();
});