package ui;
import db.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel statusLabel;

    public LoginFrame() {
        setTitle("Online Retail Database Login");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(245, 247, 250));

        JLabel title = new JLabel("Online Retail System");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(new Color(245, 247, 250));

        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField();

        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField();

        usernameLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        passwordLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        usernameField.setPreferredSize(new Dimension(150, 28));
        passwordField.setPreferredSize(new Dimension(150, 28));

        loginButton = new JButton("Login");
        loginButton.setBackground(new Color(70, 130, 180));
        loginButton.setForeground(Color.BLACK);
        loginButton.setFocusPainted(false);

        statusLabel = new JLabel("");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

        panel.add(usernameLabel);
        panel.add(usernameField);

        panel.add(passwordLabel);
        panel.add(passwordField);

        panel.add(new JLabel(""));
        panel.add(loginButton);

        panel.add(new JLabel(""));
        panel.add(statusLabel);

        add(title, BorderLayout.NORTH);
        add(panel, BorderLayout.CENTER);

        loginButton.addActionListener(e -> validateLogin());
    }

    private void validateLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if(username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Enter username and password.");
            return;
        }

        String sql = "SELECT * FROM dbo.Customers WHERE username = ? and password = ?";

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
        ) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();
            if(rs.next()) {
                statusLabel.setText("Login successful!");

                MainFrame mainFrame = new MainFrame();
                mainFrame.setVisible(true);

                dispose();
            } else {
                statusLabel.setText("Invalid username or password.");
            }
        } catch (Exception ex) {
            statusLabel.setText("Database error.");
            ex.printStackTrace();
        }
    }

    public JTextField getUsernameField() {
        return usernameField;
    }

    public JPasswordField getPasswordField() {
        return passwordField;
    }

    public JButton getLoginButton() {
        return loginButton;
    }

    public JLabel getStatusLabel() {
        return statusLabel;
    }
}