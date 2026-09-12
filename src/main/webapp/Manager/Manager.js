document.addEventListener('DOMContentLoaded', function() {
    const sidebarToggle = document.getElementById('sidebarToggle');
    const sidebar = document.getElementById('sidebar');
    const mainContent = document.getElementById('mainContent');
    const logoutBtn = document.getElementById('logoutBtn');

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
        const icon = sidebarToggle.querySelector('i');
        
        if (window.innerWidth <= 991) {
            // Mobile: when sidebar is open, show X; when closed, show bars
            if (sidebar.classList.contains('open')) {
                icon.classList.remove('fa-bars');
                icon.classList.add('fa-times');
            } else {
                icon.classList.remove('fa-times');
                icon.classList.add('fa-bars');
            }
        } else {
            // Desktop: when sidebar is visible (not collapsed), show X; when hidden (collapsed), show bars
            if (sidebar.classList.contains('collapsed')) {
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
            sidebar.classList.remove('collapsed');
            mainContent.classList.remove('expanded');
        } else {
            sidebar.classList.remove('open');
        }
        updateToggleIcon();
    }

    sidebarToggle.addEventListener('click', toggleSidebar);
    window.addEventListener('resize', handleResponsive);
    handleResponsive();

    document.addEventListener('click', function(e) {
        if (window.innerWidth <= 991) {
            if (!sidebar.contains(e.target) && !sidebarToggle.contains(e.target)) {
                sidebar.classList.remove('open');
                updateToggleIcon();
            }
        }
    });
	
	logoutBtn.addEventListener('click', function(e) {
	       e.preventDefault();
	       if (confirm('Are you sure you want to logout?')) {
	           fetch('/ELMS_3.0/logout', { method: 'POST' })
	               .then(() => window.location.href = '/ELMS_3.0/Manager/ManagerLogin.jsp')
	               .catch(() => window.location.href = '/ELMS_3.0/Manager/Manager.jsp');
	       }
	   });
	   
	   
	   
});


 