<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page session="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%
    //Check if user is logged in and is admin
    if (session.getAttribute("adminId") == null) {
       response.sendRedirect("/ELMS_3.0/Admin/AdminLogin.jsp");
       return;
    }
    
    // Get admin information from session
    String adminName = (String) session.getAttribute("adminName");
    if (adminName == null) adminName = "Admin User";
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="icon" type="image/png" href="../imnsb_logo.png">
    <title>Employee List - IMNSB Employee Leave Management System</title>
    <link rel="stylesheet" href="/ELMS_3.0/css/styles.css">
    <link rel="stylesheet" href="/ELMS_3.0/css/dashboard.css">
    <link rel="stylesheet" href="/ELMS_3.0/css/admin.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        .employee-list-container {
            background: white;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
            overflow: hidden;
        }

        .employee-list-header {
            background: linear-gradient(135deg, var(--primary-color), var(--primary-dark));
            color: white;
            padding: 1.5rem;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .employee-list-header h3 {
            margin: 0;
            font-size: 1.5rem;
            display: flex;
            align-items: center;
            gap: 0.5rem;
        }

        .employee-list-header p {
            margin: 0.5rem 0 0 0;
            opacity: 0.9;
            font-size: 0.9rem;
        }

        .header-actions {
            display: flex;
            gap: 1rem;
            align-items: center;
        }

        .search-box {
            position: relative;
            display: flex;
            align-items: center;
        }

        .search-box input {
            padding: 0.5rem 1rem 0.5rem 2.5rem;
            border: 1px solid rgba(255, 255, 255, 0.3);
            border-radius: 20px;
            background: rgba(255, 255, 255, 0.1);
            color: white;
            font-size: 0.9rem;
            width: 250px;
            backdrop-filter: blur(10px);
        }

        .search-box input::placeholder {
            color: rgba(255, 255, 255, 0.7);
        }

        .search-box i {
            position: absolute;
		    left: 0.8rem;
		    color: rgba(255, 255, 255, 0.7);
		    z-index: 10;
        }

        .btn-add {
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

        .btn-add:hover {
            background: rgba(255, 255, 255, 0.3);
            color: white;
            transform: translateY(-1px);
        }

        .employee-table-container {
            overflow-x: auto;
        }

        .employee-table {
            width: 100%;
            border-collapse: collapse;
            font-size: 0.9rem;
        }

        .employee-table th {
            background: #f8f9fa;
            color: #495057;
            padding: 1rem;
            text-align: left;
            font-weight: 600;
            border-bottom: 2px solid #dee2e6;
            white-space: nowrap;
        }

        .employee-table td {
            padding: 1rem;
            border-bottom: 1px solid #dee2e6;
            vertical-align: middle;
        }

        .employee-table tr:hover {
            background: #f8f9fa;
        }

        .employee-avatar {
            width: 45px;
            height: 45px;
            border-radius: 50%;
            object-fit: cover;
            border: 2px solid #e9ecef;
        }

        .employee-info {
            display: flex;
            align-items: center;
            gap: 1rem;
        }

        .employee-details h4 {
            margin: 0;
            color: #495057;
            font-size: 1rem;
        }

        .employee-details p {
            margin: 0.2rem 0 0 0;
            color: #6c757d;
            font-size: 0.85rem;
        }


        .action-buttons {
            display: flex;
            gap: 0.5rem;
        }

        .btn-action {
            padding: 0.4rem 0.8rem;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 0.8rem;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 0.3rem;
            transition: all 0.3s ease;
        }

        .btn-view {
            background: #007bff;
            color: white;
        }

        .btn-view:hover {
            background: #0056b3;
            color: white;
        }

        .btn-delete {
            background: #dc3545;
            color: white;
        }

        .btn-delete:hover {
            background: #c82333;
            color: white;
        }

        .empty-state {
            text-align: center;
            padding: 3rem;
            color: #6c757d;
        }

        .empty-state i {
            font-size: 4rem;
            margin-bottom: 1rem;
            opacity: 0.5;
        }

        .empty-state h3 {
            margin: 0 0 0.5rem 0;
            color: #495057;
        }

        .empty-state p {
            margin: 0;
            font-size: 0.9rem;
        }

        .alert {
            padding: 15px;
            margin-bottom: 20px;
            border: 1px solid transparent;
            border-radius: 4px;
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

        .pagination {
            display: flex;
            justify-content: center;
            padding: 1rem;
            gap: 0.5rem;
        }

        .pagination a, .pagination span {
            padding: 0.5rem 0.75rem;
            border: 1px solid #dee2e6;
            color: #007bff;
            text-decoration: none;
            border-radius: 4px;
        }

        .pagination .current {
            background: #007bff;
            color: white;
            border-color: #007bff;
        }

        .pagination a:hover {
            background: #e9ecef;
        }

        /* Admin Password Modal styles */
        .modal {
            display: none;
            position: fixed;
            z-index: 1000;
            left: 0;
            top: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(0, 0, 0, 0.5);
            align-items: center;
            justify-content: center;
        }

        .modal-content {
            background: white;
            border-radius: 8px;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
            max-width: 500px;
            width: 90%;
            max-height: 90vh;
            overflow-y: auto;
        }

        .modal-header {
            padding: 1.5rem;
            border-bottom: 1px solid #dee2e6;
            background: linear-gradient(135deg, #dc3545, #c82333);
            color: white;
            border-radius: 8px 8px 0 0;
        }

        .modal-header h3 {
            margin: 0;
            display: flex;
            align-items: center;
            gap: 0.5rem;
            font-size: 1.3rem;
        }

        .modal-body {
            padding: 2rem 1.5rem;
        }

        .employee-info-card {
            background: #f8f9fa;
           
            padding: 1rem;
            margin-bottom: 1.5rem;
            border-radius: 0 4px 4px 0;
        }

        .employee-info-card h4 {
            margin: 0 0 0.5rem 0;
            color: #495057;
            font-size: 1.1rem;
        }

        .employee-info-card p {
            margin: 0.2rem 0;
            color: #6c757d;
            font-size: 0.9rem;
        }
        
		.warning-message {
            background: #fff3cd;
            border: 1px solid #ffeaa7;
            color: #856404;
            padding: 1rem;
            border-radius: 4px;
            margin-bottom: 1.5rem;
            text-align: center; /* Center align the content */
            display: block; /* Change from flex to block */
        }
	    
	    .warning-message i {
            display: block; /* Make icon a block element */
            font-size: 1.5rem; /* Slightly larger than default */
            margin: 0 auto 0.5rem auto; /* Center horizontally, add bottom margin */
            color: #856404;
        }
	      .confirmation-icon {
            text-align: center;
            margin: 1rem 0 1.5rem 0;
        }
        
        .password-input-group {
            margin-bottom: 1rem;
        }

        .password-input-group label {
            display: block;
            margin-bottom: 0.5rem;
            font-weight: 600;
            color: #495057;
        }

        .password-input-container {
            position: relative;
            display: flex;
            align-items: center;
        }

        .password-input {
            width: 100%;
            padding: 0.75rem 2.5rem 0.75rem 1rem;
            border: 2px solid #dee2e6;
            border-radius: 4px;
            font-size: 1rem;
            transition: border-color 0.3s ease;
        }

        .password-input:focus {
            outline: none;
            border-color: #dc3545;
            box-shadow: 0 0 0 0.2rem rgba(220, 53, 69, 0.25);
        }

        .password-toggle {
            position: absolute;
            right: 0.75rem;
            background: none;
            border: none;
            color: #6c757d;
            cursor: pointer;
            padding: 0.25rem;
            transition: color 0.3s ease;
        }

        .password-toggle:hover {
            color: #495057;
        }

        .error-message {
            color: #dc3545;
            font-size: 0.875rem;
            margin-top: 0.5rem;
            display: none;
            align-items: center;
            gap: 0.3rem;
        }

        .modal-footer {
            padding: 1rem 1.5rem;
            border-top: 1px solid #dee2e6;
            display: flex;
            gap: 1rem;
            justify-content: center;
            background: #f8f9fa;
            border-radius: 0 0 8px 8px;
        }

        .btn {
            padding: 0.5rem 1.5rem;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 0.5rem;
            font-size: 0.9rem;
            font-weight: 500;
            transition: all 0.3s ease;
            min-width: 100px;
            justify-content: center;
        }

        .btn-secondary {
            background: #6c757d;
            color: white;
        }

        .btn-secondary:hover {
            background: #545b62;
            transform: translateY(-1px);
        }

        .btn-danger {
            background: #dc3545;
            color: white;
        }

        .btn-danger:hover {
            background: #c82333;
            transform: translateY(-1px);
        }

        .btn-danger:disabled {
            background: #e9ecef;
            color: #6c757d;
            cursor: not-allowed;
            transform: none;
        }

        .loading-spinner {
            display: none;
            width: 16px;
            height: 16px;
            border: 2px solid #ffffff;
            border-top: 2px solid transparent;
            border-radius: 50%;
            animation: spin 1s linear infinite;
        }

        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }

        /* Animation classes */
        .modal.show {
            display: flex;
            animation: modalFadeIn 0.3s ease;
        }

        .modal.hide {
            animation: modalFadeOut 0.3s ease;
        }

        @keyframes modalFadeIn {
            from { opacity: 0; }
            to { opacity: 1; }
        }

        @keyframes modalFadeOut {
            from { opacity: 1; }
            to { opacity: 0; }
        }

        .modal-content.show {
            animation: modalSlideIn 0.3s ease;
        }

        @keyframes modalSlideIn {
            from {
                transform: translateY(-50px);
                opacity: 0;
            }
            to {
                transform: translateY(0);
                opacity: 1;
            }
        }

        @keyframes fadeOut {
            from { opacity: 1; }
            to { opacity: 0; }
        }
        
        @keyframes slideInRight {
            from {
                transform: translateX(100%);
                opacity: 0;
            }
            to {
                transform: translateX(0);
                opacity: 1;
            }
        }
        
        .confirmation-step {
            text-align: center;
        }

        .password-step {
            animation: slideInUp 0.3s ease;
        }

        @keyframes slideInUp {
            from {
                transform: translateY(20px);
                opacity: 0;
            }
            to {
                transform: translateY(0);
                opacity: 1;
            }
        }

        /* Mobile responsiveness */
        @media (max-width: 768px) {
            .employee-list-header {
                flex-direction: column;
                gap: 1rem;
                align-items: stretch;
            }

            .header-actions {
                flex-direction: column;
                gap: 0.75rem;
            }

            .search-box input {
                width: 100%;
            }

            .employee-table {
                font-size: 0.8rem;
            }

            .employee-table th,
            .employee-table td {
                padding: 0.5rem;
            }

            .action-buttons {
                flex-direction: column;
                gap: 0.25rem;
            }

            .btn-action {
                padding: 0.3rem 0.6rem;
                font-size: 0.75rem;
            }
        }
        
         /* Success Modal Styles - Consistent with Interface */
        .success-modal {
            display: none;
            position: fixed;
            z-index: 1001;
            left: 0;
            top: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(0, 0, 0, 0.5);
            align-items: center;
            justify-content: center;
        }

        .success-modal-content {
            background: white;
            border-radius: 8px;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
            max-width: 500px;
            width: 90%;
            max-height: 90vh;
            overflow-y: auto;
        }

        .success-modal-header {
            padding: 1.5rem;
            border-bottom: 1px solid #dee2e6;
            background: linear-gradient(135deg, #28a745, #20c997);
            color: white;
            border-radius: 8px 8px 0 0;
        }

        .success-modal-header h3 {
            margin: 0;
            display: flex;
            align-items: center;
            gap: 0.5rem;
            font-size: 1.3rem;
        }

        .success-modal-body {
            padding: 2rem 1.5rem;
        }

        .success-message {
            font-size: 1rem;
            color: #495057;
            margin: 0;
            line-height: 1.5;
            text-align: center;
        }

        .success-employee-name {
            font-weight: 600;
            color: #2c3e50;
        }

        .success-modal-footer {
            padding: 1rem 1.5rem;
            border-top: 1px solid #dee2e6;
            display: flex;
            gap: 1rem;
            justify-content: center;
            background: #f8f9fa;
            border-radius: 0 0 8px 8px;
        }

        .btn-success {
            background: #28a745;
            color: white;
            border: none;
            padding: 0.5rem 1.5rem;
            border-radius: 4px;
            cursor: pointer;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 0.5rem;
            font-size: 0.9rem;
            font-weight: 500;
            transition: all 0.3s ease;
            min-width: 100px;
            justify-content: center;
        }

        .btn-success:hover {
            background: #218838;
            transform: translateY(-1px);
        }

        /* Simple modal animations */
        .success-modal.show {
            display: flex;
            animation: modalFadeIn 0.3s ease;
        }

        .success-modal.hide {
            animation: modalFadeOut 0.3s ease;
        }

        @keyframes modalFadeIn {
            from { opacity: 0; }
            to { opacity: 1; }
        }

        @keyframes modalFadeOut {
            from { opacity: 1; }
            to { opacity: 0; }
        }

        .success-modal-content.show {
            animation: modalSlideIn 0.3s ease;
        }

        @keyframes modalSlideIn {
            from {
                transform: translateY(-50px);
                opacity: 0;
            }
            to {
                transform: translateY(0);
                opacity: 1;
            }
        }
        
       /* Employee Details Modal Specific Styles */
#employeeDetailsModal {
    display: none;
    position: fixed;
    z-index: 1000;
    left: 0;
    top: 0;
    width: 100%;
    height: 100%;
    overflow: auto;
    background-color: rgba(0,0,0,0.5);
    animation: fadeIn 0.3s;
    align-items: center;  /* Center vertically */
    justify-content: center;  /* Center horizontally */
}

#employeeDetailsModal.show {
    display: flex;  /* Use flex for centering */
}

#employeeDetailsModal .modal-content {
    background-color: #fefefe;
    margin: auto;  /* Center the modal */
    padding: 0;
    border: 1px solid #888;
    border-radius: 12px;
    width: 90%;
    max-width: 1200px;  /* Increased width for the new column */
    max-height: 85vh;
    box-shadow: 0 8px 32px rgba(0,0,0,0.3);
    animation: slideIn 0.3s;
    display: flex;
    flex-direction: column;
}

#employeeDetailsModal .modal-header {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    padding: 24px 30px;
    border-radius: 12px 12px 0 0;
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-shrink: 0;  /* Prevent header from shrinking */
}

