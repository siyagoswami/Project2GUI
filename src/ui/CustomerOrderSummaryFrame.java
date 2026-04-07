package ui;

import db.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CustomerOrderSummaryFrame extends JFrame {
    private JTable table;
    private DefaultTableModel model;

    public CustomerOrderSummaryFrame() {
        setTitle("Customer Order Summary");
        setSize(800, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        String[] columns = {
                "Customer ID", "First Name", "Last Name", "Email", "Total Orders", "Total Spent"
        };

        model = new DefaultTableModel(columns, 0);
        table = new JTable(model);

        add(new JScrollPane(table), BorderLayout.CENTER);

        loadData();
    }

    private void loadData() {
        model.setRowCount(0);

        String sql = "SELECT * FROM dbo.CustomerOrderSummary";

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
                        rs.getInt("total_orders"),
                        rs.getDouble("total_spent")
                };
                model.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading summary.");
            e.printStackTrace();
        }
    }
}
