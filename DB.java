
import java.sql.Connection;
import java.sql.DriverManager;

public class DB {

    public static Connection getConnection() throws Exception {

        System.out.println("🔌 Подключение к PostgreSQL...");

        Class.forName("org.postgresql.Driver");

        Connection conn = DriverManager.getConnection(
                "jdbc:postgresql://127.0.0.1:5433/file_index_db",
                "index_user",
                "index_pass"
        );

        System.out.println("✅ PostgreSQL подключен");

        return conn;
    }
}