package controllers;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import io.github.cdimascio.dotenv.Dotenv;

public class controller {

    public static void main(String[] args) {


        Dotenv dotenv = Dotenv.load();
        String url = "jdbc:mysql://localhost:3306/AnimalAdoptionDB";
        String user = "root";
        String password = dotenv.get("DB_PASSWORD");

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
