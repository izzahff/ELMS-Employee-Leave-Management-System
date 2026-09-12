<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="elms.model.LeaveType" %>
<%@ page import="elms.model.FullDay" %>
<%@ page import="elms.model.HalfDay" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%
    String adminId = (String) session.getAttribute("adminId");
    String adminName = (String) session.getAttribute("adminName");
    
    if (adminId == null) {
        response.sendRedirect(request.getContextPath() + "/Admin/AdminLogin.jsp");
        return;
    }
    
    // Get leave type data from request - THESE ARE MAPS, NOT SINGLE OBJECTS
    @SuppressWarnings("unchecked")
    List<LeaveType> leaveTypes = (List<LeaveType>) request.getAttribute("leaveTypes");
    
    @SuppressWarnings("unchecked")
    Map<String, FullDay> fullDayDetails = (Map<String, FullDay>) request.getAttribute("fullDayDetails");
    
    @SuppressWarnings("unchecked")
    Map<String, HalfDay> halfDayDetails = (Map<String, HalfDay>) request.getAttribute("halfDayDetails");
    
    if (leaveTypes == null) {
        response.sendRedirect(request.getContextPath() + "/AdminLeaveTypeListController");
        return;
    }
    
    if (fullDayDetails == null) {
        fullDayDetails = new java.util.HashMap<>();
    }
    if (halfDayDetails == null) {
        halfDayDetails = new java.util.HashMap<>();
    }
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="icon" href="/ELMS_3.0/imnsb_logo.png">
    <title>Leave Types - IMNSB Employee Leave Management System</title>
    <link rel="stylesheet" href="/ELMS_3.0/css/styles.css">
    <link rel="stylesheet" href="/ELMS_3.0/css/dashboard.css">
    <link rel="stylesheet" href="/ELMS_3.0/css/admin.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        /* Action Buttons */
        .action-buttons {
            display: flex;
            gap: 5px;
            justify-content: center;
        }

        /* Admin Card Header */
        .admin-card-header {
            background: linear-gradient(135deg, var(--primary-color), var(--primary-dark));
        }

        .admin-card-header h3 {
            color: white;
        }
        
         /* Search Bar Styles - Matching Manager List */
        .search-bar-container {
            padding: 20px;
            background: white;
            border-bottom: 1px solid #e9ecef;
            display: flex;
            align-items: center;
            gap: 1rem;
        }
        
        .search-box {
            position: relative;
            display: flex;
            align-items: center;
            flex: 1;
            max-width: 500px;
        }

        .search-box input {
            width: 100%;
            padding: 0.75rem 1rem 0.75rem 2.5rem;
            border: 1px solid #ddd;
            border-radius: 8px;
            background: white;
            color: #495057;
            font-size: 0.9rem;
            transition: all 0.3s ease;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
        }
        
        .search-box input:focus {
            outline: none;
            border-color: var(--primary-color);
            box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.1);
        }

        .search-box input::placeholder {
            color: #adb5bd;
        }

        .search-box i {
            position: absolute;
            left: 1rem;
            color: #6c757d;
            z-index: 10;
            pointer-events: none;
        }
        
        .search-stats {
            color: #6c757d;
            font-size: 0.875rem;
            white-space: nowrap;
        }
        
         /* Header Actions Container */
        .admin-card-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 1rem;
        }
        
        .admin-card-header > div {
            display: flex;
            align-items: center;
            gap: 1rem;
        }
        
        /* Search Box in Header */
        .header-search-box {
            position: relative;
            display: flex;
            align-items: center;
        }

        .header-search-box input {
            padding: 0.5rem 1rem 0.5rem 2.5rem;
            border: 1px solid rgba(255, 255, 255, 0.3);
            border-radius: 20px;
            background: rgba(255, 255, 255, 0.1);
            color: white;
            font-size: 0.9rem;
            width: 300px;
            backdrop-filter: blur(10px);
            transition: all 0.3s ease;
        }
        
        .header-search-box input:focus {
            outline: none;
            background: rgba(255, 255, 255, 0.2);
            border-color: rgba(255, 255, 255, 0.5);
        }

        .header-search-box input::placeholder {
            color: rgba(255, 255, 255, 0.7);
        }

        .header-search-box i {
            position: absolute;
            left: 0.8rem;
            color: rgba(255, 255, 255, 0.7);
            z-index: 10;
        }
        
        /* Filters Section */
         /* Filters Section */
        .filters-section {
            padding: 20px;
            background: white;
            border-bottom: 1px solid #e9ecef;
            display: flex;
            align-items: flex-end;  /* Changed from center to flex-end */
            gap: 1rem;
            flex-wrap: wrap;
        }
        
        .filter-group {
            display: flex;
            flex-direction: column;
            gap: 0.5rem;
        }
        
        .filter-group label {
            font-size: 0.875rem;
            font-weight: 500;
            color: #495057;
        }
        
        .filter-group select {
            padding: 0.5rem 2rem 0.5rem 0.75rem;
            border: 1px solid #ddd;
            border-radius: 6px;
            background: white;
            color: #495057;
            font-size: 0.875rem;
            min-width: 180px;
            cursor: pointer;
            transition: all 0.3s ease;
        }
        
        .filter-group select:focus {
            outline: none;
            border-color: var(--primary-color);
            box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.1);
        }
        
        .btn-clear-filters {
            padding: 0.5rem 1rem;
            background: #6c757d;
            color: white;
            border: none;
            border-radius: 6px;
            font-size: 0.875rem;
            cursor: pointer;
            transition: all 0.3s ease;
            height: 38px;  /* Match the height of select dropdowns */
            display: flex;
            align-items: center;
            gap: 0.5rem;
        }
        
        .btn-clear-filters:hover {
            background: #5a6268;
        }

        /* Primary Button */
        .btn-primary {
            background: rgba(255, 255, 255, 0.2);
            color: white;
            border: 1px solid rgba(255, 255, 255, 0.3);
            padding: 0.5rem 1rem;
            border-radius: 6px;
            text-decoration: none;
            display: flex;
            align-items: center;
            gap: 0.5rem;
            font-size: 0.9rem;
            transition: all 0.3s ease;
            backdrop-filter: blur(10px);
        }

        .btn-primary:hover {
            background: rgba(255, 255, 255, 0.3);
            color: white;
            transform: translateY(-1px);
        }
        
        

        /* Edit Button */
        .btn-edit {
            background-color: #28a745;
            color: white;
            border: none;
            padding: 5px 10px;
            border-radius: 3px;
            cursor: pointer;
            font-size: 12px;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 3px;
        }

        .btn-edit:hover {
            background-color: #218838;
            color: white;
            text-decoration: none;
        }

        /* Delete Button */
        .btn-delete {
            background-color: #dc3545;
            color: white;
            border: none;
            padding: 5px 10px;
            border-radius: 3px;
            cursor: pointer;
            font-size: 12px;
        }

        .btn-delete:hover {
            background-color: #c82333;
        }

        /* Leave Type Badges */
        .leave-type-badge {
            display: inline-block;
            padding: 2px 8px;
            border-radius: 12px;
            font-size: 11px;
            font-weight: bold;
            color: white;
        }

        .badge-full-day {
            background-color: #007bff;
        }

        .badge-half-day {
            background-color: #28a745;
        }
        
        .badge-both {
            background-color: #17a2b8;
        }

        /* Document Requirement Badges */
        .badge-required {
            background-color: #dc3545;
            color: white;
            padding: 4px 8px;
            border-radius: 12px;
            font-size: 11px;
            font-weight: bold;
            display: inline-flex;
            align-items: center;
            gap: 4px;
        }

        .badge-optional {
            background-color: #6c757d;
            color: white;
            padding: 4px 8px;
            border-radius: 12px;
            font-size: 11px;
            font-weight: bold;
            display: inline-flex;
            align-items: center;
            gap: 4px;
        }

        /* Balance Impact Badges */
        .badge-balance-yes {
            background-color: #ffc107;
            color: #856404;
            padding: 4px 8px;
            border-radius: 12px;
            font-size: 11px;
            font-weight: bold;
            display: inline-flex;
            align-items: center;
            gap: 4px;
        }

        .badge-balance-no {
            background-color: #28a745;
            color: white;
            padding: 4px 8px;
            border-radius: 12px;
            font-size: 11px;
            font-weight: bold;
            display: inline-flex;
            align-items: center;
            gap: 4px;
        }
        
        /* Fixed Duration Badges */
        .badge-fixed {
            background-color: #dc3545;
            color: white;
            padding: 4px 8px;
            border-radius: 12px;
            font-size: 11px;
            font-weight: bold;
            display: inline-flex;
            align-items: center;
            gap: 4px;
        }

        .badge-flexible {
            background-color: #28a745;
            color: white;
            padding: 4px 8px;
            border-radius: 12px;
            font-size: 11px;
            font-weight: bold;
            display: inline-flex;
            align-items: center;
            gap: 4px;
        }

        /* Details Text */
        .details-text {
            font-size: 12px;
            color: #6c757d;
            margin-top: 2px;
        }

        /* Alert Styles */
        .alert {
            padding: 15px;
            margin-bottom: 20px;
            border: 1px solid transparent;
            border-radius: 4px;
            display: flex;
            align-items: center;
            gap: 0.5rem;
        }

        .alert-danger {
            color: #721c24;
            background-color: #f8d7da;
            border-color: #f5c6cb;
        }

        .alert-success {
            color: #155724;
            background-color: #d4edda;
            border-color: #c3e6cb;
        }

        .alert-info {
            color: #0c5460;
            background-color: #d1ecf1;
            border-color: #bee5eb;
        }

        /* Modal Styles */
        .modal {
            position: fixed;
            z-index: 1000;
            left: 0;
            top: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(0, 0, 0, 0.5);
            animation: fadeIn 0.3s ease-in;
        }

        .modal-content {
            background-color: #ffffff;
            margin: 5% auto;
            border: none;
            border-radius: 8px;
            width: 90%;
            max-width: 600px;
            max-height: 90vh;
            overflow-y: auto;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
            animation: slideIn 0.3s ease-out;
        }

        .modal-header {
            padding: 20px 30px;
            border-bottom: 1px solid #e9ecef;
            display: flex;
            justify-content: space-between;
            align-items: center;
            background-color: #f8f9fa;
            border-radius: 8px 8px 0 0;
        }

        .modal-header h3 {
            margin: 0;
            color: #495057;
            font-size: 1.25rem;
            font-weight: 600;
        }

        .modal-header h3 i {
            margin-right: 8px;
            color: var(--primary-color);
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

        .loading-indicator {
            text-align: center;
            padding: 40px;
            color: #6c757d;
            font-size: 1.1rem;
        }

        .loading-indicator i {
            margin-right: 8px;
            font-size: 1.2rem;
        }

        /* Form Styles in Modal */
        .modal .form-group {
            margin-bottom: 1.5rem;
        }

        .modal .form-group label {
            display: block;
            margin-bottom: 0.5rem;
            font-weight: 500;
            color: #495057;
        }

        .modal .form-group input,
        .modal .form-group select,
        .modal .form-group textarea {
            width: 100%;
            padding: 0.75rem;
            border: 1px solid #ced4da;
            border-radius: 4px;
            font-size: 0.875rem;
            transition: border-color 0.15s ease-in-out, box-shadow 0.15s ease-in-out;
        }

        .modal .form-group input:focus,
        .modal .form-group select:focus,
        .modal .form-group textarea:focus {
            outline: none;
            border-color: var(--primary-color);
            box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, 0.25);
        }

        .modal .form-error {
            color: #dc3545;
            font-size: 0.875rem;
            margin-top: 5px;
            display: none;
        }

        .modal .form-group input.error,
        .modal .form-group select.error,
        .modal .form-group textarea.error {
            border-color: #dc3545;
            box-shadow: 0 0 0 0.2rem rgba(220, 53, 69, 0.25);
        }

        .modal .field-info {
            font-size: 0.875rem;
            color: #6c757d;
            margin-top: 5px;
            font-style: italic;
        }

       /* Checkbox Group - Updated for better layout */
			
		.modal .checkbox-group {
		    display: flex;
		    align-items: flex-start;
		    gap: 12px;
		    padding: 15px;
		    background: white;  /* Changed from #f8f9fa */
		    border-radius: 8px;
		    border: 1px solid #dee2e6;
		    cursor: pointer;
		    transition: all 0.3s ease;
		    text-align: left;
		}
		
		.modal .checkbox-group:hover {
		    background: #f8f9fa;  /* Light background on hover */
		    border-color: #adb5bd;
		}
		
		.modal .checkbox-group input[type="checkbox"] {
		    width: 20px;
		    height: 20px;
		    cursor: pointer;
		    margin: 0;
		    margin-top: 2px;
		    flex-shrink: 0;
		}
		
		.modal .checkbox-group label {
		    margin: 0;
		    cursor: pointer;
		    flex: 1;
		    font-weight: 600;  /* Make it bold */
		    font-size: 1rem;
		    color: #333;
		    text-align: left;
		}
		
		.modal .field-info {
		    font-size: 0.85rem;  /* Slightly smaller */
		    color: #6c757d;
		    margin-top: 10px;
		    margin-left: 0;  /* Remove left margin */
		    padding-left: 15px;
		    font-style: italic;
		    line-height: 1.6;
		    text-align: left;
		}

        /* Form Buttons */
        .modal .form-buttons {
            display: flex;
            gap: 1rem;
            justify-content: flex-end;
            margin-top: 2rem;
            padding-top: 1rem;
            border-top: 1px solid #e9ecef;
        }

        .modal .form-buttons .btn {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            gap: 0.5rem;
            padding: 0.5rem 1rem;
            font-size: 0.875rem;
            font-weight: 500;
            text-decoration: none;
            border-radius: 4px;
            border: none;
            cursor: pointer;
            transition: all 0.2s ease;
            min-width: 120px;
        }

        .modal .btn-secondary {
            background-color: #6c757d;
            color: white;
            border: 1px solid #6c757d;
        }

        .modal .btn-secondary:hover {
            background-color: #545b62;
            border-color: #545b62;
        }

        .modal .btn-primary {
            background-color: var(--primary-color);
            color: white;
            border: 1px solid var(--primary-color);
        }

        .modal .btn-primary:hover {
            background-color: var(--primary-dark);
            border-color: var(--primary-dark);
        }

        .modal .btn-primary:disabled {
            background-color: #6c757d;
            border-color: #6c757d;
            opacity: 0.8;
            cursor: not-allowed;
        }

        /* Conditional Fields */
        .modal .conditional-field {
            display: none;
            animation: fadeIn 0.3s ease-in;
        }

        .modal .conditional-field.show {
            display: block;
        }

        /* Animations */
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
                transform: translateY(-50px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }

        /* Responsive Design */
        @media (max-width: 768px) {
            .modal-content {
                margin: 2% auto;
                width: 95%;
                max-height: 95vh;
            }

            .modal-header {
                padding: 15px 20px;
            }

            .modal-body {
                padding: 20px;
            }

            .modal .form-buttons {
                flex-direction: column;
                gap: 0.75rem;
            }

            .modal .form-buttons .btn {
                width: 100%;
                min-width: auto;
            }
        }
        
        /* Password toggle button */
		#toggleDeletePassword:hover {
		    color: #495057;
		}
		
		#toggleDeletePassword:focus {
		    outline: none;
		    color: #007bff;
		}
        
        /* Modal Styles - Consistent with Employee Leave Application */
		.popup-overlay {
		    position: fixed;
		    top: 0;
		    left: 0;
		    width: 100%;
		    height: 100%;
		    background-color: rgba(0, 0, 0, 0.5);
		    display: flex;
		    justify-content: center;
		    align-items: center;
		    z-index: 10000;
		    animation: fadeIn 0.3s ease-in-out;
		}
		
		.popup-content {
		    background: white;
		    padding: 30px;
		    border-radius: 10px;
		    text-align: center;
		    min-width: 300px;
		    max-width: 600px;
		    max-height: 85vh;
		    overflow-y: auto;
		    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
		    animation: slideIn 0.3s ease-in-out;
		    position: relative;
		}
		
		.popup-icon {
    font-size: 3rem;
    margin-bottom: 15px;
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100%;
    text-align: center; /* Add this */
}
		
		.popup-content h3 {
		    margin: 0 0 15px 0;
		    color: #333;
		    text-align: center;
		}
		
		.popup-content p {
		    margin: 0 0 20px 0;
		    color: #666;
		    line-height: 1.5;
		    text-align: center;
		}
		
		.popup-btn {
		    background-color: #007bff;
		    color: white;
		    border: none;
		    padding: 10px 30px;
		    border-radius: 5px;
		    cursor: pointer;
		    font-size: 1rem;
		    font-weight: 500;
		    transition: background-color 0.3s ease;
		    margin: 5px;
		}
		
		.popup-btn:hover {
		    background-color: #0056b3;
		}
		
		.popup-btn.btn-success {
		    background-color: #28a745;
		}
		
		.popup-btn.btn-success:hover {
		    background-color: #1e7e34;
		}
		
		.success-popup .popup-content {
		    border-top: 5px solid #28a745;
		}
		
		.success-popup .popup-icon {
		    color: #28a745;
		}
		
		.error-popup .popup-content {
		    border-top: 5px solid #dc3545;
		}
		
		.error-popup .popup-icon {
		    color: #dc3545;
		}
		
		.loading-indicator {
		    text-align: center;
		    padding: 40px;
		    color: #6c757d;
		    font-size: 1.1rem;
		}
		
		.loading-indicator i {
		    margin-right: 8px;
		    font-size: 1.5rem;
		    color: #007bff;
		}
		
		@keyframes fadeIn {
		    from { opacity: 0; }
		    to { opacity: 1; }
		}
		
		@keyframes slideIn {
		    from { 
		        transform: translateY(-50px);
		        opacity: 0;
		    }
		    to { 
		        transform: translateY(0);
		        opacity: 1;
		    }
		}
		
		@media (max-width: 768px) {
		    .popup-content {
                margin: 1rem;
                width: calc(100% - 2rem);
                min-width: auto;
            }
            
            .admin-card-header {
                flex-direction: column;
                align-items: flex-start !important;
                gap: 1rem;
            }
            
            .admin-card-header > div {
                width: 100%;
                flex-direction: column;
            }
            
            .header-search-box input {
                width: 100%;
            }
            
            .filters-section {
                flex-direction: column;
                align-items: stretch;
            }
            
            .filter-group {
                width: 100%;
            }
            
            .filter-group select {
                width: 100%;
            }
            
            .filter-stats {
                margin-left: 0;
                text-align: center;
            }
            
           
            .btn-clear-filters {
                width: 100%;
                justify-content: center;
            }
        
		}
		
		/* Form validation states */
		.modal .form-group input.valid,
		.modal .form-group textarea.valid {
		    border-color: #28a745;
		    box-shadow: 0 0 0 0.2rem rgba(40, 167, 69, 0.25);
		}
		
		.modal .form-group input.error,
		.modal .form-group select.error,
		.modal .form-group textarea.error {
		    border-color: #dc3545;
		    box-shadow: 0 0 0 0.2rem rgba(220, 53, 69, 0.25);
		}
		
		/* Success indicator */
		.form-success {
		    color: #28a745;
		    font-size: 0.875rem;
		    margin-top: 5px;
		    display: none;
		    font-weight: 500;
		}
		
		.form-success i {
		    margin-right: 5px;
		}
		
		/* Tooltip for Update Button */
		#edit-submit-button-tooltip {
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
		
		#edit-submit-button-tooltip.show {
		    opacity: 1;
		    visibility: visible;
		}
		
		/* Arrow for tooltip */
		#edit-submit-button-tooltip::after {
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
		
		/* Inline validation message styles */
		.validation-message {
		    margin-top: 8px;
		    padding: 12px 15px;
		    background: linear-gradient(135deg, #fff3cd 0%, #ffeaa7 100%);
		    border-left: 4px solid #ffc107;
		    color: #856404;
		    font-size: 0.875rem;
		    border-radius: 4px;
		    display: flex;
		    align-items: flex-start;
		    gap: 8px;
		    animation: slideDown 0.3s ease-out;
		    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
		}
		
		.validation-message i {
		    margin-top: 2px;
		    flex-shrink: 0;
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
		
    </style>
</head>
<body>
	
    <!-- Hidden divs to store server messages -->
    <div id="hiddenSuccessMessage" style="display:none;"><c:out value="${requestScope.successMessage}" escapeXml="false" /></div>
    <div id="hiddenErrorMessage" style="display:none;"><c:out value="${requestScope.errorMessage}" escapeXml="false" /></div>
    
   
    <div class="dashboard-container">
        <nav class="sidebar" id="sidebar">
            <div class="sidebar-header">
                <img src="/ELMS_3.0/imnsb_logowbg.png" alt="IMNSB Logo" class="sidebar-logo">
                <h3>IMNSB Admin</h3>
            </div>
            <ul class="sidebar-menu">
                <li>
                    <a href="<%= request.getContextPath() %>/admin-dashboard">
                        <i class="fas fa-user"></i> <span>Admin Officer</span>
                    </a>
                </li>
                <li>
                    <a href="/ELMS_3.0/Admin/AdminViewManagerListController">
                        <i class="fas fa-user-tie"></i> <span>Approval Managers</span>
                    </a>
                </li>
                <li>
                    <a href="/ELMS_3.0/Admin/AdminViewEmployeeListController">
                        <i class="fas fa-users"></i> <span>Employees</span>
                    </a>
                </li>
                <li class="active">
                    <a href="/ELMS_3.0/AdminLeaveTypeListController">
                        <i class="fas fa-list-alt"></i> <span>Leave Types</span>
                    </a>
                </li>
                <li>
                    <a href="<%= request.getContextPath() %>/admin-leave-requests">
                        <i class="fas fa-clipboard-list"></i> <span>Leave Requests</span>
                    </a>
                </li>
                <li>
                    <a href="/ELMS_3.0/admin-leave-reports">
                        <i class="fas fa-chart-bar"></i> <span>View Report</span>
                    </a>
                </li>
                <li>
                    <a href="/ELMS_3.0/Admin/AdminSettings.jsp">
                        <i class="fas fa-cog"></i> <span>Settings</span>
                    </a>
                </li>
                <li class="logout">
                    <a href="/ELMS_3.0/Admin/AdminLogin.jsp" id="logoutBtn">
                        <i class="fas fa-sign-out-alt"></i> <span>Logout</span>
                    </a>
                </li>
            </ul>
        </nav>
        
        <main class="main-content" id="mainContent">
            <header class="content-header">
                <div class="header-left">
                    <button id="sidebarToggle" class="sidebar-toggle">
                        <i class="fas fa-bars"></i>
                    </button>
                    <h2>Leave Types</h2>
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
                
                
                <div class="admin-card">
                    <div class="admin-card-header">
                        <div>
                            <h3>Leave Types List</h3>
                        </div>
                        <div>
                            <div class="header-search-box">
                                <i class="fas fa-search"></i>
                                <input type="text" 
                                       id="leaveTypeSearch" 
                                       placeholder="Search leave types..."
                                       autocomplete="off">
                            </div>
                            <a href="<%=request.getContextPath()%>/CreateLeaveTypeController" class="btn btn-primary">
                                <i class="fas fa-plus"></i> Add New Leave Type
                            </a>
                        </div>
                    </div>
                   
                    
                    <!-- Filters Section -->
                    <div class="filters-section">
                        <div class="filter-group">
                            <label for="filterCategory">
                                <i class="fas fa-filter"></i> Category
                            </label>
                            <select id="filterCategory">
                                <option value="">All Categories</option>
                                <option value="Full Day">Full Day</option>
                                <option value="Half Day">Half Day</option>
                                <option value="Both">Both</option>
                            </select>
                        </div>
                        
                        <div class="filter-group">
                            <label for="filterDocument">
                                <i class="fas fa-file-upload"></i> Document Required
                            </label>
                            <select id="filterDocument">
                                <option value="">All</option>
                                <option value="required">Required</option>
                                <option value="optional">Optional</option>
                            </select>
                        </div>
                        
                        <div class="filter-group">
                            <label for="filterBalance">
                                <i class="fas fa-calculator"></i> Affects Balance
                            </label>
                            <select id="filterBalance">
                                <option value="">All</option>
                                <option value="yes">Yes</option>
                                <option value="no">No</option>
                            </select>
                        </div>
                        
                        <div class="filter-group">
                            <label for="filterDuration">
                                <i class="fas fa-lock"></i> Duration Mode
                            </label>
                            <select id="filterDuration">
                                <option value="">All</option>
                                <option value="fixed">Fixed</option>
                                <option value="flexible">Flexible</option>
                            </select>
                        </div>
                        
                        
                        <!-- ADD THIS NEW SORT DROPDOWN -->
                        <div class="filter-group">
                            <label for="sortOrder">
                                <i class="fas fa-sort"></i> Sort By
                            </label>
                            <select id="sortOrder">
                                <option value="name-asc">Name (A-Z)</option>
                                <option value="name-desc">Name (Z-A)</option>
                               
                            </select>
                        </div>
                        
                        <button class="btn-clear-filters" id="clearFilters">
                            <i class="fas fa-times"></i> Clear Filters
                        </button>
                        
                          
                    </div>
                    
                    <div class="admin-card-content">

                        <div class="table-container">
                            <c:choose>
                                <c:when test="${empty leaveTypes}">
                                    <div style="text-align: center; padding: 40px; color: #6c757d;">
                                        <i class="fas fa-list-alt" style="font-size: 48px; margin-bottom: 16px;"></i>
                                        <h4>No Leave Types Found</h4>
                                        <p>Start by creating your first leave type using the "Add New Leave Type" button above.</p>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <table class="admin-table">
                                        <thead>
                                            <tr>
                                                <th>Leave Type</th>
                                                <th>Description</th>
                                                <th>Details</th>
                                                <th>Document Required</th>
                                                <th>Affects Balance</th>
                                                <th>Duration Mode</th>
                                                <th>Actions</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <%
                                            for (LeaveType leaveType : leaveTypes) {
                                                // Get details from the maps using leave type ID
                                                FullDay fullDay = fullDayDetails.get(leaveType.getLeaveTypeId());
                                                HalfDay halfDay = halfDayDetails.get(leaveType.getLeaveTypeId());
                                            %>
                                                <tr data-name="<%= leaveType.getLeaveTypeName().toLowerCase() %>" 
                                                    data-created="<%= leaveType.getLeaveTypeId() %>">
                                                    <td>
                                                        <div>
                                                            <strong><%= leaveType.getLeaveTypeName() %></strong>
                                                            <br>
                                                            <span class="leave-type-badge <%= leaveType.getLeaveTypeCategory().equals("Full Day") ? "badge-full-day" : (leaveType.getLeaveTypeCategory().equals("Both") ? "badge-both" : "badge-half-day") %>">
                                                                <%= leaveType.getLeaveTypeCategory() %>
                                                            </span>
                                                        </div>
                                                    </td>
                                                    <td>
                                                        <div style="max-width: 300px; word-wrap: break-word;">
                                                            <%= leaveType.getLeaveTypeDescription() %>
                                                        </div>
                                                    </td>
                                                    <td>
                                                        <% if ("Full Day".equals(leaveType.getLeaveTypeCategory()) || "Both".equals(leaveType.getLeaveTypeCategory())) { %>
                                                            <div class="details-text">
                                                                <i class="fas fa-calendar-day"></i> 
                                                                Standard Duration: 
                                                                <% if (fullDay != null && fullDay.getStandardDuration() > 0) { %>
                                                                    <%= fullDay.getStandardDuration() %> days
                                                                <% } else { %>
                                                                    Not specified
                                                                <% } %>
                                                            </div>
                                                        <% } %>
                                                        <% if ("Half Day".equals(leaveType.getLeaveTypeCategory()) || "Both".equals(leaveType.getLeaveTypeCategory())) { %>
                                                            <div class="details-text">
                                                                <i class="fas fa-clock"></i> 
                                                                Available Shifts: 
                                                                <% if (halfDay != null) { %>
                                                                    <% if ("Morning".equals(halfDay.getShift())) { %>
                                                                        Morning Only
                                                                    <% } else if ("Afternoon".equals(halfDay.getShift())) { %>
                                                                        Afternoon Only
                                                                    <% } else if ("Both".equals(halfDay.getShift())) { %>
                                                                        Morning & Afternoon
                                                                    <% } else { %>
                                                                        <%= halfDay.getShift() %>
                                                                    <% } %>
                                                                <% } else { %>
                                                                    Shift selection available
                                                                <% } %>
                                                            </div>
                                                        <% } %>
                                                    </td>
                                                    <td style="text-align: center;">
                                                        <% if (leaveType.isRequiresDocument()) { %>
                                                            <span class="badge-required">
                                                                <i class="fas fa-file-upload"></i> Required
                                                            </span>
                                                        <% } else { %>
                                                            <span class="badge-optional">
                                                                <i class="fas fa-minus-circle"></i> Optional
                                                            </span>
                                                        <% } %>
                                                    </td>
                                                    <td style="text-align: center;">
                                                        <% if (leaveType.isAffectsBalance()) { %>
                                                            <span class="badge-balance-yes">
                                                                <i class="fas fa-check-circle"></i> Yes
                                                            </span>
                                                        <% } else { %>
                                                            <span class="badge-balance-no">
                                                                <i class="fas fa-times-circle"></i> No
                                                            </span>
                                                        <% } %>
                                                    </td>
                                                    <td style="text-align: center;">
                                                        <% if (leaveType.isFixedDuration()) { %>
                                                            <span class="badge-fixed">
                                                                <i class="fas fa-lock"></i> Fixed
                                                            </span>
                                                        <% } else { %>
                                                            <span class="badge-flexible">
                                                                <i class="fas fa-unlock"></i> Flexible
                                                            </span>
                                                        <% } %>
                                                    </td>
                                                    <td>
                                                        <div class="action-buttons">
                                                            <button class="btn-edit" 
                                                                    onclick="openEditModal('<%= leaveType.getLeaveTypeId() %>')" 
                                                                    title="Edit Leave Type">
                                                                <i class="fas fa-edit"></i> Edit
                                                            </button>
                                                            
                                                            <button class="btn-delete" 
															        onclick="openDeleteModal('<%= leaveType.getLeaveTypeId() %>', '<%= leaveType.getLeaveTypeName().replace("'", "\\'") %>')" 
															        title="Delete Leave Type">
															    <i class="fas fa-trash"></i> Delete
															</button>
                                                        </div>
                                                    </td>
                                                </tr>
                                            <%
                                            }
                                            %>
                                        </tbody>
                                    </table>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </div>
            </div>
        </main>
    </div> <!--  dashboard container closes-->
    
<!-- Delete Confirmation Modal -->
<div id="deleteLeaveTypeModal" class="popup-overlay" style="display: none;">
    <div class="popup-content" style="max-width: 600px; border-top: 5px solid #dc3545;">
        <div class="modal-body" style="padding: 30px;">
            <!-- Loading Indicator -->
            <div id="deleteLoading" class="loading-indicator" style="display: none;">
                <i class="fas fa-spinner fa-spin"></i> Checking deletion eligibility...
            </div>
            
            <!-- Error Message -->
            <div id="deleteError" style="display: none;">
                <div class="popup-icon" style="color: #dc3545;">❌</div>
                <h3 style="color: #dc3545; margin: 0 0 15px 0;">Error</h3>
                <p id="deleteErrorMessage" style="color: #666; margin: 0 0 20px 0;"></p>
                <button type="button" class="popup-btn" onclick="closeDeleteModalHandler(event)" style="background-color: #dc3545;">
                    <i class="fas fa-times"></i> Close
                </button>
            </div>
            
            <!-- Warning Message (Cannot Delete) -->
            <div id="deleteWarningSection" style="display: none;">
                <div class="popup-icon" style="color: #ffc107;">⚠️</div>
                <h3 style="color: #856404; margin: 0 0 15px 0;">Cannot Delete</h3>
                <p id="deleteWarningMessage" style="color: #666; margin: 0 0 20px 0; line-height: 1.6;"></p>
                <button type="button" class="popup-btn" id="cancelDeleteBtnWarning" onclick="closeDeleteModalHandler(event)" style="background-color: #6c757d;">
                    <i class="fas fa-times"></i> Close
                </button>
            </div>
            
            <!-- Delete Form (Can Delete) -->
            <div id="deleteFormSection" style="display: none;">
		     <div style="text-align: center;">
		    <div class="popup-icon" style="color: #dc3545; font-size: 3rem; margin-bottom: 15px; display: inline-block;">
		        <i class="fas fa-exclamation-triangle"></i>
		    </div>
		</div>
		<h3 style="color: #dc3545; margin: 0 0 15px 0; text-align: center;">Confirm Deletion</h3>
                
                
                <p style="margin-bottom: 15px; color: #666; font-size: 1rem;">
                    Are you sure you want to delete the leave type: 
                    <strong id="deleteLeaveTypeName" style="color: #333;"></strong>?
                </p>
                
                <!-- Impact Summary -->
                <div id="deleteStats" style="display: none; background: #f8f9fa; padding: 15px; border-radius: 8px; margin-bottom: 20px; border: 1px solid #dee2e6;">
                    <h4 style="margin-top: 0; color: #495057; font-size: 1rem; text-align: left;">
                        <i class="fas fa-info-circle"></i> Impact Summary:
                    </h4>
                    <ul style="margin: 10px 0 0 0; padding-left: 20px; text-align: left; color: #495057;">
                        <li id="totalAppsInfo"></li>
                        <li id="approvedAppsInfo"></li>
                        <li id="rejectedAppsInfo"></li>
                    </ul>
                </div>
                
                <!-- Warning Box -->
                <div style="background: linear-gradient(135deg, #fff5f5 0%, #ffe5e5 100%); padding: 15px; border-radius: 8px; margin-bottom: 20px; border-left: 4px solid #dc3545;">
                    <p style="margin: 0; color: #721c24; font-size: 0.9rem; line-height: 1.5;">
                        <i class="fas fa-exclamation-triangle"></i>
                        <strong>Warning:</strong> This action cannot be undone. All associated data will be permanently deleted.
                    </p>
                </div>
                
                <form id="deleteLeaveTypeForm" method="post" action="<%=request.getContextPath()%>/AdminDeleteLeaveTypeController">
                    <input type="hidden" name="action" value="delete">
                    <input type="hidden" id="deleteLeaveTypeId" name="leaveTypeId">
                    <input type="hidden" id="deleteLeaveTypeNameHidden" name="leaveTypeName">
                    
                    <!-- Password Confirmation Field -->
					<div style="margin-bottom: 20px; text-align: left;">
					    <label for="deleteAdminPassword" style="display: block; margin-bottom: 8px; font-weight: 500; color: #495057; font-size: 0.9rem;">
					        <i class="fas fa-lock"></i> Confirm Your Password <span style="color: red;">*</span>
					    </label>
					    <div style="position: relative;">
					        <input type="password" 
					               id="deleteAdminPassword" 
					               name="adminPassword" 
					               placeholder="Enter your admin password to confirm deletion"
					               required
					               style="width: 100%; padding: 12px 45px 12px 12px; border: 1px solid #ced4da; border-radius: 6px; font-size: 0.9rem; box-sizing: border-box;">
					        <button type="button" 
					                id="toggleDeletePassword" 
					                onclick="toggleDeletePasswordVisibility()"
					                style="position: absolute; right: 12px; top: 50%; transform: translateY(-50%); background: none; border: none; color: #6c757d; cursor: pointer; padding: 0; width: 24px; height: 24px; display: flex; align-items: center; justify-content: center;">
					            <i class="fas fa-eye" id="deletePasswordIcon"></i>
					        </button>
					    </div>
					    <div id="deletePasswordError" style="color: #dc3545; font-size: 0.875rem; margin-top: 5px; display: none;"></div>
					    <div style="font-size: 0.875rem; color: #6c757d; margin-top: 8px; font-style: italic;">
					        <i class="fas fa-info-circle"></i>
					        Enter your admin password to authorize this deletion.
					    </div>
					</div>
					<!-- Confirmation Checkbox -->
					<div style="margin-bottom: 20px; text-align: left;">
					    <label class="checkbox-label" style="display: flex; align-items: flex-start; gap: 0.5rem; cursor: pointer; font-size: 0.95rem; line-height: 1.4;">
					        <input type="checkbox" id="confirmLeaveTypeDelete" name="confirmDelete" value="true" required 
					               style="margin-top: 0.2rem; transform: scale(1.2);">
					        <span>I understand this action is permanent and want to delete this leave type.</span>
					    </label>
					</div>
                    
                    <div style="display: flex; gap: 10px; justify-content: center; margin-top: 20px;">
                        <button type="button" class="popup-btn" id="cancelDeleteBtn" onclick="closeDeleteModalHandler(event)" style="background-color: #6c757d;">
                            <i class="fas fa-times"></i> Cancel
                        </button>
                        <button type="submit" class="popup-btn" id="confirmDeleteBtn" disabled style="background-color: #dc3545; opacity: 0.6;">
						    <i class="fas fa-trash"></i> Delete Permanently
						</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<!-- Edit Leave Type Modal -->
<div id="editLeaveTypeModal" class="popup-overlay" style="display: none;">
    <div class="popup-content" style="max-width: 1000px; text-align: center; border-top: 5px solid #007bff; padding-top: 65px;">
        <!-- Close button -->
        <button class="close" id="closeEditModal" style="position: absolute; top: 15px; right: 20px; background: none; border: none; font-size: 28px; color: #6c757d; cursor: pointer; z-index: 1; line-height: 1;">&times;</button>
        
        <div class="popup-icon" style="color: #007bff; font-size: 2.5rem; margin-bottom: 10px;">
            <i class="fas fa-edit"></i>
        </div>
        <h3 style="color: #333; margin: 0 0 20px 0;">Edit Leave Type</h3>
        
        <div id="modalLoading" class="loading-indicator" style="display: none;">
            <i class="fas fa-spinner fa-spin"></i> Loading leave type data...
        </div>
        
        <div id="modalError" style="display: none; background: linear-gradient(135deg, #fff5f5 0%, #ffe5e5 100%); padding: 15px; border-radius: 8px; margin-bottom: 20px; border-left: 4px solid #dc3545;">
            <p style="margin: 0; color: #721c24;">
                <i class="fas fa-exclamation-circle"></i>
                <span id="modalErrorMessage"></span>
            </p>
        </div>
        
        <form id="editLeaveTypeForm" class="admin-form" method="post" 
              action="<%=request.getContextPath()%>/AdminEditLeaveTypeController" style="display: none; text-align: left;">
            <input type="hidden" name="action" value="updateLeaveType">
            <input type="hidden" id="editLeaveTypeId" name="leaveTypeId">
            <input type="hidden" id="editLeaveTypeHidden" name="leaveType">
            <input type="hidden" id="editStandardDurationHidden" name="standardDuration">
            <input type="hidden" id="editAffectsBalanceHidden" name="affectsBalance">
            
            <!-- READ-ONLY: Leave Duration Type -->
			<div class="form-group">
			    <label>
			        <i class="fas fa-clock"></i> Leave Duration Type
			    </label>
			    <div style="padding: 0.75rem; background: #f8f9fa; border: 1px solid #dee2e6; border-radius: 6px; color: #495057; display: flex; align-items: center; gap: 8px;">
			        <i class="fas fa-lock" style="color: #6c757d;"></i>
			        <strong id="displayLeaveType" style="color: #333;">Full Day</strong>
			    </div>
			    <div style="background: #fff3cd; padding: 10px; border-radius: 6px; margin-top: 8px; border-left: 4px solid #ffc107;">
			        <p style="margin: 0; color: #856404; font-size: 0.85rem; line-height: 1.5;">
			            <i class="fas fa-info-circle"></i>
			            Cannot be changed after creation to maintain data integrity with existing applications
			        </p>
			    </div>
			</div>
            
            <!-- READ-ONLY or HIDDEN: Standard Duration -->
			<div class="form-group" id="editStandardDurationGroup" style="display: none;">
			    <label>
			        <i class="fas fa-calendar-alt"></i> Standard Duration (Days)
			    </label>
			    <div id="standardDurationDisplay" style="padding: 0.75rem; background: #f8f9fa; border: 1px solid #dee2e6; border-radius: 6px; color: #495057; display: flex; align-items: center; gap: 8px;">
			        <i class="fas fa-lock" style="color: #6c757d;"></i>
			        <strong id="displayStandardDuration" style="color: #333;">0</strong> days
			    </div>
			    <div style="background: #fff3cd; padding: 10px; border-radius: 6px; margin-top: 8px; border-left: 4px solid #ffc107;">
			        <p style="margin: 0; color: #856404; font-size: 0.85rem; line-height: 1.5;">
			            <i class="fas fa-info-circle"></i>
			            Cannot be changed: Existing leave applications and employee entitlements depend on this setting. Changing it would cause data inconsistency
			        </p>
			    </div>
			</div>
            
           <!-- EDITABLE: Half Day Shift Configuration -->
			<div class="form-group" id="editHalfDayShiftGroup" style="display: none;">
			    <label for="editHalfDayShift">
			        <i class="fas fa-sun"></i> Available Shifts <span style="color: red;">*</span>
			    </label>
			    <select id="editHalfDayShift" name="halfDayShift">
			        <option value="">Select Available Shifts</option>
			        <option value="Morning">Morning Shift Only</option>
			        <option value="Afternoon">Afternoon Shift Only</option>
			        <option value="Both">Both Morning & Afternoon</option>
			    </select>
			    <div style="background: #d1ecf1; padding: 10px; border-radius: 6px; margin-top: 8px; border-left: 4px solid #17a2b8;">
			        <p style="margin: 0; color: #0c5460; font-size: 0.85rem; line-height: 1.5;">
			            <i class="fas fa-info-circle"></i>
			            Choose which shifts employees can select when applying for half day leave
			        </p>
			    </div>
			    <div class="form-error" id="editHalfDayShiftError"></div>
			</div>
            
            <!-- EDITABLE: Leave Type Name -->
			<div class="form-group">
			    <label for="editLeaveTypeName">
			        <i class="fas fa-tag"></i> Leave Type Name <span style="color: red;">*</span>
			    </label>
			    <input type="text" id="editLeaveTypeName" name="leaveTypeName" required 
			           placeholder="Enter leave type name (e.g., Annual Leave, Sick Leave)">
			   
			    <!-- ✅ Success message (inline validation - keep as is) -->
			    <div class="form-success" id="editLeaveTypeNameSuccess" style="display: none; margin-top: 8px; padding: 12px 15px; background: linear-gradient(135deg, #d4edda 0%, #c3e6cb 100%); border-left: 4px solid #28a745; color: #155724; font-size: 0.875rem; border-radius: 4px; display: none; align-items: center; gap: 8px; animation: slideDown 0.3s ease-out; box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);">
			        <i class="fas fa-check-circle" style="color: #28a745;"></i>
			        <span>This name is available</span>
			    </div>
			</div>
            
            <!-- EDITABLE: Description -->
            <div class="form-group">
                <label for="editLeaveTypeDescription">
                    <i class="fas fa-align-left"></i> Description <span style="color: red;">*</span>
                </label>
                <textarea id="editLeaveTypeDescription" name="leaveTypeDescription" rows="4" required 
                          placeholder="Enter detailed description of this leave type"></textarea>
                <div class="form-error" id="editLeaveTypeDescriptionError"></div>
            </div>
            
			<!-- EDITABLE: Requires Document -->
			<div class="form-group" style="margin-bottom: 1.5rem;">
			    <div class="checkbox-group" style="border: 1px solid #dee2e6; padding: 15px; border-radius: 8px; background: white; display: flex; align-items: center; gap: 12px;">
			        <input type="checkbox" id="editRequiresDocument" name="requiresDocument" style="width: 20px; height: 20px; cursor: pointer; margin: 0; flex-shrink: 0;">
			        <label for="editRequiresDocument" style="font-weight: 600; color: #333; margin: 0; cursor: pointer; display: flex; align-items: center; gap: 8px; font-size: 1rem;">
			            <i class="fas fa-file-upload"></i> Require Supporting Document
			        </label>
			    </div>
			    <div style="background: #d1ecf1; padding: 10px; border-radius: 6px; margin-top: 8px; border-left: 4px solid #17a2b8;">
			        <p style="margin: 0; color: #0c5460; font-size: 0.85rem; line-height: 1.5;">
			            <i class="fas fa-info-circle"></i>
			            Check this box if employees must upload supporting documents when applying for this leave type
			        </p>
			    </div>
			</div>
            
            <!-- READ-ONLY: Affects Balance -->
            <div class="form-group" style="margin-bottom: 1.5rem;">
                <div class="checkbox-group" style="border: 1px solid #dee2e6; padding: 15px; border-radius: 8px; background: #f8f9fa; display: flex; align-items: center; gap: 12px; opacity: 0.7; cursor: not-allowed;">
                    <input type="checkbox" id="editAffectsBalance" name="affectsBalanceDisplay" disabled style="width: 20px; height: 20px; margin: 0; flex-shrink: 0; cursor: not-allowed;">
                    <label for="editAffectsBalance" style="font-weight: 600; color: #6c757d; margin: 0; cursor: not-allowed; display: flex; align-items: center; gap: 8px; font-size: 1rem;">
                        <i class="fas fa-calculator"></i> Track Leave Balance (Deduct from Annual Entitlement)
                    </label>
                </div>
                <div style="background: #fff3cd; padding: 10px; border-radius: 6px; margin-top: 8px; border-left: 4px solid #ffc107;">
                    <p style="margin: 0; color: #856404; font-size: 0.85rem; line-height: 1.5;">
                        <i class="fas fa-lock"></i>
                        <strong>Cannot be changed:</strong> Existing employee leave balances and applications depend on this setting. Changing it would cause data inconsistency.
                    </p>
                </div>
            </div>
            
            <!-- EDITABLE WITH WARNING: Fixed Duration -->
			<div class="form-group" style="margin-bottom: 1.5rem;">
			    <div class="checkbox-group" style="border: 1px solid #dee2e6; padding: 15px; border-radius: 8px; background: white; display: flex; align-items: center; gap: 12px;">
			        <input type="checkbox" id="editFixedDuration" name="fixedDuration" style="width: 20px; height: 20px; cursor: pointer; margin: 0; flex-shrink: 0;">
			        <label for="editFixedDuration" style="font-weight: 600; color: #333; margin: 0; cursor: pointer; display: flex; align-items: center; gap: 8px; font-size: 1rem;">
			            <i class="fas fa-lock"></i> Fixed Duration (Must Take Exact Amount)
			        </label>
			    </div>
			    <div style="background: #d1ecf1; padding: 10px; border-radius: 6px; margin-top: 8px; border-left: 4px solid #17a2b8;">
			        <p style="margin: 0; color: #0c5460; font-size: 0.85rem; line-height: 1.5;">
			            <i class="fas fa-info-circle"></i>
			            Check this box if employees must take the exact standard duration amount. <strong>Note: This option is only available for "Full Day" leave types.</strong>
			        </p>
			    </div>
			    <div id="fixedDurationWarning" style="display: none; background: #fff3cd; padding: 12px; border-radius: 6px; margin-top: 8px; border-left: 4px solid #ffc107;">
			        <p style="margin: 0; color: #856404; font-size: 0.85rem; line-height: 1.5;">
			            <i class="fas fa-exclamation-triangle"></i>
			            <strong>Warning:</strong> This leave type has <strong id="applicationCount">0</strong> existing applications. Changing this setting may affect employee expectations and future applications.
			        </p>
			    </div>
			</div>
            
            <div class="form-buttons" style="display: flex; gap: 10px; justify-content: center; margin-top: 30px; padding-top: 20px; border-top: 1px solid #e9ecef;">
                <button type="button" class="popup-btn" id="cancelEditBtn" style="background-color: #6c757d;">
                    <i class="fas fa-times"></i> Cancel
                </button>
                <button type="submit" class="popup-btn" id="updateLeaveTypeBtn" style="background-color: #007bff;">
                    <i class="fas fa-save"></i> Update Leave Type
                </button>
            </div>
        </form>
    </div>
</div>
</div>
    <!-- Success Modal -->
		<div id="successModal" class="popup-overlay success-popup" style="display: none;">
		    <div class="popup-content" style="max-width: 600px;">
		        <div class="popup-icon">✅</div>
		        <h3>Success!</h3>
		        <div id="successMessage" style="text-align: left; margin: 0 0 20px 0;"></div>
		        <button class="popup-btn btn-success" onclick="closeSuccessModal()">Continue</button>
		    </div>
		</div>
		
		<!-- Error Modal (similar to Employee Leave Application) -->
		<div id="errorModal" class="popup-overlay error-popup" style="display: none;">
		    <div class="popup-content">
		        <div class="popup-icon">❌</div>
		        <h3>Error</h3>
		        <p id="errorMessage"></p>
		        <button class="popup-btn" onclick="closeErrorModal()">Close</button>
		    </div>
		</div>

   <div id="edit-submit-button-tooltip"></div>
    
<script src="/ELMS_3.0/Admin/Admin.js"></script>
<script>
// ============================================
// SUCCESS/ERROR MODAL FUNCTIONS - MUST BE DEFINED FIRST
// ============================================
function showSuccessModal(message) {
const successModal = document.getElementById('successModal');
const successMessage = document.getElementById('successMessage');
if (successModal && successMessage) {
successMessage.innerHTML = message;
successModal.style.display = 'flex';
}
}
function closeSuccessModal() {
const successModal = document.getElementById('successModal');
if (successModal) {
successModal.style.display = 'none';
}
// Check if there's a new leave type ID to scroll to
var newLeaveTypeId = '<c:out value="${requestScope.newLeaveTypeId}" escapeXml="true" />';

if (newLeaveTypeId && newLeaveTypeId.trim() !== '') {
    window.location.reload();
} else {
    window.location.reload();
}
}
function showErrorModal(message) {
const errorModal = document.getElementById('errorModal');
const errorMessage = document.getElementById('errorMessage');
if (errorModal && errorMessage) {
errorMessage.textContent = message;
errorModal.style.display = 'flex';
}
}
function closeErrorModal() {
const errorModal = document.getElementById('errorModal');
if (errorModal) {
errorModal.style.display = 'none';
}
}
// Close modals when clicking outside
window.addEventListener('click', function(event) {
const modals = ['successModal', 'errorModal'];
modals.forEach(modalId => {
const modal = document.getElementById(modalId);
if (modal && event.target === modal) {
modal.style.display = 'none';
}
});
});
// Close modals with Escape key
document.addEventListener('keydown', function(event) {
if (event.key === 'Escape') {
const modals = ['successModal', 'errorModal'];
modals.forEach(modalId => {
const modal = document.getElementById(modalId);
if (modal && modal.style.display === 'flex') {
modal.style.display = 'none';
}
});
}
});
// ============================================
// CHECK FOR SERVER MESSAGES - RUNS IMMEDIATELY ON PAGE LOAD
// ============================================
document.addEventListener('DOMContentLoaded', function() {
console.log('=== Checking for server messages ===');
var successMsgDiv = document.getElementById('hiddenSuccessMessage');
var errorMsgDiv = document.getElementById('hiddenErrorMessage');

if (successMsgDiv && successMsgDiv.innerHTML.trim() !== '') {
    console.log('✅ Success message found');
    showSuccessModal(successMsgDiv.innerHTML);
}

if (errorMsgDiv && errorMsgDiv.innerHTML.trim() !== '') {
    console.log('❌ Error message found');
    showErrorModal(errorMsgDiv.innerHTML);
}

if ((!successMsgDiv || successMsgDiv.innerHTML.trim() === '') && 
    (!errorMsgDiv || errorMsgDiv.innerHTML.trim() === '')) {
    console.log('ℹ️ No messages found');
}
});
//============================================
//EDIT FORM: GLOBAL VARIABLES
//============================================
let editNameValidationTimeout;
let isEditNameValidated = false;
let originalLeaveTypeName = '';
let editLeaveTypeNameInput;
let editLeaveTypeDescriptionTextarea;
//============================================
// HELPER FUNCTIONS
//============================================
// Helper function to clear edit errors
function clearEditError(errorId) {
const errorElement = document.getElementById(errorId);
if (errorElement) {
errorElement.textContent = '';
errorElement.style.display = 'none';
}
// Also remove inline validation message
const inputElement = document.getElementById(errorId.replace('Error', ''));
if (inputElement) {
    const existingMsg = inputElement.parentElement.querySelector('.validation-message');
    if (existingMsg) {
        existingMsg.remove();
    }
}
}
// Helper function to show edit errors
function showEditError(elementId, message) {
// Just show the inline validation message
const inputElement = document.getElementById(elementId.replace('Error', ''));
if (inputElement) {
const existingMsg = inputElement.parentElement.querySelector('.validation-message');
if (existingMsg) {
existingMsg.remove();
}
    if (message) {
        const validationMsg = document.createElement('div');
        validationMsg.className = 'validation-message';
        validationMsg.innerHTML = '<i class="fas fa-exclamation-circle"></i> <span>' + message + '</span>';
        inputElement.parentElement.appendChild(validationMsg);
    }
}
}
// Function to update tooltip dynamically
function updateTooltip() {
const tooltip = document.getElementById('edit-submit-button-tooltip');
const updateBtn = document.getElementById('updateLeaveTypeBtn');
if (!tooltip || !updateBtn) return;

const incompleteFields = [];

const leaveTypeHidden = document.getElementById('editLeaveTypeHidden');
const selectedLeaveType = leaveTypeHidden ? leaveTypeHidden.value : '';

// Check name validation
if (!isEditNameValidated) {
    const nameValue = editLeaveTypeNameInput ? editLeaveTypeNameInput.value.trim() : '';
    if (!nameValue) {
        incompleteFields.push('Leave Type Name');
    } else {
        // Check if there's a validation error visible
        const hasValidationError = editLeaveTypeNameInput && 
            editLeaveTypeNameInput.parentElement.querySelector('.validation-message');
        if (hasValidationError) {
            incompleteFields.push('Leave Type Name');
        } else {
            incompleteFields.push('Leave Type Name (validation pending)');
        }
    }
}

// Check description
const descriptionTextarea = document.getElementById('editLeaveTypeDescription');
const descValue = descriptionTextarea ? descriptionTextarea.value.trim() : '';
if (!descValue) {
    incompleteFields.push('Description');
} else if (descValue.length < 10) {
    incompleteFields.push('Description (min 10 characters)');
}

// Check half day shift if required
const halfDayShiftSelect = document.getElementById('editHalfDayShift');
if ((selectedLeaveType === 'Half Day' || selectedLeaveType === 'Both') && 
    halfDayShiftSelect && !halfDayShiftSelect.value.trim()) {
    incompleteFields.push('Available Shifts');
}

// Update tooltip and button state
if (incompleteFields.length > 0) {
    tooltip.innerHTML = 'Please complete:<br>• ' + incompleteFields.join('<br>• ');
    updateBtn.disabled = true;
    updateBtn.style.opacity = '0.6';
} else {
    tooltip.innerHTML = '';
    updateBtn.disabled = false;
    updateBtn.style.opacity = '1';
}
}
//============================================
//EDIT FORM: NAME DUPLICATE CHECK WITH SPACE NORMALIZATION
//============================================
// Function to check for duplicate names during edit
function checkEditNameDuplicate(newName, leaveTypeId) {
clearTimeout(editNameValidationTimeout);
// Get the update button
const updateBtn = document.getElementById('updateLeaveTypeBtn');
const successMsg = document.getElementById('editLeaveTypeNameSuccess');

// Normalize the name: trim and collapse multiple spaces
const normalizedName = newName.trim().replace(/\s+/g, ' ');

// Don't check if name hasn't changed
if (normalizedName.toLowerCase() === originalLeaveTypeName.toLowerCase()) {
    isEditNameValidated = true;
    clearEditError('editLeaveTypeNameError');
    if (editLeaveTypeNameInput) {
        editLeaveTypeNameInput.classList.remove('error');
        editLeaveTypeNameInput.classList.remove('valid');
    }
    if (successMsg) successMsg.style.display = 'none';
    if (updateBtn) {
        updateBtn.disabled = false;
        updateBtn.style.opacity = '1';
    }
    updateTooltip();
    return;
}

editNameValidationTimeout = setTimeout(() => {
    console.log('🔍 Checking duplicate for edit: "' + normalizedName + '"');
    
    // Basic validation first
    if (!normalizedName) {
        showEditError('editLeaveTypeNameError', 'Leave type name is required');
        if (editLeaveTypeNameInput) {
            editLeaveTypeNameInput.classList.add('error');
            editLeaveTypeNameInput.classList.remove('valid');
        }
        if (successMsg) successMsg.style.display = 'none';
        if (updateBtn) {
            updateBtn.disabled = true;
            updateBtn.style.opacity = '0.6';
        }
        isEditNameValidated = false;
        updateTooltip();
        return;
    }
    
    if (normalizedName.length < 3) {
        showEditError('editLeaveTypeNameError', 'Must be at least 3 characters long');
        if (editLeaveTypeNameInput) {
            editLeaveTypeNameInput.classList.add('error');
            editLeaveTypeNameInput.classList.remove('valid');
        }
        if (successMsg) successMsg.style.display = 'none';
        if (updateBtn) {
            updateBtn.disabled = true;
            updateBtn.style.opacity = '0.6';
        }
        isEditNameValidated = false;
        updateTooltip();
        return;
    }
    
    if (normalizedName.length > 100) {
        showEditError('editLeaveTypeNameError', 'Cannot exceed 100 characters');
        if (editLeaveTypeNameInput) {
            editLeaveTypeNameInput.classList.add('error');
            editLeaveTypeNameInput.classList.remove('valid');
        }
        if (successMsg) successMsg.style.display = 'none';
        if (updateBtn) {
            updateBtn.disabled = true;
            updateBtn.style.opacity = '0.6';
        }
        isEditNameValidated = false;
        updateTooltip();
        return;
    }
    
    if (!/[a-zA-Z]/.test(normalizedName)) {
        showEditError('editLeaveTypeNameError', 'Must contain at least one letter');
        if (editLeaveTypeNameInput) {
            editLeaveTypeNameInput.classList.add('error');
            editLeaveTypeNameInput.classList.remove('valid');
        }
        if (successMsg) successMsg.style.display = 'none';
        if (updateBtn) {
            updateBtn.disabled = true;
            updateBtn.style.opacity = '0.6';
        }
        isEditNameValidated = false;
        updateTooltip();
        return;
    }
    
    if (/[^a-zA-Z0-9\s]/.test(normalizedName)) {
        showEditError('editLeaveTypeNameError', 'Only letters, numbers, and spaces allowed');
        if (editLeaveTypeNameInput) {
            editLeaveTypeNameInput.classList.add('error');
            editLeaveTypeNameInput.classList.remove('valid');
        }
        if (successMsg) successMsg.style.display = 'none';
        if (updateBtn) {
            updateBtn.disabled = true;
            updateBtn.style.opacity = '0.6';
        }
        isEditNameValidated = false;
        updateTooltip();
        return;
    }
    
    // AJAX duplicate check
    const checkUrl = '<%=request.getContextPath()%>/CreateLeaveTypeController?action=checkDuplicate&name=' + 
                    encodeURIComponent(normalizedName) + 
                    '&excludeId=' + encodeURIComponent(leaveTypeId);
    
    fetch(checkUrl, {
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
        console.log('← Duplicate check result:', data.exists);
        
        if (data.exists === true) {
            console.log('❌ DUPLICATE FOUND!');
            
            if (editLeaveTypeNameInput) {
                editLeaveTypeNameInput.classList.add('error');
                editLeaveTypeNameInput.classList.remove('valid');
                
                // Remove existing validation message
                const existingMsg = editLeaveTypeNameInput.parentElement.querySelector('.validation-message');
                if (existingMsg) {
                    existingMsg.remove();
                }
                
                // Add new validation message with warning icon
                const validationMsg = document.createElement('div');
                validationMsg.className = 'validation-message';
                validationMsg.innerHTML = '<i class="fas fa-exclamation-circle"></i> <span>This leave type name already exists - please choose a different name</span>';
                editLeaveTypeNameInput.parentElement.appendChild(validationMsg);
            }
            
            if (successMsg) successMsg.style.display = 'none';
            
            if (updateBtn) {
                updateBtn.disabled = true;
                updateBtn.style.opacity = '0.6';
            }
            
            isEditNameValidated = false;
            updateTooltip();
            
        } else {
            console.log('✅ Name is AVAILABLE!');
            clearEditError('editLeaveTypeNameError');
            
            if (editLeaveTypeNameInput) {
                editLeaveTypeNameInput.classList.remove('error');
                editLeaveTypeNameInput.classList.add('valid');
            }
            
            if (successMsg) successMsg.style.display = 'flex';
            
            if (updateBtn) {
                updateBtn.disabled = false;
                updateBtn.style.opacity = '1';
            }
            
            isEditNameValidated = true;
            updateTooltip();
        }
    })
    .catch(error => {
        console.error('❌ AJAX Error:', error);
        
        if (editLeaveTypeNameInput) {
            editLeaveTypeNameInput.classList.add('error');
            editLeaveTypeNameInput.classList.remove('valid');
            
            const existingMsg = editLeaveTypeNameInput.parentElement.querySelector('.validation-message');
            if (existingMsg) {
                existingMsg.remove();
            }
            
            const validationMsg = document.createElement('div');
            validationMsg.className = 'validation-message';
            validationMsg.innerHTML = '<i class="fas fa-exclamation-circle"></i> <span>Could not check if name exists. Please try again.</span>';
            editLeaveTypeNameInput.parentElement.appendChild(validationMsg);
        }
        
        if (successMsg) successMsg.style.display = 'none';
        
        if (updateBtn) {
            updateBtn.disabled = true;
            updateBtn.style.opacity = '0.6';
        }
        
        isEditNameValidated = false;
        updateTooltip();
    });
}, 500); // Debounce delay
}
// ============================================
// EDIT AND DELETE MODAL LOGIC
// ============================================
document.addEventListener('DOMContentLoaded', function() {
console.log('=== AdminLeaveTypeList.jsp loaded ===');
// Modal elements
const editModal = document.getElementById('editLeaveTypeModal');
const closeEditModalBtn = document.getElementById('closeEditModal');
const cancelEditBtn = document.getElementById('cancelEditBtn');
const editForm = document.getElementById('editLeaveTypeForm');
const modalLoading = document.getElementById('modalLoading');
const modalError = document.getElementById('modalError');
const modalErrorMessage = document.getElementById('modalErrorMessage');

// Form elements - Assign to global variables
editLeaveTypeNameInput = document.getElementById('editLeaveTypeName');
editLeaveTypeDescriptionTextarea = document.getElementById('editLeaveTypeDescription');
const editHalfDayShiftGroup = document.getElementById('editHalfDayShiftGroup');
const editHalfDayShiftSelect = document.getElementById('editHalfDayShift');
const updateBtn = document.getElementById('updateLeaveTypeBtn');

// Add name validation listener
if (editLeaveTypeNameInput) {
    editLeaveTypeNameInput.addEventListener('input', function() {
        const leaveTypeId = document.getElementById('editLeaveTypeId')?.value;
        if (leaveTypeId) {
            checkEditNameDuplicate(this.value, leaveTypeId);
        }
    });
}

// Add description validation listener with inline messages
let descriptionValidationTimeout;
if (editLeaveTypeDescriptionTextarea) {
    editLeaveTypeDescriptionTextarea.addEventListener('input', function() {
        clearTimeout(descriptionValidationTimeout);
        
        const description = this.value.trim();
        
        descriptionValidationTimeout = setTimeout(() => {
            // Clear previous validation messages
            const existingMsg = this.parentElement.querySelector('.validation-message');
            if (existingMsg) {
                existingMsg.remove();
            }
            
            // Remove existing success/error classes
            this.classList.remove('error', 'valid');
            
            // Validate description
            if (!description) {
                const validationMsg = document.createElement('div');
                validationMsg.className = 'validation-message';
                validationMsg.innerHTML = '<i class="fas fa-exclamation-circle"></i> <span>Description is required</span>';
                this.parentElement.appendChild(validationMsg);
                this.classList.add('error');
            } else if (description.length < 10) {
                const validationMsg = document.createElement('div');
                validationMsg.className = 'validation-message';
                validationMsg.innerHTML = '<i class="fas fa-exclamation-circle"></i> <span>Description must be at least 10 characters long (currently ' + description.length + ' characters)</span>';
                this.parentElement.appendChild(validationMsg);
                this.classList.add('error');
            } else if (description.length > 500) {
                const validationMsg = document.createElement('div');
                validationMsg.className = 'validation-message';
                validationMsg.innerHTML = '<i class="fas fa-exclamation-circle"></i> <span>Description cannot exceed 500 characters (currently ' + description.length + ' characters)</span>';
                this.parentElement.appendChild(validationMsg);
                this.classList.add('error');
            } else if (!/[a-zA-Z]/.test(description)) {
                const validationMsg = document.createElement('div');
                validationMsg.className = 'validation-message';
                validationMsg.innerHTML = '<i class="fas fa-exclamation-circle"></i> <span>Description must contain at least one letter</span>';
                this.parentElement.appendChild(validationMsg);
                this.classList.add('error');
            } else {
                // Valid description - show green border
                this.classList.add('valid');
            }
            
            // Update tooltip after validation
            updateTooltip();
        }, 300); // Debounce delay
    });
}

// Add shift validation listener
if (editHalfDayShiftSelect) {
    editHalfDayShiftSelect.addEventListener('change', function() {
        updateTooltip(); // Update tooltip when shift changes
    });
}

// Function to open edit modal
window.openEditModal = function(leaveTypeId) {
    console.log('=== Opening edit modal for leave type ID:', leaveTypeId);
    
    if (!editModal) {
        console.error('Edit modal not found');
        return;
    }
    
    // Show modal and loading indicator
    editModal.style.display = 'flex';
    if (modalLoading) modalLoading.style.display = 'block';
    if (editForm) editForm.style.display = 'none';
    if (modalError) modalError.style.display = 'none';
    
    // Reset form
    if (editForm) editForm.reset();
    clearEditValidationErrors();
    
    // Build the URL for the AJAX request
    const ajaxUrl = '<%=request.getContextPath()%>/AdminEditLeaveTypeController?action=getLeaveTypeData&leaveTypeId=' + encodeURIComponent(leaveTypeId);
    console.log('AJAX URL:', ajaxUrl);
    
    // Fetch leave type data
    fetch(ajaxUrl)
        .then(response => {
            console.log('Response status:', response.status);
            
            if (!response.ok) {
                throw new Error('HTTP error! status: ' + response.status);
            }
            
            return response.json();
        })
        .then(data => {
            console.log('Parsed response data:', data);
            
            if (modalLoading) modalLoading.style.display = 'none';
            
            if (data.success) {
                // Set hidden fields
                const editLeaveTypeIdInput = document.getElementById('editLeaveTypeId');
                const editLeaveTypeHidden = document.getElementById('editLeaveTypeHidden');
                const editStandardDurationHidden = document.getElementById('editStandardDurationHidden');
                const editAffectsBalanceHidden = document.getElementById('editAffectsBalanceHidden');
                
                if (editLeaveTypeIdInput) editLeaveTypeIdInput.value = data.leaveTypeId;
                if (editLeaveTypeHidden) editLeaveTypeHidden.value = data.leaveType;
                
                // Display read-only leave type
                const displayLeaveType = document.getElementById('displayLeaveType');
                if (displayLeaveType) {
                    displayLeaveType.textContent = data.leaveType;
                }
                
                // Set editable fields
                if (editLeaveTypeNameInput) {
                    editLeaveTypeNameInput.value = data.leaveTypeName;
                    originalLeaveTypeName = data.leaveTypeName;
                    isEditNameValidated = true;
                    editLeaveTypeNameInput.classList.remove('error', 'valid');
                }
                
                // Ensure update button is enabled for original name
                const updateBtn = document.getElementById('updateLeaveTypeBtn');
                if (updateBtn) {
                    updateBtn.disabled = false;
                    updateBtn.style.opacity = '1';
                }

                // Hide success message initially
                const successMsg = document.getElementById('editLeaveTypeNameSuccess');
                if (successMsg) successMsg.style.display = 'none';
                
                // Set description
                if (editLeaveTypeDescriptionTextarea) {
                    editLeaveTypeDescriptionTextarea.value = data.leaveTypeDescription || '';
                }
                
                // Set requires document checkbox (EDITABLE)
                const editRequiresDocument = document.getElementById('editRequiresDocument');
                if (editRequiresDocument) {
                    editRequiresDocument.checked = data.requiresDocument === true;
                }
                
                // Set affects balance checkbox (READ-ONLY)
                const affectsBalanceCheckbox = document.getElementById('editAffectsBalance');
                if (affectsBalanceCheckbox && editAffectsBalanceHidden) {
                    affectsBalanceCheckbox.checked = data.affectsBalance === true;
                    editAffectsBalanceHidden.value = data.affectsBalance === true ? 'true' : 'false';
                }
                
             // Set fixed duration checkbox (EDITABLE WITH WARNING) - ONLY for Full Day
                const fixedDurationCheckbox = document.getElementById('editFixedDuration');
                const fixedDurationWarning = document.getElementById('fixedDurationWarning');
                const applicationCountSpan = document.getElementById('applicationCount');
                const fixedDurationGroup = fixedDurationCheckbox?.closest('.form-group');

                if (fixedDurationCheckbox && fixedDurationGroup) {
                    // ✅ ONLY enable Fixed Duration for Full Day leave types
                    if (data.leaveType === 'Full Day') {
                        fixedDurationCheckbox.checked = data.fixedDuration === true;
                        fixedDurationCheckbox.disabled = false;
                        
                        // Remove disabled styling from checkbox group
                        const checkboxGroup = fixedDurationCheckbox.closest('.checkbox-group');
                        if (checkboxGroup) {
                            checkboxGroup.style.opacity = '1';
                            checkboxGroup.style.cursor = 'pointer';
                            checkboxGroup.style.background = 'white';
                        }
                        
                        // Show warning if there are existing applications
                        if (data.totalApplications && data.totalApplications > 0) {
                            if (fixedDurationWarning && applicationCountSpan) {
                                applicationCountSpan.textContent = data.totalApplications;
                                fixedDurationWarning.style.display = 'block';
                            }
                        } else {
                            if (fixedDurationWarning) fixedDurationWarning.style.display = 'none';
                        }
                        
                    } else {
                        // ❌ Disable Fixed Duration for Half Day and Both
                        fixedDurationCheckbox.checked = false;
                        fixedDurationCheckbox.disabled = true;
                        
                        // Apply disabled styling to checkbox group
                        const checkboxGroup = fixedDurationCheckbox.closest('.checkbox-group');
                        if (checkboxGroup) {
                            checkboxGroup.style.opacity = '0.5';
                            checkboxGroup.style.cursor = 'not-allowed';
                            checkboxGroup.style.background = '#f8f9fa';
                        }
                        
                        // Hide warning
                        if (fixedDurationWarning) fixedDurationWarning.style.display = 'none';
                    }
                }              
                
                // Handle standard duration and shift based on leave type
                const standardDurationGroup = document.getElementById('editStandardDurationGroup');
                const displayStandardDuration = document.getElementById('displayStandardDuration');
                
                if (data.leaveType === 'Full Day') {
                    // Show standard duration as read-only
                    if (standardDurationGroup) standardDurationGroup.style.display = 'block';
                    if (displayStandardDuration) displayStandardDuration.textContent = data.standardDuration || 0;
                    if (editStandardDurationHidden) editStandardDurationHidden.value = data.standardDuration || 0;
                    
                    // Hide half day shift
                    if (editHalfDayShiftGroup) editHalfDayShiftGroup.style.display = 'none';
                    if (editHalfDayShiftSelect) editHalfDayShiftSelect.value = '';
                    
                } else if (data.leaveType === 'Half Day') {
                    // Hide standard duration
                    if (standardDurationGroup) standardDurationGroup.style.display = 'none';
                    if (editStandardDurationHidden) editStandardDurationHidden.value = '';
                    
                    // Show half day shift as editable
                    if (editHalfDayShiftGroup) editHalfDayShiftGroup.style.display = 'block';
                    if (editHalfDayShiftSelect) editHalfDayShiftSelect.value = data.halfDayShift || '';
                    
                } else if (data.leaveType === 'Both') {
                    // Show standard duration as read-only
                    if (standardDurationGroup) standardDurationGroup.style.display = 'block';
                    if (displayStandardDuration) displayStandardDuration.textContent = data.standardDuration || 0;
                    if (editStandardDurationHidden) editStandardDurationHidden.value = data.standardDuration || 0;
                    
                    // Show half day shift as editable
                    if (editHalfDayShiftGroup) editHalfDayShiftGroup.style.display = 'block';
                    if (editHalfDayShiftSelect) editHalfDayShiftSelect.value = data.halfDayShift || '';
                }
                
                // Show the form
                if (editForm) editForm.style.display = 'block';
                
            } else {
                // Show error message
                if (modalErrorMessage) modalErrorMessage.textContent = data.error || 'Failed to load leave type data';
                if (modalError) modalError.style.display = 'block';
            }
        })
        .catch(error => {
            console.error('Fetch error:', error);
            if (modalLoading) modalLoading.style.display = 'none';
            if (modalErrorMessage) modalErrorMessage.textContent = 'Network error: ' + error.message;
            if (modalError) modalError.style.display = 'block';
        });
};

// Function to close modals
function closeEditModalHandler() {
    if (editModal) editModal.style.display = 'none';
    if (editForm) editForm.reset();
    clearEditValidationErrors();
    if (updateBtn) {
        updateBtn.innerHTML = '<i class="fas fa-save"></i> Update Leave Type';
        updateBtn.disabled = false;
    }
}

// Event listeners for closing modals
if (closeEditModalBtn) {
    closeEditModalBtn.addEventListener('click', closeEditModalHandler);
}
if (cancelEditBtn) {
    cancelEditBtn.addEventListener('click', closeEditModalHandler);
}

// Form validation and submission
if (editForm) {
    editForm.addEventListener('submit', function(e) {
        console.log('=== Edit form submission started ===');
        
        let isValid = true;
        
        // Clear previous errors
        clearEditValidationErrors();
        
        // Normalize name before validation
        if (editLeaveTypeNameInput) {
            const normalizedValue = editLeaveTypeNameInput.value.trim().replace(/\s+/g, ' ');
            editLeaveTypeNameInput.value = normalizedValue;
        }
        
        // Validate half day shift for Half Day and Both
        const leaveTypeHidden = document.getElementById('editLeaveTypeHidden');
        if (leaveTypeHidden && (leaveTypeHidden.value === 'Half Day' || leaveTypeHidden.value === 'Both')) {
            if (editHalfDayShiftSelect && !editHalfDayShiftSelect.value.trim()) {
                showEditError('editHalfDayShiftError', 'Please select available shifts');
                if (editHalfDayShiftSelect) editHalfDayShiftSelect.classList.add('error');
                isValid = false;
            }
        }
        
        // Validate leave type name
        const normalizedEditName = editLeaveTypeNameInput ? editLeaveTypeNameInput.value.trim().replace(/\s+/g, ' ') : '';

        if (editLeaveTypeNameInput && !normalizedEditName) {
            showEditError('editLeaveTypeNameError', 'Leave type name is required');
            editLeaveTypeNameInput.classList.add('error');
            isValid = false;
        } else if (editLeaveTypeNameInput && normalizedEditName.length < 3) {
            showEditError('editLeaveTypeNameError', 'Leave type name must be at least 3 characters long');
            editLeaveTypeNameInput.classList.add('error');
            isValid = false;
        } else if (editLeaveTypeNameInput && normalizedEditName.length > 100) {
            showEditError('editLeaveTypeNameError', 'Leave type name cannot exceed 100 characters');
            editLeaveTypeNameInput.classList.add('error');
            isValid = false;
        } else if (editLeaveTypeNameInput && !/[a-zA-Z]/.test(normalizedEditName)) {
            showEditError('editLeaveTypeNameError', 'Leave type name must contain at least one letter');
            editLeaveTypeNameInput.classList.add('error');
            isValid = false;
        } else if (editLeaveTypeNameInput && /[^a-zA-Z0-9\s]/.test(normalizedEditName)) {
            showEditError('editLeaveTypeNameError', 'Only letters, numbers, and spaces allowed');
            editLeaveTypeNameInput.classList.add('error');
            isValid = false;
        } else if (editLeaveTypeNameInput && !isEditNameValidated) {
            showEditError('editLeaveTypeNameError', 'Please wait for name validation to complete or name already exists');
            editLeaveTypeNameInput.classList.add('error');
            isValid = false;
        }
        
        // Validate description
        if (editLeaveTypeDescriptionTextarea && !editLeaveTypeDescriptionTextarea.value.trim()) {
            showEditError('editLeaveTypeDescriptionError', 'Description is required');
            editLeaveTypeDescriptionTextarea.classList.add('error');
            isValid = false;
        } else if (editLeaveTypeDescriptionTextarea && editLeaveTypeDescriptionTextarea.value.trim().length < 10) {
            showEditError('editLeaveTypeDescriptionError', 'Description must be at least 10 characters long');
            editLeaveTypeDescriptionTextarea.classList.add('error');
            isValid = false;
        }
        
        console.log('Edit form validation result:', isValid);
        
        if (!isValid) {
            e.preventDefault();
            console.log('Edit form submission prevented due to validation errors');
        } else {
            console.log('Edit form validation passed, submitting...');
            // Show loading state
            if (updateBtn) {
                updateBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Updating...';
                updateBtn.disabled = true;
            }
        }
    });
}

// Helper functions
function clearEditValidationErrors() {
    // Clear form error divs
    document.querySelectorAll('#editLeaveTypeModal .form-error').forEach(error => {
        error.style.display = 'none';
    });
    
    // Clear validation classes
    document.querySelectorAll('#editLeaveTypeModal input, #editLeaveTypeModal select, #editLeaveTypeModal textarea').forEach(input => {
        input.classList.remove('error', 'valid');
    });
    
    // Clear all inline validation messages
    document.querySelectorAll('#editLeaveTypeModal .validation-message').forEach(msg => {
        msg.remove();
    });
}

// ============================================
// DELETE MODAL CODE - START
// ============================================

const deleteModal = document.getElementById('deleteLeaveTypeModal');
const deleteForm = document.getElementById('deleteLeaveTypeForm');
const deleteLoading = document.getElementById('deleteLoading');
const deleteError = document.getElementById('deleteError');
const deleteWarningSection = document.getElementById('deleteWarningSection');
const deleteFormSection = document.getElementById('deleteFormSection');
const deleteStats = document.getElementById('deleteStats');
const confirmDeleteBtn = document.getElementById('confirmDeleteBtn');
const deleteAdminPassword = document.getElementById('deleteAdminPassword');

console.log('Delete Modal Elements Check:', {
    modal: !!deleteModal,
    warningSection: !!deleteWarningSection,
    formSection: !!deleteFormSection
});

// Function to reset modal to initial state
function resetDeleteModal() {
    // Hide ALL sections
    if (deleteLoading) deleteLoading.style.display = 'none';
    if (deleteError) deleteError.style.display = 'none';
    if (deleteWarningSection) deleteWarningSection.style.display = 'none';
    if (deleteFormSection) deleteFormSection.style.display = 'none';
    if (deleteStats) deleteStats.style.display = 'none';
    
    // Clear form
    if (deleteForm) deleteForm.reset();
    if (deleteAdminPassword) deleteAdminPassword.value = '';
    
    // Reset button state
    if (confirmDeleteBtn) {
        confirmDeleteBtn.disabled = false;
        confirmDeleteBtn.innerHTML = '<i class="fas fa-trash"></i> Delete Permanently';
    }
    
    // Clear password error
    const passwordError = document.getElementById('deletePasswordError');
    if (passwordError) passwordError.style.display = 'none';
    if (deleteAdminPassword) deleteAdminPassword.classList.remove('error');
}

// Close delete modal
function closeDeleteModalHandler(event) {
    if (!deleteModal) return;
    
    // Prevent event from bubbling if it exists
    if (event) {
        event.stopPropagation();
    }
    
    console.log('Closing delete modal');
    deleteModal.style.display = 'none';
    resetDeleteModal();
}

// Make closeDeleteModalHandler globally available
window.closeDeleteModalHandler = closeDeleteModalHandler;

// Form validation and submission
if (deleteForm) {
    deleteForm.addEventListener('submit', function(e) {
        const passwordError = document.getElementById('deletePasswordError');
        
        if (deleteAdminPassword && !deleteAdminPassword.value.trim()) {
            e.preventDefault();
            if (passwordError) {
                passwordError.textContent = 'Password is required to confirm deletion';
                passwordError.style.display = 'block';
            }
            if (deleteAdminPassword) {
                deleteAdminPassword.classList.add('error');
                deleteAdminPassword.focus();
            }
            return false;
        }
        
        // Clear any previous errors
        if (passwordError) passwordError.style.display = 'none';
        if (deleteAdminPassword) deleteAdminPassword.classList.remove('error');
        
        // Show loading state
        if (confirmDeleteBtn) {
            confirmDeleteBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Deleting...';
            confirmDeleteBtn.disabled = true;
        }
    });
}

// Function to open delete modal
window.openDeleteModal = function(leaveTypeId, leaveTypeName) {
    console.log('=== Opening delete modal for:', leaveTypeName, '(' + leaveTypeId + ')');
    
    if (!deleteModal) {
        console.error('Delete modal not found!');
        return;
    }
    
    // Show modal first
    deleteModal.style.display = 'flex';
    
    // Reset everything
    resetDeleteModal();
    
    // Show loading
    if (deleteLoading) deleteLoading.style.display = 'block';
    console.log('Loading shown');
    
    // Check deletion eligibility
    const checkUrl = '<%=request.getContextPath()%>/AdminDeleteLeaveTypeController?action=checkDelete&leaveTypeId=' + encodeURIComponent(leaveTypeId);
    
    fetch(checkUrl)
        .then(response => response.json())
        .then(data => {
            console.log('Delete check response:', data);
            
            // Hide loading
            if (deleteLoading) deleteLoading.style.display = 'none';
            
            if (data.error) {
                // Show error
                const errorMsg = document.getElementById('deleteErrorMessage');
                if (errorMsg) errorMsg.textContent = data.error;
                if (deleteError) deleteError.style.display = 'block';
                
            } else if (!data.canDelete) {
                // Cannot delete - show warning section
                console.log('!!! CANNOT DELETE - SHOWING WARNING !!!');
                console.log('Message to display:', data.message);
                
                if (deleteWarningSection) {
                    // Build the warning HTML
                    deleteWarningSection.innerHTML = `
                        <div class="alert alert-info">
                            <i class="fas fa-info-circle"></i>
                            <span id="warningMessageSpan"></span>
                        </div>
                        <div style="display: flex; justify-content: flex-end; margin-top: 20px;">
                            <button type="button" class="btn btn-secondary" onclick="closeDeleteModalHandler(event)">
                                <i class="fas fa-times"></i> Close
                            </button>
                        </div>
                    `;
                    
                    // Set the message using textContent
                    const warningSpan = document.getElementById('warningMessageSpan');
                    if (warningSpan) {
                        warningSpan.textContent = data.message;
                    }
                    
                    deleteWarningSection.style.display = 'block';
                }
                
            } else {
                // Can delete - show form section
                console.log('Can delete - showing form section');
                
                const nameElem = document.getElementById('deleteLeaveTypeName');
                const idElem = document.getElementById('deleteLeaveTypeId');
                const hiddenNameElem = document.getElementById('deleteLeaveTypeNameHidden');
                
                if (nameElem) nameElem.textContent = data.leaveTypeName || leaveTypeName;
                if (idElem) idElem.value = leaveTypeId;
                if (hiddenNameElem) hiddenNameElem.value = data.leaveTypeName || leaveTypeName;
                
                // Show stats if there are applications
                if (data.totalApplications > 0) {
                    const totalInfo = document.getElementById('totalAppsInfo');
                    const approvedInfo = document.getElementById('approvedAppsInfo');
                    const rejectedInfo = document.getElementById('rejectedAppsInfo');
                    
                    if (totalInfo) totalInfo.textContent = 'Total Applications: ' + data.totalApplications;
                    if (approvedInfo) approvedInfo.textContent = 'Approved: ' + (data.approvedApplications || 0);
                    if (rejectedInfo) rejectedInfo.textContent = 'Rejected: ' + (data.rejectedApplications || 0);
                    if (deleteStats) deleteStats.style.display = 'block';
                }
                
                // Show the form section
                if (deleteFormSection) deleteFormSection.style.display = 'block';
            }
        })
        .catch(error => {
            console.error('Delete check error:', error);
            if (deleteLoading) deleteLoading.style.display = 'none';
            
            const errorMsg = document.getElementById('deleteErrorMessage');
            if (errorMsg) {
                errorMsg.textContent = 'Failed to check deletion eligibility: ' + error.message;
            }
            if (deleteError) deleteError.style.display = 'block';
        });
};

// ============================================
// DELETE MODAL CODE - END
// ============================================

// Close edit modal when clicking outside
window.addEventListener('click', function(event) {
    if (editModal && event.target === editModal) {
        closeEditModalHandler();
    }
});

// Close modal on Escape key
document.addEventListener('keydown', function(event) {
    if (event.key === 'Escape') {
        if (editModal && editModal.style.display === 'flex') {
            closeEditModalHandler();
        }
        if (deleteModal && deleteModal.style.display === 'flex') {
            closeDeleteModalHandler();
        }
    }
});

// ============================================
// UPDATE BUTTON TOOLTIP ON HOVER
// ============================================

const updateBtnForTooltip = document.getElementById('updateLeaveTypeBtn');
if (updateBtnForTooltip) {
    updateBtnForTooltip.addEventListener('mouseenter', function() {
        if (this.disabled) {
            const tooltip = document.getElementById('edit-submit-button-tooltip');
            if (tooltip && tooltip.innerHTML) {
                const rect = this.getBoundingClientRect();
                tooltip.style.left = (rect.left + rect.width / 2) + 'px';
                tooltip.style.top = (rect.top - 10) + 'px';
                tooltip.style.transform = 'translate(-50%, -100%)';
                tooltip.classList.add('show');
            }
        }
    });

    updateBtnForTooltip.addEventListener('mouseleave', function() {
        const tooltip = document.getElementById('edit-submit-button-tooltip');
        if (tooltip) {
            tooltip.classList.remove('show');
        }
    });
}

console.log('Leave Types page loaded successfully');
});
// Password visibility toggle for delete modal
function toggleDeletePasswordVisibility() {
const passwordInput = document.getElementById('deleteAdminPassword');
const passwordIcon = document.getElementById('deletePasswordIcon');
if (passwordInput && passwordIcon) {
    if (passwordInput.type === 'password') {
        passwordInput.type = 'text';
        passwordIcon.classList.remove('fa-eye');
        passwordIcon.classList.add('fa-eye-slash');
    } else {
        passwordInput.type = 'password';
        passwordIcon.classList.remove('fa-eye-slash');
        passwordIcon.classList.add('fa-eye');
    }
}
}
// Initialize delete form validation for leave type
document.addEventListener('DOMContentLoaded', function() {
const deletePasswordInput = document.getElementById('deleteAdminPassword');
const deleteCheckbox = document.getElementById('confirmLeaveTypeDelete');
const deleteBtn = document.getElementById('confirmDeleteBtn');
function validateDeleteForm() {
    if (deletePasswordInput && deleteCheckbox && deleteBtn) {
        const hasPassword = deletePasswordInput.value.trim().length > 0;
        const isConfirmed = deleteCheckbox.checked;
        
        deleteBtn.disabled = !(hasPassword && isConfirmed);
        deleteBtn.style.opacity = deleteBtn.disabled ? '0.6' : '1';
    }
}

if (deletePasswordInput) deletePasswordInput.addEventListener('input', validateDeleteForm);
if (deleteCheckbox) deleteCheckbox.addEventListener('change', validateDeleteForm);

// Enter key to submit
if (deletePasswordInput) {
    deletePasswordInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter' && deleteBtn && !deleteBtn.disabled) {
            e.preventDefault();
            const deleteForm = document.getElementById('deleteLeaveTypeForm');
            if (deleteForm) deleteForm.submit();
        }
    });
}
});
// ============================================
// SEARCH + FILTERS FUNCTIONALITY FOR LEAVE TYPES
// ============================================
document.addEventListener('DOMContentLoaded', function() {
const searchInput = document.getElementById('leaveTypeSearch');
const filterCategory = document.getElementById('filterCategory');
const filterDocument = document.getElementById('filterDocument');
const filterBalance = document.getElementById('filterBalance');
const filterDuration = document.getElementById('filterDuration');
const clearFiltersBtn = document.getElementById('clearFilters');
const sortOrderSelect = document.getElementById('sortOrder');
const tableBody = document.querySelector('.admin-table tbody');

if (!tableBody) return;

const totalRows = tableBody.querySelectorAll('tr').length;

// Initialize
sortRows();

// Apply filters function
function applyFilters() {
    const searchTerm = searchInput ? searchInput.value.toLowerCase().trim() : '';
    const categoryFilter = filterCategory ? filterCategory.value : '';
    const documentFilter = filterDocument ? filterDocument.value : '';
    const balanceFilter = filterBalance ? filterBalance.value : '';
    const durationFilter = filterDuration ? filterDuration.value : '';
    
    const rows = tableBody.querySelectorAll('tr');
    let visibleCount = 0;
    
    rows.forEach(row => {
        // Get row data
        const leaveTypeName = row.querySelector('td:nth-child(1) strong')?.textContent.toLowerCase() || '';
        const categoryBadge = row.querySelector('.leave-type-badge')?.textContent.trim() || '';
        const description = row.querySelector('td:nth-child(2) div')?.textContent.toLowerCase() || '';
        
        // Check document required
        const hasRequiredBadge = row.querySelector('.badge-required') !== null;
        const documentStatus = hasRequiredBadge ? 'required' : 'optional';
        
        // Check affects balance
        const hasBalanceYes = row.querySelector('.badge-balance-yes') !== null;
        const balanceStatus = hasBalanceYes ? 'yes' : 'no';
        
        // Check duration mode
        const hasFixedBadge = row.querySelector('.badge-fixed') !== null;
        const durationStatus = hasFixedBadge ? 'fixed' : 'flexible';
        
        // Search filter
        const searchableText = leaveTypeName + ' ' + categoryBadge.toLowerCase() + ' ' + description;
        const matchesSearch = searchTerm === '' || searchableText.includes(searchTerm);
        
        // Category filter
        const matchesCategory = categoryFilter === '' || categoryBadge === categoryFilter;
        
        // Document filter
        const matchesDocument = documentFilter === '' || documentStatus === documentFilter;
        
        // Balance filter
        const matchesBalance = balanceFilter === '' || balanceStatus === balanceFilter;
        
        // Duration filter
        const matchesDuration = durationFilter === '' || durationStatus === durationFilter;
        
        // Show/hide row
        if (matchesSearch && matchesCategory && matchesDocument && matchesBalance && matchesDuration) {
            row.style.display = '';
            visibleCount++;
        } else {
            row.style.display = 'none';
        }
    });
    
    // Sort visible rows
    sortRows();
    
    showNoResultsMessage(visibleCount);
}

// Sort rows based on selected order
function sortRows() {
    if (!sortOrderSelect) return;
    
    const sortOrder = sortOrderSelect.value;
    const rows = Array.from(tableBody.querySelectorAll('tr')).filter(row => {
        return row.style.display !== 'none' && !row.classList.contains('no-search-results');
    });
    
    rows.sort((a, b) => {
        const aName = a.getAttribute('data-name') || '';
        const bName = b.getAttribute('data-name') || '';
        
        switch(sortOrder) {
            case 'name-asc':
                return aName.localeCompare(bName);
            case 'name-desc':
                return bName.localeCompare(aName);
            default:
                return 0;
        }
    });
    
    // Re-append sorted rows
    rows.forEach(row => tableBody.appendChild(row));
}

// Show no results message
function showNoResultsMessage(visibleCount) {
    const existingMessage = document.querySelector('.no-search-results');
    if (existingMessage) {
        existingMessage.remove();
    }
    
    if (visibleCount === 0 && tableBody) {
        const noResultsRow = document.createElement('tr');
        noResultsRow.className = 'no-search-results';
        noResultsRow.innerHTML = `
            <td colspan="7" style="text-align: center; padding: 40px; color: #6c757d;">
                <i class="fas fa-search" style="font-size: 3rem; margin-bottom: 16px; opacity: 0.5;"></i>
                <h4>No Leave Types Found</h4>
                <p>No leave types match your search and filter criteria.</p>
                <button onclick="clearAllFilters()" 
                        class="btn btn-secondary" style="margin-top: 10px;">
                    <i class="fas fa-times"></i> Clear All Filters
                </button>
            </td>
        `;
        tableBody.appendChild(noResultsRow);
    }
}

// Clear all filters
window.clearAllFilters = function() {
    if (searchInput) searchInput.value = '';
    if (filterCategory) filterCategory.value = '';
    if (filterDocument) filterDocument.value = '';
    if (filterBalance) filterBalance.value = '';
    if (filterDuration) filterDuration.value = '';
    if (sortOrderSelect) sortOrderSelect.value = 'name-asc';
    applyFilters();
};

// Event listeners
if (searchInput) searchInput.addEventListener('input', applyFilters);
if (filterCategory) filterCategory.addEventListener('change', applyFilters);
if (filterDocument) filterDocument.addEventListener('change', applyFilters);
if (filterBalance) filterBalance.addEventListener('change', applyFilters);
if (filterDuration) filterDuration.addEventListener('change', applyFilters);
if (sortOrderSelect) sortOrderSelect.addEventListener('change', applyFilters);
if (clearFiltersBtn) clearFiltersBtn.addEventListener('click', clearAllFilters);
});
</script>
</body>
</html>