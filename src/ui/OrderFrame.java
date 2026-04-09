package ui;

import db.DBConnection;
import model.OrderLineItem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class OrderFrame extends JFrame {

    private MainFrame mainFrame;
    private JComboBox<String> customerComboBox;
    private JComboBox<String> accountComboBox;
    private JComboBox<String> itemComboBox;

    private JTextField quantityField;
    private JTextField discountField;

    private JButton addItemButton;
    private JButton saveOrderButton;

    private JTable orderTable;
    private DefaultTableModel tableModel;

    private JLabel totalLabel;

    private ArrayList<OrderLineItem> orderItems;

    public OrderFrame(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setTitle("Create Customer Order");
        setSize(950, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        orderItems = new ArrayList<>();

        JPanel topPanel = new JPanel(new GridLayout(3, 4, 10, 10));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        customerComboBox = new JComboBox<>();
        accountComboBox = new JComboBox<>();
        itemComboBox = new JComboBox<>();

        quantityField = new JTextField();
        discountField = new JTextField();

        addItemButton = new JButton("Add Item To Order");
        saveOrderButton = new JButton("Save Order");

        topPanel.add(new JLabel("Customer:"));
        topPanel.add(customerComboBox);
        topPanel.add(new JLabel("Account:"));
        topPanel.add(accountComboBox);

        topPanel.add(new JLabel("Item:"));
        topPanel.add(itemComboBox);
        topPanel.add(new JLabel("Quantity:"));
        topPanel.add(quantityField);

        topPanel.add(new JLabel("Discount:"));
        topPanel.add(discountField);
        topPanel.add(addItemButton);
        topPanel.add(saveOrderButton);

        String[] columns = {"Item ID", "Item Name", "Quantity", "Unit Price", "Discount", "Line Total"};
        tableModel = new DefaultTableModel(columns, 0);
        orderTable = new JTable(tableModel);

        totalLabel = new JLabel("Order Total: $0.00");
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 16));

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(orderTable), BorderLayout.CENTER);
        add(totalLabel, BorderLayout.SOUTH);

        loadCustomers();
        loadItems();

        addItemButton.addActionListener(e -> addItemToOrder());
        saveOrderButton.addActionListener(e -> saveOrder());
    }

    private void loadCustomers() {
        customerComboBox.removeAllItems();

        String sql = "SELECT customer_id, firstName, lastName FROM dbo.Customers ORDER BY customer_id";

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()
        ) {
            while (rs.next()) {
                String displayValue = rs.getInt("customer_id") + " - "
                        + rs.getString("firstName") + " "
                        + rs.getString("lastName");
                customerComboBox.addItem(displayValue);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading customers.");
            ex.printStackTrace();
        }

        customerComboBox.addActionListener(e -> loadAccountsForSelectedCustomer());

        if (customerComboBox.getItemCount() > 0) {
            customerComboBox.setSelectedIndex(0);
            loadAccountsForSelectedCustomer();
        }
    }

    private void loadAccountsForSelectedCustomer() {
        accountComboBox.removeAllItems();

        String selectedCustomer = (String) customerComboBox.getSelectedItem();
        if (selectedCustomer == null) {
            return;
        }

        int customerId = Integer.parseInt(selectedCustomer.split(" - ")[0]);

        String sql = "SELECT account_id, bank_name, account_number " +
                "FROM dbo.Accounts WHERE customer_id = ? ORDER BY account_id";

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setInt(1, customerId);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String displayValue = rs.getInt("account_id") + " - "
                        + rs.getString("bank_name") + " - "
                        + rs.getString("account_number");
                accountComboBox.addItem(displayValue);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading accounts.");
            ex.printStackTrace();
        }
    }

    private void loadItems() {
        itemComboBox.removeAllItems();

        String sql = "SELECT item_id, item_name, price, quantity FROM dbo.Items WHERE is_active = 1 ORDER BY item_id";

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()
        ) {
            while (rs.next()) {
                String displayValue = rs.getInt("item_id") + " - "
                        + rs.getString("item_name")
                        + " ($" + rs.getDouble("price") + ", Stock: " + rs.getInt("quantity") + ")";
                itemComboBox.addItem(displayValue);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading items.");
            ex.printStackTrace();
        }
    }

    private void addItemToOrder() {
        try {
            String selectedItem = (String) itemComboBox.getSelectedItem();

            if (selectedItem == null) {
                JOptionPane.showMessageDialog(this, "Please select an item.");
                return;
            }

            String quantityText = quantityField.getText().trim();
            String discountText = discountField.getText().trim();

            if (quantityText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a quantity.");
                return;
            }

            int quantity = Integer.parseInt(quantityText);
            double discount = discountText.isEmpty() ? 0.0 : Double.parseDouble(discountText);

            int itemId = Integer.parseInt(selectedItem.split(" - ")[0]);
            String itemName = selectedItem.split(" - ")[1].split("\\(\\$")[0].trim();

            double unitPrice = getItemPrice(itemId);
            int stockQuantity = getItemStock(itemId);

            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be greater than 0.");
                return;
            }

            if (quantity > stockQuantity) {
                JOptionPane.showMessageDialog(this, "Not enough stock available.");
                return;
            }

            for (OrderLineItem existingItem : orderItems) {
                if (existingItem.getItemId() == itemId) {
                    JOptionPane.showMessageDialog(this,
                            "This item is already in the order. Use one row per item.");
                    return;
                }
            }

            OrderLineItem orderLineItem = new OrderLineItem(itemId, itemName, quantity, unitPrice, discount);
            orderItems.add(orderLineItem);

            tableModel.addRow(new Object[]{
                    orderLineItem.getItemId(),
                    orderLineItem.getItemName(),
                    orderLineItem.getQuantity(),
                    orderLineItem.getUnitPrice(),
                    orderLineItem.getDiscount(),
                    orderLineItem.getLineTotal()
            });

            updateOrderTotal();

            quantityField.setText("");
            discountField.setText("");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error adding item to order.");
            ex.printStackTrace();
        }
    }

    private double getItemPrice(int itemId) {
        String sql = "SELECT price FROM dbo.Items WHERE item_id = ?";

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setInt(1, itemId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("price");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return 0.0;
    }

    private int getItemStock(int itemId) {
        String sql = "SELECT quantity FROM dbo.Items WHERE item_id = ?";

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setInt(1, itemId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("quantity");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return 0;
    }

    private void updateOrderTotal() {
        double total = 0.0;

        for (OrderLineItem item : orderItems) {
            total += item.getLineTotal();
        }

        totalLabel.setText(String.format("Order Total: $%.2f", total));
    }

    private double getCurrentOrderTotal() {
        double total = 0.0;

        for (OrderLineItem item : orderItems) {
            total += item.getLineTotal();
        }

        return total;
    }

    private int getNextOrderId(Connection conn) throws Exception {
        String sql = "SELECT ISNULL(MAX(order_id), 40752) + 1 AS next_order_id FROM dbo.Orders";

        try (
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()
        ) {
            if (rs.next()) {
                return rs.getInt("next_order_id");
            }
        }

        return 40753;
    }

    private void saveOrder() {
        if (orderItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please add at least one item to the order.");
            return;
        }

        String selectedCustomer = (String) customerComboBox.getSelectedItem();
        String selectedAccount = (String) accountComboBox.getSelectedItem();

        if (selectedCustomer == null || selectedAccount == null) {
            JOptionPane.showMessageDialog(this, "Please select a customer and account.");
            return;
        }

        int customerId = Integer.parseInt(selectedCustomer.split(" - ")[0]);
        int accountId = Integer.parseInt(selectedAccount.split(" - ")[0]);
        double totalAmount = getCurrentOrderTotal();

        String insertOrderSql =
                "INSERT INTO dbo.Orders (order_id, customer_id, account_id, order_date, total_amount) " +
                        "VALUES (?, ?, ?, ?, ?)";

        String insertOrderLineSql =
                "INSERT INTO dbo.OrderLines (order_id, item_id, quantity, unit_price, discount) " +
                        "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                for (OrderLineItem item : orderItems) {
                    int currentStock = getItemStockForTransaction(conn, item.getItemId());

                    if (item.getQuantity() > currentStock) {
                        throw new Exception("Not enough stock for item: " + item.getItemName());
                    }
                }

                int newOrderId = getNextOrderId(conn);

                try (PreparedStatement orderStmt = conn.prepareStatement(insertOrderSql)) {
                    orderStmt.setInt(1, newOrderId);
                    orderStmt.setInt(2, customerId);
                    orderStmt.setInt(3, accountId);
                    orderStmt.setDate(4, new Date(System.currentTimeMillis()));
                    orderStmt.setDouble(5, totalAmount);
                    orderStmt.executeUpdate();
                }

                try (PreparedStatement lineStmt = conn.prepareStatement(insertOrderLineSql)) {
                    for (OrderLineItem item : orderItems) {
                        lineStmt.setInt(1, newOrderId);
                        lineStmt.setInt(2, item.getItemId());
                        lineStmt.setInt(3, item.getQuantity());
                        lineStmt.setDouble(4, item.getUnitPrice());
                        lineStmt.setDouble(5, item.getDiscount());
                        lineStmt.executeUpdate();
                    }
                }

                conn.commit();
                JOptionPane.showMessageDialog(this, "Order saved successfully. Order ID: " + newOrderId);

                if (mainFrame != null) {
                    mainFrame.refreshItemsTable();
                }
                
                clearOrderForm();
                loadItems();

            } catch (Exception ex) {
                conn.rollback();
                JOptionPane.showMessageDialog(this, "Order save failed: " + ex.getMessage());
                ex.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database error while saving order.");
            ex.printStackTrace();
        }

    }

    private int getItemStockForTransaction(Connection conn, int itemId) throws Exception {
        String sql = "SELECT quantity FROM dbo.Items WHERE item_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("quantity");
                }
            }
        }

        return 0;
    }

    private void clearOrderForm() {
        orderItems.clear();
        tableModel.setRowCount(0);
        quantityField.setText("");
        discountField.setText("");
        totalLabel.setText("Order Total: $0.00");
    }
}