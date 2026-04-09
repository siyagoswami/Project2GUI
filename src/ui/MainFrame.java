package ui;

import db.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MainFrame extends JFrame {
    private JTextField searchField;
    private JButton searchButton;
    private JButton orderButton;
    private JButton updateOrderButton;
    private JButton insertButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton refreshButton;
    private JButton statsButton;
    private JButton logoutButton;
    private JButton viewCustomersButton;
    private JButton viewCustOrderButton;
    private JButton removeOrderButton;
    private JButton customerOrderDetailsButton;
    private JTable itemsTable;
    private DefaultTableModel tableModel;

    public MainFrame() {
        setTitle("Customer Retail Database");
        setSize(1100, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(245, 247, 250));

        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.setBackground(new Color(245, 247, 250));

        JLabel searchLabel = new JLabel("Search Item Name:");
        searchField = new JTextField(20);
        searchButton = new JButton("Search");
        orderButton = new JButton("Create Order");
        updateOrderButton = new JButton("Update Order");
        insertButton = new JButton("Insert Item");
        updateButton = new JButton("Update Item");
        deleteButton = new JButton("Delete Item");
        refreshButton = new JButton("Refresh");
        statsButton = new JButton("Show Stats");
        customerOrderDetailsButton = new JButton("Customer Order Details");
        removeOrderButton = new JButton("Remove Order");
        viewCustomersButton = new JButton("View Customers");
        viewCustOrderButton = new JButton("View Customer Orders");
        logoutButton = new JButton("Logout");

        topPanel.add(searchLabel);
        topPanel.add(searchField);
        topPanel.add(searchButton);
        topPanel.add(orderButton);
        topPanel.add(updateOrderButton);
        topPanel.add(removeOrderButton);
        topPanel.add(insertButton);
        topPanel.add(updateButton);
        topPanel.add(deleteButton);
        topPanel.add(refreshButton);
        topPanel.add(statsButton);
        topPanel.add(viewCustomersButton);
        topPanel.add(viewCustOrderButton);
        topPanel.add(customerOrderDetailsButton);
        topPanel.add(logoutButton);

        JButton[] buttons = {
                searchButton, orderButton, updateOrderButton, insertButton, updateButton, deleteButton,
                refreshButton, statsButton, viewCustomersButton,
                viewCustOrderButton, customerOrderDetailsButton, removeOrderButton, logoutButton
        };

        for (JButton btn : buttons) {
            btn.setFocusPainted(false);
            btn.setForeground(Color.BLACK);
            btn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        }

        searchLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        searchField.setPreferredSize(new Dimension(180, 28));

        String[] columnNames = {
                "Item ID", "Class ID", "Item Name", "Quantity", "Price", "Description"
        };

        tableModel = new DefaultTableModel(columnNames, 0);
        itemsTable = new JTable(tableModel);
        itemsTable.setRowHeight(25);
        itemsTable.setGridColor(new Color(220, 220, 220));
        itemsTable.setSelectionBackground(new Color(100, 149, 237));
        itemsTable.setSelectionForeground(Color.WHITE);
        itemsTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        itemsTable.getTableHeader().setBackground(new Color(230, 236, 245));

        JScrollPane scrollPane = new JScrollPane(itemsTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        loadAllItems();

        searchButton.addActionListener(e -> searchItems());
        orderButton.addActionListener(e -> openOrderFrame());
        updateOrderButton.addActionListener(e -> updateOrder());
        refreshButton.addActionListener(e -> loadAllItems());
        insertButton.addActionListener(e -> insertItem());
        updateButton.addActionListener(e -> updateItem());
        deleteButton.addActionListener(e -> deleteItem());
        statsButton.addActionListener(e -> showStats());
        viewCustomersButton.addActionListener(e -> openCustomerView());
        viewCustOrderButton.addActionListener(e -> openCustOrderView());
        customerOrderDetailsButton.addActionListener(e -> showCustomerOrderDetails());
        removeOrderButton.addActionListener(e -> removeOrder());
        logoutButton.addActionListener(e -> logout());
    }

    private void loadAllItems() {
        tableModel.setRowCount(0);

        String sql = "SELECT item_id, class_id, item_name, quantity, price, description FROM dbo.Items WHERE is_active = 1";

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()
        ) {
            while (rs.next()) {
                Object[] row = {
                        rs.getInt("item_id"),
                        rs.getObject("class_id"),
                        rs.getString("item_name"),
                        rs.getInt("quantity"),
                        rs.getDouble("price"),
                        rs.getString("description")
                };
                tableModel.addRow(row);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading items.");
            ex.printStackTrace();
        }
    }

    private void searchItems() {
        tableModel.setRowCount(0);
        String keyword = searchField.getText().trim();

        String sql = "SELECT item_id, class_id, item_name, quantity, price, description FROM dbo.Items WHERE is_active = 1 AND item_name LIKE ?";

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Object[] row = {
                        rs.getInt("item_id"),
                        rs.getObject("class_id"),
                        rs.getString("item_name"),
                        rs.getInt("quantity"),
                        rs.getDouble("price"),
                        rs.getString("description")
                };
                tableModel.addRow(row);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading items.");
            ex.printStackTrace();
        }
    }

    private void openOrderFrame() {
        OrderFrame orderFrame = new OrderFrame(this);
        orderFrame.setVisible(true);
    }

    private void updateOrder() {
        try {
            String orderIdStr = JOptionPane.showInputDialog(this, "Enter Order ID to update:");
            if (orderIdStr == null || orderIdStr.trim().isEmpty()) return;

            int orderId = Integer.parseInt(orderIdStr.trim());

            if (!orderExists(orderId)) {
                JOptionPane.showMessageDialog(this, "No order found with that ID.");
                return;
            }

            showOrderLines(orderId);

            String[] options = {"Update Existing Item", "Add New Item", "Remove Item"};
            String choice = (String) JOptionPane.showInputDialog(
                    this,
                    "Choose what you want to do:",
                    "Update Order",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    options,
                    options[0]
            );

            if (choice == null) return;

            try (Connection conn = DBConnection.getConnection()) {
                conn.setAutoCommit(false);

                try {
                    if (choice.equals("Update Existing Item")) {
                        updateExistingOrderLine(conn, orderId);
                    } else if (choice.equals("Add New Item")) {
                        addNewOrderLine(conn, orderId);
                    } else {
                        removeExistingOrderLine(conn, orderId);
                    }

                    updateOrderTotal(conn, orderId);
                    conn.commit();
                    JOptionPane.showMessageDialog(this, "Order updated successfully.");
                    loadAllItems();
                    showOrderLines(orderId);

                } catch (Exception ex) {
                    conn.rollback();
                    JOptionPane.showMessageDialog(this, "Order update failed: " + ex.getMessage());
                    ex.printStackTrace();
                } finally {
                    conn.setAutoCommit(true);
                }
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric Order ID.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error updating order.");
            ex.printStackTrace();
        }
    }

    private boolean orderExists(int orderId) throws Exception {
        String sql = "SELECT 1 FROM dbo.Orders WHERE order_id = ?";

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        }
    }

    private void showOrderLines(int orderId) throws Exception {
        String[] columnNames = {"Item ID", "Item Name", "Quantity", "Unit Price", "Discount"};
        DefaultTableModel detailsModel = new DefaultTableModel(columnNames, 0);
        JTable detailsTable = new JTable(detailsModel);

        String sql = "SELECT ol.item_id, i.item_name, ol.quantity, ol.unit_price, ol.discount " +
                "FROM dbo.OrderLines ol " +
                "JOIN dbo.Items i ON ol.item_id = i.item_id " +
                "WHERE ol.order_id = ? ORDER BY ol.item_id";

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Object[] row = {
                        rs.getInt("item_id"),
                        rs.getString("item_name"),
                        rs.getInt("quantity"),
                        rs.getDouble("unit_price"),
                        rs.getObject("discount")
                };
                detailsModel.addRow(row);
            }
        }

        JScrollPane scrollPane = new JScrollPane(detailsTable);
        scrollPane.setPreferredSize(new Dimension(650, 180));

        JOptionPane.showMessageDialog(
                this,
                scrollPane,
                "Current Items In Order " + orderId,
                JOptionPane.PLAIN_MESSAGE
        );
    }

    private void updateExistingOrderLine(Connection conn, int orderId) throws Exception {
        String itemIdStr = JOptionPane.showInputDialog(this, "Enter Item ID already in the order:");
        if (itemIdStr == null || itemIdStr.trim().isEmpty()) {
            throw new Exception("Update cancelled.");
        }

        int itemId = Integer.parseInt(itemIdStr.trim());

        if (!orderLineExists(conn, orderId, itemId)) {
            throw new Exception("That item is not in this order.");
        }

        String quantityStr = JOptionPane.showInputDialog(this, "Enter new quantity:");
        if (quantityStr == null || quantityStr.trim().isEmpty()) {
            throw new Exception("Update cancelled.");
        }

        String discountStr = JOptionPane.showInputDialog(this, "Enter new discount (use 0 if none):");
        if (discountStr == null || discountStr.trim().isEmpty()) {
            throw new Exception("Update cancelled.");
        }

        int quantity = Integer.parseInt(quantityStr.trim());
        double discount = Double.parseDouble(discountStr.trim());

        if (quantity <= 0) {
            throw new Exception("Quantity must be greater than 0.");
        }

        int stock = getItemStock(conn, itemId);
        if (quantity > stock) {
            throw new Exception("Not enough stock available.");
        }

        String sql = "UPDATE dbo.OrderLines SET quantity = ?, discount = ? WHERE order_id = ? AND item_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, quantity);
            pstmt.setDouble(2, discount);
            pstmt.setInt(3, orderId);
            pstmt.setInt(4, itemId);
            pstmt.executeUpdate();
        }
    }

    private void addNewOrderLine(Connection conn, int orderId) throws Exception {
        String itemIdStr = JOptionPane.showInputDialog(this, "Enter new Item ID to add:");
        if (itemIdStr == null || itemIdStr.trim().isEmpty()) {
            throw new Exception("Add cancelled.");
        }

        int itemId = Integer.parseInt(itemIdStr.trim());

        if (!itemExists(conn, itemId)) {
            throw new Exception("No item found with that Item ID.");
        }

        if (orderLineExists(conn, orderId, itemId)) {
            throw new Exception("That item is already in the order.");
        }

        String quantityStr = JOptionPane.showInputDialog(this, "Enter quantity:");
        if (quantityStr == null || quantityStr.trim().isEmpty()) {
            throw new Exception("Add cancelled.");
        }

        String discountStr = JOptionPane.showInputDialog(this, "Enter discount (use 0 if none):");
        if (discountStr == null || discountStr.trim().isEmpty()) {
            throw new Exception("Add cancelled.");
        }

        int quantity = Integer.parseInt(quantityStr.trim());
        double discount = Double.parseDouble(discountStr.trim());
        double unitPrice = getItemPrice(conn, itemId);

        if (quantity <= 0) {
            throw new Exception("Quantity must be greater than 0.");
        }

        int stock = getItemStock(conn, itemId);
        if (quantity > stock) {
            throw new Exception("Not enough stock available.");
        }

        String sql = "INSERT INTO dbo.OrderLines (order_id, item_id, quantity, unit_price, discount) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            pstmt.setInt(2, itemId);
            pstmt.setInt(3, quantity);
            pstmt.setDouble(4, unitPrice);
            pstmt.setDouble(5, discount);
            pstmt.executeUpdate();
        }
    }

    private void removeExistingOrderLine(Connection conn, int orderId) throws Exception {
        String itemIdStr = JOptionPane.showInputDialog(this, "Enter Item ID to remove from the order:");
        if (itemIdStr == null || itemIdStr.trim().isEmpty()) {
            throw new Exception("Remove cancelled.");
        }

        int itemId = Integer.parseInt(itemIdStr.trim());

        if (!orderLineExists(conn, orderId, itemId)) {
            throw new Exception("That item is not in this order.");
        }

        String sql = "DELETE FROM dbo.OrderLines WHERE order_id = ? AND item_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            pstmt.setInt(2, itemId);
            pstmt.executeUpdate();
        }
    }

    private boolean orderLineExists(Connection conn, int orderId, int itemId) throws Exception {
        String sql = "SELECT 1 FROM dbo.OrderLines WHERE order_id = ? AND item_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            pstmt.setInt(2, itemId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        }
    }

    private boolean itemExists(Connection conn, int itemId) throws Exception {
        String sql = "SELECT 1 FROM dbo.Items WHERE item_id = ? AND is_active = 1";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        }
    }

    private double getItemPrice(Connection conn, int itemId) throws Exception {
        String sql = "SELECT price FROM dbo.Items WHERE item_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("price");
            }
        }

        throw new Exception("Unable to find item price.");
    }

    private int getItemStock(Connection conn, int itemId) throws Exception {
        String sql = "SELECT quantity FROM dbo.Items WHERE item_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("quantity");
            }
        }

        throw new Exception("Item not found.");
    }

    private void updateOrderTotal(Connection conn, int orderId) throws Exception {
        String totalSql = "SELECT ISNULL(SUM((quantity * unit_price) - ISNULL(discount, 0)), 0) AS new_total " +
                "FROM dbo.OrderLines WHERE order_id = ?";

        double newTotal = 0.0;

        try (PreparedStatement totalStmt = conn.prepareStatement(totalSql)) {
            totalStmt.setInt(1, orderId);
            ResultSet rs = totalStmt.executeQuery();
            if (rs.next()) {
                newTotal = rs.getDouble("new_total");
            }
        }

        String updateSql = "UPDATE dbo.Orders SET total_amount = ? WHERE order_id = ?";

        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
            updateStmt.setDouble(1, newTotal);
            updateStmt.setInt(2, orderId);
            updateStmt.executeUpdate();
        }
    }

    private void insertItem() {
        try {
            String itemIdStr = JOptionPane.showInputDialog(this, "Enter Item ID:");
            if(itemIdStr == null) return;

            String classIdStr = JOptionPane.showInputDialog(this, "Enter Class ID:");
            if(classIdStr == null) return;

            String itemName = JOptionPane.showInputDialog(this, "Enter Item Name:");
            if(itemName == null || itemName.trim().isEmpty()) return;

            String quantityStr = JOptionPane.showInputDialog(this, "Enter Quantity:");
            if(quantityStr == null) return;

            String priceStr = JOptionPane.showInputDialog(this, "Enter Price:");
            if(priceStr == null) return;

            String description = JOptionPane.showInputDialog(this, "Enter Description:");
            if(description == null) return;

            int itemId = Integer.parseInt(itemIdStr.trim());
            Integer classId = classIdStr.trim().isEmpty() ? null : Integer.parseInt(classIdStr.trim());
            int quantity = Integer.parseInt(quantityStr.trim());
            double price = Double.parseDouble(priceStr.trim());

            String sql = "INSERT INTO dbo.Items (item_id, class_id, item_name, quantity, price, description) VALUES (?, ?, ?, ?, ?, ?)";

            try (
                    Connection conn = DBConnection.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)
            ) {
                pstmt.setInt(1, itemId);
                if(classId == null) {
                    pstmt.setNull(2, java.sql.Types.INTEGER);
                } else {
                    pstmt.setInt(2, classId);
                }

                pstmt.setString(3, itemName);
                pstmt.setInt(4, quantity);
                pstmt.setDouble(5, price);
                pstmt.setString(6, description);

                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Item inserted successfully.");
                loadAllItems();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error inserting item.");
            ex.printStackTrace();
        }
    }

    private void updateItem() {
        try {
            String itemIdStr = JOptionPane.showInputDialog(this, "Enter Item ID to update:");
            if (itemIdStr == null) return;

            String newName = JOptionPane.showInputDialog(this, "Enter new Item Name:");
            if (newName == null || newName.trim().isEmpty()) return;

            String newQuantityStr = JOptionPane.showInputDialog(this, "Enter new Quantity:");
            if (newQuantityStr == null) return;

            String newPriceStr = JOptionPane.showInputDialog(this, "Enter new Price:");
            if (newPriceStr == null) return;

            String newDescription = JOptionPane.showInputDialog(this, "Enter new Description:");
            if (newDescription == null) return;

            int itemId = Integer.parseInt(itemIdStr.trim());
            int newQuantity = Integer.parseInt(newQuantityStr.trim());
            double newPrice = Double.parseDouble(newPriceStr.trim());

            String sql = "UPDATE dbo.Items SET item_name = ?, quantity = ?, price = ?, description = ? WHERE item_id = ?";

            try (
                    Connection conn = DBConnection.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)
            ) {
                pstmt.setString(1, newName);
                pstmt.setInt(2, newQuantity);
                pstmt.setDouble(3, newPrice);
                pstmt.setString(4, newDescription);
                pstmt.setInt(5, itemId);

                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Item updated successfully.");
                } else {
                    JOptionPane.showMessageDialog(this, "No item found with that ID.");
                }

                loadAllItems();
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error updating item.");
            ex.printStackTrace();
        }
    }

    private void deleteItem() {
        try {
            String itemIdStr = JOptionPane.showInputDialog(this, "Enter Item ID to delete:");
            if (itemIdStr == null) return;

            int itemId = Integer.parseInt(itemIdStr.trim());

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to delete item ID " + itemId + "?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            String sql = "UPDATE dbo.Items SET is_active = 0 WHERE item_id = ?";

            try (
                    Connection conn = DBConnection.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)
            ) {
                pstmt.setInt(1, itemId);

                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Item marked inactive successfully.");
                } else {
                    JOptionPane.showMessageDialog(this, "No item found with that ID.");
                }

                loadAllItems();
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error deleting item.");
            ex.printStackTrace();
        }
    }

    private void showStats() {
        String sql = "SELECT " +
                "MAX(price) AS max_price, MIN(price) AS min_price, AVG(price) AS avg_price, " +
                "MAX(quantity) AS max_quantity, MIN(quantity) AS min_quantity, AVG(quantity) AS avg_quantity " +
                "FROM dbo.Items";

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()
        ) {
            if (rs.next()) {
                double maxPrice = rs.getDouble("max_price");
                double minPrice = rs.getDouble("min_price");
                double avgPrice = rs.getDouble("avg_price");

                int maxQuantity = rs.getInt("max_quantity");
                int minQuantity = rs.getInt("min_quantity");
                double avgQuantity = rs.getDouble("avg_quantity");

                JOptionPane.showMessageDialog(this,
                        "Price Statistics:\n" +
                                "Max Price: " + maxPrice + "\n" +
                                "Min Price: " + minPrice + "\n" +
                                "Average Price: " + avgPrice + "\n\n" +
                                "Quantity Statistics:\n" +
                                "Max Quantity: " + maxQuantity + "\n" +
                                "Min Quantity: " + minQuantity + "\n" +
                                "Average Quantity: " + avgQuantity);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error calculating statistics.");
            ex.printStackTrace();
        }
    }

    private void openCustomerView() {
        CustomerViewFrame customerViewFrame = new CustomerViewFrame();
        customerViewFrame.setVisible(true);
    }

    private void openCustOrderView() {
        CustomerOrderSummaryFrame customerOrderSummaryFrame = new CustomerOrderSummaryFrame();
        customerOrderSummaryFrame.setVisible(true);
    }

    private void showCustomerOrderDetails() {
        try {
            String customerIdStr = JOptionPane.showInputDialog(this, "Enter Customer ID:");
            if (customerIdStr == null || customerIdStr.trim().isEmpty()) return;

            int customerId = Integer.parseInt(customerIdStr.trim());

            String[] columnNames = {
                    "Order ID", "Order Date", "Item ID", "Item Name",
                    "Quantity", "Unit Price", "Discount", "Order Total"
            };

            DefaultTableModel detailsModel = new DefaultTableModel(columnNames, 0);
            JTable detailsTable = new JTable(detailsModel);
            detailsTable.setRowHeight(25);
            detailsTable.setGridColor(new Color(220, 220, 220));
            detailsTable.setSelectionBackground(new Color(100, 149, 237));
            detailsTable.setSelectionForeground(Color.WHITE);
            detailsTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
            detailsTable.getTableHeader().setBackground(new Color(230, 236, 245));

            String sql = "SELECT o.order_id, o.order_date, i.item_id, i.item_name, " +
                    "ol.quantity, ol.unit_price, ol.discount, o.total_amount " +
                    "FROM dbo.Orders o " +
                    "JOIN dbo.OrderLines ol ON o.order_id = ol.order_id " +
                    "JOIN dbo.Items i ON ol.item_id = i.item_id " +
                    "WHERE o.customer_id = ? " +
                    "ORDER BY o.order_date, o.order_id";

            try (
                    Connection conn = DBConnection.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)
            ) {
                pstmt.setInt(1, customerId);
                ResultSet rs = pstmt.executeQuery();

                boolean found = false;

                while (rs.next()) {
                    found = true;
                    Object[] row = {
                            rs.getInt("order_id"),
                            rs.getDate("order_date"),
                            rs.getInt("item_id"),
                            rs.getString("item_name"),
                            rs.getInt("quantity"),
                            rs.getDouble("unit_price"),
                            rs.getObject("discount"),
                            rs.getDouble("total_amount")
                    };
                    detailsModel.addRow(row);
                }

                if (!found) {
                    JOptionPane.showMessageDialog(this, "No orders found for that customer.");
                    return;
                }
            }

            JScrollPane scrollPane = new JScrollPane(detailsTable);
            scrollPane.setPreferredSize(new Dimension(900, 250));

            JOptionPane.showMessageDialog(
                    this,
                    scrollPane,
                    "Customer Order Details",
                    JOptionPane.PLAIN_MESSAGE
            );

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading customer order details.");
            ex.printStackTrace();
        }
    }

    private void removeOrder() {
        try {
            String orderIdStr = JOptionPane.showInputDialog(this, "Enter Order ID to remove:");
            if (orderIdStr == null || orderIdStr.trim().isEmpty()) return;

            int orderId = Integer.parseInt(orderIdStr.trim());

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to remove order ID " + orderId + "?",
                    "Confirm Remove Order",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            String deleteOrderLinesSql = "DELETE FROM dbo.OrderLines WHERE order_id = ?";
            String deleteOrderSql = "DELETE FROM dbo.Orders WHERE order_id = ?";

            try (
                    Connection conn = DBConnection.getConnection();
                    PreparedStatement deleteOrderLinesStmt = conn.prepareStatement(deleteOrderLinesSql);
                    PreparedStatement deleteOrderStmt = conn.prepareStatement(deleteOrderSql)
            ) {
                conn.setAutoCommit(false);

                try {
                    deleteOrderLinesStmt.setInt(1, orderId);
                    deleteOrderLinesStmt.executeUpdate();

                    deleteOrderStmt.setInt(1, orderId);
                    int rowsAffected = deleteOrderStmt.executeUpdate();

                    if (rowsAffected > 0) {
                        conn.commit();
                        JOptionPane.showMessageDialog(this, "Order removed successfully.");
                        loadAllItems();
                    } else {
                        conn.rollback();
                        JOptionPane.showMessageDialog(this, "No order found with that ID.");
                    }

                } catch (Exception ex) {
                    conn.rollback();
                    throw ex;
                } finally {
                    conn.setAutoCommit(true);
                }
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error removing order.");
            ex.printStackTrace();
        }
    }

    private void logout() {
        this.dispose();

        LoginFrame loginFrame = new LoginFrame();
        loginFrame.setVisible(true);
    }

    public void refreshItemsTable() {
        loadAllItems();
    }
}