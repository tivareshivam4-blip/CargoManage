================================================================================
CARGOMANAGE – ENTERPRISE WAREHOUSE, INVENTORY & BUSINESS MANAGEMENT SYSTEM
Manage. Track. Control.
================================================================================

CargoManage is a professional, database-driven enterprise warehouse, inventory, 
payment, and business management system powered by Java Swing, MySQL via JDBC, 
BCrypt password hashing, and PDF receipt/report generation.

--------------------------------------------------------------------------------
1. SYSTEM REQUIREMENTS
--------------------------------------------------------------------------------
- Windows 10/11 (or compatible OS)
- Java Development Kit (JDK) 17 or newer (JDK 21 tested and verified)
- MySQL 8.0+ or MariaDB (via XAMPP, MySQL Installer, or standalone service)
- Port 3306 (default MySQL port)

--------------------------------------------------------------------------------
2. PROJECT FOLDER STRUCTURE
--------------------------------------------------------------------------------
CargoManage/
├── bin/                          # Compiled Java class binaries
├── lib/                          # External libraries
│   ├── mysql-connector-j-8.3.0.jar # Official MySQL JDBC Driver
│   ├── jbcrypt-0.4.jar           # BCrypt password hashing algorithm
│   └── itext-2.1.7.jar           # PDF generation engine
├── src/                          # Java Source Code
│   ├── CargoManage.java          # Primary application launcher
│   ├── CargoGrid.java            # Legacy launcher compatibility wrapper
│   └── cargomanage/
│       ├── CargoManageApp.java   # Modular application entry
│       ├── config/
│       │   └── DatabaseConfig.java # Properties loader & JDBC connection strings
│       ├── dao/
│       │   ├── DatabaseManager.java# Auto-DB creation, table schemas & demo seeding
│       │   ├── UserDAO.java        # User CRUD, role management, password security
│       │   ├── BranchDAO.java      # Warehouse & branch queries
│       │   ├── ProductDAO.java     # Catalog, valuation, cross-branch availability
│       │   ├── StockDAO.java       # Atomic stock updates, low-stock thresholds
│       │   ├── TransferDAO.java    # Atomic transfer approvals & stock movement
│       │   ├── PurchaseDAO.java    # Procurement & automatic stock additions
│       │   ├── ShipmentDAO.java    # Logistics transit tracking
│       │   ├── PaymentDAO.java     # Financial transactions & receipt generation
│       │   └── AuditDAO.java       # Security & operational audit logging
│       ├── model/
│       │   ├── User.java, Branch.java, Product.java, Stock.java,
│       │   ├── Transfer.java, Purchase.java, Shipment.java,
│       │   └── Payment.java, AuditLog.java
│       ├── security/
│       │   ├── PasswordUtil.java   # BCrypt salt hashing & verification
│       │   └── SessionContext.java # Role permissions & session management
│       ├── util/
│       │   ├── UIHelper.java       # Theme colors, styling, stable buttons & dialogs
│       │   ├── PdfGenerator.java   # PDF export for receipts, notes & reports
│       │   └── PrintManager.java   # Native Windows print engine
│       └── ui/
│           ├── LoginFrame.java     # Secure login interface
│           ├── MainFrame.java      # Multi-tab operational workspace
│           ├── DashboardPanel.java # Live metrics, cards & inventory status
│           ├── BranchPanel.java    # Branch & depot management (Admin)
│           ├── UserManagementPanel.java # User accounts & role manager (Admin)
│           ├── ProductPanel.java   # Enhanced catalog, SKU, pricing & search
│           ├── ProductDetailDialog.java # Full specifications & branch stock
│           ├── StockPanel.java     # Live branch stock & adjustments
│           ├── TransferPanel.java  # Stock transfer workflow & approvals
│           ├── TransferReceiptDialog.java # Transfer dispatch document (Print/PDF)
│           ├── PurchasePanel.java  # Vendor purchases & payment reconciliation
│           ├── ShipmentPanel.java  # Vehicle dispatch & delivery status
│           ├── PaymentPanel.java   # Financial ledger, receipts & summaries
│           ├── PaymentReceiptDialog.java # Professional business receipt (Print/PDF)
│           ├── ReportPanel.java    # Business reports with PDF & CSV export
│           ├── AuditPanel.java     # Immutable system audit trail
│           ├── DbConfigDialog.java # In-app MySQL connection configuration
│           └── ChangePasswordDialog.java # Self-service password changer
├── db.properties                 # MySQL connection configuration file
├── cargomanage.sql               # Complete MySQL schema & seed data SQL script
├── run.bat                       # Compilation and execution batch script
└── README.txt                    # Documentation & setup guide

