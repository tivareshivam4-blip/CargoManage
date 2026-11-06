package cargomanage.security;

import cargomanage.model.User;

public class SessionContext {
    private static User currentUser;

    public static synchronized void setCurrentUser(User user) {
        currentUser = user;
    }

    public static synchronized User getCurrentUser() {
        return currentUser;
    }

    public static synchronized void clear() {
        currentUser = null;
    }

    public static synchronized boolean isLoggedIn() {
        return currentUser != null;
    }

    public static synchronized String getUsername() {
        return currentUser != null ? currentUser.getUsername() : "SYSTEM";
    }

    public static synchronized String getRole() {
        return currentUser != null ? currentUser.getRole() : "GUEST";
    }

    public static synchronized Integer getBranchId() {
        return currentUser != null ? currentUser.getBranchId() : null;
    }

    public static synchronized boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    public static synchronized boolean isManager() {
        return currentUser != null && currentUser.isManager();
    }

    public static synchronized boolean isAccountant() {
        return currentUser != null && currentUser.isAccountant();
    }

    public static synchronized boolean isBranchStaff() {
        return currentUser != null && currentUser.isBranchStaff();
    }

    public static synchronized boolean canManageUsers() {
        return isAdmin();
    }

    public static synchronized boolean canManageBranches() {
        return isAdmin();
    }

    public static synchronized boolean canModifyProducts() {
        return isAdmin() || isManager();
    }

    public static synchronized boolean canUpdateStock() {
        return isAdmin() || isManager();
    }

    public static synchronized boolean canApproveTransfers() {
        return isAdmin() || isManager();
    }

    public static synchronized boolean canManagePayments() {
        return isAdmin() || isAccountant();
    }

    public static synchronized boolean canViewPayments() {
        return isAdmin() || isAccountant();
    }

    public static synchronized boolean canViewShipments() {
        return isAdmin() || isManager() || isBranchStaff();
    }

    public static synchronized boolean canUpdateShipmentStatus() {
        return isAdmin() || isManager();
    }
}

