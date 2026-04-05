package ui;

import db.DBConnection;

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
    private JButton insertButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton refreshButton;
    private JTable itemsTable;
    private DefaultTableModel tableModel;

    public MainFrame() {
        setTitle("Project 2 Main Window");
        setSize(1000, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout());
        JLabel searchLabel = new JLabel("Search Item Name:");
        searchField = new JTextField(20);
        searchButton = new JButton("Search");
        insertButton = new JButton("Insert Item");
        updateButton = new JButton("Update Item");
        deleteButton = new JButton("Delete Item");
        refreshButton = new JButton("Refresh");

        topPanel.add(searchLabel);
        topPanel.add(searchField);
        topPanel.add(searchButton);
        topPanel.add(insertButton);
        topPanel.add(updateButton);
        topPanel.add(deleteButton);
        topPanel.add(refreshButton);

        String [] columnNames = {
                "Item ID", "Class ID", "Item Name", "Quantity", "Price", "Description"
        };

        tableModel = new DefaultTableModel(columnNames, 0);
        itemsTable = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(itemsTable);
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        loadAllItems();

        searchButton.addActionListener(e -> searchItems());
        refreshButton.addActionListener(e -> loadAllItems());
        insertButton.addActionListener(e -> insertItem());
        updateButton.addActionListener(e -> updateItem());
        deleteButton.addActionListener(e -> deleteItem());
    }

    private void loadAllItems() {
        tableModel.setRowCount(0);

        String sql = "SELECT item_id, class_id, item_name, quantity, price, description FROM dbo.Items";

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

        String sql = "SELECT item_id, class_id, item_name, quantity, price, description " +
                "FROM dbo.Items WHERE item_name LIKE ?";

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

            String sql = "DELETE FROM dbo.Items WHERE item_id = ?";

            try (
                    Connection conn = DBConnection.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)
            ) {
                pstmt.setInt(1, itemId);

                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Item deleted successfully.");
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
}