#employeeDetailsModal .modal-header h2 {
    font-size: 1.5rem;
    margin: 0;
    color: white;
}

#employeeDetailsModal .close {
    color: white;
    font-size: 32px;
    font-weight: bold;
    cursor: pointer;
    transition: all 0.3s;
    line-height: 1;
}

#employeeDetailsModal .close:hover,
#employeeDetailsModal .close:focus {
    transform: scale(1.2);
    text-shadow: 0 0 10px rgba(255,255,255,0.8);
}

#employeeDetailsModal .modal-body {
    padding: 30px;
    overflow-y: auto;
    flex: 1;  /* Allow body to grow and scroll */
}

/* Scrollbar styling for modal body */
#employeeDetailsModal .modal-body::-webkit-scrollbar {
    width: 8px;
}

#employeeDetailsModal .modal-body::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 10px;
}

#employeeDetailsModal .modal-body::-webkit-scrollbar-thumb {
    background: #888;
    border-radius: 10px;
}

#employeeDetailsModal .modal-body::-webkit-scrollbar-thumb:hover {
    background: #555;
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

/* Mobile responsiveness */
@media (max-width: 768px) {
    #employeeDetailsModal .modal-content {
        width: 95%;
        max-height: 90vh;
    }
    
    #employeeDetailsModal .modal-body {
        padding: 20px;
    }
}

        /* Mobile responsiveness */
        @media (max-width: 768px) {
            .success-modal-content {
                max-width: 95%;
                margin: 1rem;
            }
            
            .success-modal-header {
                padding: 1.5rem 1.5rem 1rem 1.5rem;
            }
            
            .success-modal-body {
                padding: 1.5rem;
            }
            
            .success-modal-footer {
                padding: 1rem 1.5rem 1.5rem 1.5rem;
            }
        }
        
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
    border-radius: 10px;
    max-width: 500px;
    width: 90%;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
    animation: slideIn 0.3s ease-in-out;
    overflow: hidden;
}