--------------------------------------------------------------------------------
3. DATABASE CONFIGURATION & AUTOMATIC SETUP
--------------------------------------------------------------------------------
CargoManage is configured out-of-the-box to connect to your local MySQL instance:
- Host: localhost
- Port: 3306
- Database: cargomanage
- Username: root
- Password: (blank by default for standard XAMPP / local installations)

To customize credentials, either:
A) Edit 'db.properties' in the project root folder.
B) Click "⚙ Database Connection Settings" on the CargoManage login screen.

AUTOMATIC DATABASE INITIALIZATION:
When you launch CargoManage:
1. It automatically connects to MySQL and creates the 'cargomanage' database if not existing.
2. It automatically creates all 9 relational tables with proper indexes and foreign key constraints.
3. It seeds 12 Indian warehouses, 10 products with complete specifications, demo stock,
   sample purchases, transfers, shipments, payments, and 4 default role-based accounts.

MANUAL SQL SCRIPT EXECUTION (OPTIONAL):
If you prefer creating the database manually via phpMyAdmin, MySQL Workbench, or CLI:
  mysql -u root -p < cargomanage.sql

--------------------------------------------------------------------------------
4. DEFAULT ROLE-BASED ACCOUNTS
--------------------------------------------------------------------------------
1. System Administrator:
   - Username: admin
   - Password: admin123
   - Permissions: Full system access across all modules, user account management,
     branch setup, password resets, financial ledger, and operational overrides.

2. Warehouse Operations Manager:
   - Username: manager
   - Password: manager123
   - Permissions: Warehouse operations, product management, stock monitoring,
     approval/rejection of inter-branch transfers, inbound purchases, and transit tracking.

3. Chief Financial Accountant:
   - Username: accountant
   - Password: account123
   - Permissions: Dedicated Payment Management module, payment recording (incoming/outgoing),
     supplier payment settlements, official receipt generation (Print/PDF), and
     financial summary reports. Cannot modify physical warehouse inventory.

4. Branch Staff (Murbad Warehouse):
   - Username: murbad
   - Password: staff123
   - Permissions: Scoped strictly to Murbad Warehouse (Branch #1). View available branch
     stock, submit transfer requests, record inbound purchases, and confirm receipt
     of arriving stock.

--------------------------------------------------------------------------------
5. HOW TO RUN THE APPLICATION
--------------------------------------------------------------------------------
1. Ensure MySQL is running (e.g., Start MySQL in XAMPP Control Panel or Windows Service).
2. Double-click 'run.bat' in this folder.
   - The script will automatically verify your JDK, compile all classes into 'bin/',
     and start the CargoManage application with required library JARs in classpath.
3. Sign in using any of the default demo credentials listed above.

--------------------------------------------------------------------------------
6. KEY ENHANCEMENTS & FEATURES
--------------------------------------------------------------------------------
✔ MySQL Database Integration via JDBC with PreparedStatements and atomic transactions.
✔ Strict BCrypt Password Hashing with salt rounds (no plain-text passwords stored).
✔ Dedicated Accountant Payment Management with instant receipt generation.
✔ Professional Business Receipts with print preview, Windows printer support, and PDF export.
✔ 5-Step Inter-Warehouse Transfer Workflow:
   Request -> Manager Approval -> Source Stock Deducted -> Dispatch Document ->
   Destination Confirmation & Receiving Credit (zero negative or duplicate stock).
✔ Enhanced Product Catalog with SKU, barcode, unit, purchase price, selling price,
   reorder level, supplier, shelf location, and multi-branch stock distribution dialog.
✔ Automated Inbound Procurement: Purchases instantly update warehouse stock levels
   and seamlessly link with Accountant payment records.
✔ Real-Time Analytics Dashboard with valuation, cash balances, and low-stock indicators.
✔ Multi-Format Business Reports with 1-click PDF and CSV export.
✔ Immutable Audit Trail logging every login, stock update, approval, and transaction.
✔ Preserved Navy Blue & Modern Swing UI Theme with clean responsive dialogs.
✔ Stable button interactions across all screens with subtle click-only feedback.
================================================================================
