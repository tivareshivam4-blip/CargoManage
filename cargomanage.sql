-- =====================================================================
-- CARGOMANAGE – Enterprise Warehouse, Inventory & Business Management System
-- Tagline: Manage. Track. Control.
-- MySQL Database Creation Script
-- MySQL 8.0+ Compatible
-- =====================================================================

-- Step 1: Create Database
CREATE DATABASE IF NOT EXISTS `cargomanage`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE `cargomanage`;

-- Disable foreign key checks during schema creation
SET FOREIGN_KEY_CHECKS = 0;

-- Step 2: Drop Existing Tables (if re-creating)
DROP TABLE IF EXISTS `audit_logs`;
DROP TABLE IF EXISTS `payments`;
DROP TABLE IF EXISTS `shipments`;
DROP TABLE IF EXISTS `transfers`;
DROP TABLE IF EXISTS `purchases`;
DROP TABLE IF EXISTS `stocks`;
DROP TABLE IF EXISTS `products`;
DROP TABLE IF EXISTS `users`;
DROP TABLE IF EXISTS `branches`;

-- ---------------------------------------------------------------------
-- Table 1: branches
-- Stores physical warehouses, regional distribution centers & depots
-- ---------------------------------------------------------------------
CREATE TABLE `branches` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL,
    `location` VARCHAR(255) NOT NULL,
    `code` VARCHAR(20) UNIQUE,
    `phone` VARCHAR(30),
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- Table 2: users
-- User accounts with 4 role tiers and BCrypt password security
-- Roles: 'ADMIN', 'MANAGER', 'ACCOUNTANT', 'BRANCH_STAFF'
-- ---------------------------------------------------------------------
CREATE TABLE `users` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password_hash` VARCHAR(100) NOT NULL,
    `full_name` VARCHAR(100) NOT NULL,
    `role` ENUM('ADMIN', 'MANAGER', 'ACCOUNTANT', 'BRANCH_STAFF') NOT NULL,
    `branch_id` INT NULL,
    `status` ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `last_login` TIMESTAMP NULL,
    CONSTRAINT `fk_user_branch` FOREIGN KEY (`branch_id`)
        REFERENCES `branches`(`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- Table 3: products
