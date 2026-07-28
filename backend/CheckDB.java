import java.sql.*;

public class CheckDB {
    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/smartbus?useSSL=false", "root", "");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM fund_transactions WHERE fund_type = 'BANK_TRANSFER'");
            boolean hasRows = false;
            while(rs.next()) {
                hasRows = true;
                System.out.println("ID: " + rs.getLong("id") + ", Amount: " + rs.getDouble("amount") + ", Type: " + rs.getString("fund_type"));
            }
            if(!hasRows) System.out.println("No BANK_TRANSFER transactions found!");
            
            rs = stmt.executeQuery("SELECT id, payment_method, status FROM bookings ORDER BY id DESC LIMIT 5");
            System.out.println("\nLatest 5 Bookings:");
            while(rs.next()) {
                System.out.println("Booking ID: " + rs.getLong("id") + ", PM: " + rs.getString("payment_method") + ", Status: " + rs.getString("status"));
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
