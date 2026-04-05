//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
//import db.DBConnection;
import ui.LoginFrame;

//import java.sql.Connection;

public class Main {
    public static void main(String [] args) {
//        try (Connection conn = DBConnection.getConnection()) {
//            System.out.println("Connected to Project2DB successfully");
//        } catch (Exception e) {
//            System.out.println("Connection failed");
//            e.printStackTrace();
//        }

        javax.swing.SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}