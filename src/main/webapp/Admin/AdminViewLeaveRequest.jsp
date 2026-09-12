<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page import="elms.service.NudgeNotificationService" %>
<%@ page import="java.util.List" %>
<%
    // Check if admin is logged in
    String adminId = (String) session.getAttribute("adminId");
    String adminName = (String) session.getAttribute("adminName");
    
    if (adminId == null) {
        response.sendRedirect(request.getContextPath() + "/Admin/AdminLogin.jsp");
        return;
    }
    
    // Set default value if adminName is null
    if (adminName == null || adminName.trim().isEmpty()) {
        adminName = "Admin User";
    }
    

    
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="icon" type="image/png" href="<%= request.getContextPath() %>/imnsb_logo.png">
    <title>Leave Requests - IMNSB Admin Panel</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/styles.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/admin.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/dashboard.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        .leave-requests-container {
            max-width: 1400px;
            margin: 0 auto;
        }
        
        .filters-section {
            background: white;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            margin-bottom: 20px;
            overflow: hidden;
        }
        
        .filters-header {
            background: linear-gradient(135deg, var(--primary-color), var(--primary-dark));
            color: white;
            padding: 15px 20px;
            display: flex;
            align-items: center;
            gap: 10px;
        }
        
        .filters-content {
            padding: 20px;
        }
        
        .filter-row {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 15px;
            margin-bottom: 15px;
        }
        
        .filter-group {
            display: flex;
            flex-direction: column;
        }
        
        .filter-group label {
            font-weight: 600;
            margin-bottom: 5px;
            color: #333;
        }
        
        .filter-group select, .filter-group input {
            padding: 8px 12px;
            border: 1px solid #ddd;
            border-radius: 4px;
            font-size: 14px;
        }
        
        .filter-buttons {
            display: flex;
            gap: 10px;
            justify-content: flex-end;
            margin-top: 15px;
        }
        
        .btn {
            padding: 8px 16px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 14px;
            font-weight: 500;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 5px;
            transition: all 0.3s ease;
        }
        
        .btn-primary {
            background: #007bff;
            color: white;
        }
        
        .btn-primary:hover {
            background: #0056b3;
        }
        
        .btn-secondary {
            background: #6c757d;
            color: white;
        }
        
        .btn-secondary:hover {
            background: #545b62;
        }
        
        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }
        
        .stat-card {
            background: white;
            border-radius: 8px;
            padding: 20px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            text-align: center;
        }
        
        .stat-card .stat-number {
            font-size: 2rem;
            font-weight: bold;
            margin-bottom: 5px;
        }
        
        .stat-card .stat-label {
            color: #666;
            font-size: 0.9rem;
        }
        
        .stat-pending { border-left: 4px solid #ffc107; }
        .stat-pending .stat-number { color: #ffc107; }
        
        .stat-approved { border-left: 4px solid #28a745; }
        .stat-approved .stat-number { color: #28a745; }
        
        .stat-rejected { border-left: 4px solid #dc3545; }
        .stat-rejected .stat-number { color: #dc3545; }
        
        .stat-cancelled { border-left: 4px solid #6c757d; }
        .stat-cancelled .stat-number { color: #6c757d; }
        
        .stat-total { border-left: 4px solid #007bff; }
        .stat-total .stat-number { color: #007bff; }
        
         /* Compact Nudge Notification Styles */
        .nudge-section {
            background: white;
            border-radius: 8px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
            margin-bottom: 20px;
            position: relative;
            overflow: hidden;
            border: 1px solid #e0e0e0;
        }
        
        
        
        .nudge-header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 12px 16px;
            border-bottom: 1px solid #f0f0f0;
            background: #fafafa;
        }
        
        .nudge-title-section {
            display: flex;
            align-items: center;
            gap: 10px;
        }
        
        .nudge-icon-container {
            width: 36px;
            height: 36px;
            border-radius: 8px;
            background: linear-gradient(135deg, #ff6b6b, #ff8e53);
            display: flex;
            align-items: center;
            justify-content: center;
            box-shadow: 0 2px 6px rgba(255, 107, 107, 0.25);
        }
        
        .nudge-icon-container i {
            font-size: 18px;
            color: white;
            animation: ring 2s infinite;
        }
        
        @keyframes ring {
            0%, 100% { transform: rotate(0deg); }
            10% { transform: rotate(-15deg); }
            20% { transform: rotate(15deg); }
            30% { transform: rotate(-15deg); }
            40% { transform: rotate(15deg); }
            50% { transform: rotate(0deg); }
        }
        
        .nudge-title-text {
            display: flex;
            flex-direction: column;
            gap: 2px;
        }
        
        .nudge-title {
            font-size: 1rem;
            font-weight: 700;
            color: #2d3748;
            margin: 0;
            display: flex;
            align-items: center;
            gap: 8px;
        }
        
        .nudge-badge {
            background: linear-gradient(135deg, #ff6b6b, #ff8e53);
            color: white;
            padding: 2px 8px;
            border-radius: 12px;
            font-size: 0.7rem;
            font-weight: 700;
            box-shadow: 0 1px 4px rgba(255, 107, 107, 0.3);
        }
        
        .nudge-subtitle {
            font-size: 0.75rem;
            color: #718096;
            font-weight: 400;
        }
        
        .nudge-content {
            padding: 14px 16px;
        }
        
        .nudge-info-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
            gap: 10px;
            margin-bottom: 12px;
        }
        
        .nudge-info-card {
            background: linear-gradient(135deg, #fff5f5, #fff);
            border: 1px solid #fee;
            border-radius: 6px;
            padding: 10px;
            display: flex;
            align-items: start;
            gap: 10px;
        }
        
        .nudge-info-icon {
            width: 28px;
            height: 28px;
            border-radius: 6px;
            background: linear-gradient(135deg, #ff6b6b, #ff8e53);
            display: flex;
            align-items: center;
            justify-content: center;
            flex-shrink: 0;
        }
        
        .nudge-info-icon i {
            font-size: 13px;
            color: white;
        }
        
        .nudge-info-text {
            flex: 1;
        }
        
        .nudge-info-text strong {
            display: block;
            color: #2d3748;
            font-size: 0.8125rem;
            margin-bottom: 2px;
        }
        
        .nudge-info-text p {
            margin: 0;
            color: #718096;
            font-size: 0.75rem;
            line-height: 1.4;
        }
        
        .nudge-actions {
            display: flex;
            gap: 8px;
            padding-top: 12px;
            border-top: 1px solid #f0f0f0;
            flex-wrap: wrap;
        }
        
        .btn-nudge {
            background: linear-gradient(135deg, #ff6b6b, #ff8e53);
            color: white;
            border: none;
            padding: 8px 16px;
            border-radius: 6px;
            font-weight: 600;
            font-size: 0.8125rem;
            cursor: pointer;
            transition: all 0.15s ease;
            display: inline-flex;
            align-items: center;
            gap: 6px;
            box-shadow: 0 2px 6px rgba(255, 107, 107, 0.2);
        }
        
        .btn-nudge:hover {
            transform: translateY(-1px);
            box-shadow: 0 4px 10px rgba(255, 107, 107, 0.3);
        }
        
        .btn-nudge:active {
            transform: translateY(0);
        }
        
        .btn-nudge.secondary {
            background: white;
            color: #4a5568;
            border: 1px solid #e2e8f0;
            box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
        }
        
        .btn-nudge.secondary:hover {
            background: #f7fafc;
            border-color: #cbd5e0;
            box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
        }

        
        .btn-nudge-individual {
            background: #ff9800;
            color: white;
            border: none;
            padding: 4px 8px;
            border-radius: 4px;
            font-size: 0.75rem;
            cursor: pointer;
            transition: all 0.15s ease;
        }
        
        .btn-nudge-individual:hover {
            background: #f57c00;
            transform: scale(1.05);
        }
        
        .btn-nudged-badge {
            background: linear-gradient(135deg, #28a745, #20c997);
            color: white;
            border: none;
            padding: 4px 8px;
            border-radius: 4px;
            font-size: 0.75rem;
            display: inline-flex;
            align-items: center;
            gap: 4px;
            cursor: default;
            font-weight: 600;
            box-shadow: 0 2px 4px rgba(40, 167, 69, 0.2);
        }
        
        .btn-nudged-badge i {
            animation: pulse-check 2s infinite;
        }
        
        @keyframes pulse-check {
            0%, 100% { opacity: 1; transform: scale(1); }
            50% { opacity: 0.7; transform: scale(0.95); }
        }
        
        /* Updated nudge button to show remaining count */
        .btn-nudge-individual {
            position: relative;
        }
        
      
        
        
        .overdue-indicator {
            background: #ffcdd2;
            color: #c62828;
            padding: 2px 6px;
            border-radius: 10px;
            font-size: 0.7rem;
            font-weight: 600;
            margin-left: 5px;
            animation: blink 1.5s infinite;
        }
        
        @keyframes blink {
            0%, 50% { opacity: 1; }
            51%, 100% { opacity: 0.5; }
        }
        
        .days-pending {
            color: #d32f2f;
            font-weight: 600;
        }
        
          /* Custom Fast Tooltips */
        .btn-nudge-individual,
        .btn-nudged-badge {
            position: relative;
        }
        
        .btn-nudge-individual::before,
        .btn-nudged-badge::before {
            content: attr(title);
            position: absolute;
            bottom: 100%;
            left: 50%;
            transform: translateX(-50%) translateY(-5px);
            background: #2d3748;
            color: white;
            padding: 6px 10px;
            border-radius: 6px;
            font-size: 0.7rem;
            white-space: nowrap;
            opacity: 0;
            pointer-events: none;
            transition: opacity 0.1s ease, transform 0.1s ease;
            z-index: 1000;
            margin-bottom: 5px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
        }
        
        .btn-nudge-individual::after,
        .btn-nudged-badge::after {
            content: '';
            position: absolute;
            bottom: 100%;
            left: 50%;
            transform: translateX(-50%);
            border: 5px solid transparent;
            border-top-color: #2d3748;
            opacity: 0;
            pointer-events: none;
            transition: opacity 0.1s ease;
            z-index: 1000;
        }
        
        .btn-nudge-individual:hover::before,
        .btn-nudge-individual:hover::after,
        .btn-nudged-badge:hover::before,
        .btn-nudged-badge:hover::after {
            opacity: 1;
            transform: translateX(-50%) translateY(0);
        }
        
        .btn-nudge-individual:hover::after,
        .btn-nudged-badge:hover::after {
            transform: translateX(-50%);
        }
        
        .requests-table {
            background: white;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            overflow: hidden;
        }
        
        .table-header {
            background: linear-gradient(135deg, var(--primary-color), var(--primary-dark));
            color: white;
            padding: 20px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        
        .table-header h3 {
            margin: 0;
            display: flex;
            align-items: center;
            gap: 10px;
        }
        
        .page-size-selector {
            display: flex;
            align-items: center;
            gap: 10px;
            color: white;
        }
        
        .page-size-selector select {
            padding: 5px 8px;
            border: 1px solid white;
            border-radius: 4px;
            background: white;
            color: #333;
        }
        
        .table-content {
            overflow-x: auto;
        }
        
        table {
            width: 100%;
            border-collapse: collapse;
        }
        
        th, td {
            padding: 12px;
            text-align: left;
            border-bottom: 1px solid #ddd;
        }
        
        th {
            background: #f8f9fa;
            font-weight: 600;
            color: #333;
            position: sticky;
            top: 0;
        }
        
        .employee-info {
            display: flex;
            align-items: center;
            gap: 8px;
        }
        
        .employee-avatar {
            width: 32px;
            height: 32px;
            border-radius: 50%;
            object-fit: cover;
            border: 2px solid #ddd;
        }
        
        .employee-details {
            display: flex;
            flex-direction: column;
        }
        
        .employee-name {
            font-weight: 600;
            color: #333;
        }
        
        .employee-id {
            font-size: 0.8rem;
            color: #666;
        }
        
        .status-badge {
            padding: 4px 8px;
            border-radius: 12px;
            font-size: 0.8rem;
            font-weight: 500;
            position: relative;
            display: inline-flex;
            align-items: center;
            gap: 4px;
        }
        
        .status-pending { background: #fff3cd; color: #856404; }
        .status-approved { background: #d4edda; color: #155724; }
        .status-rejected { 
            background: #f8d7da; 
            color: #721c24; 
            cursor: pointer;
            transition: all 0.3s ease;
        }
        .status-rejected:hover {
            opacity: 0.8;
            transform: scale(1.02);
        }
        .status-cancelled { background: #e2e3e5; color: #383d41; }
        
        
        
        .action-buttons {
            display: flex;
            gap: 5px;
            flex-wrap: wrap;
        }
        
        .btn-sm {
            padding: 4px 8px;
            font-size: 0.75rem;
            border-radius: 4px;
            border: none;
            cursor: pointer;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 4px;
            transition: all 0.15s ease;
        }
        
        .btn-view {
            background: #17a2b8;
            color: white;
        }
        
        .btn-view:hover {
            background: #138496;
        }
        
        .btn-attachment {
            background: #28a745;
            color: white;
        }
        
        .btn-attachment:hover {
            background: #218838;
        }
        
        .btn-delete {
            background: #dc3545;
            color: white;
        }
        
        .btn-delete:hover {
            background: #c82333;
        }
        
        /* Pagination Styles */
        .pagination-container {
            padding: 20px;
            background: white;
            border-top: 1px solid #ddd;
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 15px;
        }
        
        .pagination-info {
            color: #666;
            font-size: 14px;
        }
        
        .pagination {
            display: flex;
            align-items: center;
            gap: 5px;
        }
        
        .pagination a, .pagination span {
            padding: 8px 12px;
            border: 1px solid #ddd;
            text-decoration: none;
            color: #007bff;
            border-radius: 4px;
            transition: all 0.3s ease;
            min-width: 40px;
            text-align: center;
        }
        
        .pagination a:hover {
            background: #f8f9fa;
            border-color: #007bff;
        }
        
        .pagination .current {
            background: #007bff;
            color: white;
            border-color: #007bff;
        }
        
        .pagination .disabled {
            color: #6c757d;
            cursor: not-allowed;
            background: #f8f9fa;
        }
        
        .pagination .disabled:hover {
            background: #f8f9fa;
            border-color: #ddd;
        }
        
        .pagination .ellipsis {
            color: #6c757d;
            cursor: default;
        }
        
        .empty-state {
            text-align: center;
            padding: 40px;
            color: #666;
        }
        
      .popup-overlay {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(0, 0, 0, 0.5);
            display: none;
            justify-content: center;
            align-items: center;
            z-index: 10000;
            animation: fadeIn 0.3s ease-in-out;
        }
        
        .popup-overlay.show {
            display: flex;
        }
        
        .popup-content {
            background: white;
            padding: 30px;
            border-radius: 10px;
            text-align: center;
            min-width: 300px;
            max-width: 500px;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
            animation: slideIn 0.3s ease-in-out;
        }
        
        .popup-icon {
            font-size: 3rem;
            margin-bottom: 15px;
            display: flex;
            justify-content: center;
            align-items: center;
            width: 100%;
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
        
        .popup-btn.btn-danger {
            background-color: #dc3545;
        }
        
        .popup-btn.btn-danger:hover {
            background-color: #c82333;
        }
        
        .popup-btn.btn-warning {
            background-color: #ffc107;
            color: #333;
        }
        
        .popup-btn.btn-warning:hover {
            background-color: #e0a800;
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
        
        .warning-popup .popup-content {
            border-top: 5px solid #ffc107;
        }
        
        .warning-popup .popup-icon {
            color: #ffc107;
        }
        
        .info-popup .popup-content {
            border-top: 5px solid #17a2b8;
        }
        
        .info-popup .popup-icon {
            color: #17a2b8;
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
        
        .priority-indicator {
            width: 8px;
            height: 100%;
            position: absolute;
            left: 0;
            top: 0;
        }
        
        .priority-high { background: #dc3545; }
        .priority-medium { background: #ffc107; }
        .priority-low { background: #28a745; }
        
        .duration-badge {
            background: #e9ecef;
            color: #495057;
            padding: 2px 6px;
            border-radius: 10px;
            font-size: 0.75rem;
            font-weight: 500;
        }
        
        .leave-type-tag {
            background: #f8f9fa;
            color: #495057;
            padding: 2px 8px;
            border-radius: 4px;
            font-size: 0.8rem;
            border: 1px solid #dee2e6;
        }
        
        @media (max-width: 768px) {
        
        	.nudge-header {
                flex-direction: column;
                align-items: flex-start;
                gap: 16px;
            }
            
            .nudge-info-grid {
                grid-template-columns: 1fr;
            }
            
            .nudge-actions {
                flex-direction: column;
                width: 100%;
            }
            
            .btn-nudge {
                width: 100%;
                justify-content: center;
            }
            .filter-row {
                grid-template-columns: 1fr;
            }
            
            .stats-grid {
                grid-template-columns: repeat(2, 1fr);
            }
            
            .action-buttons {
                flex-direction: column;
            }
            
            .btn-sm {
                justify-content: center;
            }
            
            .pagination-container {
                flex-direction: column;
                text-align: center;
            }
            
            .pagination {
                flex-wrap: wrap;
                justify-content: center;
            }
            
            .table-header {
                flex-direction: column;
                gap: 10px;
                text-align: center;
            }
            
            .employee-info {
                flex-direction: column;
                text-align: center;
                gap: 4px;
            }
            
            .employee-details {
                align-items: center;
            }
            
            .rejection-modal-content {
                margin: 10% auto;
                width: 90%;
            }
            
            .nudge-content {
                flex-direction: column;
                align-items: stretch;
            }
            
            .nudge-actions {
                justify-content: center;
            }
            
            .nudge-header {
                flex-direction: column;
                align-items: flex-start;
                gap: 10px;
            }
            
            .rejection-modal-content {
		        margin: 10% auto;
		        width: 90%;
		    }
        }
        
      .toast-notification {
    position: fixed;
    top: 80px; /* Below header */
    right: 20px;
    background: #28a745;
    color: white;
    padding: 16px 24px;
    border-radius: 8px;
    box-shadow: 0 4px 20px rgba(0,0,0,0.25);
    z-index: 10001;
    animation: slideInRight 0.3s ease-out;
    display: flex;
    align-items: center;
    gap: 12px;
    max-width: 400px;
    font-weight: 500;
}

.toast-notification.error {
    background: #dc3545;
}

.toast-notification i {
    font-size: 1.2rem;
}

@keyframes slideInRight {
    from {
        transform: translateX(450px);
        opacity: 0;
    }
    to {
        transform: translateX(0);
        opacity: 1;
    }
}

/* Rejection reason modal styling */
.rejection-modal {
    display: none;
    position: fixed;
    z-index: 1050;
    left: 0;
    top: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0,0,0,0.5);
}

.rejection-modal-content {
    background-color: #fefefe;
    margin: 15% auto;
    padding: 0;
    border: none;
    border-radius: 8px;
    width: 500px;
    max-width: 90%;
    box-shadow: 0 4px 20px rgba(0,0,0,0.3);
}

.rejection-modal-header {
    background: #dc3545;
    color: white;
    padding: 15px 20px;
    border-radius: 8px 8px 0 0;
    display: flex;
    align-items: center;
    gap: 10px;
}

.rejection-modal-header h3 {
    margin: 0;
    font-size: 1.2rem;
}

.rejection-modal-body {
    padding: 20px;
}

.rejection-reason-text {
    color: #333;
    font-size: 14px;
    line-height: 1.6;
    word-wrap: break-word;
    background: #f8f9fa;
    padding: 15px;
    border-radius: 6px;
    border-left: 4px solid #dc3545;
}

.rejection-modal-footer {
    padding: 15px 20px;
    border-top: 1px solid #ddd;
    display: flex;
    justify-content: flex-end;
}
        
    </style>
</head>
<body>
    <div class="dashboard-container">
        <!-- Admin Sidebar -->
        <nav class="sidebar" id="sidebar">
            <div class="sidebar-header">
                <div class="logo-container">
                    <img src="<%= request.getContextPath() %>/imnsb_logowbg.png" alt="IMNSB Logo" class="sidebar-logo">
                </div>
                <h3>IMNSB Admin</h3><br>
            </div>
            <ul class="sidebar-menu">
                 <li>
                <a href="<%= request.getContextPath() %>/admin-dashboard"><i class="fas fa-user"></i> <span>Admin Officer</span></a>
	            </li>
	            <li>
	                <a href="/ELMS_3.0/Admin/AdminViewManagerListController"><i class="fas fa-user-tie"></i> <span>Approval Managers</span></a>
	            </li>
	            <li>
	                <a href="/ELMS_3.0/Admin/AdminViewEmployeeListController"><i class="fas fa-users"></i> <span>Employees</span></a>
	            </li>
	            <li>
	                <a href="/ELMS_3.0/AdminLeaveTypeListController"><i class="fas fa-list-alt"></i> <span>Leave Types</span></a>
	            </li>
	            <li class="active">
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
                    <h2>Leave Requests Management</h2>
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
               
               
   
<!-- ✅ SESSION ERROR/SUCCESS MESSAGES (Show immediately on redirect) -->
<%
    String sessionSuccessMsg = (String) session.getAttribute("successMessage");
    String sessionErrorMsg = (String) session.getAttribute("errorMessage");
    String sessionInfoMsg = (String) session.getAttribute("infoMessage");
    
    // ✅ CRITICAL: Make them available to JSTL
    pageContext.setAttribute("sessionSuccessMsg", sessionSuccessMsg);
    pageContext.setAttribute("sessionErrorMsg", sessionErrorMsg);
    pageContext.setAttribute("sessionInfoMsg", sessionInfoMsg);
    
    // Clear session messages after reading
    if (sessionSuccessMsg != null) session.removeAttribute("successMessage");
    if (sessionErrorMsg != null) session.removeAttribute("errorMessage");
    if (sessionInfoMsg != null) session.removeAttribute("infoMessage");
%>

<!-- Show session error message immediately -->
<% if (sessionErrorMsg != null) { %>
    <div class="alert alert-danger" style="margin: 0 0 20px 0; padding: 20px; background: #f8d7da; border: 1px solid #f5c6cb; border-radius: 8px; border-left: 4px solid #dc3545;">
        <div style="display: flex; justify-content: space-between; align-items: start;">
            <div style="flex: 1;">
                <h5 style="margin: 0 0 10px 0; color: #721c24;">
                    <i class="fas fa-exclamation-circle"></i> Error
                </h5>
                <p style="margin: 0; color: #721c24;">
                    <%= sessionErrorMsg %>
                </p>
            </div>
            <button type="button" onclick="this.parentElement.parentElement.remove()" 
                    style="background: none; border: none; color: #721c24; font-size: 1.5rem; cursor: pointer; padding: 0; margin-left: 15px;">
                <i class="fas fa-times"></i>
            </button>
        </div>
    </div>
<% } %>

<!-- Show session success message immediately -->
<% if (sessionSuccessMsg != null) { %>
    <div class="alert alert-success" style="margin: 0 0 20px 0; padding: 20px; background: #d4edda; border: 1px solid #c3e6cb; border-radius: 8px; border-left: 4px solid #28a745;">
        <div style="display: flex; justify-content: space-between; align-items: start;">
            <div style="flex: 1;">
                <h5 style="margin: 0 0 10px 0; color: #155724;">
                    <i class="fas fa-check-circle"></i> Success
                </h5>
                <p style="margin: 0; color: #155724;">
                    <%= sessionSuccessMsg %>
                </p>
            </div>
            <button type="button" onclick="this.parentElement.parentElement.remove()" 
                    style="background: none; border: none; color: #155724; font-size: 1.5rem; cursor: pointer; padding: 0; margin-left: 15px;">
                <i class="fas fa-times"></i>
            </button>
        </div>
    </div>
<% } %>

    
    <div class="leave-requests-container">
                
                <div class="leave-requests-container">

                    <!-- Statistics Cards -->
                    <div class="stats-grid">
                        <div class="stat-card stat-total">
                            <div class="stat-number">${totalRequests}</div>
                            <div class="stat-label">Total Requests</div>
                        </div>
                        <div class="stat-card stat-pending">
                            <div class="stat-number">${pendingCount}</div>
                            <div class="stat-label">Pending Review</div>
                        </div>
                        <div class="stat-card stat-approved">
                            <div class="stat-number">${approvedCount}</div>
                            <div class="stat-label">Approved</div>
                        </div>
                        <div class="stat-card stat-rejected">
                            <div class="stat-number">${rejectedCount}</div>
                            <div class="stat-label">Rejected</div>
                        </div>
                        <div class="stat-card stat-cancelled">
                            <div class="stat-number">${cancelledCount}</div>
                            <div class="stat-label">Cancelled</div>
                        </div>
                    </div>
                    
                      <!-- Professional Nudge Notification Section -->
                    <c:if test="${overdueCount > 0}">
                        <div class="nudge-section">
                            <!-- Accent Bar -->
                           
                            
                            <!-- Header -->
                            <div class="nudge-header">
                                <div class="nudge-title-section">
                                    <div class="nudge-icon-container">
                                        <i class="fas fa-bell"></i>
                                    </div>
                                    <div class="nudge-title-text">
                                        <h3 class="nudge-title">
                                            Overdue Applications
                                            <span class="nudge-badge">${overdueCount}</span>
                                        </h3>
                                       <p class="nudge-subtitle">Applications pending for more than 2 days (1 nudge per day allowed)</p>
                                    </div>
                                </div>
                            </div>
                            
                            <!-- Content -->
                            <div class="nudge-content">
                                <!-- Info Cards Grid -->
                                <div class="nudge-info-grid">
                                    <div class="nudge-info-card">
                                        <div class="nudge-info-icon">
                                            <i class="fas fa-clock"></i>
                                        </div>
                                        <div class="nudge-info-text">
                                            <strong>${overdueCount} Overdue Application(s)</strong>
                                            <p>Applications awaiting manager review for extended period</p>
                                        </div>
                                    </div>
                                    
                                    <div class="nudge-info-card">
                                        <div class="nudge-info-icon">
                                            <i class="fas fa-paper-plane"></i>
                                        </div>
                                        <div class="nudge-info-text">
                                            <strong>Send Urgent Reminders</strong>
                                            <p>Notify all managers to expedite their review process</p>
                                        </div>
                                    </div>
                                    
                                    <div class="nudge-info-card">
								    <div class="nudge-info-icon">
								        <i class="fas fa-envelope"></i>
								    </div>
								    <div class="nudge-info-text">
								        <strong>Rate Limit: 1 Nudge Per Day</strong>
								        <p>Each application can only be nudged once per day</p>
								    </div>
								</div>
                                
                                <!-- Actions -->
                                <div class="nudge-actions">
                                    <button onclick="sendBulkNudge()" class="btn-nudge">
                                        <i class="fas fa-paper-plane"></i>
                                        Nudge All Overdue Applications
                                    </button>
                                    <button onclick="toggleOverdueDetails()" class="btn-nudge secondary" id="toggleOverdueBtn">
                                        <i class="fas fa-eye"></i>
                                        View Application Details
                                    </button>
                                </div>
                            </div>
                            
                            <!-- Overdue Applications Details (Initially Hidden) -->
                            <div id="overdueDetails" style="display: none; padding: 0 24px 24px 24px; border-top: 1px solid #f0f0f0;">
                                <h4 style="color: #2d3748; margin: 20px 0 16px 0; font-size: 1rem; font-weight: 600;">
                                    <i class="fas fa-list" style="color: #ff6b6b;"></i> Overdue Applications Details
                                </h4>
                                <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 16px;">
                                    <c:forEach var="overdueApp" items="${overdueApplications}">
                                        <div style="background: #fafafa; padding: 16px; border-radius: 8px; border-left: 4px solid #ff6b6b; border: 1px solid #e0e0e0;">
                                            <p style="margin: 0 0 12px 0; font-weight: 600; color: #2d3748; font-size: 0.9375rem;">
                                                <i class="fas fa-user" style="color: #ff6b6b;"></i> ${employeeNames[overdueApp.employeeid]}
                                            </p>
                                            <p style="margin: 0 0 6px 0; font-size: 0.8125rem; color: #718096;">
                                                <strong style="color: #4a5568;">ID:</strong> ${overdueApp.applicationid}
                                            </p>
                                            <p style="margin: 0 0 6px 0; font-size: 0.8125rem; color: #718096;">
                                                <strong style="color: #4a5568;">Type:</strong> ${leaveTypeNames[overdueApp.leavetypeid]}
                                            </p>
                                            <p style="margin: 0 0 12px 0; font-size: 0.8125rem; color: #718096;">
                                                <strong style="color: #4a5568;">Applied:</strong> 
                                                <fmt:parseDate value="${overdueApp.appliedon}" pattern="yyyy-MM-dd HH:mm:ss" var="appliedDate"/>
                                                <fmt:formatDate value="${appliedDate}" pattern="MMM dd, yyyy"/>
                                            </p>
                                            <div style="display: flex; justify-content: space-between; align-items: center; padding-top: 12px; border-top: 1px solid #e0e0e0;">
                                                <span style="color: #ff6b6b; font-weight: 600; font-size: 0.8125rem;">
                                                    <i class="fas fa-clock"></i> 
                                                    <c:set var="appliedOnStr" value="${overdueApp.appliedon}" />
                                                    <jsp:scriptlet>
                                                        // Calculate days pending
                                                        String appliedOnStr = (String)pageContext.getAttribute("appliedOnStr");
                                                        int daysPending = 0;
                                                        try {
                                                            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                            java.util.Date appliedDate = sdf.parse(appliedOnStr);
                                                            java.util.Date currentDate = new java.util.Date();
                                                            long diffInMillies = currentDate.getTime() - appliedDate.getTime();
                                                            daysPending = (int)java.util.concurrent.TimeUnit.DAYS.convert(diffInMillies, java.util.concurrent.TimeUnit.MILLISECONDS);
                                                        } catch (Exception e) {
                                                            daysPending = 0;
                                                        }
                                                        pageContext.setAttribute("daysPending", daysPending);
                                                    </jsp:scriptlet>
                                                    ${daysPending} days
                                                </span>
                                                <jsp:scriptlet>
                                                    // Check nudge count for overdue application
                                                    elms.model.LeaveApplication overdueApp = (elms.model.LeaveApplication)pageContext.getAttribute("overdueApp");
                                                    String overdueAppId = overdueApp.getApplicationid();
                                                    int overdueNudgeCount = elms.service.NudgeNotificationService.getDailyNudgeCount(overdueAppId);
                                                    int overdueRemainingNudges = elms.service.NudgeNotificationService.getRemainingNudges(overdueAppId);
                                                    pageContext.setAttribute("overdueNudgeCount", overdueNudgeCount);
                                                    pageContext.setAttribute("overdueRemainingNudges", overdueRemainingNudges);
                                                </jsp:scriptlet>
                                                
                                                <c:choose>
                                                    <c:when test="${overdueRemainingNudges > 0}">
                                                        <button onclick="sendIndividualNudge('${overdueApp.applicationid}', this)" 
                                                                class="btn-nudge-individual"
                                                                title="${overdueRemainingNudges} nudge(s) remaining today">
                                                            <i class="fas fa-bell"></i> Nudge
                                                            
                                                        </button>
                                                    </c:when>
                                                    <c:otherwise>
													<span class="btn-sm btn-nudged-badge" style="font-size: 0.7rem; padding: 3px 6px;"
													      title="Already nudged today - Daily limit reached (1 per day)">
													    <i class="fas fa-check-circle"></i> Nudged Today
													</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>
                                        </div>
                                    </c:forEach>
                                </div>
                            </div>
                        </div>
                    </c:if>
                    
                     <!-- Filters Section -->
                    <div class="filters-section">
                        <div class="filters-header">
                            <i class="fas fa-filter"></i>
                            <h3>Filter Leave Requests</h3>
                        </div>
                        <div class="filters-content">
                            <form id="filterForm" method="GET" action="<%= request.getContextPath() %>/admin-leave-requests">
                                <!-- Hidden field to maintain page size -->
                                <input type="hidden" name="size" value="${pageSize}">
                                
                                <div class="filter-row">
                                    <div class="filter-group">
                                        <label for="statusFilter">Status</label>
                                        <select id="statusFilter" name="status">
                                            <option value="">All Status</option>
                                            <option value="Pending" ${param.status == 'Pending' ? 'selected' : ''}>Pending</option>
                                            <option value="Approved" ${param.status == 'Approved' ? 'selected' : ''}>Approved</option>
                                            <option value="Rejected" ${param.status == 'Rejected' ? 'selected' : ''}>Rejected</option>
                                            <option value="Cancelled" ${param.status == 'Cancelled' ? 'selected' : ''}>Cancelled</option>
                                        </select>
                                    </div>
                                    
                                    <div class="filter-group">
									    <label for="leaveTypeFilter">Leave Type</label>
									    <select id="leaveTypeFilter" name="leaveType">
									        <option value="">All Leave Types</option>
									        <!-- Options will be populated by JavaScript -->
									    </select>
									</div>
                                    
                                    <div class="filter-group">
                                        <label for="employeeFilter">Employee</label>
                                        <input type="text" id="employeeFilter" name="employee" 
                                               placeholder="Search by name or ID..." 
                                               value="${param.employee}">
                                    </div>
                                    
                                    <div class="filter-group">
                                        <label for="fromDate">From Date</label>
                                        <input type="date" id="fromDate" name="fromDate" value="${param.fromDate}">
                                    </div>
                                    
                                    <div class="filter-group">
                                        <label for="toDate">To Date</label>
                                        <input type="date" id="toDate" name="toDate" value="${param.toDate}">
                                    </div>
                                </div>
                                
                                <div class="filter-buttons">
                                    <button type="button" id="clearFilters" class="btn btn-secondary">
                                        <i class="fas fa-eraser"></i> Clear Filters
                                    </button>
                                    <button type="submit" class="btn btn-primary">
                                        <i class="fas fa-search"></i> Apply Filters
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>
                    
                    <!-- Leave Requests Table -->
                    <div class="requests-table">
                        <div class="table-header">
                            <h3><i class="fas fa-calendar-check"></i> Leave Requests</h3>
                            <div class="page-size-selector">
                                <label for="pageSizeSelect">Show:</label>
                                <select id="pageSizeSelect" onchange="changePageSize(this.value)">
                                    <c:forEach var="size" items="${allowedPageSizes}">
                                        <option value="${size}" ${pageSize == size ? 'selected' : ''}>${size}</option>
                                    </c:forEach>
                                </select>
                                <span>per page</span>
                            </div>
                        </div>
                        <div class="table-content">
                            <c:choose>
                                <c:when test="${not empty allRequests}">
                                    <table>
                                        <thead>
                                            <tr>
                                                <th>Employee</th>
                                                <th>Application ID</th>
                                                <th>Leave Type</th>
                                                <th>Start Date</th>
                                                <th>End Date</th>
                                                <th>Duration</th>
                                                <th>Status</th>
                                                <th>Applied On</th>
                                                <th>Actions</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="request" items="${allRequests}">
                                                <tr data-application-id="${request.applicationid}" 
                                                    data-employee-name="${fn:escapeXml(employeeNames[request.employeeid])}" 
                                                    data-employee-id="${request.employeeid}"
                                                    data-status="${request.leavestatus}">
                                                    <td>
                                                        <div class="employee-info">
                                                            <img src="${empty employeeProfilePictures[request.employeeid] ? pageContext.request.contextPath.concat('/defaultprofilepicture.jpg') : pageContext.request.contextPath.concat('/').concat(employeeProfilePictures[request.employeeid])}" 
                                                                 alt="Employee Avatar" class="employee-avatar"
                                                                 onerror="this.src='${pageContext.request.contextPath}/defaultprofilepicture.jpg';">
                                                            <div class="employee-details">
                                                                <div class="employee-name">${employeeNames[request.employeeid]}</div>
                                                                <div class="employee-id">${request.employeeid}</div>
                                                            </div>
                                                        </div>
                                                    </td>
                                                    <td>
                                                        <strong>${request.applicationid}</strong>
                                                    </td>
                                                    <td>
                                                        <span class="leave-type-tag">
                                                            <c:choose>
                                                                <c:when test="${not empty leaveTypeNames[request.leavetypeid]}">
                                                                    ${leaveTypeNames[request.leavetypeid]}
                                                                </c:when>
                                                                <c:otherwise>
                                                                    Unknown Type
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </span>
                                                    </td>
                                                    <td>
                                                        <fmt:parseDate value="${request.leavestartdate}" pattern="yyyy-MM-dd" var="startDate"/>
                                                        <fmt:formatDate value="${startDate}" pattern="MMM dd, yyyy"/>
                                                    </td>
                                                    <td>
                                                        <fmt:parseDate value="${request.leaveenddate}" pattern="yyyy-MM-dd" var="endDate"/>
                                                        <fmt:formatDate value="${endDate}" pattern="MMM dd, yyyy"/>
                                                    </td>
                                                    <td>
                                                        <span class="duration-badge">${request.leaveduration} day(s)</span>
                                                    </td>
                                                    <td style="position: relative;">
                                                        <c:choose>
                                                            <c:when test="${request.leavestatus == 'Pending'}">
                                                                <span class="status-badge status-pending">Pending</span>
                                                            </c:when>
                                                            <c:when test="${request.leavestatus == 'Approved'}">
                                                                <span class="status-badge status-approved">Approved</span>
                                                            </c:when>
                                                            <c:when test="${request.leavestatus == 'Rejected'}">
                                                                <c:choose>
                                                                    <c:when test="${not empty request.rejectReason}">
                                                                        <span class="status-badge status-rejected" 
                                                                              onclick="showRejectionReason('${request.applicationid}', '${fn:escapeXml(request.rejectReason)}')"
                                                                              title="Click to view rejection reason">
                                                                            Rejected <i class="fas fa-info-circle"></i>
                                                                        </span>
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        <span class="status-badge status-rejected">Rejected</span>
                                                                    </c:otherwise>
                                                                </c:choose>
                                                            </c:when>
                                                            <c:when test="${request.leavestatus == 'Cancelled'}">
                                                                <span class="status-badge status-cancelled">Cancelled</span>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <span class="status-badge">${request.leavestatus}</span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td>
                                                        <fmt:parseDate value="${request.appliedon}" pattern="yyyy-MM-dd HH:mm:ss" var="appliedDate"/>
                                                        <fmt:formatDate value="${appliedDate}" pattern="MMM dd, yyyy"/>
                                                    </td>
                                                    <td>
                                                        <div class="action-buttons">
                                                            <!-- View Application Form Button -->
                                                            <a href="<%= request.getContextPath() %>/admin-leave-application-pdf?applicationId=${request.applicationid}" 
                                                               target="_blank" class="btn-sm btn-view" title="View Application Form">
                                                                <i class="fas fa-eye"></i> View Form
                                                            </a>
                                                            
                                                            <!-- View Attachment Button -->
                                                            <c:if test="${not empty request.attachment}">
                                                                <a href="<%= request.getContextPath() %>/admin-file-download?file=${request.attachment}&applicationId=${request.applicationid}" 
                                                                   target="_blank" class="btn-sm btn-attachment" title="View Attachment">
                                                                    <i class="fas fa-paperclip"></i> Attachment
                                                                </a>
                                                            </c:if>
                                                            
                                                              <!-- Nudge Button for Pending Applications -->
                                            <c:if test="${request.leavestatus == 'Pending'}">
                                                <c:set var="appliedOnStr" value="${request.appliedon}" />
                                                <jsp:scriptlet>
                                                    // Calculate days pending for this application
                                                    String appliedOnStr = (String)pageContext.getAttribute("appliedOnStr");
                                                    int daysPending = 0;
                                                    try {
                                                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                        java.util.Date appliedDate = sdf.parse(appliedOnStr);
                                                        java.util.Date currentDate = new java.util.Date();
                                                        long diffInMillies = currentDate.getTime() - appliedDate.getTime();
                                                        daysPending = (int)java.util.concurrent.TimeUnit.DAYS.convert(diffInMillies, java.util.concurrent.TimeUnit.MILLISECONDS);
                                                    } catch (Exception e) {
                                                        daysPending = 0;
                                                    }
                                                    pageContext.setAttribute("daysPending", daysPending);
                                                    
                                                    // ✅ NEW: Check nudge count for this application
                                                    elms.model.LeaveApplication currentRequest = (elms.model.LeaveApplication)pageContext.getAttribute("request");
                                                    String applicationId = currentRequest.getApplicationid();
                                                    int nudgeCount = elms.service.NudgeNotificationService.getDailyNudgeCount(applicationId);
                                                    int remainingNudges = elms.service.NudgeNotificationService.getRemainingNudges(applicationId);
                                                    pageContext.setAttribute("nudgeCount", nudgeCount);
                                                    pageContext.setAttribute("remainingNudges", remainingNudges);
                                                </jsp:scriptlet>
                                                
                                                <c:if test="${daysPending >= 3}">
                                                    <!-- ✅ Check if nudges are still available -->
                                                    <c:choose>
                                                        <c:when test="${remainingNudges > 0}">
                                                            <!-- Show active nudge button with remaining count -->
                                                            <button onclick="sendIndividualNudge('${request.applicationid}', this)" 
                                                                    class="btn-sm btn-nudge-individual" 
                                                                    title="Send nudge notification (${daysPending} days pending, ${remainingNudges} nudge(s) remaining today)">
                                                                <i class="fas fa-bell"></i> Nudge
                                                                <span class="overdue-indicator">${daysPending}d</span>
                                                                
                                                            </button>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <!-- Show "Already Nudged" badge -->
                           										<span class="btn-sm btn-nudged-badge" 
															      title="Already nudged today - Daily limit: 1 nudge per day. Will reset tomorrow.">
															    <i class="fas fa-check-circle"></i> Nudged Today
															</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </c:if>
                                            </c:if>

                                                            
                                                            <!-- Delete Button (only for non-pending applications) -->
                                                            <c:if test="${request.leavestatus != 'Pending'}">
                                                                <button onclick="confirmDeleteFromRow(this)" 
                                                                        class="btn-sm btn-delete" title="Delete Application">
                                                                    <i class="fas fa-trash"></i> Delete
                                                                </button>
                                                           </c:if>
                                                        </div>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </c:when>
                                <c:otherwise>
                                    <div class="empty-state">
                                        <i class="fas fa-calendar-times" style="font-size: 3rem; color: #ccc; margin-bottom: 20px;"></i>
                                        <h3>No Leave Requests Found</h3>
                                        <p>No leave requests match your current filter criteria.</p>
                                        <button onclick="clearAllFilters()" class="btn btn-secondary">
                                            <i class="fas fa-eraser"></i> Clear All Filters
                                        </button>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        
                        <!-- Pagination Controls -->
                        <c:if test="${totalPages > 1}">
                            <div class="pagination-container">
                                <div class="pagination-info">
                                    ${showingInfo}
                                </div>
                                <div class="pagination">
                                    <!-- First Page -->
                                    <c:if test="${currentPage > 1}">
                                        <a href="?page=1&size=${pageSize}&status=${param.status}&leaveType=${param.leaveType}&employee=${param.employee}&fromDate=${param.fromDate}&toDate=${param.toDate}" title="First Page">
                                            <i class="fas fa-angle-double-left"></i>
                                        </a>
                                    </c:if>
                                    <c:if test="${currentPage <= 1}">
                                        <span class="disabled">
                                            <i class="fas fa-angle-double-left"></i>
                                        </span>
                                    </c:if>
                                    
                                    <!-- Previous Page -->
                                    <c:if test="${currentPage > 1}">
                                        <a href="?page=${currentPage - 1}&size=${pageSize}&status=${param.status}&leaveType=${param.leaveType}&employee=${param.employee}&fromDate=${param.fromDate}&toDate=${param.toDate}" title="Previous Page">
                                            <i class="fas fa-angle-left"></i>
                                        </a>
                                    </c:if>
                                    <c:if test="${currentPage <= 1}">
                                        <span class="disabled">
                                            <i class="fas fa-angle-left"></i>
                                        </span>
                                    </c:if>
                                    
                                    <!-- Page Numbers -->
                                    <c:forEach var="pageNum" begin="${startPage}" end="${endPage}">
                                        <c:choose>
                                            <c:when test="${pageNum == currentPage}">
                                                <span class="current">${pageNum}</span>
                                            </c:when>
                                            <c:otherwise>
                                                <a href="?page=${pageNum}&size=${pageSize}&status=${param.status}&leaveType=${param.leaveType}&employee=${param.employee}&fromDate=${param.fromDate}&toDate=${param.toDate}">${pageNum}</a>
                                            </c:otherwise>
                                        </c:choose>
                                    </c:forEach>
                                    
                                    <!-- Show ellipsis if there are more pages -->
                                    <c:if test="${endPage < totalPages}">
                                        <c:if test="${endPage < totalPages - 1}">
                                            <span class="ellipsis">...</span>
                                        </c:if>
                                        <a href="?page=${totalPages}&size=${pageSize}&status=${param.status}&leaveType=${param.leaveType}&employee=${param.employee}&fromDate=${param.fromDate}&toDate=${param.toDate}">${totalPages}</a>
                                    </c:if>
                                    
                                    <!-- Next Page -->
                                    <c:if test="${currentPage < totalPages}">
                                        <a href="?page=${currentPage + 1}&size=${pageSize}&status=${param.status}&leaveType=${param.leaveType}&employee=${param.employee}&fromDate=${param.fromDate}&toDate=${param.toDate}" title="Next Page">
                                            <i class="fas fa-angle-right"></i>
                                        </a>
                                    </c:if>
                                    <c:if test="${currentPage >= totalPages}">
                                        <span class="disabled">
                                            <i class="fas fa-angle-right"></i>
                                        </span>
                                    </c:if>
                                    
                                    <!-- Last Page -->
                                    <c:if test="${currentPage < totalPages}">
                                        <a href="?page=${totalPages}&size=${pageSize}&status=${param.status}&leaveType=${param.leaveType}&employee=${param.employee}&fromDate=${param.fromDate}&toDate=${param.toDate}" title="Last Page">
                                            <i class="fas fa-angle-double-right"></i>
                                        </a>
                                    </c:if>
                                    <c:if test="${currentPage >= totalPages}">
                                        <span class="disabled">
                                            <i class="fas fa-angle-double-right"></i>
                                        </span>
                                    </c:if>
                                </div>
                            </div>
                        </c:if>
                    </div>
                </div>
            </div>
        </main>
    </div>
    
    
    
    <!-- Modern Delete Confirmation Modal -->
	<div id="deleteModal" class="popup-overlay warning-popup" style="display: none;">
	    <div class="popup-content">
	        <div class="popup-icon">⚠️</div>
	        <h3>Delete Leave Application</h3>
	        <p><strong>Are you sure you want to delete this leave application?</strong></p>
	        <div id="deleteApplicationDetails" style="margin: 15px 0; padding: 15px; background: #fff3cd; border-radius: 8px; border-left: 4px solid #ffc107;">
	            <!-- Application details will be inserted here -->
	        </div>
	        <p style="font-size: 0.9rem; color: #856404; margin-bottom: 20px;">
	            <i class="fas fa-exclamation-triangle"></i> <strong>Warning:</strong> This action cannot be undone. The application will be permanently removed from the system.
	        </p>
	        <div>
	            <button class="popup-btn btn-secondary" onclick="closeDeleteModal()">
	                <i class="fas fa-times"></i> No, Keep It
	            </button>
	            <button class="popup-btn btn-danger" onclick="executeDelete()">
	                <i class="fas fa-check"></i> Yes, Delete It
	            </button>
	        </div>
	    </div>
	</div>
	
	<!-- Rejection Reason Modal -->
<div id="rejectionModal" class="rejection-modal">
    <div class="rejection-modal-content">
        <div class="rejection-modal-header">
            <i class="fas fa-exclamation-triangle"></i>
            <h3>Rejection Reason</h3>
        </div>
        <div class="rejection-modal-body">
            <div id="rejectionReasonText" class="rejection-reason-text">
                <!-- Rejection reason will be inserted here -->
            </div>
        </div>
        <div class="rejection-modal-footer">
            <button onclick="closeRejectionModal()" class="btn btn-secondary">
                <i class="fas fa-times"></i> Close
            </button>
        </div>
    </div>
</div>
    
    <!-- Success Modal -->
    <div id="successModal" class="popup-overlay success-popup">
        <div class="popup-content">
            <div class="popup-icon">✅</div>
            <h3>Success!</h3>
            <p id="successMessage"></p>
            <button class="popup-btn btn-success" onclick="closeSuccessModal()">OK</button>
        </div>
    </div>

    <!-- Error Modal -->
    <div id="errorModal" class="popup-overlay error-popup">
        <div class="popup-content">
            <div class="popup-icon">❌</div>
            <h3>Error</h3>
            <p id="errorMessage"></p>
            <button class="popup-btn btn-danger" onclick="closeErrorModal()">OK</button>
        </div>
    </div>

    <!-- Info Modal -->
    <div id="infoModal" class="popup-overlay info-popup">
        <div class="popup-content">
            <div class="popup-icon">ℹ️</div>
            <h3>Information</h3>
            <p id="infoMessage"></p>
            <button class="popup-btn" onclick="closeInfoModal()">OK</button>
        </div>
    </div>

   <script src="<%= request.getContextPath() %>/Admin/Admin.js"></script>
<script>
// ========================================
// SESSION MESSAGE HANDLING ON PAGE LOAD
// ========================================

// ✅ CHECK FOR SESSION MESSAGES AND SHOW POPUPS
document.addEventListener('DOMContentLoaded', function() {
    <% if (sessionSuccessMsg != null) { %>
        showSuccessModal('<%= sessionSuccessMsg.replace("'", "\\'") %>');
    <% } %>
    
    <% if (sessionErrorMsg != null) { %>
        showErrorModal('<%= sessionErrorMsg.replace("'", "\\'") %>');
    <% } %>
    
    // Add event listener for clear filters button
    const clearFiltersBtn = document.getElementById('clearFilters');
    if (clearFiltersBtn) {
        clearFiltersBtn.addEventListener('click', clearAllFilters);
    }
    
    // ✅ POPULATE AND SORT LEAVE TYPE FILTER
    populateLeaveTypeFilter();
});

// ========================================
// POPULATE AND SORT LEAVE TYPE FILTER
// ========================================

function populateLeaveTypeFilter() {
    const leaveTypeSelect = document.getElementById('leaveTypeFilter');
    
    if (!leaveTypeSelect) {
        console.log('Leave type filter not found');
        return;
    }
    
    // Get the current selected value (if any)
    const currentSelection = '${param.leaveType}';
    
    // Collect all leave types from the JSP
    const leaveTypes = [];
    <c:forEach var="entry" items="${leaveTypeNames}">
        leaveTypes.push({
            key: '${entry.key}',
            value: '${entry.value}'
        });
    </c:forEach>
    
    // Sort alphabetically by leave type name
    leaveTypes.sort((a, b) => a.value.localeCompare(b.value));
    
    // Clear existing options except the first one ("All Leave Types")
    while (leaveTypeSelect.options.length > 1) {
        leaveTypeSelect.remove(1);
    }
    
    // Populate with sorted leave types
    leaveTypes.forEach(function(leaveType) {
        const option = document.createElement('option');
        option.value = leaveType.key;
        option.textContent = leaveType.value;
        
        // Restore previous selection if it exists
        if (currentSelection && currentSelection === leaveType.key) {
            option.selected = true;
        }
        
        leaveTypeSelect.appendChild(option);
    });
    
    console.log('✅ Admin leave type filter populated and sorted (' + leaveTypes.length + ' types)');
}


// ========================================
// GLOBAL VARIABLES
// ========================================

let applicationToDelete = null;


// ========================================
// DELETE FUNCTIONS
// ========================================

function confirmDeleteFromRow(button) {
    const row = button.closest('tr');
    
    if (!row) {
        showErrorModal('Error: Could not find table row');
        return;
    }
    
    const dataAppId = row.getAttribute('data-application-id');
    const dataEmpName = row.getAttribute('data-employee-name');
    const dataEmpId = row.getAttribute('data-employee-id');
    const dataStatus = row.getAttribute('data-status');
    
    if (!dataAppId) {
        showErrorModal('Error: No application ID found');
        return;
    }
    
    const displayName = dataEmpName || `Employee ID: ${dataEmpId}`;
    applicationToDelete = dataAppId;
    
    const content = 
        '<p style="margin: 5px 0; color: #333;"><strong>Application ID:</strong> ' + dataAppId + '</p>' +
        '<p style="margin: 5px 0; color: #333;"><strong>Employee:</strong> ' + displayName + '</p>' +
        '<p style="margin: 5px 0; color: #333;"><strong>Status:</strong> ' + dataStatus + '</p>';
    
    const modal = document.getElementById('deleteModal');
    const details = document.getElementById('deleteApplicationDetails');
    
    if (!modal || !details) {
        showErrorModal('Error: Modal elements not found');
        return;
    }
    
    details.innerHTML = content;
    modal.style.display = 'flex';
}

function closeDeleteModal() {
    document.getElementById('deleteModal').style.display = 'none';
    applicationToDelete = null;
}

function executeDelete() {
    if (!applicationToDelete) {
        showErrorModal('No application selected for deletion.');
        return;
    }
    
    const deleteButton = document.querySelector('#deleteModal .popup-btn.btn-danger');
    const originalText = deleteButton ? deleteButton.innerHTML : '';
    if (deleteButton) {
        deleteButton.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Deleting...';
        deleteButton.disabled = true;
    }
    
    try {
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = '<%= request.getContextPath() %>/admin-leave-requests';
        
        const actionInput = document.createElement('input');
        actionInput.type = 'hidden';
        actionInput.name = 'action';
        actionInput.value = 'delete';
        form.appendChild(actionInput);
        
        const applicationIdInput = document.createElement('input');
        applicationIdInput.type = 'hidden';
        applicationIdInput.name = 'applicationId';
        applicationIdInput.value = applicationToDelete;
        form.appendChild(applicationIdInput);
        
        const currentUrl = new URL(window.location);
        const paramsToPreserve = ['page', 'size', 'status', 'leaveType', 'employee', 'fromDate', 'toDate'];
        
        paramsToPreserve.forEach(param => {
            const value = currentUrl.searchParams.get(param);
            if (value) {
                const input = document.createElement('input');
                input.type = 'hidden';
                input.name = param;
                input.value = value;
                form.appendChild(input);
            }
        });
        
        document.body.appendChild(form);
        form.submit();
        
        closeDeleteModal();
        
    } catch (error) {
        console.error('Error during deletion:', error);
        showErrorModal('An error occurred while deleting the application. Please try again.');
        
        if (deleteButton) {
            deleteButton.innerHTML = originalText;
            deleteButton.disabled = false;
        }
    }
}

// ========================================
// NUDGE FUNCTIONS
// ========================================

function toggleOverdueDetails() {
    const detailsDiv = document.getElementById('overdueDetails');
    const toggleBtn = document.getElementById('toggleOverdueBtn');
    
    if (detailsDiv && toggleBtn) {
        if (detailsDiv.style.display === 'none') {
            detailsDiv.style.display = 'block';
            toggleBtn.innerHTML = '<i class="fas fa-eye-slash"></i> Hide Details';
        } else {
            detailsDiv.style.display = 'none';
            toggleBtn.innerHTML = '<i class="fas fa-eye"></i> View Details';
        }
    }
}

/**
 * Send bulk nudge with progress tracking
 */
function sendBulkNudge() {
    const overdueCount = ${overdueCount};
    
    if (overdueCount === 0) {
        showToast('No overdue applications found.', 'error');
        return;
    }
    
    const confirmed = confirm(
        'Are you sure you want to send nudge notifications for all ' + overdueCount + ' overdue applications?\n\n' +
        'This will send urgent reminder emails to all managers.\n' +
        'Note: Each application can only be nudged once per day.\n\n' +
        'This may take 10-20 seconds. Click OK to proceed or Cancel to abort.'
    );
    
    if (!confirmed) return;
    
    // ✅ STEP 1: Show progress modal
    const progressModal = createProgressModal();
    document.body.appendChild(progressModal);
    
    const params = new URLSearchParams();
    params.append('action', 'bulkNudge');
    
    updateProgressModal(0, overdueCount, 'Initializing bulk nudge...');
    
    console.log('📤 Starting bulk nudge...');
    
    // ✅ STEP 2: Simulate progress animation
    let currentProgress = 0;
    const progressInterval = setInterval(() => {
        currentProgress += 1;
        if (currentProgress <= overdueCount) {
            updateProgressModal(currentProgress, overdueCount, 
                'Processing application ' + currentProgress + ' of ' + overdueCount + '...');
        } else {
            clearInterval(progressInterval);
        }
    }, (overdueCount > 0) ? Math.max(1000, (15000 / overdueCount)) : 1000);
    
    fetch('<%= request.getContextPath() %>/admin-leave-requests', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: params.toString()
    })
    .then(response => {
        clearInterval(progressInterval);
        
        console.log('📥 Bulk nudge response status:', response.status);
        
        if (!response.ok) {
            throw new Error('Server returned ' + response.status + ': ' + response.statusText);
        }
        return response.json();
    })
    .then(data => {
        console.log('✅ Bulk nudge response:', data);
        
        // ✅ STEP 3: Hide progress modal
        progressModal.remove();
        
        // ✅ STEP 4: Show success or error modal
        if (data.success) {
            showSuccessModal(data.message);
            // ✅ Page will reload after modal closes - buttons will update
        } else {
            showErrorModal(data.message);
            // ✅ Page will reload after modal closes - buttons stay the same
        }
    })
    .catch(error => {
        clearInterval(progressInterval);
        console.error('❌ Error in bulk nudge:', error);
        
        // ✅ STEP 3: Hide progress modal
        progressModal.remove();
        
        // ✅ STEP 4: Show error modal
        showErrorModal('Failed to send bulk nudge: ' + error.message);
    });
}

/**
 * Create progress modal
 */
function createProgressModal() {
    const modal = document.createElement('div');
    modal.id = 'bulkNudgeProgressModal';
    modal.className = 'popup-overlay info-popup';
    modal.style.display = 'flex';
    
    modal.innerHTML = `
        <div class="popup-content" style="min-width: 400px;">
            <div class="popup-icon">
                <i class="fas fa-spinner fa-spin" style="color: #17a2b8;"></i>
            </div>
            <h3>Sending Bulk Nudges</h3>
            <p id="progressMessage" style="margin: 10px 0;">Initializing...</p>
            <div style="width: 100%; background: #e9ecef; border-radius: 10px; height: 20px; margin: 15px 0; overflow: hidden;">
                <div id="progressBar" style="width: 0%; background: linear-gradient(90deg, #17a2b8, #138496); height: 100%; transition: width 0.3s ease; display: flex; align-items: center; justify-content: center; color: white; font-size: 0.75rem; font-weight: 600;">
                    <span id="progressPercentage">0%</span>
                </div>
            </div>
            <p id="progressCount" style="font-size: 0.9rem; color: #666; margin: 0;">
                Processing: <strong id="currentCount">0</strong> / <strong id="totalCount">0</strong> applications
            </p>
            <p style="font-size: 0.85rem; color: #999; margin-top: 15px;">
                <i class="fas fa-info-circle"></i> Please wait, this may take 10-20 seconds...
            </p>
        </div>
    `;
    
    return modal;
}

/**
 * Update progress modal
 */
function updateProgressModal(current, total, message) {
    const progressBar = document.getElementById('progressBar');
    const progressPercentage = document.getElementById('progressPercentage');
    const progressMessage = document.getElementById('progressMessage');
    const currentCount = document.getElementById('currentCount');
    const totalCount = document.getElementById('totalCount');
    
    if (progressBar && progressPercentage && progressMessage && currentCount && totalCount) {
        const percentage = Math.round((current / total) * 100);
        
        progressBar.style.width = percentage + '%';
        progressPercentage.textContent = percentage + '%';
        progressMessage.textContent = message;
        currentCount.textContent = current;
        totalCount.textContent = total;
    }
}


/**
 * Send individual nudge with proper button state management
 */
function sendIndividualNudge(applicationId, buttonElement) {
    if (!applicationId) {
        showToast('Invalid application ID.', 'error');
        return;
    }
    
    const confirmed = confirm(
        'Send nudge notification for application ' + applicationId + '?\n\n' +
        'This will send an urgent reminder email to all managers.\n' +
        'Note: You can only nudge this application once per day.\n\n' +
        'This may take 1-2 seconds. Click OK to send nudge or Cancel to abort.'
    );
    
    if (!confirmed) return;
    
    const clickedButton = buttonElement || event.target.closest('.btn-nudge-individual');
    const originalHTML = clickedButton ? clickedButton.innerHTML : '';
    
    // ✅ STEP 1: Show "Sending..." state
    if (clickedButton) {
        clickedButton.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Sending...';
        clickedButton.disabled = true;
        clickedButton.style.opacity = '0.7';
        clickedButton.title = 'Sending email notifications...';
    }
    
    const params = new URLSearchParams();
    params.append('action', 'nudge');
    params.append('applicationId', applicationId);
    
    console.log('📤 Sending nudge request:', { action: 'nudge', applicationId: applicationId });
    
    fetch('<%= request.getContextPath() %>/admin-leave-requests', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: params.toString()
    })
    .then(response => {
        console.log('📥 Response status:', response.status);
        
        if (!response.ok) {
            throw new Error('Server returned ' + response.status + ': ' + response.statusText);
        }
        return response.json();
    })
    .then(data => {
        console.log('✅ Nudge response:', data);
        
        // ✅ STEP 2: Email sending complete - show modal based on result
        if (data.success) {
            // ✅ SUCCESS: Show success modal, then update button
            showSuccessModal(data.message);
            
            // ✅ STEP 3: Update button to "Nudged Today" AFTER modal is shown
            if (clickedButton) {
                clickedButton.outerHTML = 
                    '<span class="btn-sm btn-nudged-badge" ' +
                          'title="Nudged successfully today">' +
                        '<i class="fas fa-check-circle"></i> Nudged Today' +
                    '</span>';
            }
            
        } else {
            // ❌ ERROR: Show error modal, then restore button
            showErrorModal(data.message);
            
            // ✅ STEP 3: Restore button to original state AFTER modal is shown
            if (clickedButton) {
                clickedButton.innerHTML = originalHTML;
                clickedButton.disabled = false;
                clickedButton.style.opacity = '1';
                clickedButton.title = 'Send nudge notification';
            }
        }
    })
    .catch(error => {
        console.error('❌ Error sending nudge:', error);
        
        // ❌ NETWORK ERROR: Show error modal, then restore button
        showErrorModal('Failed to send nudge: ' + error.message);
        
        if (clickedButton) {
            clickedButton.innerHTML = originalHTML;
            clickedButton.disabled = false;
            clickedButton.style.opacity = '1';
            clickedButton.title = 'Send nudge notification';
        }
    });
}



// ========================================
// HELPER FUNCTIONS
// ========================================

function showToast(message, type) {
    type = type || 'success';
    
    const toast = document.createElement('div');
    toast.className = 'toast-notification' + (type === 'error' ? ' error' : '');
    
    const iconClass = type === 'error' ? 'exclamation-circle' : 'check-circle';
    toast.innerHTML = '<i class="fas fa-' + iconClass + '"></i><span>' + message + '</span>';
    
    document.body.appendChild(toast);
    
    setTimeout(function() {
        toast.style.animation = 'slideInRight 0.3s ease-out reverse';
        setTimeout(function() {
            toast.remove();
        }, 300);
    }, 3000);
}

function changePageSize(newSize) {
    const currentUrl = new URL(window.location);
    currentUrl.searchParams.set('size', newSize);
    currentUrl.searchParams.set('page', '1');
    window.location.href = currentUrl.toString();
}

function clearAllFilters() {
    const url = new URL(window.location);
    url.search = '?size=' + ${pageSize};
    window.location.href = url.toString();
}



// ========================================
// MODAL FUNCTIONS
// ========================================

function showSuccessModal(message) {
    document.getElementById('successMessage').textContent = message;
    document.getElementById('successModal').classList.add('show');
}

function closeSuccessModal() {
    document.getElementById('successModal').classList.remove('show');
    // ✅ Reload page to show updated button states
    location.reload();
}

function showErrorModal(message) {
    document.getElementById('errorMessage').textContent = message;
    document.getElementById('errorModal').classList.add('show');
}

function closeErrorModal() {
    document.getElementById('errorModal').classList.remove('show');
    // ✅ Reload page (buttons will stay in original state if failed)
    location.reload();
}

//========================================
//REJECTION REASON MODAL FUNCTIONS
//========================================

function showRejectionReason(applicationId, reason) {
 document.getElementById('rejectionReasonText').innerHTML = reason;
 document.getElementById('rejectionModal').style.display = 'block';
}

function closeRejectionModal() {
 document.getElementById('rejectionModal').style.display = 'none';
}

//Close rejection modal when clicking outside
window.addEventListener('click', function(event) {
 const rejectionModal = document.getElementById('rejectionModal');
 if (event.target === rejectionModal) {
     closeRejectionModal();
 }
});

function showInfoModal(message) {
    document.getElementById('infoMessage').textContent = message;
    document.getElementById('infoModal').classList.add('show');
}

function closeInfoModal() {
    document.getElementById('infoModal').classList.remove('show');
}

// Close modals when clicking outside
document.addEventListener('click', function(event) {
    const modals = ['successModal', 'errorModal', 'infoModal'];
    modals.forEach(modalId => {
        const modal = document.getElementById(modalId);
        if (event.target === modal) {
            modal.classList.remove('show');
        }
    });
});

//Close modals with Escape key
document.addEventListener('keydown', function(event) {
    if (event.key === 'Escape') {
        const modals = ['successModal', 'errorModal', 'infoModal'];
        modals.forEach(modalId => {
            document.getElementById(modalId).classList.remove('show');
        });
        closeDeleteModal();
        closeRejectionModal(); // ✅ ADD THIS LINE
    }
});

// Close delete modal when clicking outside
window.addEventListener('click', function(event) {
    const deleteModal = document.getElementById('deleteModal');
    if (event.target === deleteModal) {
        closeDeleteModal();
    }
});
</script>
</body>
</html>