<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="elms.model.Employee" %>
<%
    Employee employee = (Employee) session.getAttribute("loggedInEmployee");
    if (employee == null) {
        response.sendRedirect("/ELMS_3.0/Employee/EmployeeLogin.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="icon" type="image/png" href="/ELMS_3.0/imnsb_logo.png">
    <title>Edit Account - IMNSB Employee Leave Management System</title>
    <link rel="stylesheet" href="/ELMS_3.0/css/styles.css">
    <link rel="stylesheet" href="/ELMS_3.0/css/dashboard.css">
    <link rel="stylesheet" href="/ELMS_3.0/css/settings.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
</head>
<body>
    <div class="dashboard-container">
        <!-- Sidebar Navigation -->
        <nav class="sidebar" id="sidebar">
            <div class="sidebar-header">
                <div class="logo-container">
                    <img src="/ELMS_3.0/imnsb_logowbg.png" alt="IMNSB Logo" class="sidebar-logo">
                </div>
                <h3>IMNSB Employee</h3>
            </div>
            <ul class="sidebar-menu">
                <li>
                    <a href="/ELMS_3.0/Employee/EmployeeDashboard.jsp"><i class="fas fa-user"></i> <span>Profile</span></a>
                </li>
                <li>
                    <a href="/ELMS_3.0/LeaveApplicationController"><i class="fas fa-calendar-plus"></i> <span>Apply Leave</span></a>
                </li>
                <li>
                    <a href="<%= request.getContextPath() %>/leave-history"><i class="fas fa-history"></i> <span>Leave History</span></a>
                </li>
                <li class="active">
                    <a href="/ELMS_3.0/Employee/EmployeeSettings.jsp"><i class="fas fa-cog"></i> <span>Settings</span></a>
                </li>
                <li class="logout">
                    <a href="/ELMS_3.0/Employee/EmployeeLogin.jsp" id="logoutBtn"><i class="fas fa-sign-out-alt"></i> <span>Logout</span></a>
                </li>
            </ul>
        </nav>
        
        <!-- Main Content -->
        <main class="main-content" id="mainContent">
            <header class="content-header">
                <div class="header-left">
                    <button id="sidebarToggle" class="sidebar-toggle">
                        <i class="fas fa-bars"></i>
                    </button>
                    <h2>Edit Account Information</h2>
                </div>
               <div class="header-right">
                <span id="userName"><%= employee.getEmployeeName() %></span>
                <div class="user-avatar">
                    <%
                        String headerProfilePicPath = employee.getProfilePicturePath();
                        if (headerProfilePicPath != null && !headerProfilePicPath.isEmpty()) {
                    %>
                    <img src="/ELMS_3.0/<%= headerProfilePicPath %>" alt="User Avatar" id="headerAvatar">
                    <% } else { %>
                    <img src="/ELMS_3.0/defaultprofilepicture.jpg" alt="User Avatar" id="headerAvatar">
                    <% } %>
                </div>
            </div>
            </header>
            
            <div class="content-body">
            
            
            
                <div class="settings-container">
                    <!-- Success/Error Messages -->
                    <% String message = (String) request.getAttribute("message"); %>
                    <% String messageType = (String) request.getAttribute("messageType"); %>
                    <% if (message != null) { %>
                    <div class="alert alert-<%= messageType != null ? messageType : "info" %>" id="alertMessage">
                        <i class="fas fa-<%= "success".equals(messageType) ? "check-circle" : "exclamation-triangle" %>"></i>
                        <%= message %>
                    </div>
                    <% } %>
                    
                    <div class="settings-form-card">
                        <form id="editAccountForm" action="/ELMS_3.0/update-account" method="post" enctype="multipart/form-data">
                            
                            <!-- Profile Picture Section with Inline Preview -->
                            <div class="profile-picture-section">
                                <div class="profile-picture" id="profilePictureContainer">
                                    <% 
                                        String profilePicPath = employee.getProfilePicturePath();
                                        if (profilePicPath != null && !profilePicPath.isEmpty()) {
                                            
                                    %>
                                        <img src="/ELMS_3.0/<%= profilePicPath %>" alt="Profile Picture" id="profilePreview" 
                                             onerror="console.log('Image load error'); this.src='/ELMS_3.0/defaultprofilepicture.jpg';">
                                    <% } else { %>
                                        <img src="/ELMS_3.0/defaultprofilepicture.jpg" alt="Profile Picture" id="profilePreview">
                                    <% } %>
                                    <div class="profile-picture-overlay">
                                        <label for="profilePicture" class="upload-btn">
                                            <i class="fas fa-camera"></i>
                                            Change Photo
                                        </label>
                                        <!-- INLINE ONCHANGE - Guaranteed to work -->
                                        <input type="file" 
                                               id="profilePicture" 
                                               name="profile_picture_path" 
                                               accept="image/*" 
                                               style="display: none;"
                                               onchange="handleProfilePictureChange(this)">
                                    </div>
                                </div>
                                <p class="profile-hint">Click on the image to change your profile picture</p>
                                
                                <!-- Remove Profile Picture Button -->
                                <button type="button" class="remove-profile-btn" onclick="removeProfilePicture()" title="Remove profile picture">
                                    <i class="fas fa-trash"></i>
                                    Remove Photo
                                </button>
                                
                                <!-- Status indicator -->
                                <div id="profileStatus" style="margin-top: 10px; font-size: 12px; color: #666; text-align: center; min-height: 20px;"></div>
                            </div>

                            <!-- Hidden field for employee ID -->
                            <input type="hidden" name="employeeid" value="<%= employee.getEmployeeId() %>">
                            <!-- Hidden field to indicate if profile picture should be removed -->
                            <input type="hidden" name="remove_profile_picture" value="false" id="removeProfilePictureFlag">

                            <div class="form-group">
							    <label for="fullName">Full Name</label>
							    <input type="text" id="fullName" name="employeename" value="<%= employee.getEmployeeName() != null ? employee.getEmployeeName() : "" %>" required>
							    <div class="form-error" id="nameError"></div>
							</div>
                            <div class="form-group">
							    <label for="email">Email Address</label>
							    <input type="email" id="email" name="employeeemail" value="<%= employee.getEmployeeEmail() != null ? employee.getEmployeeEmail() : "" %>" required>
							    <div class="form-error" id="emailError"></div>
							</div>
                            
                            <div class="form-group">
							    <label for="phone">Mobile Number</label>
<input type="tel" id="phone" name="employeenophone" 
       value="<%= employee.getEmployeeNoPhone() != null ? employee.getEmployeeNoPhone() : "+60 " %>" 
       maxlength="17" required placeholder="+60 12-345 6789">
<div class="phone-format-help" id="phoneHelp">Format: +60 1x-xxx xxxx (Malaysian mobile number)</div>
<div class="form-error" id="phoneError"></div>
							</div>
                            
                            <div class="form-buttons">
                                <a href="/ELMS_3.0/Employee/EmployeeSettings.jsp" class="btn btn-secondary">Cancel</a>
                                <button type="submit" class="btn btn-primary" id="saveButton">Save Changes</button>
                            </div>
                        </form>
                    </div>
                </div>
           </div>
        </main>
        <div id="save-button-tooltip"></div>
    </div>

<script src="/ELMS_3.0/Employee/Employee.js"></script>

<!-- INLINE PROFILE PICTURE PREVIEW - Guaranteed to work -->
<script>
function handleProfilePictureChange(input) {
    console.log('🔥 EMPLOYEE PROFILE PICTURE CHANGE HANDLER CALLED!');
    
    const file = input.files[0];
    const preview = document.getElementById('profilePreview');
    const header = document.getElementById('headerAvatar');
    const status = document.getElementById('profileStatus');
    
    if (!file) {
        console.log('❌ No file selected');
        return;
    }
    
    console.log('📁 File selected: ' + file.name + ' (' + file.size + ' bytes)');
    
   
    
    // Validate file type
    if (!file.type.match(/^image\/(jpeg|jpg|png|gif)$/i)) {
        alert('Please select a valid image file (JPG, PNG, or GIF)');
        input.value = '';
        if (status) {
            status.innerHTML = '❌ Invalid file type';
            status.style.color = '#dc3545';
        }
        return;
    }
    
    // Validate file size (5MB max)
    if (file.size > 5 * 1024 * 1024) {
        alert('File size must be less than 5MB. Selected file is ' + formatFileSize(file.size));
        input.value = '';
        if (status) {
            status.innerHTML = '❌ File too large (' + formatFileSize(file.size) + ')';
            status.style.color = '#dc3545';
        }
        return;
    }
    
    console.log('✅ File validation passed');
    
    // Show loading state
    if (preview) {
        preview.style.opacity = '0.6';
        preview.style.filter = 'blur(1px)';
    }
    
    // Read the file
    const reader = new FileReader();
    
    reader.onload = function(e) {
        const imageDataUrl = e.target.result;
        
        console.log('✅ File read successfully, data URL length: ' + imageDataUrl.length);
        
        // Update main preview image
        if (preview) {
            preview.src = imageDataUrl;
            preview.style.opacity = '1';
            preview.style.filter = 'none';
            console.log('✅ Main preview updated');
        }
        
        // Update header avatar
        if (header) {
            header.src = imageDataUrl;
            console.log('✅ Header avatar updated');
        }
        
        
        
        // Show success notification
        showNotification('✅ Profile picture preview updated! Click "Save Changes" to apply.');
        
        console.log('🎉 Employee profile picture preview complete!');
    };
    
    reader.onerror = function() {
        console.log('❌ FileReader error');
        alert('Error reading the image file. Please try again.');
        
        // Reset states
        if (preview) {
            preview.style.opacity = '1';
            preview.style.filter = 'none';
        }
        
        if (status) {
            status.innerHTML = '❌ Error reading file';
            status.style.color = '#dc3545';
        }
        
        input.value = '';
    };
    
    // Start reading the file
    console.log('📖 Starting to read file...');
    reader.readAsDataURL(file);
}

function showNotification(message) {
    // Remove existing notification
    const existing = document.querySelector('.profile-notification');
    if (existing) {
        existing.remove();
    }
    
    // Create notification
    const notification = document.createElement('div');
    notification.className = 'profile-notification';
    notification.innerHTML = message;
    notification.style.cssText = 
        'position: fixed; top: 20px; right: 20px; ' +
        'background: #28a745; color: white; ' +
        'padding: 15px 20px; border-radius: 8px; ' +
        'font-size: 14px; font-weight: 500; z-index: 10000; ' +
        'box-shadow: 0 4px 16px rgba(40, 167, 69, 0.3); ' +
        'border-left: 4px solid #155724; ' +
        'max-width: 300px; font-family: Arial, sans-serif; ' +
        'animation: slideInRight 0.3s ease;';
    
    document.body.appendChild(notification);
    
    // Auto-remove after 4 seconds
    setTimeout(function() {
        if (notification && notification.parentNode) {
            notification.style.animation = 'slideOutRight 0.3s ease';
            setTimeout(function() {
                if (notification.parentNode) {
                    notification.remove();
                }
            }, 300);
        }
    }, 4000);
}

function formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

// Remove profile picture function
function removeProfilePicture() {
    console.log('🗑️ EMPLOYEE REMOVE PROFILE PICTURE CALLED!');
    
    const confirmRemove = confirm('Are you sure you want to remove your profile picture? This will set it back to the default avatar.');
    
    if (!confirmRemove) {
        console.log('❌ User cancelled profile picture removal');
        return;
    }
    
    const preview = document.getElementById('profilePreview');
    const header = document.getElementById('headerAvatar');
    const status = document.getElementById('profileStatus');
    const fileInput = document.getElementById('profilePicture');
    const removeFlag = document.getElementById('removeProfilePictureFlag');
    
    // Reset file input
    if (fileInput) {
        fileInput.value = '';
    }
    
    // Set the default avatar image
    const defaultAvatar = '/ELMS_3.0/defaultprofilepicture.jpg';
    
    if (preview) {
        preview.src = defaultAvatar;
        console.log('✅ Main preview set to default avatar');
    }
    
    if (header) {
        header.src = defaultAvatar;
        console.log('✅ Header avatar set to default avatar');
    }
    
    // Set the hidden flag to indicate removal
    if (removeFlag) {
        removeFlag.value = 'true';
        console.log('✅ Remove flag set to true');
    }
    
    // Update status
    if (status) {
        status.innerHTML = '🗑️ Profile picture removed! Click "Save Changes" to apply permanently.';
        status.style.color = '#dc3545';
        status.style.fontWeight = 'bold';
    }
    
    // Show notification
    showNotification('🗑️ Profile picture removed! Click "Save Changes" to apply permanently.');
    
    console.log('🎉 Employee profile picture removal complete!');
}

// Test function for debugging
function testProfilePictureInput() {
    console.log('🧪 Testing profile picture input...');
    const input = document.getElementById('profilePicture');
    if (input) {
        input.click();
        console.log('📂 File dialog should have opened');
    } else {
        console.log('❌ Profile picture input not found');
    }
}

//===== NEW VALIDATION CODE STARTS HERE =====

document.addEventListener('DOMContentLoaded', function() {
    const nameInput = document.getElementById('fullName');
    const emailInput = document.getElementById('email');
    const phoneInput = document.getElementById('phone');
    const saveButton = document.getElementById('saveButton');
    const phoneHelp = document.getElementById('phoneHelp');
    
    // Initialize phone field
    if (!phoneInput.value || phoneInput.value.trim() === '' || !phoneInput.value.startsWith('+60')) {
        phoneInput.value = '+60 ';
    }
    
    // ========================================
    // VALIDATION HELPER FUNCTIONS (DEFINE FIRST!)
    // ========================================
    
    function isValidName(name) {
        return /^[a-zA-Z\s'-]+$/.test(name) && name.length >= 2 && name.length <= 100;
    }
    
    function isValidEmail(email) {
        return /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(email);
    }
    
    function isValidMalaysianPhone(phone) {
        return /^\+60\s1[0-9]-[0-9]{3}\s[0-9]{4}$/.test(phone);
    }
    
    
    
    // ========================================
    // FORM VALIDITY CHECK
    // ========================================
    
    function checkFormValidity() {
        const name = nameInput.value.trim();
        const email = emailInput.value.trim();
        const phone = phoneInput.value.trim();
        
        const isNameValid = name && isValidName(name);
        const isEmailValid = email && isValidEmail(email);
        const isPhoneValid = isValidMalaysianPhone(phone);
        
        const tooltip = document.getElementById('save-button-tooltip');
        
        if (isNameValid && isEmailValid && isPhoneValid) {
            saveButton.disabled = false;
            
            if (tooltip) {
                tooltip.classList.remove('show');
                tooltip.innerHTML = '';
            }
            
            console.log('✅ All fields valid - Save button ENABLED');
        } else {
            saveButton.disabled = true;
            
            const incompleteFields = [];
            if (!isNameValid) incompleteFields.push('Name');
            if (!isEmailValid) incompleteFields.push('Email');
            if (!isPhoneValid) incompleteFields.push('Mobile Number');
            
            if (tooltip && incompleteFields.length > 0) {
                tooltip.innerHTML = 'Please complete:<br>• ' + incompleteFields.join('<br>• ');
            } else if (tooltip) {
                tooltip.textContent = 'Please ensure all fields are valid';
            }
            
            console.log('❌ Save button DISABLED - Missing:', incompleteFields.join(', '));
        }
    }
    
 // ========================================
 // FORMAT EXISTING PHONE NUMBER ON LOAD
 // ========================================

 function formatExistingPhone() {
     const currentValue = phoneInput.value.trim();
     
     // If already in correct format, skip
     if (/^\+60\s1[0-9]-[0-9]{3}\s[0-9]{4}$/.test(currentValue)) {
         console.log('✅ Phone already in correct format');
         return;
     }
     
     // Extract only digits
     let digitsOnly = currentValue.replace(/\D/g, '');
     
     // Remove country code if present
     if (digitsOnly.startsWith('60')) {
         digitsOnly = digitsOnly.substring(2);
     }
     
     // Check if it's a valid Malaysian mobile (starts with 1 and has 9-10 digits)
     if (digitsOnly.startsWith('1') && digitsOnly.length >= 9) {
         // Format: +60 1x-xxx xxxx
         const formatted = '+60 ' + 
                          digitsOnly.substring(0, 2) + '-' + 
                          digitsOnly.substring(2, 5) + ' ' + 
                          digitsOnly.substring(5, 9);
         
         phoneInput.value = formatted;
         console.log('✅ Formatted existing phone:', formatted);
     } else {
         // Invalid format, reset to +60
         phoneInput.value = '+60 ';
         console.log('⚠️ Invalid phone format, reset to +60');
     }
 }

 // Format existing phone number first
 formatExistingPhone();

 // Then run initial validation
 checkFormValidity();
    
    
    
    // ========================================
    // FIELD REMINDER FUNCTION
    // ========================================
    
    function showFieldReminder(field, message) {
        const existingReminder = field.parentElement.querySelector('.field-reminder');
        if (existingReminder) {
            existingReminder.remove();
        }
        
        const reminder = document.createElement('div');
        reminder.className = 'field-reminder';
        reminder.textContent = message;
        field.parentElement.appendChild(reminder);
        
        setTimeout(() => {
            if (reminder.parentElement) {
                reminder.remove();
            }
        }, 3000);
    }
    
    function checkPreviousFields(currentField) {
        const fields = [nameInput, emailInput, phoneInput];
        const currentIndex = fields.indexOf(currentField);
        
        for (let i = 0; i < currentIndex; i++) {
            const field = fields[i];
            const value = field.value.trim();
            let shouldRemind = false;
            let message = '';
            
            if (field === nameInput && (!value || !isValidName(value))) {
                shouldRemind = true;
                message = 'Please fill in Name';
            } else if (field === emailInput && (!value || !isValidEmail(value))) {
                shouldRemind = true;
                message = 'Please fill in Email';
            } else if (field === phoneInput && (!value || !isValidMalaysianPhone(value))) {
                shouldRemind = true;
                message = 'Please fill in Mobile Number';
            }
            
            if (shouldRemind) {
                field.classList.add('shake', 'error');
                setTimeout(() => field.classList.remove('shake'), 400);
                showFieldReminder(field, message);
                break;
            }
        }
    }
    
    // ========================================
    // NAME VALIDATION
    // ========================================
    
    nameInput.addEventListener('input', function() {
        const name = this.value;
        const namePattern = /^[a-zA-Z\s'-]+$/;
        const errorElement = document.getElementById('nameError');
        
        if (name && (!namePattern.test(name) || name.length < 2)) {
            this.classList.add('error');
            this.classList.remove('valid');
            if (errorElement) {
                errorElement.textContent = 'Name can only contain letters, spaces, hyphens, and apostrophes (min 2 characters)';
                errorElement.style.display = 'block';
            }
        } else if (name) {
            this.classList.remove('error');
            this.classList.add('valid');
            if (errorElement) {
                errorElement.style.display = 'none';
            }
        } else {
            this.classList.remove('error', 'valid');
            if (errorElement) {
                errorElement.style.display = 'none';
            }
        }
        
        checkFormValidity();
    });
    
    // ========================================
    // EMAIL VALIDATION
    // ========================================
    
    emailInput.addEventListener('input', function() {
        const email = this.value;
        const emailPattern = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;
        const errorElement = document.getElementById('emailError');
        
        if (email && !emailPattern.test(email)) {
            this.classList.add('error');
            this.classList.remove('valid');
            if (errorElement) {
                errorElement.textContent = 'Please enter a valid email address';
                errorElement.style.display = 'block';
            }
        } else if (email) {
            this.classList.remove('error');
            this.classList.add('valid');
            if (errorElement) {
                errorElement.style.display = 'none';
            }
        } else {
            this.classList.remove('error', 'valid');
            if (errorElement) {
                errorElement.style.display = 'none';
            }
        }
        
        checkFormValidity();
    });
    
    // ========================================
    // PHONE NUMBER FORMATTING & VALIDATION
    // ========================================

phoneInput.addEventListener('input', function(e) {
    let value = e.target.value;
    
    // Extract only digits (remove +, spaces, dashes)
    let digitsOnly = value.replace(/\D/g, '');
    
    console.log('📱 Raw value:', value);
    console.log('📱 Digits only:', digitsOnly);
    
    // Handle empty field
    if (digitsOnly === '' || digitsOnly === '6' || digitsOnly === '60') {
        e.target.value = '+60 ';
        this.classList.remove('error', 'valid');
        phoneHelp.classList.remove('valid', 'invalid');
        phoneHelp.textContent = 'Format: +60 1x-xxx xxxx (Malaysian mobile number)';
        if (document.getElementById('phoneError')) {
            document.getElementById('phoneError').style.display = 'none';
        }
        checkFormValidity(); // ✅ CHECK VALIDITY IMMEDIATELY WHEN EMPTY
        return;
    }
    
    // Remove country code prefix if present (we'll add it back)
    if (digitsOnly.startsWith('60')) {
        digitsOnly = digitsOnly.substring(2);
    }
    
    // Remove leading 0 if present (Malaysian numbers sometimes written as 012-345-6789)
    if (digitsOnly.startsWith('0')) {
        digitsOnly = digitsOnly.substring(1);
    }
    
    // Limit to 9 local digits
    if (digitsOnly.length > 9) {
        digitsOnly = digitsOnly.substring(0, 9);
    }
    
    console.log('📱 Local digits:', digitsOnly);
    
    // Build formatted string: +60 1x-xxx xxxx
    let formatted = '+60';
    
    if (digitsOnly.length > 0) {
        formatted += ' ' + digitsOnly.substring(0, Math.min(2, digitsOnly.length));
    }
    if (digitsOnly.length > 2) {
        formatted += '-' + digitsOnly.substring(2, Math.min(5, digitsOnly.length));
    }
    if (digitsOnly.length > 5) {
        formatted += ' ' + digitsOnly.substring(5);
    }
    
    console.log('📱 Final formatted:', formatted);
    
    e.target.value = formatted;
    
    const malaysianPhonePattern = /^\+60\s1[0-9]-[0-9]{3}\s[0-9]{4}$/;
    const errorElement = document.getElementById('phoneError');
    
    if (formatted && malaysianPhonePattern.test(formatted)) {
        this.classList.remove('error');
        this.classList.add('valid');
        phoneHelp.classList.add('valid');
        phoneHelp.classList.remove('invalid');
        phoneHelp.textContent = '✓ Valid Malaysian mobile number format';
        if (errorElement) errorElement.style.display = 'none';
    } else if (formatted.length > 4) {
        // Check if number doesn't start with 1 (not a mobile number)
        if (digitsOnly.length > 0 && !digitsOnly.startsWith('1')) {
            this.classList.add('error');
            this.classList.remove('valid');
            phoneHelp.classList.add('invalid');
            phoneHelp.classList.remove('valid');
            phoneHelp.textContent = 'Invalid format';
            if (errorElement) {
                errorElement.textContent = 'Mobile number must start with 1 (e.g., +60 12-345 6789)';
                errorElement.style.display = 'block';
            }
        } 
        // Number starts with 1 but incomplete
        else if (digitsOnly.startsWith('1') && digitsOnly.length < 9) {
            this.classList.add('error');
            this.classList.remove('valid');
            phoneHelp.classList.add('invalid');
            phoneHelp.classList.remove('valid');
            phoneHelp.textContent = 'Continue typing... Format: +60 1x-xxx xxxx';
            if (errorElement) errorElement.style.display = 'none';
        }
        // Number is complete but invalid format
        else {
            this.classList.add('error');
            this.classList.remove('valid');
            phoneHelp.classList.add('invalid');
            phoneHelp.classList.remove('valid');
            phoneHelp.textContent = 'Invalid format';
            if (errorElement) {
                errorElement.textContent = 'Invalid mobile number format';
                errorElement.style.display = 'block';
            }
        }
    } else {
        this.classList.remove('error', 'valid');
        phoneHelp.classList.remove('valid', 'invalid');
        phoneHelp.textContent = 'Format: +60 1x-xxx xxxx (Malaysian mobile number)';
        if (errorElement) errorElement.style.display = 'none';
    }
    
    checkFormValidity(); // ✅ CHECK VALIDITY AFTER EVERY INPUT
});
    
    phoneInput.addEventListener('keydown', function(e) {
        const cursorPosition = this.selectionStart;
        
        if (e.key === 'Backspace' && cursorPosition <= 4) {
            e.preventDefault();
            this.value = '+60 ';
            this.setSelectionRange(4, 4);
            checkFormValidity(); // ✅ CHECK VALIDITY AFTER BACKSPACE
        }
    });
    
    phoneInput.addEventListener('focus', function() {
        if (this.value === '+60 ' || this.value === '+60' || this.value === '') {
            this.value = '+60 ';
            setTimeout(() => {
                this.setSelectionRange(4, 4);
            }, 0);
        }
    });
    
    phoneInput.addEventListener('keypress', function(e) {
        const char = String.fromCharCode(e.which);
        if (!/[\d+\s-]/.test(char)) {
            e.preventDefault();
        }
    });
    
    // Validate when leaving the phone field
    phoneInput.addEventListener('blur', function() {
        const value = this.value.trim();
        const errorElement = document.getElementById('phoneError');
        
        // If field is empty or only has +60, reset it
        if (!value || value === '+60' || value === '+60 ') {
            this.value = '+60 ';
            this.classList.remove('error', 'valid');
            phoneHelp.classList.remove('valid', 'invalid');
            phoneHelp.textContent = 'Format: +60 1x-xxx xxxx (Malaysian mobile number)';
            if (errorElement) errorElement.style.display = 'none';
        } else {
            // Check if format is complete and valid
            const malaysianPhonePattern = /^\+60\s1[0-9]-[0-9]{3}\s[0-9]{4}$/;
            
            if (!malaysianPhonePattern.test(value)) {
                this.classList.add('error');
                this.classList.remove('valid');
                if (errorElement) {
                    errorElement.textContent = 'Please enter a valid mobile number';
                    errorElement.style.display = 'block';
                }
            }
        }
        
        // Always check form validity when leaving the field
        checkFormValidity();
    });
    
    // ========================================
    // FOCUS LISTENERS FOR FIELD REMINDERS
    // ========================================
    
    emailInput.addEventListener('focus', function() {
        checkPreviousFields(this);
    });
    
    phoneInput.addEventListener('focus', function() {
        checkPreviousFields(this);
    });
    
    // ========================================
    // TOOLTIP HOVER HANDLERS
    // ========================================
    
    saveButton.addEventListener('mouseenter', function() {
        if (this.disabled) {
            const tooltip = document.getElementById('save-button-tooltip');
            if (tooltip && tooltip.innerHTML) {
                const rect = this.getBoundingClientRect();
                tooltip.style.left = (rect.left + rect.width / 2) + 'px';
                tooltip.style.top = (rect.top - 10) + 'px';
                tooltip.style.transform = 'translate(-50%, -100%)';
                tooltip.classList.add('show');
            }
        }
    });
    
    saveButton.addEventListener('mouseleave', function() {
        const tooltip = document.getElementById('save-button-tooltip');
        if (tooltip) {
            tooltip.classList.remove('show');
        }
    });
    
    console.log('✅ Employee edit account validation loaded');
    console.log('📱 Phone format: +60 1x-xxx xxxx (Mobile number only)');
    console.log('🔒 Save button disabled until all fields valid');
});
</script>

<style>
/* Profile Picture Styles */
.settings-form-card {
    background: white;
    border-radius: 8px;
    padding: 2rem;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
    border: 1px solid #e5e7eb;
    margin-bottom: 2rem;
}

.profile-picture-section {
    text-align: center;
    margin-bottom: 2rem;
}

.profile-picture {
    position: relative;
    display: inline-block;
    width: 120px;
    height: 120px;
    border-radius: 50%;
    overflow: hidden;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    transition: transform 0.3s ease, box-shadow 0.3s ease;
    cursor: pointer;
}

.profile-picture:hover {
    transform: scale(1.02);
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.2);
}

.profile-picture img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: opacity 0.3s ease, filter 0.3s ease;
}

.profile-picture-overlay {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    background: rgba(0, 0, 0, 0.8);
    color: white;
    padding: 0.75rem;
    text-align: center;
    opacity: 0;
    transition: opacity 0.3s ease;
    border-radius: 0 0 50% 50%;
}

.profile-picture:hover .profile-picture-overlay {
    opacity: 1;
}

.upload-btn {
    cursor: pointer;
    font-size: 0.875rem;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 0.25rem;
    text-decoration: none;
    color: white;
}

.upload-btn:hover {
    color: #e5e7eb;
}

.remove-profile-btn {
    cursor: pointer;
    font-size: 0.875rem;
    display: inline-flex;
    align-items: center;
    gap: 0.5rem;
    text-decoration: none;
    color: #dc3545;
    background: none;
    border: 1px solid #dc3545;
    
    padding: 0.5rem 1rem;
    border-radius: 6px;
    transition: all 0.3s ease;
}

.remove-profile-btn:hover {
    color: white;
    background-color: #dc3545;
    border-color: #dc3545;
}

.profile-hint {
    margin-top: 0.5rem;
    font-size: 0.875rem;
    color: #6b7280;
    font-style: italic;
}

/* Form Styles */
.form-group {
    margin-bottom: 1rem;
}

.form-group label {
    display: block;
    margin-bottom: 0.5rem;
    font-weight: 500;
    color: #374151;
}

.form-group input {
    width: 100%;
    padding: 0.75rem;
    border: 1px solid #d1d5db;
    border-radius: 6px;
    font-size: 1rem;
    transition: border-color 0.3s ease;
}

.form-group input:focus {
    outline: none;
    border-color: #3b82f6;
    box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.phone-format-help {
    margin-top: 0.5rem;
    font-size: 0.875rem;
    color: #6b7280;
    font-style: italic;
}

.form-error {
    margin-top: 0.25rem;
    font-size: 0.875rem;
    color: #dc2626;
}

.form-buttons {
    display: flex;
    gap: 1rem;
    margin-top: 2rem;
    justify-content: center;
}

.btn {
    padding: 0.75rem 1.5rem;
    border-radius: 6px;
    font-weight: 500;
    text-decoration: none;
    border: none;
    cursor: pointer;
    transition: all 0.3s ease;
    display: inline-flex;
    align-items: center;
    gap: 0.5rem;
}

.btn:disabled {
    opacity: 0.6;
    cursor: not-allowed;
}



/* Alert Styles */
.alert {
    padding: 1rem;
    border-radius: 6px;
    margin-bottom: 1rem;
    display: flex;
    align-items: center;
    gap: 0.5rem;
    transition: opacity 0.3s ease;
}

.alert-success {
    background-color: #d1fae5;
    color: #065f46;
    border: 1px solid #a7f3d0;
}

.alert-error {
    background-color: #fee2e2;
    color: #991b1b;
    border: 1px solid #fca5a5;
}

/* Notification Animations */
@keyframes slideInRight {
    from {
        opacity: 0;
        transform: translateX(100%);
    }
    to {
        opacity: 1;
        transform: translateX(0);
    }
}

@keyframes slideOutRight {
    from {
        opacity: 1;
        transform: translateX(0);
    }
    to {
        opacity: 0;
        transform: translateX(100%);
    }
}

/* Loading spinner */
.fa-spinner {
    animation: spin 1s linear infinite;
}

@keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
}

/* Mobile responsiveness */
@media (max-width: 768px) {
    .form-buttons {
        flex-direction: column;
    }
    
    .settings-form-card {
        padding: 1rem;
    }
    
    .profile-picture {
        width: 100px;
        height: 100px;
    }
    
    .profile-picture-overlay {
        padding: 0.5rem;
        font-size: 0.75rem;
    }
}

/* Shake Animation */
@keyframes shake {
    0%, 100% { transform: translateX(0); }
    25% { transform: translateX(-10px); }
    75% { transform: translateX(10px); }
}

.shake {
    animation: shake 0.4s ease-in-out;
    border-color: #dc3545 !important;
}

/* Field Reminder Tooltip */
.field-reminder {
    position: absolute;
    top: -40px;
    left: 0;
    background: #dc3545;
    color: white;
    padding: 8px 12px;
    border-radius: 4px;
    font-size: 0.85rem;
    white-space: nowrap;
    z-index: 1000;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
    animation: fadeInDown 0.3s ease-in-out;
}

@keyframes fadeInDown {
    from {
        opacity: 0;
        transform: translateY(-10px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.field-reminder::after {
    content: '';
    position: absolute;
    bottom: -5px;
    left: 20px;
    width: 0;
    height: 0;
    border-left: 5px solid transparent;
    border-right: 5px solid transparent;
    border-top: 5px solid #dc3545;
}

/* Ensure form-group has relative positioning */
.form-group {
    position: relative;
    margin-bottom: 1.5rem;
}

/* Save Button Tooltip */
#save-button-tooltip {
    position: fixed;
    background: #000000;
    color: #ffffff;
    padding: 14px 18px;
    border-radius: 8px;
    font-size: 0.9rem;
    font-weight: 600;
    text-align: left;
    line-height: 1.6;
    opacity: 0;
    visibility: hidden;
    pointer-events: none;
    transition: opacity 0.2s ease, visibility 0.2s ease;
    z-index: 999999;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.95);
    border: 2px solid rgba(255, 255, 255, 0.3);
    max-width: 280px;
    white-space: pre-line;
}

#save-button-tooltip.show {
    opacity: 1;
    visibility: visible;
}

#save-button-tooltip::after {
    content: '';
    position: absolute;
    top: 100%;
    left: 50%;
    transform: translateX(-50%);
    width: 0;
    height: 0;
    border-left: 8px solid transparent;
    border-right: 8px solid transparent;
    border-top: 8px solid #000000;
}

/* Input Validation States */
.form-group input.error {
    border-color: #dc3545 !important;
    box-shadow: 0 0 0 0.2rem rgba(220, 53, 69, 0.25);
}

.form-group input.valid {
    border-color: #28a745 !important;
    box-shadow: 0 0 0 0.2rem rgba(40, 167, 69, 0.25);
}

/* Error Message */
.form-error {
    color: #dc3545;
    font-size: 0.875rem;
    margin-top: 5px;
    display: none;
    font-weight: 400;
}

/* Phone Format Help */
.phone-format-help {
    margin-top: 0.5rem;
    font-size: 0.875rem;
    color: #6b7280;
    font-style: italic;
    font-weight: 300;
}

.phone-format-help.valid {
    color: #28a745;
    font-weight: 500;
}

.phone-format-help.invalid {
    color: #dc3545;
    font-weight: 500;
}

/* Disabled Button Styling */
.btn:disabled {
    cursor: not-allowed !important;
    background-color: #6c757d !important;
    color: rgba(255, 255, 255, 0.6) !important;
    opacity: 1 !important;
}

.btn:disabled:hover {
    background-color: #6c757d !important;
    transform: none !important;
}
</style>

</body>
</html>
