import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class controller {

    public static void main(String[] args) {

        // System.out.println(System.getProperty("java.class.path"));
        // try {
        //     Class.forName("com.mysql.cj.jdbc.Driver");
        //     System.out.println("Driver FOUND");
        // } catch (ClassNotFoundException e) {
        //     System.out.println("Driver NOT on classpath");
        // }

        String url = "jdbc:mysql://localhost:3306/AnimalAdoptionDB";
        String user = "root";
        String password = "root";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM RegisteredUser")) {

            System.out.println("Connected!");

            while (rs.next()) {
                System.out.println(rs.getInt(1) + " " + rs.getString(2));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
