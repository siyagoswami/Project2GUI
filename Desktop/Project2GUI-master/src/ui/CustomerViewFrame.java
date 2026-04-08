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
        getContentPane().setBackground(new Color(245, 247, 250));

        JLabel title = new JLabel("Customer List");
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        String[] columnNames = {
                "Customer ID", "First Name", "Last Name", "Email", "Username"
        };

        tableModel = new DefaultTableModel(columnNames, 0);
        customerTable = new JTable(tableModel);

        customerTable.setRowHeight(25);
        customerTable.setGridColor(new Color(220, 220, 220));
        customerTable.setSelectionBackground(new Color(100, 149, 237));
        customerTable.setSelectionForeground(Color.WHITE);
        customerTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        customerTable.getTableHeader().setBackground(new Color(230, 236, 245));

        JScrollPane scrollPane = new JScrollPane(customerTable);
        scrollPane.getViewport().setBackground(Color.WHITE);

        add(title, BorderLayout.NORTH);
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