.popup-header {
    background: linear-gradient(135deg, #dc3545, #c82333);
    color: white;
    padding: 1.5rem;
    text-align: center;
}

.popup-header h3 {
    margin: 0;
    font-size: 1.3rem;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 0.5rem;
}

.popup-body {
    padding: 2rem 1.5rem;
}

.delete-warning {
    text-align: center;
    margin-bottom: 1.5rem;
}

.delete-warning i {
    font-size: 3rem;
    color: #dc3545;
    margin-bottom: 1rem;
}

.delete-warning h4 {
    color: #495057;
    margin-bottom: 1rem;
}

.employee-info-display {
    background: #f8f9fa;
    padding: 1rem;
    border-radius: 6px;
    margin: 1rem 0;
    text-align: left;
}

.employee-info-display p {
    margin: 0.5rem 0;
    color: #495057;
}

.warning-text {
    color: #856404;
    background: #fff3cd;
    padding: 0.75rem;
    border-radius: 4px;
    border: 1px solid #ffeaa7;
    font-size: 0.9rem;
    margin: 1rem 0;
}

.form-group {
    margin-bottom: 1.5rem;
}

.form-group label {
    display: block;
    margin-bottom: 0.5rem;
    font-weight: 600;
    color: #333;
}

.form-group input[type="password"] {
    width: 100%;
    padding: 0.75rem;
    border: 2px solid #ddd;
    border-radius: 6px;
    font-size: 1rem;
    transition: border-color 0.3s ease;
}

.form-group input[type="password"]:focus {
    outline: none;
    border-color: #dc3545;
    box-shadow: 0 0 0 3px rgba(220, 53, 69, 0.1);
}

.checkbox-label {
    display: flex;
    align-items: flex-start;
    gap: 0.5rem;
    cursor: pointer;
    font-size: 0.95rem;
    line-height: 1.4;
}

.checkbox-label input[type="checkbox"] {
    margin-top: 0.2rem;
    transform: scale(1.2);
}

.popup-footer {
    padding: 1rem 1.5rem;
    border-top: 1px solid #dee2e6;
    display: flex;
    gap: 1rem;
    justify-content: center;
    background: #f8f9fa;
}

.btn {
    padding: 0.5rem 1.5rem;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    text-decoration: none;
    display: inline-flex;
    align-items: center;
    gap: 0.5rem;
    font-size: 0.9rem;
    font-weight: 500;
    transition: all 0.3s ease;
    min-width: 120px;
    justify-content: center;
}

.btn-secondary {
    background: #6c757d;
    color: white;
}

.btn-secondary:hover {
    background: #545b62;
    transform: translateY(-1px);
}

.btn-danger {
    background: #dc3545;
    color: white;
}

.btn-danger:hover {
    background: #c82333;
    transform: translateY(-1px);
}

.btn-danger:disabled {
    background: #e9ecef;
    color: #6c757d;
    cursor: not-allowed;
    transform: none;
}

.btn-success {
    background: #28a745;
    color: white;
}

.btn-success:hover {
    background: #218838;
    transform: translateY(-1px);
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
    display: inline-flex;
    align-items: center;
    gap: 0.5rem;
}

.popup-btn:hover {
    background-color: #0056b3;
}

.popup-btn.btn-secondary,
.popup-btn[style*="background-color: #6c757d"] {
    background-color: #6c757d !important;
}

.popup-btn.btn-secondary:hover,
.popup-btn[style*="background-color: #6c757d"]:hover {
    background-color: #545b62 !important;
}

.popup-btn.btn-danger,
.popup-btn[style*="background-color: #dc3545"] {
    background-color: #dc3545 !important;
}

.popup-btn.btn-danger:hover,
.popup-btn[style*="background-color: #dc3545"]:hover {
    background-color: #c82333 !important;
}

.popup-btn:disabled {
    background-color: #e9ecef !important;
    color: #6c757d !important;
    cursor: not-allowed;
    opacity: 0.6;
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

/* Responsive */
@media (max-width: 600px) {
    .popup-footer {
        flex-direction: column;
    }
    
    .popup-content {
        margin: 1rem;
        width: calc(100% - 2rem);
    }
}
    </style>
</head>
<body>
    <div class="dashboard-container">
        <!-- Admin Sidebar Navigation -->
        <nav class="sidebar" id="sidebar">
            <div class="sidebar-header">
                <img src="/ELMS_3.0/imnsb_logowbg.png" alt="IMNSB Logo" class="sidebar-logo">
                <h3>IMNSB Admin</h3>
            </div>
            <ul class="sidebar-menu">
                <li>
                <a href="<%= request.getContextPath() %>/admin-dashboard"><i class="fas fa-user"></i> <span>Admin Officer</span></a>
	            </li>
	            <li>
	                <a href="/ELMS_3.0/Admin/AdminViewManagerListController"><i class="fas fa-user-tie"></i> <span>Approval Managers</span></a>
	            </li>
	            <li class="active">
	                <a href="/ELMS_3.0/Admin/AdminViewEmployeeListController"><i class="fas fa-users"></i> <span>Employees</span></a>
	            </li>
	            <li>
	                <a href="/ELMS_3.0/AdminLeaveTypeListController"><i class="fas fa-list-alt"></i> <span>Leave Types</span></a>
	            </li>
	            <li>
	                <a href="<%= request.getContextPath() %>/admin-leave-requests"><i class="fas fa-clipboard-list"></i> <span>Leave Requests</span></a>
	            </li>
	            <li>
	                <a href="/ELMS_3.0/admin-leave-reports"><i class="fas fa-chart-bar"></i> <span>View Report</span></a>
	            </li>
	            <li>
	                <a href="/ELMS_3.0/Admin/AdminSettings.jsp"><i class="fas fa-cog"></i> <span>Settings</span></a>
	            </li>
	            <li class="logout">
	                <a href="/ELMS_3.0/Admin/AdminLogin.jsp" id="logoutBtn"><i class="fas fa-sign-out-alt"></i> <span>Logout</span></a>
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
                    <h2>Employees</h2>
                </div>
                 <div class="header-right">
				    <span id="userName"><%= adminName %></span>
				    <div class="user-avatar">
				        <% 
				            String headerProfilePicPath = (String) session.getAttribute("adminProfilePicturePath");
				            if (headerProfilePicPath != null && !headerProfilePicPath.isEmpty()) {
				        %>
				            <img src="/ELMS_3.0/<%= headerProfilePicPath %>" alt="Admin Avatar" id="headerAvatar" 
				                 onerror="this.src='/ELMS_3.0/defaultprofilepicture.jpg';">
				        <% } else { %>
				            <img src="/ELMS_3.0/defaultprofilepicture.jpg" alt="Admin Avatar" id="headerAvatar">
				        <% } %>
				    </div>
				</div>
            </header>
            
            <div class="content-body">
                <!-- Display messages -->
                <c:if test="${not empty requestScope.errorMessage}">
                    <div class="alert alert-danger">
                        <i class="fas fa-exclamation-circle"></i>
                        ${requestScope.errorMessage}
                    </div>
                </c:if>
                
                <c:if test="${not empty requestScope.successMessage}">
                    <div class="alert alert-success">
                        <i class="fas fa-check-circle"></i>
                        ${requestScope.successMessage}
                    </div>
                </c:if>
                
                <c:if test="${not empty requestScope.infoMessage}">
                    <div class="alert alert-info">
                        <i class="fas fa-info-circle"></i>
                        ${requestScope.infoMessage}
                    </div>
                </c:if>

                <div class="employee-list-container">
                    <div class="employee-list-header">
                        <div>
                            <h3><i class="fas fa-users"></i> Employee Management</h3>
                            <p>Manage employees and their information</p>
                        </div>
                        <div class="header-actions">
                            <div class="search-box">
                                <i class="fas fa-search"></i>
                                <input type="text" id="searchInput" placeholder="Search employees..." 
                                       value="${param.search}">
                            </div>
                        </div>
                    </div>

                    <div class="employee-table-container">
                        <c:choose>
                            <c:when test="${empty employeeList}">
                                <div class="empty-state">
                                    <i class="fas fa-users"></i>
                                    <h3>No Employees Found</h3>
                                    <p>There are currently no employees in the system.</p>
                                    <a href="/ELMS_3.0/Admin/AdminAddEmployee.jsp" class="btn btn-primary" style="margin-top: 1rem;">
                                        <i class="fas fa-plus"></i> Add First Employee
                                    </a>
                                </div>
                            </c:when>
                            <c:otherwise>
							<table class="employee-table">
    <thead>
        <tr>
            <th>Employee</th>
            <th>Employee ID</th>
            <th>Mobile Number</th>
            <th>Actions</th>
        </tr>
    </thead>
    <tbody id="employeeTableBody">
        <c:forEach var="employee" items="${employeeList}" varStatus="status">
            <tr data-employee-id="${employee.employeeId}">
                <td>
                    <div class="employee-info">
                        <img src="${empty employee.profilePicturePath ? '/ELMS_3.0/defaultprofilepicture.jpg' : pageContext.request.contextPath.concat('/').concat(employee.profilePicturePath)}" 
                             alt="Employee Avatar" class="employee-avatar"
                             onerror="this.src='/ELMS_3.0/defaultprofilepicture.jpg';">
                        <div class="employee-details">
                            <h4>${employee.employeeName}</h4>
                            <p>${employee.employeeEmail}</p>
                        </div>
                    </div>
                </td>
                <td>
                    <code style="background: #f8f9fa; padding: 0.2rem 0.5rem; border-radius: 3px; font-size: 0.85rem;">
                        ${employee.employeeId}
                    </code>
                </td>
                <td>
                    <span style="font-family: monospace; color: #495057;">
                        ${empty employee.employeeNoPhone ? 'N/A' : employee.employeeNoPhone}
                    </span>
                </td>
                <td>
                    <div class="action-buttons">
                        <button type="button" class="btn-action btn-view" 
                                onclick="viewEmployeeDetails('${employee.employeeId}', '${fn:escapeXml(employee.employeeName)}')"
                                title="View Leave Details">
                            <i class="fas fa-eye"></i> View Details
                        </button>
                        <button type="button" class="btn-action btn-delete" 
                                onclick="confirmDeleteEmployee('${employee.employeeId}', '${fn:escapeXml(employee.employeeName)}', '${employee.employeeEmail}')"
                                title="Delete Employee">
                            <i class="fas fa-trash"></i> Delete
                        </button>
                    </div>
                </td>
            </tr>
        </c:forEach>
    </tbody>
</table>

                                <!-- Pagination-->
                                <c:if test="${totalPages > 1}">
                                    <div class="pagination">
                                        <c:if test="${currentPage > 1}">
                                            <a href="?page=${currentPage - 1}&search=${param.search}">
                                                <i class="fas fa-chevron-left"></i> Previous
                                            </a>
                                        </c:if>
                                        
                                        <c:forEach var="i" begin="1" end="${totalPages}">
                                            <c:choose>
                                                <c:when test="${i == currentPage}">
                                                    <span class="current">${i}</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <a href="?page=${i}&search=${param.search}">${i}</a>
                                                </c:otherwise>
                                            </c:choose>
                                        </c:forEach>
                                        
                                        <c:if test="${currentPage < totalPages}">
                                            <a href="?page=${currentPage + 1}&search=${param.search}">
                                                Next <i class="fas fa-chevron-right"></i>
                                            </a>
                                        </c:if>
                                    </div>
                                </c:if>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
        </main>
    </div>

	<!-- Employee Details Modal -->
	<div id="employeeDetailsModal" class="modal">
	    <div class="modal-content">
	        <div class="modal-header">
	            <h2 id="modalEmployeeName">Employee Details</h2>
	            <span class="close" onclick="closeModal()">&times;</span>
	        </div>
	        <div class="modal-body" id="modalBody">
	            <div class="loading">
	                <i class="fas fa-spinner fa-spin"></i> Loading employee details...
	            </div>
	        </div>
	    </div>
	</div>
    <!-- Admin Password Confirmation Modal -->
  <!-- Delete Employee Confirmation Modal -->
<div id="deleteEmployeeModal" class="popup-overlay" style="display: none;">
    <div class="popup-content" style="border-top: 5px solid #dc3545;">
        <div class="popup-body">
            <div style="text-align: center; padding: 1rem 0;">
                <i class="fas fa-exclamation-triangle" style="font-size: 4rem; color: #dc3545; margin-bottom: 1rem;"></i>
                <h3 style="color: #dc3545; font-size: 1.5rem; margin: 0 0 1rem 0;">Confirm Employee Deletion</h3>
                <p style="color: #495057; font-size: 1rem; margin: 0 0 1rem 0; line-height: 1.5;">
                    Are you sure you want to delete this employee?
                </p>
                
                <div style="background: #f8f9fa; padding: 15px; border-radius: 8px; margin: 15px 0; border: 1px solid #dee2e6; text-align: left;">
                    <p style="margin: 5px 0;"><strong>Employee:</strong> <span id="deleteEmployeeDisplayName">John Doe</span></p>
                    <p style="margin: 5px 0;"><strong>ID:</strong> <span id="deleteEmployeeDisplayId">EMP001</span></p>
                    <p style="margin: 5px 0;"><strong>Email:</strong> <span id="deleteEmployeeDisplayEmail">john@example.com</span></p>
                </div>
                
                <div style="background: linear-gradient(135deg, #fff5f5 0%, #ffe5e5 100%); padding: 15px; border-radius: 8px; margin-bottom: 20px; border-left: 4px solid #dc3545;">
                    <p style="margin: 0; color: #721c24; font-size: 0.9rem; line-height: 1.5;">
                        <i class="fas fa-exclamation-triangle"></i>
                        <strong>Warning:</strong> This action will permanently delete the employee account and all associated leave records. This cannot be undone.
                    </p>
                </div>
            </div>
            
            <form id="deleteEmployeeForm">
                <div style="margin-bottom: 20px; text-align: left;">
                    <label for="adminPasswordConfirm" style="display: block; margin-bottom: 8px; font-weight: 500; color: #495057; font-size: 0.9rem;">
                        <i class="fas fa-lock"></i> Confirm Your Password <span style="color: red;">*</span>
                    </label>
                    <div style="position: relative;">
                        <input type="password" 
                               id="adminPasswordConfirm" 
                               name="adminPassword" 
                               placeholder="Enter your admin password to confirm deletion"
                               required
                               style="width: 100%; padding: 12px 45px 12px 12px; border: 1px solid #ced4da; border-radius: 6px; font-size: 0.9rem; box-sizing: border-box;">
                        <button type="button" 
                                id="toggleEmployeeDeletePassword" 
                                onclick="toggleEmployeeDeletePasswordVisibility()"
                                style="position: absolute; right: 12px; top: 50%; transform: translateY(-50%); background: none; border: none; color: #6c757d; cursor: pointer; padding: 0; width: 24px; height: 24px; display: flex; align-items: center; justify-content: center;">
                            <i class="fas fa-eye" id="employeeDeletePasswordIcon"></i>
                        </button>
                    </div>
                    <div style="font-size: 0.875rem; color: #6c757d; margin-top: 8px; font-style: italic;">
                        <i class="fas fa-info-circle"></i>
                        Enter your admin password to authorize this deletion.
                    </div>
                </div>
                
                <div style="margin-bottom: 20px; text-align: left;">
                    <label class="checkbox-label" style="display: flex; align-items: flex-start; gap: 0.5rem; cursor: pointer; font-size: 0.95rem; line-height: 1.4;">
                        <input type="checkbox" id="confirmEmployeeDelete" name="confirmDelete" value="true" required 
                               style="margin-top: 0.2rem; transform: scale(1.2);">
                        <span>I understand this action is permanent and want to delete this employee account.</span>
                    </label>
                </div>
            </form>
        </div>
        <div class="popup-footer" style="padding: 1rem 1.5rem; border-top: 1px solid #dee2e6; display: flex; gap: 1rem; justify-content: center; background: #f8f9fa;">
            <button type="button" class="btn btn-secondary" onclick="closeDeleteEmployeeModal()" style="padding: 0.5rem 1.5rem; border: none; border-radius: 4px; min-width: 120px;">
                <i class="fas fa-times"></i> Cancel
            </button>
            <button type="button" class="btn btn-danger" id="confirmEmployeeDeleteBtn" disabled onclick="executeEmployeeDeletion()" style="padding: 0.5rem 1.5rem; border: none; border-radius: 4px; min-width: 120px;">
                <i class="fas fa-trash"></i> Delete Employee
            </button>
        </div>
    </div>
</div>

<!-- Employee Success Modal -->
<div id="deleteEmployeeSuccessModal" class="popup-overlay" style="display: none;">
    <div class="popup-content" style="border-top: 5px solid #28a745;">
        <div class="popup-body">
            <div style="text-align: center; padding: 1rem 0;">
                <i class="fas fa-check-circle" style="font-size: 4rem; color: #28a745; margin-bottom: 1rem;"></i>
                <h3 style="color: #28a745; font-size: 1.5rem; margin: 0 0 0.5rem 0;">Employee Deleted Successfully!</h3>
                <p style="color: #495057; font-size: 1rem; margin: 0 0 0.5rem 0;">
                    Employee <strong id="deletedEmployeeName">John Doe</strong> has been successfully deleted from the system.
                </p>
                <p style="color: #6c757d; font-size: 0.9rem; margin: 0;">
                    All associated leave records have been permanently removed.
                </p>
            </div>
        </div>
        <div class="popup-footer" style="padding: 1rem 1.5rem; border-top: 1px solid #dee2e6; display: flex; justify-content: center; background: #f8f9fa;">
            <button type="button" class="btn btn-success" onclick="closeDeleteEmployeeSuccessModal()" style="padding: 0.5rem 1.5rem; border: none; border-radius: 4px; min-width: 120px;">
                <i class="fas fa-check"></i> OK
            </button>
        </div>
    </div>
</div>

<!-- Employee Error Modal -->
<div id="deleteEmployeeErrorModal" class="popup-overlay" style="display: none;">
    <div class="popup-content" style="border-top: 5px solid #dc3545;">
        <div class="popup-body">
            <div style="text-align: center; padding: 1rem 0;">
                <i class="fas fa-exclamation-circle" style="font-size: 4rem; color: #dc3545; margin-bottom: 1rem;"></i>
                <h3 style="color: #dc3545; font-size: 1.5rem; margin: 0 0 0.5rem 0;">Deletion Failed</h3>
                <p id="deleteEmployeeErrorMessage" style="color: #495057; font-size: 1rem; margin: 0 0 0.5rem 0;"></p>
                <p style="color: #6c757d; font-size: 0.9rem; margin: 0;">
                    Please try again or contact support if the problem persists.
                </p>
            </div>
        </div>
        <div class="popup-footer" style="padding: 1rem 1.5rem; border-top: 1px solid #dee2e6; display: flex; justify-content: center; background: #f8f9fa;">
            <button type="button" class="btn btn-secondary" onclick="closeDeleteEmployeeErrorModal()" style="padding: 0.5rem 1.5rem; border: none; border-radius: 4px; min-width: 120px;">
                <i class="fas fa-times"></i> Close
            </button>
        </div>
    </div>
</div>

  <!-- JavaScript -->
    <script src="/ELMS_3.0/Admin/Admin.js"></script>
    <script>

 // ============================================
 // EMPLOYEE DETAILS MODAL
 // ============================================

function viewEmployeeDetails(employeeId, employeeName) {
    const modal = document.getElementById('employeeDetailsModal');
    const modalBody = document.getElementById('modalBody');
    const modalTitle = document.getElementById('modalEmployeeName');
    
    modalTitle.textContent = employeeName + ' - Leave Details';
    modalBody.innerHTML = '<div class="loading"><i class="fas fa-spinner fa-spin"></i> Loading employee details...</div>';
    
    // Show modal with flex display for centering
    modal.classList.add('show');
    document.body.style.overflow = 'hidden';
    
    fetch('<%= request.getContextPath() %>/Admin/AdminViewEmployeeListController?action=getEmployeeDetails&employeeId=' + encodeURIComponent(employeeId))
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to load employee details');
            }
            return response.text();
        })
        .then(html => {
            modalBody.innerHTML = html;
        })
        .catch(error => {
            modalBody.innerHTML = '<div style="text-align: center; padding: 40px; color: #dc3545;"><i class="fas fa-exclamation-triangle" style="font-size: 3rem; margin-bottom: 15px;"></i><h3>Error Loading Details</h3><p>' + error.message + '</p></div>';
        });
}

