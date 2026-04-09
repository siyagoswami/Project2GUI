package ui;

import db.DBConnection;
import ui.LoginFrame;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;

public class MainFrame extends JFrame {
    private JTextField searchField;
    private JButton searchButton;
    private JButton orderButton;
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
        setSize(1000, 550);
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
                searchButton, orderButton, insertButton, updateButton, deleteButton,
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

        String [] columnNames = {
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

            String sql = "INSERT INTO dbo.Items (item_id, class_id, item_name, quantity, price, description) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

            try (
              Connection conn = DBConnection.getConnection();
              PreparedStatement pstmt = conn.prepareStatement(sql);
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

            String sql = "UPDATE dbo.Items SET item_name = ?, quantity = ?, price = ?, description = ? " +
                    "WHERE item_id = ?";

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
        this.dispose(); // close current window

        LoginFrame loginFrame = new LoginFrame();
        loginFrame.setVisible(true);
    }

    public void refreshItemsTable() {
        loadAllItems();
    }
}