-- Enhanced catalog with SKU, barcode, unit, pricing, reorder levels
-- ---------------------------------------------------------------------
CREATE TABLE `products` (
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
    `status` ENUM('Active', 'Inactive') NOT NULL DEFAULT 'Active',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_product_name` (`name`),
    INDEX `idx_product_sku` (`sku`),
    INDEX `idx_product_barcode` (`barcode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- Table 4: stocks
-- Live stock levels partitioned per branch and product
-- ---------------------------------------------------------------------
CREATE TABLE `stocks` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `branch_id` INT NOT NULL,
    `product_id` INT NOT NULL,
    `quantity` INT NOT NULL DEFAULT 0,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `uq_branch_product` UNIQUE (`branch_id`, `product_id`),
    CONSTRAINT `fk_stock_branch` FOREIGN KEY (`branch_id`)
        REFERENCES `branches`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_stock_product` FOREIGN KEY (`product_id`)
        REFERENCES `products`(`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- Table 5: purchases
-- Inbound vendor purchases and supplier procurement records
-- ---------------------------------------------------------------------
CREATE TABLE `purchases` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `supplier_name` VARCHAR(100) NOT NULL,
    `supplier_contact` VARCHAR(100) NULL,
    `product_id` INT NOT NULL,
    `branch_id` INT NOT NULL,
    `quantity` INT NOT NULL,
    `purchase_price` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    `total_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    `purchase_date` VARCHAR(30) NOT NULL,
    `payment_status` ENUM('PAID', 'PENDING', 'PARTIAL') NOT NULL DEFAULT 'PENDING',
    `payment_ref` VARCHAR(100) NULL,
    `received_qty` INT NOT NULL DEFAULT 0,
    `status` ENUM('ORDERED', 'RECEIVED', 'CANCELLED') NOT NULL DEFAULT 'RECEIVED',
    `created_by` VARCHAR(50) NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_purch_product` FOREIGN KEY (`product_id`)
        REFERENCES `products`(`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_purch_branch` FOREIGN KEY (`branch_id`)
        REFERENCES `branches`(`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- Table 6: transfers
-- Inter-branch stock transfers with approval and receiving workflow
-- ---------------------------------------------------------------------
CREATE TABLE `transfers` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `product_id` INT NOT NULL,
    `source_branch_id` INT NOT NULL,
    `dest_branch_id` INT NOT NULL,
    `quantity` INT NOT NULL,
    `status` ENUM('PENDING', 'APPROVED', 'DISPATCHED', 'RECEIVED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    `requested_by` VARCHAR(50) NOT NULL,
    `approved_by` VARCHAR(50) NULL,
    `received_by` VARCHAR(50) NULL,
    `request_date` VARCHAR(30) NOT NULL,
    `dispatch_date` VARCHAR(30) NULL,
    `received_date` VARCHAR(30) NULL,
    `remarks` TEXT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_trans_product` FOREIGN KEY (`product_id`)
        REFERENCES `products`(`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_trans_source` FOREIGN KEY (`source_branch_id`)
        REFERENCES `branches`(`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_trans_dest` FOREIGN KEY (`dest_branch_id`)
        REFERENCES `branches`(`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- Table 7: shipments
-- Transit tracking for approved stock transfer orders
-- ---------------------------------------------------------------------
CREATE TABLE `shipments` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `transfer_id` INT NOT NULL,
    `tracking_number` VARCHAR(50) NULL,
    `carrier` VARCHAR(50) DEFAULT 'CargoManage Logistics',
    `status` ENUM('DISPATCHED', 'IN TRANSIT', 'DELIVERED', 'RETURNED') NOT NULL DEFAULT 'DISPATCHED',
    `eta` VARCHAR(50) NULL,
    `dispatch_date` VARCHAR(30) NULL,
    `delivery_date` VARCHAR(30) NULL,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_ship_transfer` FOREIGN KEY (`transfer_id`)
        REFERENCES `transfers`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- Table 8: payments
-- Financial management ledger, receipts, incoming & outgoing settlements
-- ---------------------------------------------------------------------
CREATE TABLE `payments` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `transaction_id` VARCHAR(50) NOT NULL UNIQUE,
    `receipt_no` VARCHAR(50) NOT NULL UNIQUE,
    `payment_date` VARCHAR(30) NOT NULL,
    `party_name` VARCHAR(100) NOT NULL,
    `party_type` VARCHAR(30) DEFAULT 'SUPPLIER',
    `related_type` VARCHAR(30) NULL,
    `related_id` INT NULL,
    `payment_method` ENUM('Cash', 'UPI', 'Bank Transfer', 'Card', 'Cheque') NOT NULL DEFAULT 'Bank Transfer',
    `direction` ENUM('Incoming', 'Outgoing') NOT NULL,
    `amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    `status` ENUM('Paid', 'Pending', 'Failed') NOT NULL DEFAULT 'Paid',
    `reference_no` VARCHAR(100) NULL,
    `description` VARCHAR(255) NULL,
    `recorded_by` VARCHAR(50) NOT NULL,
    `remarks` TEXT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_pay_receipt` (`receipt_no`),
    INDEX `idx_pay_txn` (`transaction_id`),
    INDEX `idx_pay_party` (`party_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- Table 9: audit_logs
-- Immutable audit log for security, authentication, and operations
-- ---------------------------------------------------------------------
CREATE TABLE `audit_logs` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL,
    `action` VARCHAR(100) NOT NULL,
    `details` TEXT,
    `timestamp` VARCHAR(30) NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;


-- =====================================================================
-- SEED DEMO DATA
-- =====================================================================

-- 1. Insert 12 Indian Branches
INSERT INTO `branches` (`id`, `name`, `location`, `code`, `phone`) VALUES
(1, 'Murbad Warehouse', 'Murbad, Thane, Maharashtra', 'BR-MB01', '+91 98200 11001'),
(2, 'Mumbai Depot', 'Mumbai, Maharashtra', 'BR-MUM02', '+91 98200 11002'),
(3, 'Pune Distribution Center', 'Pune, Maharashtra', 'BR-PUN03', '+91 98200 11003'),
(4, 'Delhi NCR Hub', 'Delhi, NCR', 'BR-DEL04', '+91 98200 11004'),
(5, 'Bengaluru Warehouse', 'Bengaluru, Karnataka', 'BR-BLR05', '+91 98200 11005'),
(6, 'Chennai Port Depot', 'Chennai, Tamil Nadu', 'BR-CHN06', '+91 98200 11006'),
(7, 'Hyderabad Distribution Center', 'Hyderabad, Telangana', 'BR-HYD07', '+91 98200 11007'),
(8, 'Kolkata Warehouse', 'Kolkata, West Bengal', 'BR-KOL08', '+91 98200 11008'),
(9, 'Ahmedabad Depot', 'Ahmedabad, Gujarat', 'BR-AMD09', '+91 98200 11009'),
(10, 'Kochi Export Warehouse', 'Kochi, Kerala', 'BR-KOC10', '+91 98200 11010'),
(11, 'Jaipur Distribution Center', 'Jaipur, Rajasthan', 'BR-JAI11', '+91 98200 11011'),
(12, 'Lucknow Warehouse', 'Lucknow, Uttar Pradesh', 'BR-LKO12', '+91 98200 11012');

-- 2. Insert 4 Default Accounts with BCrypt Passwords
-- admin / admin123
-- manager / manager123
-- accountant / account123
-- murbad / staff123
INSERT INTO `users` (`id`, `username`, `password_hash`, `full_name`, `role`, `branch_id`, `status`) VALUES
(1, 'admin', '$2a$10$xp6ipD8qRxHk3plrSFXW.eJv9yAav/OFNF58pD3NkKjRktNEgaS8q', 'System Administrator', 'ADMIN', NULL, 'ACTIVE'),
(2, 'manager', '$2a$10$7d.HUcNWB.uO.hf0LXm4oOcCzBzFA.jKa1dPe22RO9CkCAR6XWrfe', 'Warehouse Operations Manager', 'MANAGER', NULL, 'ACTIVE'),
(3, 'accountant', '$2a$10$.gDaBuOByNgX0.ehgl78Z.H/NFZ6thyAUAybYHN7tynoft4A3CfvW', 'Chief Financial Accountant', 'ACCOUNTANT', NULL, 'ACTIVE'),
(4, 'murbad', '$2a$10$qqzjRNcCeBxGVDNStv/9nuqP.7sIDYElOxJ6J65zH.lheZwJeWzEW', 'Murbad Warehouse Supervisor', 'BRANCH_STAFF', 1, 'ACTIVE');

-- 3. Insert 10 Detailed Products
INSERT INTO `products` (`id`, `name`, `category`, `brand`, `description`, `sku`, `barcode`, `unit`, `purchase_price`, `selling_price`, `reorder_level`, `supplier_name`, `location`, `mfg_date`, `exp_date`, `status`) VALUES
(1, 'Turmeric Powder', 'Spices', 'Everfresh Spices', 'High-curcumin pure ground turmeric powder', 'CM-SP-001', '890123400001', 'KG', 140.00, 190.00, 25, 'Maharashtra Agro Spices', 'Rack A-1', '2026-01-10', '2027-01-10', 'Active'),
(2, 'Black Pepper', 'Spices', 'Malabar Gold', 'Premium Malabar graded black pepper whole/coarse', 'CM-SP-002', '890123400002', 'KG', 450.00, 580.00, 20, 'Kerala Spice Exports', 'Rack A-2', '2026-02-01', '2027-08-01', 'Active'),
(3, 'Red Chilli Powder', 'Spices', 'Guntur Pride', 'Vibrant red hot chilli powder from Guntur farms', 'CM-SP-003', '890123400003', 'KG', 220.00, 290.00, 25, 'Andhra Agro Traders', 'Rack A-3', '2026-01-15', '2027-01-15', 'Active'),
(4, 'Cumin Powder', 'Spices', 'Royal Cumin', 'Aromatic roasted cumin powder with intense flavor', 'CM-SP-004', '890123400004', 'KG', 280.00, 360.00, 20, 'Gujarat Seed Corporation', 'Rack A-4', '2026-03-05', '2027-03-05', 'Active'),
(5, 'Coriander Powder', 'Spices', 'Aroma Pure', 'Freshly grounded coriander seed powder', 'CM-SP-005', '890123400005', 'KG', 160.00, 210.00, 20, 'Rajasthan Agro Hub', 'Rack A-5', '2026-02-20', '2027-02-20', 'Active'),
(6, 'Garam Masala', 'Spices', 'Chef Special', 'Master blend of 14 roasted aromatic whole spices', 'CM-SP-006', '890123400006', 'KG', 320.00, 420.00, 15, 'North India Spice Mills', 'Rack B-1', '2026-03-10', '2027-03-10', 'Active'),
(7, 'Cardamom', 'Whole Spices', 'Green Pods', 'Premium 8mm jumbo green cardamom from Idukki', 'CM-SP-007', '890123400007', 'KG', 1800.00, 2400.00, 10, 'Idukki Green Estates', 'Rack B-2', '2026-01-20', '2027-07-20', 'Active'),
(8, 'Black Tea Powder', 'Tea', 'Assam Valley', 'Rich strong CTC black tea granular powder', 'CM-TEA-001', '890123400008', 'KG', 310.00, 410.00, 25, 'Assam Tea Orchards', 'Rack B-3', '2026-02-12', '2027-08-12', 'Active'),
(9, 'Soya Chunks', 'Food', 'NutriMeal', 'High protein textured soy granules / chunks', 'CM-FD-001', '890123400009', 'KG', 110.00, 150.00, 30, 'Indore Protein Mills', 'Rack C-1', '2026-02-25', '2027-02-25', 'Active'),
(10, 'Basmati Rice', 'Grains', 'Royal Heritage', 'Extra long grain traditional aged basmati rice', 'CM-RC-001', '890123400010', 'KG', 480.00, 620.00, 50, 'Punjab Grain Logistics', 'Rack C-2', '2026-01-05', '2028-01-05', 'Active');

-- 4. Insert Demo Stock Across Branches
INSERT INTO `stocks` (`branch_id`, `product_id`, `quantity`) VALUES
(1,1,120),(1,2,80),(1,3,55),(1,4,45),(1,5,90),
(2,1,250),(2,2,140),(2,3,120),(2,4,75),(2,8,160),
(3,1,180),(3,5,95),(3,6,60),(3,10,300),
(4,1,100),(4,2,70),(4,7,35),(4,8,110),
(5,3,130),(5,4,85),(5,9,170),
(6,1,160),(6,5,110),(6,8,200),
(7,2,90),(7,6,55),(7,10,220),
(8,3,75),(8,7,25),(8,8,140),
(9,1,95),(9,4,70),(9,5,85),
(10,2,60),(10,6,45),(10,9,120),
(11,1,115),(11,3,90),(11,10,180),
(12,4,65),(12,5,75),(12,8,100);

-- 5. Insert Sample Purchases
INSERT INTO `purchases` (`id`, `supplier_name`, `supplier_contact`, `product_id`, `branch_id`, `quantity`, `purchase_price`, `total_amount`, `purchase_date`, `payment_status`, `payment_ref`, `received_qty`, `status`, `created_by`) VALUES
(101, 'Maharashtra Agro Spices', '+91 98210 22001', 1, 1, 120, 140.00, 16800.00, '2026-10-15 10:30:00', 'PAID', 'UTR-20261015-8812', 120, 'RECEIVED', 'admin'),
(102, 'Kerala Spice Exports', '+91 98210 22002', 2, 2, 140, 450.00, 63000.00, '2026-10-20 14:15:00', 'PAID', 'UTR-20261020-9421', 140, 'RECEIVED', 'manager'),
(103, 'Punjab Grain Logistics', '+91 98210 22010', 10, 3, 300, 480.00, 144000.00, '2026-10-25 11:00:00', 'PENDING', '', 300, 'RECEIVED', 'admin');

-- 6. Insert Sample Transfers
INSERT INTO `transfers` (`id`, `product_id`, `source_branch_id`, `dest_branch_id`, `quantity`, `status`, `requested_by`, `approved_by`, `received_by`, `request_date`, `dispatch_date`, `received_date`, `remarks`) VALUES
(201, 1, 2, 1, 50, 'RECEIVED', 'murbad', 'manager', 'murbad', '2026-10-18 09:30:00', '2026-10-19 11:00:00', '2026-10-21 16:45:00', 'Regular monthly stock replenishment'),
(202, 3, 1, 3, 20, 'DISPATCHED', 'manager', 'admin', NULL, '2026-10-28 15:00:00', '2026-10-29 10:30:00', NULL, 'Express transfer for local wholesale demand');

-- 7. Insert Sample Shipments
INSERT INTO `shipments` (`id`, `transfer_id`, `tracking_number`, `carrier`, `status`, `eta`, `dispatch_date`, `delivery_date`) VALUES
(301, 201, 'CM-TRK-202601', 'CargoManage Fleet Truck #MH-04-9812', 'DELIVERED', 'Completed', '2026-10-19 11:00:00', '2026-10-21 16:45:00'),
(302, 202, 'CM-TRK-202602', 'CargoManage Express Carrier #MH-12-4411', 'IN TRANSIT', 'Within 2 business days', '2026-10-29 10:30:00', NULL);

-- 8. Insert Sample Payments (with Receipts)
INSERT INTO `payments` (`transaction_id`, `receipt_no`, `payment_date`, `party_name`, `party_type`, `related_type`, `related_id`, `payment_method`, `direction`, `amount`, `status`, `reference_no`, `description`, `recorded_by`, `remarks`) VALUES
('TXN-20261015-001', 'RCP-202610-001', '2026-10-15 11:30:00', 'Maharashtra Agro Spices', 'SUPPLIER', 'PURCHASE', 101, 'Bank Transfer', 'Outgoing', 16800.00, 'Paid', 'UTR-20261015-8812', 'Payment for Purchase #101 (120 KG Turmeric Powder)', 'accountant', 'NEFT transfer cleared via corporate account'),
('TXN-20261020-002', 'RCP-202610-002', '2026-10-20 15:45:00', 'Kerala Spice Exports', 'SUPPLIER', 'PURCHASE', 102, 'Bank Transfer', 'Outgoing', 63000.00, 'Paid', 'UTR-20261020-9421', 'Payment for Purchase #102 (140 KG Black Pepper)', 'accountant', 'RTGS transfer approved by Finance Manager'),
('TXN-20261022-003', 'RCP-202610-003', '2026-10-22 14:00:00', 'Metro Supermarkets Pvt Ltd', 'CUSTOMER', 'SALES', 401, 'UPI', 'Incoming', 85000.00, 'Paid', 'UPI/629104882199', 'Wholesale distribution billing advance receipt', 'accountant', 'Received into primary business UPI VPA'),
('TXN-20261026-004', 'RCP-202610-004', '2026-10-26 10:15:00', 'Punjab Grain Logistics', 'SUPPLIER', 'PURCHASE', 103, 'Bank Transfer', 'Outgoing', 144000.00, 'Pending', 'PO-PUNJAB-9921', 'Payment for Purchase #103 (300 KG Basmati Rice)', 'accountant', 'Scheduled for payment batch on next credit cycle');

-- 9. Insert Audit Log
INSERT INTO `audit_logs` (`username`, `action`, `details`, `timestamp`) VALUES
('SYSTEM', 'SYSTEM READY', 'CargoManage enterprise MySQL database initialized with demo branches, products, stock, and default accounts.', '2026-10-15 00:00:00');