function closeModal() {
    const modal = document.getElementById('employeeDetailsModal');
    if (modal) {
        modal.classList.remove('show');
        document.body.style.overflow = '';
    }
}

 // ============================================
 // EMPLOYEE DELETION
 // ============================================

 let currentEmployeeToDelete = null;

 // Make functions globally available
 window.confirmDeleteEmployee = confirmDeleteEmployee;
 window.closeDeleteEmployeeModal = closeDeleteEmployeeModal;
 window.executeEmployeeDeletion = executeEmployeeDeletion;
 window.closeDeleteEmployeeSuccessModal = closeDeleteEmployeeSuccessModal;
 window.closeDeleteEmployeeErrorModal = closeDeleteEmployeeErrorModal;

 document.addEventListener('DOMContentLoaded', function() {
     console.log('✅ Employee management functionality loaded');
     
     // Search functionality
     initializeEmployeeSearch();
     
     // Form validation for deletion
     initializeEmployeeFormValidation();
     
     // Modal handlers
     initializeEmployeeModalHandlers();
 });

 function initializeEmployeeSearch() {
     const searchInput = document.getElementById('searchInput');
     const employeeTableBody = document.getElementById('employeeTableBody');
     
     if (searchInput && employeeTableBody) {
         searchInput.addEventListener('input', function() {
             const searchTerm = this.value.toLowerCase();
             const rows = employeeTableBody.getElementsByTagName('tr');
             
             Array.from(rows).forEach(row => {
                 const text = row.textContent.toLowerCase();
                 row.style.display = text.includes(searchTerm) ? '' : 'none';
             });
         });
     }
 }

 function initializeEmployeeFormValidation() {
     const passwordInput = document.getElementById('adminPasswordConfirm');
     const confirmCheckbox = document.getElementById('confirmEmployeeDelete');
     const deleteBtn = document.getElementById('confirmEmployeeDeleteBtn');

     function validateForm() {
         if (passwordInput && confirmCheckbox && deleteBtn) {
             const hasPassword = passwordInput.value.trim().length > 0;
             const isConfirmed = confirmCheckbox.checked;
             
             deleteBtn.disabled = !(hasPassword && isConfirmed);
             deleteBtn.style.opacity = deleteBtn.disabled ? '0.6' : '1';
         }
     }

     if (passwordInput) passwordInput.addEventListener('input', validateForm);
     if (confirmCheckbox) confirmCheckbox.addEventListener('change', validateForm);
     
     // Enter key to submit
     if (passwordInput) {
         passwordInput.addEventListener('keypress', function(e) {
             if (e.key === 'Enter' && deleteBtn && !deleteBtn.disabled) {
                 e.preventDefault();
                 executeEmployeeDeletion();
             }
         });
     }
 }
 
