package ui;

import db.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CustomerViewFrame extends JFrame {

    private JTable customerTable;
    private DefaultTableModel tableModel;

    public CustomerViewFrame() {
        setTitle("Customer View");
        setSize(700, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        String[] columnNames = {
                "Customer ID", "First Name", "Last Name", "Email", "Username"
        };

        tableModel = new DefaultTableModel(columnNames, 0);
        customerTable = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(customerTable);
        add(scrollPane, BorderLayout.CENTER);

        loadCustomers();
    }

    private void loadCustomers() {
        tableModel.setRowCount(0);

        String sql = "SELECT customer_id, firstName, lastName, email, username FROM dbo.Customers";

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()
        ) {
            while (rs.next()) {
                Object[] row = {
                        rs.getInt("customer_id"),
                        rs.getString("firstName"),
                        rs.getString("lastName"),
                        rs.getString("email"),
                        rs.getString("username")
                };
                tableModel.addRow(row);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading customers.");
            ex.printStackTrace();
        }
    }
}