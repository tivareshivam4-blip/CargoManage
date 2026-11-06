package cargomanage.dao;

import cargomanage.config.DatabaseConfig;
import cargomanage.security.PasswordUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DatabaseManager {

    private static boolean initialized = false;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found in classpath: " + e.getMessage());
        }
    }

    public static String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                DatabaseConfig.getDatabaseUrl(),
                DatabaseConfig.getUser(),
                DatabaseConfig.getPassword()
        );
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            return false;
        }
    }

    public static synchronized void initializeDatabase() throws SQLException {
        if (initialized) return;

        // Step 1: Connect to server and create database if not present
        try (Connection conn = DriverManager.getConnection(
                DatabaseConfig.getServerUrl(),
                DatabaseConfig.getUser(),
                DatabaseConfig.getPassword());
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + DatabaseConfig.getDbName() +
                    "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        }

        // Step 2: Connect to specific database and create tables
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Branches
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS `branches` (
                    `id` INT AUTO_INCREMENT PRIMARY KEY,
                    `name` VARCHAR(100) NOT NULL,
                    `location` VARCHAR(255) NOT NULL,
                    `code` VARCHAR(20),
                    `phone` VARCHAR(30),
                    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """);

            // Users
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS `users` (
                    `id` INT AUTO_INCREMENT PRIMARY KEY,
                    `username` VARCHAR(50) NOT NULL UNIQUE,
                    `password_hash` VARCHAR(100) NOT NULL,
                    `full_name` VARCHAR(100) NOT NULL,
                    `role` VARCHAR(20) NOT NULL,
                    `branch_id` INT NULL,
                    `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    `last_login` TIMESTAMP NULL,
                    FOREIGN KEY (`branch_id`) REFERENCES `branches`(`id`) ON DELETE SET NULL
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """);

            // Products
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS `products` (
                    `id` INT AUTO_INCREMENT PRIMARY KEY,
                    `name` VARCHAR(150) NOT NULL,
                    `category` VARCHAR(50) NOT NULL,
                    `brand` VARCHAR(50) DEFAULT 'Generic',
                    `description` TEXT,
                    `sku` VARCHAR(50) UNIQUE,
                    `barcode` VARCHAR(50),
                    `unit` VARCHAR(20) NOT NULL DEFAULT 'KG',
                    `purchase_price` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
                    `selling_price` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
                    `reorder_level` INT NOT NULL DEFAULT 10,
                    `supplier_name` VARCHAR(100) DEFAULT '',
                    `location` VARCHAR(100) DEFAULT 'Rack A-1',
                    `mfg_date` VARCHAR(30) NULL,
                    `exp_date` VARCHAR(30) NULL,
                    `status` VARCHAR(20) NOT NULL DEFAULT 'Active',
                    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """);

            // Stock
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS `stocks` (
                    `id` INT AUTO_INCREMENT PRIMARY KEY,
                    `branch_id` INT NOT NULL,
                    `product_id` INT NOT NULL,
                    `quantity` INT NOT NULL DEFAULT 0,
                    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    UNIQUE KEY `uq_branch_product` (`branch_id`, `product_id`),
                    FOREIGN KEY (`branch_id`) REFERENCES `branches`(`id`) ON DELETE CASCADE,
                    FOREIGN KEY (`product_id`) REFERENCES `products`(`id`) ON DELETE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """);

            // Purchases
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS `purchases` (
                    `id` INT AUTO_INCREMENT PRIMARY KEY,
                    `supplier_name` VARCHAR(100) NOT NULL,
                    `supplier_contact` VARCHAR(100) NULL,
                    `product_id` INT NOT NULL,
                    `branch_id` INT NOT NULL,
                    `quantity` INT NOT NULL,
                    `purchase_price` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
                    `total_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
                    `purchase_date` VARCHAR(30) NOT NULL,
                    `payment_status` VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                    `payment_ref` VARCHAR(100) NULL,
                    `received_qty` INT NOT NULL DEFAULT 0,
                    `status` VARCHAR(20) NOT NULL DEFAULT 'RECEIVED',
                    `created_by` VARCHAR(50) NULL,
                    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (`product_id`) REFERENCES `products`(`id`),
                    FOREIGN KEY (`branch_id`) REFERENCES `branches`(`id`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """);

            // Transfers
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS `transfers` (
                    `id` INT AUTO_INCREMENT PRIMARY KEY,
                    `product_id` INT NOT NULL,
                    `source_branch_id` INT NOT NULL,
                    `dest_branch_id` INT NOT NULL,
                    `quantity` INT NOT NULL,
                    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                    `requested_by` VARCHAR(50) NOT NULL,
                    `approved_by` VARCHAR(50) NULL,
                    `received_by` VARCHAR(50) NULL,
                    `request_date` VARCHAR(30) NOT NULL,
                    `dispatch_date` VARCHAR(30) NULL,
                    `received_date` VARCHAR(30) NULL,
                    `remarks` TEXT NULL,
                    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (`product_id`) REFERENCES `products`(`id`),
                    FOREIGN KEY (`source_branch_id`) REFERENCES `branches`(`id`),
                    FOREIGN KEY (`dest_branch_id`) REFERENCES `branches`(`id`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """);

            // Shipments
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS `shipments` (
                    `id` INT AUTO_INCREMENT PRIMARY KEY,
                    `transfer_id` INT NOT NULL,
                    `tracking_number` VARCHAR(50) NULL,
                    `carrier` VARCHAR(50) DEFAULT 'CargoManage Logistics',
                    `status` VARCHAR(20) NOT NULL DEFAULT 'DISPATCHED',
                    `eta` VARCHAR(50) NULL,
                    `dispatch_date` VARCHAR(30) NULL,
                    `delivery_date` VARCHAR(30) NULL,
                    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    FOREIGN KEY (`transfer_id`) REFERENCES `transfers`(`id`) ON DELETE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """);

            // Payments
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS `payments` (
                    `id` INT AUTO_INCREMENT PRIMARY KEY,
                    `transaction_id` VARCHAR(50) NOT NULL UNIQUE,
                    `receipt_no` VARCHAR(50) NOT NULL UNIQUE,
                    `payment_date` VARCHAR(30) NOT NULL,
                    `party_name` VARCHAR(100) NOT NULL,
                    `party_type` VARCHAR(30) DEFAULT 'SUPPLIER',
                    `related_type` VARCHAR(30) NULL,
                    `related_id` INT NULL,
                    `payment_method` VARCHAR(30) NOT NULL DEFAULT 'Bank Transfer',
                    `direction` VARCHAR(20) NOT NULL DEFAULT 'Outgoing',
                    `amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
                    `status` VARCHAR(20) NOT NULL DEFAULT 'Paid',
                    `reference_no` VARCHAR(100) NULL,
                    `description` VARCHAR(255) NULL,
                    `recorded_by` VARCHAR(50) NOT NULL,
                    `remarks` TEXT NULL,
                    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """);

            // Audit Logs
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS `audit_logs` (
                    `id` INT AUTO_INCREMENT PRIMARY KEY,
                    `username` VARCHAR(50) NOT NULL,
                    `action` VARCHAR(100) NOT NULL,
                    `details` TEXT,
                    `timestamp` VARCHAR(30) NOT NULL,
                    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """);
        }

        seedDemoDataIfEmpty();
        initialized = true;
    }

    private static void seedDemoDataIfEmpty() throws SQLException {
        try (Connection conn = getConnection()) {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM branches")) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return;
                }
            }

            // 1. Insert 12 Indian branches
            String[][] branches = {
                {"Murbad Warehouse", "Murbad, Thane, Maharashtra", "BR-MB01", "+91 98200 11001"},
                {"Mumbai Depot", "Mumbai, Maharashtra", "BR-MUM02", "+91 98200 11002"},
                {"Pune Distribution Center", "Pune, Maharashtra", "BR-PUN03", "+91 98200 11003"},
                {"Delhi NCR Hub", "Delhi, NCR", "BR-DEL04", "+91 98200 11004"},
                {"Bengaluru Warehouse", "Bengaluru, Karnataka", "BR-BLR05", "+91 98200 11005"},
                {"Chennai Port Depot", "Chennai, Tamil Nadu", "BR-CHN06", "+91 98200 11006"},
                {"Hyderabad Distribution Center", "Hyderabad, Telangana", "BR-HYD07", "+91 98200 11007"},
                {"Kolkata Warehouse", "Kolkata, West Bengal", "BR-KOL08", "+91 98200 11008"},
                {"Ahmedabad Depot", "Ahmedabad, Gujarat", "BR-AMD09", "+91 98200 11009"},
                {"Kochi Export Warehouse", "Kochi, Kerala", "BR-KOC10", "+91 98200 11010"},
                {"Jaipur Distribution Center", "Jaipur, Rajasthan", "BR-JAI11", "+91 98200 11011"},
                {"Lucknow Warehouse", "Lucknow, Uttar Pradesh", "BR-LKO12", "+91 98200 11012"}
            };

            String insertBranchSql = "INSERT INTO branches (id, name, location, code, phone) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertBranchSql)) {
                for (int i = 0; i < branches.length; i++) {
                    ps.setInt(1, i + 1);
                    ps.setString(2, branches[i][0]);
                    ps.setString(3, branches[i][1]);
                    ps.setString(4, branches[i][2]);
                    ps.setString(5, branches[i][3]);
                    ps.executeUpdate();
                }
            }

            // 2. Insert 4 Default Users with BCrypt Hashing
            String insertUserSql = "INSERT INTO users (username, password_hash, full_name, role, branch_id, status) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertUserSql)) {
                // Admin
                ps.setString(1, "admin");
                ps.setString(2, PasswordUtil.hash("admin123"));
                ps.setString(3, "System Administrator");
                ps.setString(4, "ADMIN");
                ps.setNull(5, Types.INTEGER);
                ps.setString(6, "ACTIVE");
                ps.executeUpdate();

                // Manager
                ps.setString(1, "manager");
                ps.setString(2, PasswordUtil.hash("manager123"));
                ps.setString(3, "Warehouse Operations Manager");
                ps.setString(4, "MANAGER");
                ps.setNull(5, Types.INTEGER);
                ps.setString(6, "ACTIVE");
                ps.executeUpdate();

                // Accountant
                ps.setString(1, "accountant");
                ps.setString(2, PasswordUtil.hash("account123"));
                ps.setString(3, "Chief Financial Accountant");
                ps.setString(4, "ACCOUNTANT");
                ps.setNull(5, Types.INTEGER);
                ps.setString(6, "ACTIVE");
                ps.executeUpdate();

                // Branch Staff
                ps.setString(1, "murbad");
                ps.setString(2, PasswordUtil.hash("staff123"));
                ps.setString(3, "Murbad Warehouse Supervisor");
                ps.setString(4, "BRANCH_STAFF");
                ps.setInt(5, 1);
                ps.setString(6, "ACTIVE");
                ps.executeUpdate();
            }

            // 3. Insert 10 Detailed Products
            Object[][] products = {
                {1, "Turmeric Powder", "Spices", "Everfresh Spices", "High-curcumin pure ground turmeric powder", "CM-SP-001", "890123400001", "KG", 140.0, 190.0, 25, "Maharashtra Agro Spices", "Rack A-1", "2026-01-10", "2027-01-10", "Active"},
                {2, "Black Pepper", "Spices", "Malabar Gold", "Premium Malabar graded black pepper whole/coarse", "CM-SP-002", "890123400002", "KG", 450.0, 580.0, 20, "Kerala Spice Exports", "Rack A-2", "2026-02-01", "2027-08-01", "Active"},
                {3, "Red Chilli Powder", "Spices", "Guntur Pride", "Vibrant red hot chilli powder from Guntur farms", "CM-SP-003", "890123400003", "KG", 220.0, 290.0, 25, "Andhra Agro Traders", "Rack A-3", "2026-01-15", "2027-01-15", "Active"},
                {4, "Cumin Powder", "Spices", "Royal Cumin", "Aromatic roasted cumin powder with intense flavor", "CM-SP-004", "890123400004", "KG", 280.0, 360.0, 20, "Gujarat Seed Corporation", "Rack A-4", "2026-03-05", "2027-03-05", "Active"},
                {5, "Coriander Powder", "Spices", "Aroma Pure", "Freshly grounded coriander seed powder", "CM-SP-005", "890123400005", "KG", 160.0, 210.0, 20, "Rajasthan Agro Hub", "Rack A-5", "2026-02-20", "2027-02-20", "Active"},
                {6, "Garam Masala", "Spices", "Chef Special", "Master blend of 14 roasted aromatic whole spices", "CM-SP-006", "890123400006", "KG", 320.0, 420.0, 15, "North India Spice Mills", "Rack B-1", "2026-03-10", "2027-03-10", "Active"},
                {7, "Cardamom", "Whole Spices", "Green Pods", "Premium 8mm jumbo green cardamom from Idukki", "CM-SP-007", "890123400007", "KG", 1800.0, 2400.0, 10, "Idukki Green Estates", "Rack B-2", "2026-01-20", "2027-07-20", "Active"},
                {8, "Black Tea Powder", "Tea", "Assam Valley", "Rich strong CTC black tea granular powder", "CM-TEA-001", "890123400008", "KG", 310.0, 410.0, 25, "Assam Tea Orchards", "Rack B-3", "2026-02-12", "2027-08-12", "Active"},
                {9, "Soya Chunks", "Food", "NutriMeal", "High protein textured soy granules / chunks", "CM-FD-001", "890123400009", "KG", 110.0, 150.0, 30, "Indore Protein Mills", "Rack C-1", "2026-02-25", "2027-02-25", "Active"},
                {10, "Basmati Rice", "Grains", "Royal Heritage", "Extra long grain traditional aged basmati rice", "CM-RC-001", "890123400010", "KG", 480.0, 620.0, 50, "Punjab Grain Logistics", "Rack C-2", "2026-01-05", "2028-01-05", "Active"}
            };

            String insertProdSql = "INSERT INTO products (id, name, category, brand, description, sku, barcode, unit, purchase_price, selling_price, reorder_level, supplier_name, location, mfg_date, exp_date, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertProdSql)) {
                for (Object[] p : products) {
                    for (int j = 0; j < p.length; j++) {
                        ps.setObject(j + 1, p[j]);
                    }
                    ps.executeUpdate();
                }
            }

            // 4. Insert Demo Stock Across Branches
            int[][] demoStock = {
                {1,1,120},{1,2,80},{1,3,55},{1,4,45},{1,5,90},
                {2,1,250},{2,2,140},{2,3,120},{2,4,75},{2,8,160},
                {3,1,180},{3,5,95},{3,6,60},{3,10,300},
                {4,1,100},{4,2,70},{4,7,35},{4,8,110},
                {5,3,130},{5,4,85},{5,9,170},
                {6,1,160},{6,5,110},{6,8,200},
                {7,2,90},{7,6,55},{7,10,220},
                {8,3,75},{8,7,25},{8,8,140},
                {9,1,95},{9,4,70},{9,5,85},
                {10,2,60},{10,6,45},{10,9,120},
                {11,1,115},{11,3,90},{11,10,180},
                {12,4,65},{12,5,75},{12,8,100}
            };

            String insertStockSql = "INSERT INTO stocks (branch_id, product_id, quantity) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertStockSql)) {
                for (int[] s : demoStock) {
                    ps.setInt(1, s[0]);
                    ps.setInt(2, s[1]);
                    ps.setInt(3, s[2]);
                    ps.executeUpdate();
                }
            }

            // 5. Insert Sample Purchases
            String insertPurchSql = "INSERT INTO purchases (id, supplier_name, supplier_contact, product_id, branch_id, quantity, purchase_price, total_amount, purchase_date, payment_status, payment_ref, received_qty, status, created_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertPurchSql)) {
                ps.setInt(1, 101);
                ps.setString(2, "Maharashtra Agro Spices");
                ps.setString(3, "+91 98210 22001");
                ps.setInt(4, 1);
                ps.setInt(5, 1);
                ps.setInt(6, 120);
                ps.setDouble(7, 140.0);
                ps.setDouble(8, 16800.0);
                ps.setString(9, "2026-10-15 10:30:00");
                ps.setString(10, "PAID");
                ps.setString(11, "UTR-20261015-8812");
                ps.setInt(12, 120);
                ps.setString(13, "RECEIVED");
                ps.setString(14, "admin");
                ps.executeUpdate();

                ps.setInt(1, 102);
                ps.setString(2, "Kerala Spice Exports");
                ps.setString(3, "+91 98210 22002");
                ps.setInt(4, 2);
                ps.setInt(5, 2);
                ps.setInt(6, 140);
                ps.setDouble(7, 450.0);
                ps.setDouble(8, 63000.0);
                ps.setString(9, "2026-10-20 14:15:00");
                ps.setString(10, "PAID");
                ps.setString(11, "UTR-20261020-9421");
                ps.setInt(12, 140);
                ps.setString(13, "RECEIVED");
                ps.setString(14, "manager");
                ps.executeUpdate();

                ps.setInt(1, 103);
                ps.setString(2, "Punjab Grain Logistics");
                ps.setString(3, "+91 98210 22010");
                ps.setInt(4, 10);
                ps.setInt(5, 3);
                ps.setInt(6, 300);
                ps.setDouble(7, 480.0);
                ps.setDouble(8, 144000.0);
                ps.setString(9, "2026-10-25 11:00:00");
                ps.setString(10, "PENDING");
                ps.setString(11, "");
                ps.setInt(12, 300);
                ps.setString(13, "RECEIVED");
                ps.setString(14, "admin");
                ps.executeUpdate();
            }

            // 6. Insert Sample Transfers & Shipments
            String insertTransSql = "INSERT INTO transfers (id, product_id, source_branch_id, dest_branch_id, quantity, status, requested_by, approved_by, received_by, request_date, dispatch_date, received_date, remarks) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertTransSql)) {
                ps.setInt(1, 201);
                ps.setInt(2, 1);
                ps.setInt(3, 2);
                ps.setInt(4, 1);
                ps.setInt(5, 50);
                ps.setString(6, "RECEIVED");
                ps.setString(7, "murbad");
                ps.setString(8, "manager");
                ps.setString(9, "murbad");
                ps.setString(10, "2026-10-18 09:30:00");
                ps.setString(11, "2026-10-19 11:00:00");
                ps.setString(12, "2026-10-21 16:45:00");
                ps.setString(13, "Regular monthly stock replenishment");
                ps.executeUpdate();

                ps.setInt(1, 202);
                ps.setInt(2, 3);
                ps.setInt(3, 1);
                ps.setInt(4, 3);
                ps.setInt(5, 20);
                ps.setString(6, "DISPATCHED");
                ps.setString(7, "manager");
                ps.setString(8, "admin");
                ps.setString(9, null);
                ps.setString(10, "2026-10-28 15:00:00");
                ps.setString(11, "2026-10-29 10:30:00");
                ps.setString(12, null);
                ps.setString(13, "Express transfer for local wholesale demand");
                ps.executeUpdate();
            }

            String insertShipSql = "INSERT INTO shipments (id, transfer_id, tracking_number, carrier, status, eta, dispatch_date, delivery_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertShipSql)) {
                ps.setInt(1, 301);
                ps.setInt(2, 201);
                ps.setString(3, "CM-TRK-202601");
                ps.setString(4, "CargoManage Fleet Truck #MH-04-9812");
                ps.setString(5, "DELIVERED");
                ps.setString(6, "Completed");
                ps.setString(7, "2026-10-19 11:00:00");
                ps.setString(8, "2026-10-21 16:45:00");
                ps.executeUpdate();

                ps.setInt(1, 302);
                ps.setInt(2, 202);
                ps.setString(3, "CM-TRK-202602");
                ps.setString(4, "CargoManage Express Carrier #MH-12-4411");
                ps.setString(5, "IN TRANSIT");
                ps.setString(6, "Within 2 business days");
                ps.setString(7, "2026-10-29 10:30:00");
                ps.setString(8, null);
                ps.executeUpdate();
            }

            // 7. Insert Sample Payments with Unique Receipt Numbers
            String insertPaySql = "INSERT INTO payments (transaction_id, receipt_no, payment_date, party_name, party_type, related_type, related_id, payment_method, direction, amount, status, reference_no, description, recorded_by, remarks) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertPaySql)) {
                ps.setString(1, "TXN-20261015-001");
                ps.setString(2, "RCP-202610-001");
                ps.setString(3, "2026-10-15 11:30:00");
                ps.setString(4, "Maharashtra Agro Spices");
                ps.setString(5, "SUPPLIER");
                ps.setString(6, "PURCHASE");
                ps.setInt(7, 101);
                ps.setString(8, "Bank Transfer");
                ps.setString(9, "Outgoing");
                ps.setDouble(10, 16800.0);
                ps.setString(11, "Paid");
                ps.setString(12, "UTR-20261015-8812");
                ps.setString(13, "Payment for Purchase #101 (120 KG Turmeric Powder)");
                ps.setString(14, "accountant");
                ps.setString(15, "NEFT transfer cleared via HDFC corporate account");
                ps.executeUpdate();

                ps.setString(1, "TXN-20261020-002");
                ps.setString(2, "RCP-202610-002");
                ps.setString(3, "2026-10-20 15:45:00");
                ps.setString(4, "Kerala Spice Exports");
                ps.setString(5, "SUPPLIER");
                ps.setString(6, "PURCHASE");
                ps.setInt(7, 102);
                ps.setString(8, "Bank Transfer");
                ps.setString(9, "Outgoing");
                ps.setDouble(10, 63000.0);
                ps.setString(11, "Paid");
                ps.setString(12, "UTR-20261020-9421");
                ps.setString(13, "Payment for Purchase #102 (140 KG Black Pepper)");
                ps.setString(14, "accountant");
                ps.setString(15, "RTGS transfer approved by Finance Manager");
                ps.executeUpdate();

                ps.setString(1, "TXN-20261022-003");
                ps.setString(2, "RCP-202610-003");
                ps.setString(3, "2026-10-22 14:00:00");
                ps.setString(4, "Metro Supermarkets Pvt Ltd");
                ps.setString(5, "CUSTOMER");
                ps.setString(6, "SALES");
                ps.setInt(7, 401);
                ps.setString(8, "UPI");
                ps.setString(9, "Incoming");
                ps.setDouble(10, 85000.0);
                ps.setString(11, "Paid");
                ps.setString(12, "UPI/629104882199");
                ps.setString(13, "Wholesale distribution billing advance receipt");
                ps.setString(14, "accountant");
                ps.setString(15, "Received into primary business UPI VPA");
                ps.executeUpdate();

                ps.setString(1, "TXN-20261026-004");
                ps.setString(2, "RCP-202610-004");
                ps.setString(3, "2026-10-26 10:15:00");
                ps.setString(4, "Punjab Grain Logistics");
                ps.setString(5, "SUPPLIER");
                ps.setString(6, "PURCHASE");
                ps.setInt(7, 103);
                ps.setString(8, "Bank Transfer");
                ps.setString(9, "Outgoing");
                ps.setDouble(10, 144000.0);
                ps.setString(11, "Pending");
                ps.setString(12, "PO-PUNJAB-9921");
                ps.setString(13, "Payment for Purchase #103 (300 KG Basmati Rice)");
                ps.setString(14, "accountant");
                ps.setString(15, "Scheduled for payment batch on next credit cycle");
                ps.executeUpdate();
            }

            // 8. Insert System Initialization Audit Log
            String insertAuditSql = "INSERT INTO audit_logs (username, action, details, timestamp) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertAuditSql)) {
                ps.setString(1, "SYSTEM");
                ps.setString(2, "SYSTEM READY");
                ps.setString(3, "CargoManage enterprise MySQL database initialized with demo branches, products, stock, and default accounts.");
                ps.setString(4, now());
                ps.executeUpdate();
            }
        }
    }
}