//Password visibility toggle for employee delete modal
 function toggleEmployeeDeletePasswordVisibility() {
     const passwordInput = document.getElementById('adminPasswordConfirm');
     const passwordIcon = document.getElementById('employeeDeletePasswordIcon');
     
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

 function initializeEmployeeModalHandlers() {
     // Close on outside click
     window.onclick = function(event) {
         // Employee details modal
         const detailsModal = document.getElementById('employeeDetailsModal');
         if (event.target == detailsModal) {
             closeModal();
         }
         
         // Delete modals
         if (event.target.classList.contains('popup-overlay')) {
             const modalId = event.target.id;
             if (modalId === 'deleteEmployeeModal') closeDeleteEmployeeModal();
             if (modalId === 'deleteEmployeeSuccessModal') closeDeleteEmployeeSuccessModal();
             if (modalId === 'deleteEmployeeErrorModal') closeDeleteEmployeeErrorModal();
         }
     };

     // Close on Escape key
     document.addEventListener('keydown', function(event) {
         if (event.key === 'Escape') {
             closeModal();
             closeDeleteEmployeeModal();
             closeDeleteEmployeeSuccessModal();
             closeDeleteEmployeeErrorModal();
         }
     });
 }

 function confirmDeleteEmployee(employeeId, employeeName, employeeEmail) {
	    console.log('🎯 Opening employee deletion confirmation for:', employeeId, employeeName);
	    
	    if (!employeeId || !employeeName) {
	        showDeleteEmployeeError('Invalid employee data provided');
	        return;
	    }
	    
	    currentEmployeeToDelete = {
	        id: String(employeeId).trim(),
	        name: String(employeeName).trim(),
	        email: String(employeeEmail || 'N/A').trim()
	    };
	    
	    const modal = document.getElementById('deleteEmployeeModal');
	    const nameEl = document.getElementById('deleteEmployeeDisplayName');
	    const idEl = document.getElementById('deleteEmployeeDisplayId');
	    const emailEl = document.getElementById('deleteEmployeeDisplayEmail');
	    const form = document.getElementById('deleteEmployeeForm');
	    const deleteBtn = document.getElementById('confirmEmployeeDeleteBtn');
	    const formSection = document.getElementById('deleteFormSection'); // ✅ FIXED: Changed from 'deleteEmployeeFormSection'
	    
	    if (!modal || !nameEl || !idEl || !emailEl || !form || !deleteBtn) {
	        showDeleteEmployeeError('Modal elements not found. Please refresh the page.');
	        return;
	    }
	    
	    // Populate employee info
	    nameEl.textContent = currentEmployeeToDelete.name;
	    idEl.textContent = currentEmployeeToDelete.id;
	    emailEl.textContent = currentEmployeeToDelete.email;
	    
	    // Reset form
	    form.reset();
	    deleteBtn.disabled = true;
	    deleteBtn.style.opacity = '0.6';
	    
	    // Show form section (hide others)
	    if (formSection) formSection.style.display = 'block'; // ✅ Added null check
	    const loadingEl = document.getElementById('deleteEmployeeLoading');
	    const errorEl = document.getElementById('deleteEmployeeError');
	    const warningEl = document.getElementById('deleteEmployeeWarningSection');
	    if (loadingEl) loadingEl.style.display = 'none';
	    if (errorEl) errorEl.style.display = 'none';
	    if (warningEl) warningEl.style.display = 'none';
	    
	    // Show modal
	    modal.style.display = 'flex';
	    
	    // Focus password input
	    setTimeout(() => {
	        const passwordInput = document.getElementById('adminPasswordConfirm');
	        if (passwordInput) passwordInput.focus();
	    }, 100);
	}
 function closeDeleteEmployeeModal() {
     const modal = document.getElementById('deleteEmployeeModal');
     if (modal) modal.style.display = 'none';
 }

 function executeEmployeeDeletion() {
     if (!currentEmployeeToDelete || !currentEmployeeToDelete.id) {
         showDeleteEmployeeError('No employee selected. Please try again.');
         return;
     }
     
     const passwordInput = document.getElementById('adminPasswordConfirm');
     const deleteBtn = document.getElementById('confirmEmployeeDeleteBtn');
     
     if (!passwordInput) {
         showDeleteEmployeeError('Password input not found');
         return;
     }
     
     const password = passwordInput.value.trim();
     if (!password) {
         showDeleteEmployeeError('Please enter your admin password');
         passwordInput.focus();
         return;
     }
     
     const employeeToDelete = {
         id: currentEmployeeToDelete.id,
         name: currentEmployeeToDelete.name,
         email: currentEmployeeToDelete.email
     };
     
     if (deleteBtn) {
         deleteBtn.disabled = true;
         deleteBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Deleting...';
     }
     
     const formData = new URLSearchParams();
     formData.append('action', 'delete');
     formData.append('employeeId', employeeToDelete.id);
     formData.append('adminPassword', password);
     
     fetch('AdminDeleteEmployeeController', {
         method: 'POST',
         headers: {
             'Content-Type': 'application/x-www-form-urlencoded',
             'X-Requested-With': 'XMLHttpRequest'
         },
         body: formData,
         credentials: 'same-origin'
     })
     .then(response => {
         if (!response.ok) {
             throw new Error(`HTTP ${response.status}: ${response.statusText}`);
         }
         return response.text();
     })
     .then(responseText => {
         const result = responseText.trim();
         
         if (result === 'success' || result.includes('success')) {
             handleEmployeeDeletionSuccess(employeeToDelete);
         } else if (result === 'invalid_password' || result.includes('invalid_password')) {
             showDeleteEmployeeError('Invalid admin password. Please try again.');
             passwordInput.focus();
             passwordInput.select();
         } else if (result.startsWith('error:')) {
             const errorMsg = result.replace('error:', '').trim();
             showDeleteEmployeeError(errorMsg || 'Server error occurred');
         } else {
             showDeleteEmployeeError('Unexpected server response');
         }
     })
     .catch(error => {
         console.error('💥 Request error:', error);
         showDeleteEmployeeError('Network error. Please try again.');
     })
     .finally(() => {
         if (deleteBtn) {
             deleteBtn.disabled = false;
             deleteBtn.innerHTML = '<i class="fas fa-trash-alt"></i> Delete Employee';
         }
     });
 }

 function handleEmployeeDeletionSuccess(employeeData) {
     closeDeleteEmployeeModal();
     removeEmployeeFromTable(employeeData.id);
     showDeleteEmployeeSuccess(employeeData.name);
     currentEmployeeToDelete = null;
 }

 function removeEmployeeFromTable(employeeId) {
     let row = document.querySelector(`tr[data-employee-id="${employeeId}"]`);
     
     if (!row) {
         const rows = document.querySelectorAll('#employeeTableBody tr');
         for (const r of rows) {
             const codeEl = r.querySelector('code');
             if (codeEl && codeEl.textContent.trim() === employeeId) {
                 row = r;
                 break;
             }
         }
     }
     
     if (row) {
         row.style.transition = 'all 0.4s ease';
         row.style.backgroundColor = '#f8d7da';
         row.style.opacity = '0.7';
         
         setTimeout(() => {
             row.style.opacity = '0';
             row.style.transform = 'translateX(-20px) scale(0.95)';
         }, 100);
         
         setTimeout(() => {
             row.remove();
             checkIfEmployeeTableEmpty();
         }, 500);
     } else {
         setTimeout(() => window.location.reload(), 1000);
     }
 }

 function checkIfEmployeeTableEmpty() {
     const tableBody = document.getElementById('employeeTableBody');
     if (tableBody && tableBody.children.length === 0) {
         const container = document.querySelector('.employee-table-container');
         if (container) {
             container.innerHTML = `
                 <div class="empty-state">
                     <i class="fas fa-users"></i>
                     <h3>No Employees Found</h3>
                     <p>There are currently no employees in the system.</p>
                     <a href="/ELMS_3.0/Admin/AdminAddEmployee.jsp" class="btn btn-primary" style="margin-top: 1rem;">
                         <i class="fas fa-plus"></i> Add First Employee
                     </a>
                 </div>
             `;
         }
     }
 }

 function showDeleteEmployeeSuccess(employeeName) {
     const nameEl = document.getElementById('deletedEmployeeName');
     const modal = document.getElementById('deleteEmployeeSuccessModal');
     
     if (nameEl && modal) {
         nameEl.textContent = employeeName;
         modal.style.display = 'flex';
         setTimeout(closeDeleteEmployeeSuccessModal, 3000);
     }
 }

 function closeDeleteEmployeeSuccessModal() {
     const modal = document.getElementById('deleteEmployeeSuccessModal');
     if (modal) modal.style.display = 'none';
 }

 function showDeleteEmployeeError(message) {
     const messageEl = document.getElementById('deleteEmployeeErrorMessage');
     const modal = document.getElementById('deleteEmployeeErrorModal');
     
     if (messageEl && modal) {
         messageEl.textContent = message;
         modal.style.display = 'flex';
     } else {
         alert('Error: ' + message);
     }
 }

 function closeDeleteEmployeeErrorModal() {
     const modal = document.getElementById('deleteEmployeeErrorModal');
     if (modal) modal.style.display = 'none';
 }
 </script>

 
    
</body>
</html>