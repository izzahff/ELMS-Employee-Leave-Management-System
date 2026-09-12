<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page session="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%
    if (session.getAttribute("adminId") == null) {
       response.sendRedirect(request.getContextPath() + "/Admin/AdminLogin.jsp");
       return;
    }
    
    String adminName = (String) session.getAttribute("adminName");
    if (adminName == null) adminName = "Admin User";
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="icon" type="image/png" href="<%= request.getContextPath() %>/imnsb_logo.png">
    <title>Add Leave Type - IMNSB ELMS</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/dashboard.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        .conditional-field {
            display: none;
            animation: fadeIn 0.3s ease-in;
        }
        
        .conditional-field.show {
            display: block;
        }
        
        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(-10px); }
            to { opacity: 1; transform: translateY(0); }
        }
        
        .field-info {
            font-size: 0.875rem;
            color: #6c757d;
            margin-top: 5px;
            font-style: italic;
        }

        .checkbox-group {
            display: flex;
            align-items: center;
            gap: 10px;
            padding: 12px;
            background: #f8f9fa;
            border-radius: 6px;
            border: 1px solid #dee2e6;
            cursor: pointer;
            transition: all 0.3s ease;
        }

        .checkbox-group:hover {
            background: #e9ecef;
            border-color: #adb5bd;
        }

        .checkbox-group input[type="checkbox"] {
            width: 20px;
            height: 20px;
            cursor: pointer;
            margin: 0;
        }

        .checkbox-group label {
            margin: 0;
            cursor: pointer;
            flex: 1;
            font-weight: 500;
        }
        
        /* Inline Error Message at Field */
        .inline-error-message {
            display: flex;
            align-items: flex-start;
            gap: 8px;
            padding: 15px;
            background: linear-gradient(135deg, #fff5f5 0%, #ffe5e5 100%);
            border: 2px solid #dc3545;
            border-left: 5px solid #dc3545;
            border-radius: 6px;
            color: #721c24;
            box-shadow: 0 2px 8px rgba(220, 53, 69, 0.2);
            animation: slideDown 0.3s ease-out;
        }
        
        .inline-error-message i {
            color: #dc3545;
            font-size: 1.2rem;
            margin-top: 2px;
        }
        
        .inline-error-message strong {
            color: #dc3545;
        }
        
        @keyframes slideDown {
            from {
                opacity: 0;
                transform: translateY(-10px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }
        
        /* Enhanced Alert Styles (for success messages) */
        .alert {
            padding: 20px;
            margin-bottom: 20px;
            border-radius: 8px;
            display: flex;
            align-items: flex-start;
            gap: 10px;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
            font-size: 1rem;
        }
        
        .alert-danger {
            background: linear-gradient(135deg, #fff5f5 0%, #ffe5e5 100%);
            border: 2px solid #dc3545;
            color: #721c24;
        }
        
        .alert-danger strong {
            color: #dc3545;
            font-size: 1.1rem;
        }
        
        .alert-success {
            background: linear-gradient(135deg, #f0fff4 0%, #dcffe4 100%);
            border: 2px solid #28a745;
            color: #155724;
            display: flex;
            align-items: center;
        }
        
        /* Modal Styles - UPDATED FOR PROPER CENTERING */
.modal {
    position: fixed;
    z-index: 1000;
    left: 0;
    top: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    animation: fadeIn 0.3s ease-in;
}

.modal-content {
    background-color: #ffffff;
    border: none;
    border-radius: 8px;
    width: 90%;
    max-width: 600px;
    max-height: 80vh;
    overflow-y: auto;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
    animation: slideIn 0.3s ease-out;
    margin: 20px;
}

.modal-header {
    padding: 20px 30px;
    border-bottom: 1px solid #e9ecef;
    display: flex;
    justify-content: space-between;
    align-items: center;
    background: linear-gradient(135deg, #fff5f5 0%, #ffe5e5 100%);
    border-radius: 8px 8px 0 0;
    border-bottom: 2px solid #dc3545;
}

.modal-header h3 {
    margin: 0;
    color: #dc3545;
    font-size: 1.25rem;
    font-weight: 600;
}

.modal-header h3 i {
    margin-right: 8px;
}

.close {
    color: #6c757d;
    font-size: 28px;
    font-weight: bold;
    cursor: pointer;
    border: none;
    background: none;
    padding: 0;
    line-height: 1;
    transition: color 0.2s ease;
}

.close:hover,
.close:focus {
    color: #495057;
    text-decoration: none;
}

.modal-body {
    padding: 30px;
}

/* Consistent Button Styles */
.form-buttons {
    display: flex;
    gap: 10px;
    justify-content: center;
    margin-top: 30px;
}

.form-buttons .btn {
    padding: 10px 30px;
    border-radius: 5px;
    cursor: pointer;
    font-size: 1rem;
    font-weight: 500;
    border: none;
    display: inline-flex;
    align-items: center;
    gap: 8px;
    transition: background-color 0.3s ease;
    text-decoration: none;
    min-width: 150px;
    justify-content: center;
}

.form-buttons .btn-secondary {
    background-color: #6c757d;
    color: white;
}

.form-buttons .btn-secondary:hover {
    background-color: #545b62;
}

.form-buttons .btn-primary {
    background-color: #007bff;
    color: white;
}

.form-buttons .btn-primary:hover {
    background-color: #0056b3;
}

.form-buttons .btn:disabled {
    background-color: #6c757d;
    opacity: 0.6;
    cursor: not-allowed;
}

@keyframes fadeIn {
    from {
        opacity: 0;
    }
    to {
        opacity: 1;
    }
}

@keyframes slideIn {
    from {
        opacity: 0;
        transform: scale(0.9) translateY(-20px);
    }
    to {
        opacity: 1;
        transform: scale(1) translateY(0);
    }
}

.checkbox-group.disabled {
    opacity: 0.5;
    cursor: not-allowed !important;
    background: #e9ecef !important;
    pointer-events: none;
    border-color: #ced4da !important;
}

.checkbox-group.disabled:hover {
    background: #e9ecef !important;
    border-color: #ced4da !important;
    transform: none;
}

.checkbox-group.disabled label,
.checkbox-group.disabled input {
    cursor: not-allowed !important;
}

/* Disabled state when checkboxes are disabled via JavaScript */
.checkbox-group:has(input[type="checkbox"]:disabled) {
    opacity: 0.5;
    cursor: not-allowed !important;
    background: #e9ecef !important;
    border-color: #ced4da !important;
    pointer-events: none;
}

.checkbox-group:has(input[type="checkbox"]:disabled):hover {
    background: #e9ecef !important;
    border-color: #ced4da !important;
}

.checkbox-group:has(input[type="checkbox"]:disabled) label {
    cursor: not-allowed !important;
    color: #6c757d;
}

/* Active/Enabled state - only show hover when NOT disabled */
.checkbox-group:not(.disabled):not(:has(input[type="checkbox"]:disabled)):hover {
    background: #e9ecef;
    border-color: #adb5bd;
}

/* Ensure disabled checkboxes are visually distinct */
input[type="checkbox"]:disabled {
    cursor: not-allowed !important;
    opacity: 0.5;
}

/* Responsive Design */
@media (max-width: 768px) {
    .modal-content {
        width: 95%;
        max-height: 90vh;
        margin: 10px;
    }

    .modal-header {
        padding: 15px 20px;
    }

    .modal-body {
        padding: 20px;
    }
}

/* Valid and Error Input States */
.form-group input.valid,
.form-group select.valid,
.form-group textarea.valid {
    border-color: #28a745 !important;
    box-shadow: 0 0 0 0.2rem rgba(40, 167, 69, 0.25);
}

.form-group input.error,
.form-group select.error,
.form-group textarea.error {
    border-color: #dc3545 !important;
    box-shadow: 0 0 0 0.2rem rgba(220, 53, 69, 0.25);
}

/* Shake Animation */
@keyframes shake {
    0%, 100% { transform: translateX(0); }
    25% { transform: translateX(-10px); }
    75% { transform: translateX(10px); }
}

.shake {
    animation: shake 0.4s ease-in-out;
}

/* Tooltip for Submit Button */
#submit-button-tooltip {
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
    max-width: 320px;
    white-space: pre-line;
}

#submit-button-tooltip.show {
    opacity: 1;
    visibility: visible;
}

/* Arrow for tooltip */
#submit-button-tooltip::after {
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
    </style>
</head>
<body>
    <div class="dashboard-container">
        <!-- Sidebar Navigation -->
        <nav class="sidebar" id="sidebar">
            <div class="sidebar-header">
                <img src="${pageContext.request.contextPath}/imnsb_logowbg.png" alt="IMNSB Logo" class="sidebar-logo">
                <h3>IMNSB Admin</h3>
            </div>
            <ul class="sidebar-menu">
                <li><a href="<%= request.getContextPath() %>/admin-dashboard"><i class="fas fa-user"></i> <span>Admin Officer</span></a></li>
                <li><a href="/ELMS_3.0/Admin/AdminViewManagerListController"><i class="fas fa-user-tie"></i> <span>Approval Managers</span></a></li>
                <li><a href="/ELMS_3.0/Admin/AdminViewEmployeeListController"><i class="fas fa-users"></i> <span>Employees</span></a></li>
                <li class="active"><a href="/ELMS_3.0/AdminLeaveTypeListController"><i class="fas fa-list-alt"></i> <span>Leave Types</span></a></li>
                <li><a href="<%= request.getContextPath() %>/admin-leave-requests"><i class="fas fa-clipboard-list"></i> <span>Leave Requests</span></a></li>
                <li><a href="/ELMS_3.0/admin-leave-reports"><i class="fas fa-chart-bar"></i> <span>View Report</span></a></li>
                <li><a href="/ELMS_3.0/Admin/AdminSettings.jsp"><i class="fas fa-cog"></i> <span>Settings</span></a></li>
                <li class="logout"><a href="/ELMS_3.0/Admin/AdminLogin.jsp" id="logoutBtn"><i class="fas fa-sign-out-alt"></i> <span>Logout</span></a></li>
            </ul>
        </nav>
        
        <!-- Main Content -->
        <main class="main-content" id="mainContent">
            <header class="content-header">
                <div class="header-left">
                    <button id="sidebarToggle" class="sidebar-toggle">
                        <i class="fas fa-bars"></i>
                    </button>
                    <h2>Add Leave Type</h2>
                </div>
                <div class="header-right">
			    <span id="userName"><%= adminName %></span>
			    <div class="user-avatar">
			        <% 
			            String headerProfilePicPath = (String) session.getAttribute("adminProfilePicturePath");
			            if (headerProfilePicPath != null && !headerProfilePicPath.isEmpty()) {
			        %>
			            <img src="/ELMS_3.0/<%= headerProfilePicPath %>" alt="Admin Avatar" 
			                 onerror="this.src='/ELMS_3.0/defaultprofilepicture.jpg';">
			        <% } else { %>
			            <img src="/ELMS_3.0/defaultprofilepicture.jpg" alt="Admin Avatar">
			        <% } %>
			    </div>
			</div>
            </header>
            
            <div class="content-body">
                <!-- Display success message at top -->
                <c:if test="${not empty sessionScope.successMessage}">
                    <div class="alert alert-success">
                        <i class="fas fa-check-circle"></i>
                        ${sessionScope.successMessage}
                    </div>
                    <c:remove var="successMessage" scope="session" />
                </c:if>

                <div class="admin-card">
                    <div class="admin-card-header">
                        <h3><i class="fas fa-plus-circle"></i> New Leave Type Details</h3>
                        <p>Create a new leave type for the system</p>
                    </div>
                    <div class="admin-card-content">
                        <form id="addLeaveTypeForm" class="admin-form" action="${pageContext.request.contextPath}/CreateLeaveTypeController" method="post">
                            
                            <div class="form-group">
                                <label for="leaveType">
                                    <i class="fas fa-clock"></i> Leave Duration Type <span style="color: red;">*</span>
                                </label>
                                <select id="leaveType" name="leaveType" required>
                                    <option value="">Select Leave Duration</option>
                                    <option value="Full Day" ${param.leaveType == 'Full Day' ? 'selected' : ''}>Full Day Only</option>
                                    <option value="Half Day" ${param.leaveType == 'Half Day' ? 'selected' : ''}>Half Day Only</option>
                                    <option value="Both" ${param.leaveType == 'Both' ? 'selected' : ''}>Both Full Day and Half Day</option>
                                </select>
                                <div class="field-info">
                                    <i class="fas fa-info-circle"></i>
                                    Choose whether this leave type is for full days only, half days only, or allows employees to choose either option.
                                </div>
                                <div class="form-error" id="leaveTypeError"></div>
                            </div>
                            
                           <!-- Standard Duration Field (for Full Day and Both) -->
							<div class="form-group conditional-field" id="standardDurationGroup">
							    <label for="standardDuration">
							        <i class="fas fa-calendar-alt"></i> Standard Duration (Days) <span style="color: red;">*</span>
							    </label>
							    <input type="number" id="standardDuration" name="standardDuration" 
							           value="${param.standardDuration}" 
							           placeholder="Enter standard number of days (e.g., 21 for Annual Leave)" 
							           min="1" max="365" step="1">
							    <div class="field-info" id="standardDurationInfo">
								    <i class="fas fa-info-circle"></i>
								    Set the maximum number of days allowed for this leave type when used as full day leave. <strong>Must be between 1 and 365 days.</strong>
								</div>
							    <div class="form-error" id="standardDurationError"></div>
							</div>

                            <!-- Half Day Shift Configuration (for Half Day and Both) -->
                            <div class="form-group conditional-field" id="halfDayShiftGroup">
                                <label for="halfDayShift">
                                    <i class="fas fa-sun"></i> Available Shifts <span style="color: red;">*</span>
                                </label>
                                <select id="halfDayShift" name="halfDayShift">
                                    <option value="">Select Available Shifts</option>
                                    <option value="Morning" ${param.halfDayShift == 'Morning' ? 'selected' : ''}>Morning Shift Only</option>
                                    <option value="Afternoon" ${param.halfDayShift == 'Afternoon' ? 'selected' : ''}>Afternoon Shift Only</option>
                                    <option value="Both" ${param.halfDayShift == 'Both' ? 'selected' : ''}>Both Morning & Afternoon</option>
                                </select>
                                <div class="field-info">
                                    <i class="fas fa-info-circle"></i>
                                    Choose which shifts employees can select when applying for half day leave.
                                </div>
                                <div class="form-error" id="halfDayShiftError"></div>
                            </div>
                            
                            <div class="form-group">
                                <label for="leaveTypeName">
                                    <i class="fas fa-tag"></i> Leave Type Name <span style="color: red;">*</span>
                                </label>
                                <input type="text" id="leaveTypeName" name="leaveTypeName" required 
                                       value="${param.leaveTypeName}" placeholder="Enter leave type name (e.g., Annual Leave, Sick Leave)"
                                       class="${not empty requestScope.errorMessage ? 'error' : ''}">
                                
                                <!-- Display error message inline at this field -->
                                <c:if test="${not empty requestScope.errorMessage}">
                                    <div class="inline-error-message" style="margin-top: 10px;">
                                        <i class="fas fa-exclamation-triangle"></i>
                                        <strong>Error:</strong> ${requestScope.errorMessage}
                                    </div>
                                </c:if>
                                
                                <div class="form-error" id="leaveTypeNameError"></div>
                            </div>
                            
                            <div class="form-group">
                                <label for="leaveTypeDescription">
                                    <i class="fas fa-align-left"></i> Description <span style="color: red;">*</span>
                                </label>
                                <textarea id="leaveTypeDescription" name="leaveTypeDescription" rows="4" required 
                                          placeholder="Enter detailed description of this leave type">${param.leaveTypeDescription}</textarea>
                                <div class="form-error" id="leaveTypeDescriptionError"></div>
                            </div>
                            
                            <!-- Supporting Document Requirement -->
                            <div class="form-group">
                                <div class="checkbox-group">
                                    <input type="checkbox" id="requiresDocument" name="requiresDocument" 
                                           ${param.requiresDocument == 'on' ? 'checked' : ''}>
                                    <label for="requiresDocument">
                                        <i class="fas fa-file-upload"></i> Require Supporting Document
                                    </label>
                                </div>
                                <div class="field-info">
                                    <i class="fas fa-info-circle"></i>
                                    Check this box if employees must upload supporting documents when applying for this leave type.
                                </div>
                            </div>
                            
                            <!-- Affects Balance Checkbox -->
                            <div class="form-group">
                                <div class="checkbox-group">
                                    <input type="checkbox" id="affectsBalance" name="affectsBalance" 
                                           ${param.affectsBalance == 'on' ? 'checked' : ''}>
                                    <label for="affectsBalance">
                                        <i class="fas fa-calculator"></i> Track Leave Balance (Deduct from Annual Entitlement)
                                    </label>
                                </div>
                               <div class="field-info">
							    <i class="fas fa-info-circle"></i>
							    Check this box if this leave type should track employee balance annually. 
							    <strong>Note: This option is only available for Full Day and Both categories.</strong> 
							    When checked, approved leave will deduct from the employee's individual balance for this specific leave type.
							</div>
                            </div>
                            
                            <!-- Fixed Duration Checkbox - INDEPENDENT -->
                            <div class="form-group">
                                <div class="checkbox-group">
                                    <input type="checkbox" id="fixedDuration" name="fixedDuration" 
                                           ${param.fixedDuration == 'on' ? 'checked' : ''}>
                                    <label for="fixedDuration">
                                        <i class="fas fa-lock"></i> Fixed Duration (Must Take Exact Amount)
                                    </label>
                                </div>
                              <div class="field-info">
							    <i class="fas fa-info-circle"></i>
							    Check this box if employees must take the exact standard duration amount (e.g., Maternity Leave with exactly 90 days, Bereavement Leave with exactly 3 days). When unchecked, employees can request any amount up to the standard duration (flexible). <strong>Note: This option is only available for "Full Day" leave types.</strong>
							</div>
                            </div>
                            
                            <div class="form-buttons">
                                <a href="<%=request.getContextPath()%>/AdminLeaveTypeListController" class="btn btn-secondary">
                                    <i class="fas fa-times"></i> Cancel
                                </a>
                                <button type="submit" class="btn btn-primary" id="submitBtn">
                                    <i class="fas fa-plus-circle"></i> Add Leave Type
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </main>
        
         <!-- Submit Button Tooltip -->
        <div id="submit-button-tooltip"></div>
        
  <!-- Error Modal -->
<div id="errorModal" class="modal" style="display: none;">
    <div class="modal-content">
        <div class="modal-header">
            <h3><i class="fas fa-exclamation-triangle"></i> Validation Errors</h3>
            <span class="close" id="closeErrorModal">&times;</span>
        </div>
        <div class="modal-body">
            <div style="margin-bottom: 15px;">
                <strong style="font-size: 1.05rem; color: #721c24; display: block; margin-bottom: 10px;">
                    Please correct the following <span id="errorCount"></span> error<span id="errorPlural"></span>:
                </strong>
            </div>
            <ul id="errorList" style="margin: 0 0 20px 0; padding-left: 20px; line-height: 1.8; color: #721c24;"></ul>
            <div style="margin-top: 20px; padding-top: 20px; border-top: 1px solid #f5c6cb; display: flex; justify-content: flex-end;">
                <button type="button" class="btn btn-primary" id="closeErrorModalBtn" style="background-color: #dc3545; border-color: #dc3545; padding: 10px 20px;">
                    <i class="fas fa-times"></i> Close and Fix Errors
                </button>
            </div>
        </div>
    </div>
</div>
    </div>



<script src="${pageContext.request.contextPath}/Admin/Admin.js"></script>
<script>
    document.addEventListener('DOMContentLoaded', function() {
        const form = document.getElementById('addLeaveTypeForm');
        const leaveTypeSelect = document.getElementById('leaveType');
        const standardDurationGroup = document.getElementById('standardDurationGroup');
        const standardDurationInput = document.getElementById('standardDuration');
        const halfDayShiftGroup = document.getElementById('halfDayShiftGroup');
        const halfDayShiftSelect = document.getElementById('halfDayShift');
        const submitBtn = document.getElementById('submitBtn');
        const leaveTypeNameInput = document.getElementById('leaveTypeName');
        const leaveTypeDescriptionTextarea = document.getElementById('leaveTypeDescription');
        const requiresDocumentCheckbox = document.getElementById('requiresDocument');
        const affectsBalanceCheckbox = document.getElementById('affectsBalance');
        const fixedDurationCheckbox = document.getElementById('fixedDuration');
        
        // Error modal elements
        const errorModal = document.getElementById('errorModal');
        const closeErrorModal = document.getElementById('closeErrorModal');
        const closeErrorModalBtn = document.getElementById('closeErrorModalBtn');
        const errorList = document.getElementById('errorList');
        const errorCount = document.getElementById('errorCount');
        const errorPlural = document.getElementById('errorPlural');
        
        // INITIAL STATE - All fields disabled except leave type
        standardDurationInput.disabled = true;
        halfDayShiftSelect.disabled = true;
        leaveTypeNameInput.disabled = true;
        leaveTypeDescriptionTextarea.disabled = true;
        requiresDocumentCheckbox.disabled = true;
        requiresDocumentCheckbox.parentElement.classList.add('disabled');
        affectsBalanceCheckbox.disabled = true;
        affectsBalanceCheckbox.parentElement.classList.add('disabled');
        fixedDurationCheckbox.disabled = true;
        fixedDurationCheckbox.parentElement.classList.add('disabled');
        submitBtn.disabled = true;
        
        let isNameValidated = false;
        
        // ========================================
        // VALIDATION HELPER FUNCTIONS
        // ========================================
        
        function isValidDuration(value) {
            const duration = parseInt(value);
            return !isNaN(duration) && duration >= 1 && duration <= 365 && duration === Math.floor(duration);
        }
        
        function isValidName(name) {
            return name.length >= 3 && 
                   name.length <= 100 && 
                   /[a-zA-Z]/.test(name) && 
                   !/[^a-zA-Z0-9\s]/.test(name);
        }
        
        function isValidDescription(description) {
            return description.length >= 10 && 
                   description.length <= 500 && 
                   /[a-zA-Z]/.test(description);
        }
        
        // ========================================
        // INLINE VALIDATION HELPER
        // ========================================
        
        function showInlineValidation(input, isValid, message) {
            const existingMsg = input.parentElement.querySelector('.validation-message');
            if (existingMsg) {
                existingMsg.remove();
            }
            
            if (!isValid && message) {
                const validationMsg = document.createElement('div');
                validationMsg.className = 'validation-message';
                validationMsg.style.cssText = 'margin-top: 8px; padding: 8px 12px; background: #fff3cd; border-left: 4px solid #ffc107; color: #856404; font-size: 0.875rem; border-radius: 4px;';
                validationMsg.innerHTML = '<i class="fas fa-exclamation-circle"></i> ' + message;
                input.parentElement.appendChild(validationMsg);
                input.style.borderColor = '#ffc107';
            } else {
                input.style.borderColor = '';
            }
        }
        
        // ========================================
        // REMINDER TOOLTIP (like employee sign-up)
        // ========================================
        
        function showFieldReminder(field, message) {
            const existingReminder = field.parentElement.querySelector('.field-reminder');
            if (existingReminder) {
                existingReminder.remove();
            }
            
            const reminder = document.createElement('div');
            reminder.className = 'field-reminder';
            reminder.textContent = message;
            reminder.style.cssText = `
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
            `;
            field.parentElement.appendChild(reminder);
            
            setTimeout(() => {
                if (reminder.parentElement) {
                    reminder.remove();
                }
            }, 3000);
        }
        
        // Add CSS for reminder animation
        if (!document.getElementById('reminder-styles')) {
            const style = document.createElement('style');
            style.id = 'reminder-styles';
            style.textContent = `
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
                
                @keyframes shake {
                    0%, 100% { transform: translateX(0); }
                    25% { transform: translateX(-10px); }
                    75% { transform: translateX(10px); }
                }
                
                .shake {
                    animation: shake 0.4s ease-in-out;
                }
            `;
            document.head.appendChild(style);
        }
        
     // ========================================
     // CHECK FORM VALIDITY & UPDATE SUBMIT BUTTON
     // ========================================

     function checkFormValidity() {
         const selectedLeaveType = leaveTypeSelect.value;
         const name = leaveTypeNameInput.value.trim();
         const description = leaveTypeDescriptionTextarea.value.trim();
         
         let hasValidDuration = true;
         let hasValidShift = true;
         
         // Check conditional fields based on leave type
         if (selectedLeaveType === 'Full Day') {
             hasValidDuration = isValidDuration(standardDurationInput.value);
         } else if (selectedLeaveType === 'Half Day') {
             hasValidShift = halfDayShiftSelect.value.trim() !== '';
         } else if (selectedLeaveType === 'Both') {
             hasValidDuration = isValidDuration(standardDurationInput.value);
             hasValidShift = halfDayShiftSelect.value.trim() !== '';
         }
         
         // ✅ UPDATED: Check if name is valid AND has passed AJAX validation
         const isNameValid = name && isValidName(name) && isNameValidated;
         const isDescriptionValid = description && isValidDescription(description);
         const isLeaveTypeSelected = selectedLeaveType && selectedLeaveType.trim() !== '';
         
         // Build list of incomplete fields
         const incompleteFields = [];
         if (!isLeaveTypeSelected) incompleteFields.push('Leave Duration Type');
         if (selectedLeaveType === 'Full Day' && !hasValidDuration) incompleteFields.push('Standard Duration');
         if (selectedLeaveType === 'Half Day' && !hasValidShift) incompleteFields.push('Available Shifts');
         if (selectedLeaveType === 'Both' && !hasValidDuration) incompleteFields.push('Standard Duration');
         if (selectedLeaveType === 'Both' && !hasValidShift) incompleteFields.push('Available Shifts');
         if (!isNameValid) incompleteFields.push('Leave Type Name');
         if (!isDescriptionValid) incompleteFields.push('Description');
         
         // Enable/disable submit button
         const allValid = isLeaveTypeSelected && hasValidDuration && hasValidShift && isNameValid && isDescriptionValid;
         
         // Get tooltip element
         const tooltip = document.getElementById('submit-button-tooltip');
         
         if (allValid) {
             submitBtn.disabled = false;
             submitBtn.title = '';
             
             // Hide tooltip
             if (tooltip) {
                 tooltip.classList.remove('show');
                 tooltip.innerHTML = '';
             }
             
             console.log('✅ All fields valid - Submit enabled');
         } else {
             submitBtn.disabled = true;
             
             // Update tooltip content
             if (tooltip && incompleteFields.length > 0) {
                 tooltip.innerHTML = 'Please complete:<br>• ' + incompleteFields.join('<br>• ');
             } else if (tooltip) {
                 tooltip.textContent = 'Please complete all required fields';
             }
             
             console.log('❌ Submit disabled - Missing:', incompleteFields.join(', '));
         }
     }
        
        // ========================================
        // LEAVE TYPE SELECTION - ENABLE ALL FIELDS
        // ========================================
        
        leaveTypeSelect.addEventListener('change', function() {
		    const selectedValue = this.value;
		    console.log('Leave Type Selected:', selectedValue);
		    
		    if (!selectedValue || selectedValue.trim() === '') {
		        // No selection - disable everything
		        standardDurationInput.disabled = true;
		        halfDayShiftSelect.disabled = true;
		        leaveTypeNameInput.disabled = true;
		        leaveTypeDescriptionTextarea.disabled = true;
		        requiresDocumentCheckbox.disabled = true;
		        requiresDocumentCheckbox.parentElement.classList.add('disabled');
		        affectsBalanceCheckbox.disabled = true;
		        affectsBalanceCheckbox.parentElement.classList.add('disabled');
		        fixedDurationCheckbox.disabled = true;
		        fixedDurationCheckbox.parentElement.classList.add('disabled');
		        
		        showInlineValidation(this, false, 'Please select a leave duration type first');
		    } else {
		        showInlineValidation(this, true, null);
		        
		        // ✅ ENABLE ALL FIELDS (non-blocking approach)
		        leaveTypeNameInput.disabled = false;
		        leaveTypeDescriptionTextarea.disabled = false;
		        requiresDocumentCheckbox.disabled = false;
		        requiresDocumentCheckbox.parentElement.classList.remove('disabled');
		        
		        // Show/hide conditional fields based on type
		        if (selectedValue === 'Full Day') {
		            standardDurationGroup.classList.add('show');
		            standardDurationInput.disabled = false;
		            standardDurationInput.required = true;
		            halfDayShiftGroup.classList.remove('show');
		            halfDayShiftSelect.disabled = true;
		            halfDayShiftSelect.required = false;
		            halfDayShiftSelect.value = '';
		            
		            // ✅ Enable balance tracking for Full Day
		            affectsBalanceCheckbox.disabled = false;
		            affectsBalanceCheckbox.parentElement.classList.remove('disabled');
		            
		            // ✅ ONLY Full Day gets Fixed Duration
		            fixedDurationCheckbox.disabled = false;
		            fixedDurationCheckbox.parentElement.classList.remove('disabled');
		            
		            if (standardDurationInfo) {
		                standardDurationInfo.innerHTML = `
		                    <i class="fas fa-info-circle"></i>
		                    Set the maximum number of days allowed for this leave type. <strong>Must be between 1 and 365 days.</strong>
		                `;
		            }
		            
		        } else if (selectedValue === 'Half Day') {
		            standardDurationGroup.classList.remove('show');
		            standardDurationInput.disabled = true;
		            standardDurationInput.required = false;
		            standardDurationInput.value = '';
		            halfDayShiftGroup.classList.add('show');
		            halfDayShiftSelect.disabled = false;
		            halfDayShiftSelect.required = true;
		            
		            // ❌ Disable balance tracking for Half Day
		            affectsBalanceCheckbox.disabled = true;
		            affectsBalanceCheckbox.parentElement.classList.add('disabled');
		            affectsBalanceCheckbox.checked = false;
		            
		            // ❌ Disable fixed duration for Half Day
		            fixedDurationCheckbox.disabled = true;
		            fixedDurationCheckbox.parentElement.classList.add('disabled');
		            fixedDurationCheckbox.checked = false;
		            
		        } else if (selectedValue === 'Both') {
		            standardDurationGroup.classList.add('show');
		            standardDurationInput.disabled = false;
		            standardDurationInput.required = true;
		            halfDayShiftGroup.classList.add('show');
		            halfDayShiftSelect.disabled = false;
		            halfDayShiftSelect.required = true;
		            
		            // ✅ Enable balance tracking for Both
		            affectsBalanceCheckbox.disabled = false;
		            affectsBalanceCheckbox.parentElement.classList.remove('disabled');
		            
		            // ❌ Disable fixed duration for Both (ambiguous)
		            fixedDurationCheckbox.disabled = true;
		            fixedDurationCheckbox.parentElement.classList.add('disabled');
		            fixedDurationCheckbox.checked = false;
		            
		            if (standardDurationInfo) {
		                standardDurationInfo.innerHTML = `
		                    <i class="fas fa-info-circle"></i>
		                    Set the maximum number of days allowed <strong>when employees choose the full day option</strong>. 
		                    This limit does NOT apply to half day applications. <strong>Must be between 1 and 365 days.</strong>
		                `;
		            }
		            
		        }
		    }
		    
		    checkFormValidity();
		});
        
        // ========================================
        // DURATION VALIDATION
        // ========================================
        
        let durationTimeout;
        standardDurationInput.addEventListener('input', function() {
            clearTimeout(durationTimeout);
            const value = this.value.trim();
            
            durationTimeout = setTimeout(() => {
                if (!value) {
                    showInlineValidation(this, false, 'Standard duration is required');
                    this.classList.add('error');
                    this.classList.remove('valid');
                } else {
                    const duration = parseInt(value);
                    if (isNaN(duration)) {
                        showInlineValidation(this, false, 'Must be a valid number');
                        this.classList.add('error');
                        this.classList.remove('valid');
                    } else if (duration < 1) {
                        showInlineValidation(this, false, 'Minimum is 1 day (you entered ' + duration + ')');
                        this.classList.add('error');
                        this.classList.remove('valid');
                    } else if (duration > 365) {
                        showInlineValidation(this, false, 'Maximum is 365 days (you entered ' + duration + ')');
                        this.classList.add('error');
                        this.classList.remove('valid');
                    } else if (duration !== Math.floor(duration)) {
                        showInlineValidation(this, false, 'Must be a whole number (no decimals)');
                        this.classList.add('error');
                        this.classList.remove('valid');
                    } else {
                        showInlineValidation(this, true, null);
                        this.classList.remove('error');
                        this.classList.add('valid');
                    }
                }
                checkFormValidity();
            }, 300);
        });
        
        // ========================================
        // SHIFT SELECTION VALIDATION
        // ========================================
        
        halfDayShiftSelect.addEventListener('change', function() {
            if (this.value && this.value.trim() !== '') {
                this.classList.remove('error');
                this.classList.add('valid');
                showInlineValidation(this, true, null);
            } else {
                this.classList.add('error');
                this.classList.remove('valid');
                showInlineValidation(this, false, 'Please select available shifts');
            }
            checkFormValidity();
        });
        
     // ========================================
     // NAME VALIDATION WITH AJAX
     // ========================================

     let nameValidationTimeout;
     leaveTypeNameInput.addEventListener('input', function() {
         clearTimeout(nameValidationTimeout);
         const name = this.value.trim();
         
         // ✅ Reset validation state when user types
         isNameValidated = false;
         
         nameValidationTimeout = setTimeout(() => {
             console.log('=== Validating name: "' + name + '" ===');
             
             if (!name) {
                 showInlineValidation(this, false, 'Leave type name is required');
                 this.classList.add('error');
                 this.classList.remove('valid');
                 isNameValidated = false;
                 checkFormValidity();
             } else if (name.length < 3) {
                 showInlineValidation(this, false, 'Must be at least 3 characters long');
                 this.classList.add('error');
                 this.classList.remove('valid');
                 isNameValidated = false;
                 checkFormValidity();
             } else if (name.length > 100) {
                 showInlineValidation(this, false, 'Cannot exceed 100 characters');
                 this.classList.add('error');
                 this.classList.remove('valid');
                 isNameValidated = false;
                 checkFormValidity();
             } else if (!/[a-zA-Z]/.test(name)) {
                 showInlineValidation(this, false, 'Must contain at least one letter');
                 this.classList.add('error');
                 this.classList.remove('valid');
                 isNameValidated = false;
                 checkFormValidity();
             } else if (/[^a-zA-Z0-9\s]/.test(name)) {
                 showInlineValidation(this, false, 'Only letters, numbers, and spaces allowed (no special characters)');
                 this.classList.add('error');
                 this.classList.remove('valid');
                 isNameValidated = false;
                 checkFormValidity();
             } else {
            	    // Check for duplicates via AJAX
            	    console.log('✓ Basic validations passed');
            	    
            	    // Normalize name: collapse multiple spaces to single space
            	    const normalizedName = name.replace(/\s+/g, ' ');
            	    console.log('→ Checking for duplicates: "' + normalizedName + '"');
            	    
            	    const url = '${pageContext.request.contextPath}/CreateLeaveTypeController?action=checkDuplicate&name=' + encodeURIComponent(normalizedName);
                 
                 fetch(url, {
                     method: 'GET',
                     headers: { 'Accept': 'application/json' }
                 })
                 .then(response => {
                     if (!response.ok) throw new Error('HTTP ' + response.status);
                     const contentType = response.headers.get('content-type');
                     if (!contentType || !contentType.includes('application/json')) {
                         throw new Error('Server returned non-JSON response');
                     }
                     return response.json();
                 })
                 .then(data => {
                     console.log('← exists =', data.exists);
                     if (data.exists === true) {
                         console.log('❌ DUPLICATE FOUND!');
                         showInlineValidation(leaveTypeNameInput, false, 'This leave type name already exists - please choose a different name');
                         leaveTypeNameInput.classList.add('error');
                         leaveTypeNameInput.classList.remove('valid');
                         isNameValidated = false; // ✅ Name NOT valid
                     } else {
                         console.log('✅ Name is AVAILABLE!');
                         showInlineValidation(leaveTypeNameInput, true, null);
                         leaveTypeNameInput.classList.remove('error');
                         leaveTypeNameInput.classList.add('valid');
                         isNameValidated = true; // ✅ Name IS valid
                     }
                     checkFormValidity(); // ✅ Update after AJAX completes
                 })
                 .catch(error => {
                     console.error('❌ AJAX Error:', error);
                     showInlineValidation(leaveTypeNameInput, false, 'Could not check if name exists. Please try again.');
                     leaveTypeNameInput.classList.add('error');
                     leaveTypeNameInput.classList.remove('valid');
                     isNameValidated = false; // ✅ Name NOT valid due to error
                     checkFormValidity(); // ✅ Update after error
                 });
             }
         }, 500);
     });
        
        // ========================================
        // DESCRIPTION VALIDATION
        // ========================================
        
        let descriptionTimeout;
        leaveTypeDescriptionTextarea.addEventListener('input', function() {
            clearTimeout(descriptionTimeout);
            const description = this.value.trim();
            
            descriptionTimeout = setTimeout(() => {
                if (!description) {
                    showInlineValidation(this, false, 'Description is required');
                    this.classList.add('error');
                    this.classList.remove('valid');
                } else if (description.length < 10) {
                    showInlineValidation(this, false, 'Must be at least 10 characters long (currently ' + description.length + ' characters)');
                    this.classList.add('error');
                    this.classList.remove('valid');
                } else if (description.length > 500) {
                    showInlineValidation(this, false, 'Cannot exceed 500 characters (currently ' + description.length + ' characters)');
                    this.classList.add('error');
                    this.classList.remove('valid');
                } else if (!/[a-zA-Z]/.test(description)) {
                    showInlineValidation(this, false, 'Must contain at least one letter');
                    this.classList.add('error');
                    this.classList.remove('valid');
                } else {
                    showInlineValidation(this, true, null);
                    this.classList.remove('error');
                    this.classList.add('valid');
                }
                checkFormValidity();
            }, 300);
        });
        
        // ========================================
        // FIELD REMINDER ON FOCUS (like employee sign-up)
        // ========================================
        
        function checkPreviousFields(currentField) {
            const selectedLeaveType = leaveTypeSelect.value;
            
            // Check leave type
            if (!selectedLeaveType || selectedLeaveType.trim() === '') {
                leaveTypeSelect.classList.add('shake', 'error');
                setTimeout(() => leaveTypeSelect.classList.remove('shake'), 400);
                showFieldReminder(leaveTypeSelect, 'Please select Leave Duration Type first');
                return;
            }
            
            // Check duration (if required)
            if ((selectedLeaveType === 'Full Day' || selectedLeaveType === 'Both') && 
                currentField !== standardDurationInput) {
                if (!isValidDuration(standardDurationInput.value)) {
                    standardDurationInput.classList.add('shake', 'error');
                    setTimeout(() => standardDurationInput.classList.remove('shake'), 400);
                    showFieldReminder(standardDurationInput, 'Please fill in Standard Duration');
                    return;
                }
            }
            
            // Check shift (if required)
            if ((selectedLeaveType === 'Half Day' || selectedLeaveType === 'Both') && 
                currentField !== halfDayShiftSelect) {
                if (!halfDayShiftSelect.value || halfDayShiftSelect.value.trim() === '') {
                    halfDayShiftSelect.classList.add('shake', 'error');
                    setTimeout(() => halfDayShiftSelect.classList.remove('shake'), 400);
                    showFieldReminder(halfDayShiftSelect, 'Please select Available Shifts');
                    return;
                }
            }
            
            // Check name
            if (currentField !== leaveTypeNameInput) {
                const name = leaveTypeNameInput.value.trim();
                if (!name || !isValidName(name)) {
                    leaveTypeNameInput.classList.add('shake', 'error');
                    setTimeout(() => leaveTypeNameInput.classList.remove('shake'), 400);
                    showFieldReminder(leaveTypeNameInput, 'Please fill in Leave Type Name');
                    return;
                }
            }
        }
        
        leaveTypeNameInput.addEventListener('focus', function() {
            checkPreviousFields(this);
        });
        
        leaveTypeDescriptionTextarea.addEventListener('focus', function() {
            checkPreviousFields(this);
        });
        
        // ========================================
        // ERROR MODAL FUNCTIONS
        // ========================================
        
        function showErrorModal(errors) {
            errorList.innerHTML = '';
            errors.forEach(error => {
                const li = document.createElement('li');
                li.textContent = error;
                li.style.marginBottom = '8px';
                errorList.appendChild(li);
            });
            
            errorCount.textContent = errors.length;
            errorPlural.textContent = errors.length > 1 ? 's' : '';
            errorModal.style.display = 'flex';
        }
        
        function closeErrorModalHandler() {
            errorModal.style.display = 'none';
        }
        
        if (closeErrorModal) {
            closeErrorModal.addEventListener('click', closeErrorModalHandler);
        }
        if (closeErrorModalBtn) {
            closeErrorModalBtn.addEventListener('click', closeErrorModalHandler);
        }
        
        window.addEventListener('click', function(event) {
            if (event.target === errorModal) {
                closeErrorModalHandler();
            }
        });
        
        document.addEventListener('keydown', function(event) {
            if (event.key === 'Escape' && errorModal.style.display === 'flex') {
                closeErrorModalHandler();
            }
        });
        
        // Check for server-side errors on page load
        <c:if test="${not empty validationErrors}">
            const serverErrors = [
                <c:forEach var="error" items="${validationErrors}" varStatus="status">
                    "${error}"<c:if test="${!status.last}">,</c:if>
                </c:forEach>
            ];
            showErrorModal(serverErrors);
        </c:if>
        
        // ========================================
        // FORM SUBMISSION
        // ========================================
        
        form.addEventListener('submit', function(e) {
            e.preventDefault();
            
            let errors = [];
            
            console.log('=== Final Validation on Submit ===');
            
            // Validate leave type
            if (!leaveTypeSelect.value || leaveTypeSelect.value.trim() === '') {
                errors.push('Please select a leave duration type');
            }
            
            const selectedLeaveType = leaveTypeSelect.value;
            
            // Validate duration (if required)
            if (selectedLeaveType === 'Full Day' || selectedLeaveType === 'Both') {
                if (!standardDurationInput.value || !isValidDuration(standardDurationInput.value)) {
                    errors.push('Standard duration must be between 1 and 365 days (whole number)');
                }
            }
            
            // Validate shift (if required)
            if (selectedLeaveType === 'Half Day' || selectedLeaveType === 'Both') {
                if (!halfDayShiftSelect.value || halfDayShiftSelect.value.trim() === '') {
                    errors.push('Please select available shifts');
                }
            }
            
            // Validate name
            const leaveTypeName = leaveTypeNameInput.value.trim();
            if (!leaveTypeName || !isValidName(leaveTypeName)) {
                errors.push('Leave type name must be 3-100 characters, contain at least one letter, and only use letters, numbers, and spaces');
            }
            
            // Validate description
            const description = leaveTypeDescriptionTextarea.value.trim();
            if (!description || !isValidDescription(description)) {
                errors.push('Description must be 10-500 characters and contain at least one letter');
            }
            
            console.log('Total errors:', errors.length);
            
            if (errors.length > 0) {
                showErrorModal(errors);
            } else {
                submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Creating Leave Type...';
                submitBtn.disabled = true;
                form.submit();
            }
        });
        
     // ========================================
        // SUBMIT BUTTON TOOLTIP ON HOVER
        // ========================================
        
        submitBtn.addEventListener('mouseenter', function() {
            if (this.disabled) {
                const tooltip = document.getElementById('submit-button-tooltip');
                if (tooltip && tooltip.innerHTML) {
                    // Position tooltip above button
                    const rect = this.getBoundingClientRect();
                    tooltip.style.left = (rect.left + rect.width / 2) + 'px';
                    tooltip.style.top = (rect.top - 10) + 'px';
                    tooltip.style.transform = 'translate(-50%, -100%)';
                    
                    // Show tooltip
                    tooltip.classList.add('show');
                }
            }
        });

        submitBtn.addEventListener('mouseleave', function() {
            const tooltip = document.getElementById('submit-button-tooltip');
            if (tooltip) {
                tooltip.classList.remove('show');
            }
        });
        
        // ========================================
        // INITIALIZE TOOLTIP ON PAGE LOAD
        // ========================================
        
        const tooltip = document.getElementById('submit-button-tooltip');
        if (tooltip) {
            tooltip.innerHTML = 'Please complete:<br>• Leave Duration Type<br>• Leave Type Name<br>• Description';
        }
    });
</script>