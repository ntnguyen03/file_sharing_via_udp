package server;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:udp_file_system.db";

    public static List<String> getUserHistory(String username) {
        List<String> historyList = new ArrayList<>();
        // Lấy các file mà user này là người gửi (sender)
        String sql = "SELECT filename, filesize, receiver, timestamp FROM file_history WHERE sender = ? ORDER BY timestamp DESC";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                // Tạo chuỗi format: HISTORY_ITEM|filename|size|receiver|timestamp
                String item = "HISTORY_ITEM|" +
                        rs.getString("filename") + "|" +
                        rs.getLong("filesize") + "|" +
                        rs.getString("receiver") + "|" +
                        rs.getString("timestamp");
                historyList.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return historyList;
    }

    public static void init() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            // Bảng Users
            String sqlUsers = "CREATE TABLE IF NOT EXISTS users (" +
                    "username TEXT PRIMARY KEY," +
                    "password TEXT NOT NULL," +
                    "fullname TEXT)";
            stmt.execute(sqlUsers);

            // Bảng History
            String sqlHistory = "CREATE TABLE IF NOT EXISTS file_history (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "filename TEXT," +
                    "filesize INTEGER," +
                    "sender TEXT," +
                    "receiver TEXT," +
                    "status TEXT," +
                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)";
            stmt.execute(sqlHistory);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Đăng ký
    public static boolean registerUser(String username, String password, String fullname) {
        String sql = "INSERT INTO users(username, password, fullname) VALUES(?,?,?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, fullname);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    // Đăng nhập
    public static boolean checkLogin(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    // --- HÀM QUAN TRỌNG: Kiểm tra xem user có tồn tại trong DB không ---
    public static boolean isRealUser(String username) {
        String sql = "SELECT username FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    // Lưu lịch sử
    public static void saveFileLog(String filename, long size, String sender, String receiver, String status) {
        String sql = "INSERT INTO file_history(filename, filesize, sender, receiver, status) VALUES(?,?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, filename);
            pstmt.setLong(2, size);
            pstmt.setString(3, sender);
            pstmt.setString(4, receiver);
            pstmt.setString(5, status);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Lấy lịch sử
    public static List<Object[]> getAllHistory() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT * FROM file_history ORDER BY timestamp DESC";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                list.add(new Object[]{
                        rs.getString("filename"),
                        rs.getLong("filesize"),
                        rs.getString("sender"),
                        rs.getString("receiver"),
                        rs.getString("timestamp")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}
