document.addEventListener('DOMContentLoaded', function() {
    console.log('Employee.js starting...');
    
    // Sidebar functionality
    const sidebarToggle = document.getElementById('sidebarToggle');
    const sidebar = document.getElementById('sidebar');
    const mainContent = document.getElementById('mainContent');
    const logoutBtn = document.getElementById('logoutBtn');

    // Profile picture elements - PROPERLY DEFINED
    const profilePictureInput = document.getElementById('profilePicture');
    const profilePreview = document.getElementById('profilePreview');
    const headerAvatar = document.getElementById('headerAvatar');
    const editAccountForm = document.getElementById('editAccountForm');

    console.log('Element check:');
    console.log('- profilePictureInput:', !!profilePictureInput);
    console.log('- profilePreview:', !!profilePreview);
    console.log('- headerAvatar:', !!headerAvatar);
    console.log('- editAccountForm:', !!editAccountForm);

    // SIDEBAR FUNCTIONALITY
    function toggleSidebar() {
        if (window.innerWidth <= 991) {
            sidebar.classList.toggle('open');
        } else {
            sidebar.classList.toggle('collapsed');
            mainContent.classList.toggle('expanded');
        }
        updateToggleIcon();
    }

    function updateToggleIcon() {
        if (!sidebarToggle) return;
        
        const icon = sidebarToggle.querySelector('i');
        if (!icon) return;
        
        if (window.innerWidth <= 991) {
            if (sidebar && sidebar.classList.contains('open')) {
                icon.classList.remove('fa-bars');
                icon.classList.add('fa-times');
            } else {
                icon.classList.remove('fa-times');
                icon.classList.add('fa-bars');
            }
        } else {
            if (sidebar && sidebar.classList.contains('collapsed')) {
                icon.classList.remove('fa-times');
                icon.classList.add('fa-bars');
            } else {
                icon.classList.remove('fa-bars');
                icon.classList.add('fa-times');
            }
        }
    }

    function handleResponsive() {
        if (window.innerWidth <= 991) {
            if (sidebar) {
                sidebar.classList.remove('collapsed');
            }
            if (mainContent) {
                mainContent.classList.remove('expanded');
            }
        } else {
            if (sidebar) {
                sidebar.classList.remove('open');
            }
        }
        updateToggleIcon();
    }

    // Setup sidebar events
    if (sidebarToggle) {
        sidebarToggle.addEventListener('click', toggleSidebar);
    }
    window.addEventListener('resize', handleResponsive);
    handleResponsive();

    // Close sidebar when clicking outside on mobile
    document.addEventListener('click', function(e) {
        if (window.innerWidth <= 991 && sidebar) {
            if (!sidebar.contains(e.target) && sidebarToggle && !sidebarToggle.contains(e.target)) {
                sidebar.classList.remove('open');
                updateToggleIcon();
            }
        }
    });

    // LOGOUT FUNCTIONALITY
    if (logoutBtn) {
        logoutBtn.addEventListener('click', function(e) {
            e.preventDefault();
            if (confirm('Are you sure you want to logout?')) {
                fetch('/ELMS_3.0/logout', { method: 'POST' })
                    .then(() => window.location.href = '/ELMS_3.0/Employee/EmployeeLogin.jsp')
                    .catch(() => window.location.href = '/ELMS_3.0/logout');
            }
        });
    }

    // ===== PROFILE PICTURE PREVIEW FUNCTIONALITY =====
    if (profilePictureInput && profilePreview) {
        console.log('Setting up profile picture preview...');
        
        profilePictureInput.addEventListener('change', function(event) {
            const file = event.target.files[0];
            
            console.log('=== PROFILE PICTURE CHANGE EVENT ===');
            console.log('File selected:', file ? file.name : 'none');
            
            if (file) {
                console.log('File details:');
                console.log('- Name:', file.name);
                console.log('- Size:', file.size, 'bytes');
                console.log('- Type:', file.type);
                
                // Validate file type
                const validTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif'];
                if (!validTypes.includes(file.type)) {
                    alert('Please select a valid image file (JPG, PNG, or GIF).');
                    event.target.value = '';
                    return;
                }
                
                // Validate file size (5MB max)
                const maxSize = 5 * 1024 * 1024;
                if (file.size > maxSize) {
                    alert('File size must be less than 5MB. Selected file is ' + formatFileSize(file.size));
                    event.target.value = '';
                    return;
                }
                
                console.log('✅ File validation passed');
                
                // Show loading state
                profilePreview.style.opacity = '0.5';
                
                // Read and preview the file
                const reader = new FileReader();
                
                reader.onload = function(e) {
                    console.log('✅ File read successfully');
                    
                    // Update main profile preview
                    profilePreview.src = e.target.result;
                    profilePreview.style.opacity = '1';
                    console.log('✅ Profile preview updated');
                    
                    // Update header avatar immediately
                    if (headerAvatar) {
                        headerAvatar.src = e.target.result;
                        console.log('✅ Header avatar updated');
                    }
                    
                    // Show success indicator
                    showImageSelectedIndicator();
                    
                    console.log('✅ Profile picture preview complete!');
                };
                
                reader.onerror = function() {
                    console.error('❌ Error reading file');
                    alert('Error reading the selected file. Please try again.');
                    event.target.value = '';
                    profilePreview.style.opacity = '1';
                };
                
                console.log('📖 Starting to read file...');
                reader.readAsDataURL(file);
                
            } else {
                console.log('No file selected');
            }
        });
        
        console.log('✅ Profile picture input event listener attached');
    } else {
        console.log('❌ Profile picture elements not found!');
        if (!profilePictureInput) console.log('Missing: profilePictureInput');
        if (!profilePreview) console.log('Missing: profilePreview');
    }

    // Function to show image selection indicator
    function showImageSelectedIndicator() {
        console.log('Showing image selected indicator...');
        
        const indicator = document.createElement('div');
        indicator.className = 'image-changed-indicator';
        indicator.innerHTML = '<i class="fas fa-check"></i> Image selected - click Save to apply';
        indicator.style.cssText = `
            position: absolute;
            bottom: -30px;
            left: 50%;
            transform: translateX(-50%);
            background: #10b981;
            color: white;
            padding: 0.25rem 0.5rem;
            border-radius: 4px;
            font-size: 0.75rem;
            white-space: nowrap;
            box-shadow: 0 2px 8px rgba(16, 185, 129, 0.3);
            z-index: 1000;
            animation: slideUp 0.3s ease;
        `;
        
        const profileSection = document.querySelector('.profile-picture-section');
        if (profileSection) {
            profileSection.style.position = 'relative';
            
            // Remove existing indicator
            const existingIndicator = profileSection.querySelector('.image-changed-indicator');
            if (existingIndicator) {
                existingIndicator.remove();
            }
            
            profileSection.appendChild(indicator);
            
            // Auto-remove after 3 seconds
            setTimeout(() => {
                if (indicator.parentNode) {
                    indicator.remove();
                }
            }, 3000);
        }
    }

    // FORM VALIDATION AND SUBMISSION
    if (editAccountForm) {
        console.log('Setting up form submission...');
        
        editAccountForm.addEventListener('submit', function(e) {
            console.log('=== FORM SUBMISSION START ===');
            
            const fullName = document.getElementById('fullName');
            const email = document.getElementById('email');
            const phone = document.getElementById('phone');

            if (!fullName || !fullName.value.trim()) {
                e.preventDefault();
                alert('Please enter your full name.');
                console.log('❌ Validation failed: Empty full name');
                return;
            }

            if (!email || !email.value.trim()) {
                e.preventDefault();
                alert('Please enter a valid email address.');
                console.log('❌ Validation failed: Empty email');
                return;
            }

            if (!phone || !phone.value.trim()) {
                e.preventDefault();
                alert('Please enter a phone number.');
                console.log('❌ Validation failed: Empty phone');
                return;
            }

            console.log('✅ Form validation passed');
            console.log('Form data:');
            console.log('- Name:', fullName.value.trim());
            console.log('- Email:', email.value.trim());
            console.log('- Phone:', phone.value.trim());
            console.log('- Has new file:', profilePictureInput && profilePictureInput.files.length > 0);

            // Show loading state
            const submitBtn = this.querySelector('button[type="submit"]');
            if (submitBtn) {
                const originalText = submitBtn.textContent;
                submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Saving...';
                submitBtn.disabled = true;

                // Reset button after timeout (fallback)
                setTimeout(() => {
                    if (submitBtn) {
                        submitBtn.innerHTML = originalText;
                        submitBtn.disabled = false;
                    }
                }, 10000);
            }

            console.log('Form submitting...');
        });
    }

    // AUTO-HIDE ALERT MESSAGES
    const alertMessage = document.getElementById('alertMessage');
    if (alertMessage) {
        const messageType = alertMessage.classList.contains('alert-success') ? 'success' : 'error';
        const hideDelay = messageType === 'success' ? 6000 : 4000;
        
        setTimeout(() => {
            alertMessage.style.opacity = '0';
            setTimeout(() => {
                if (alertMessage.parentNode) {
                    alertMessage.remove();
                }
            }, 300);
        }, hideDelay);
    }

 
    // UTILITY FUNCTIONS
    function formatFileSize(bytes) {
        if (bytes === 0) return '0 Bytes';
        const k = 1024;
        const sizes = ['Bytes', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    }

    // DEBUG FUNCTIONS (available globally)
    window.debugProfilePicture = function() {
        console.log('=== PROFILE PICTURE DEBUG ===');
        console.log('Elements:');
        console.log('- profilePictureInput:', profilePictureInput);
        console.log('- profilePreview:', profilePreview);
        console.log('- headerAvatar:', headerAvatar);
        
        if (profilePreview) {
            console.log('Profile preview src:', profilePreview.src);
        }
        if (headerAvatar) {
            console.log('Header avatar src:', headerAvatar.src);
        }
        if (profilePictureInput) {
            console.log('Input has files:', profilePictureInput.files.length);
        }
        console.log('==============================');
    };

    window.testProfilePictureEvent = function() {
        console.log('Testing profile picture change event...');
        if (profilePictureInput) {
            const event = new Event('change', { bubbles: true });
            profilePictureInput.dispatchEvent(event);
            console.log('Change event dispatched');
        } else {
            console.log('Profile picture input not found');
        }
    };

    console.log('✅ Employee.js loaded successfully');
    console.log('Available debug functions:');
    console.log('- debugProfilePicture(): Check current state');
    console.log('- testProfilePictureEvent(): Test change event');
});