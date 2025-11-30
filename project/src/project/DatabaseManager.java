package project;

import java.sql.*;
import java.time.LocalDate;
import java.util.Optional;

/**
 * SQLite 기반 데이터베이스 관리 클래스.
 */
public class DatabaseManager {
    private final String dbPath;

    public DatabaseManager(String dbPath) {
        this.dbPath = dbPath;
    }

    private Connection getConnection() throws SQLException {
        String url = "jdbc:sqlite:" + dbPath;
        return DriverManager.getConnection(url);
    }

    public void initialize() throws SQLException {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");

            stmt.execute("CREATE TABLE IF NOT EXISTS user (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "nickname TEXT NOT NULL," +
                    "character TEXT," +
                    "weight REAL," +
                    "height REAL," +
                    "age INTEGER NOT NULL DEFAULT 0," +
                    "coin INTEGER NOT NULL DEFAULT 0," +
                    "hp INTEGER NOT NULL DEFAULT 100," +
                    "is_registered INTEGER NOT NULL DEFAULT 1" +
                    ")");

            // 과거 버전 호환: 누락된 컬럼은 ALTER TABLE로 추가 시도
            addColumnIfMissing(conn, "user", "age", "age INTEGER NOT NULL DEFAULT 0");
            addColumnIfMissing(conn, "user", "coin", "coin INTEGER NOT NULL DEFAULT 0");
            addColumnIfMissing(conn, "user", "hp", "hp INTEGER NOT NULL DEFAULT 100");
            addColumnIfMissing(conn, "user", "is_registered", "is_registered INTEGER NOT NULL DEFAULT 1");

            stmt.execute("CREATE TABLE IF NOT EXISTS exercise_log (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," +
                    "date TEXT NOT NULL," +
                    "content TEXT NOT NULL," +
                    "minutes INTEGER NOT NULL," +
                    "FOREIGN KEY (user_id) REFERENCES user(id)" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS calorie_log (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," +
                    "date TEXT NOT NULL," +
                    "food TEXT NOT NULL," +
                    "kcal INTEGER NOT NULL," +
                    "FOREIGN KEY (user_id) REFERENCES user(id)" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS sleep_log (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," +
                    "date TEXT NOT NULL," +
                    "hours REAL NOT NULL," +
                    "FOREIGN KEY (user_id) REFERENCES user(id)" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS shop_item (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "price INTEGER NOT NULL" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS user_item (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," +
                    "item_id INTEGER NOT NULL," +
                    "FOREIGN KEY (user_id) REFERENCES user(id)," +
                    "FOREIGN KEY (item_id) REFERENCES shop_item(id)" +
                    ")");
        }
    }

    private void addColumnIfMissing(Connection conn, String table, String columnName, String columnDef) throws SQLException {
        if (columnExists(conn, table, columnName)) {
            return;
        }

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("ALTER TABLE " + table + " ADD COLUMN " + columnDef);
        }
    }

    private boolean columnExists(Connection conn, String table, String columnName) throws SQLException {
        String pragma = "PRAGMA table_info(" + table + ")";
        try (PreparedStatement ps = conn.prepareStatement(pragma); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                if (columnName.equalsIgnoreCase(rs.getString("name"))) {
                    return true;
                }
            }
        }
        return false;
    }

    public Optional<User> loadFirstUser() {
        String sql = "SELECT id, nickname, character, weight, height, age, coin, hp, is_registered FROM user ORDER BY id LIMIT 1";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("nickname"),
                        rs.getString("character"),
                        rs.getDouble("weight"),
                        rs.getDouble("height"),
                        rs.getInt("age"),
                        rs.getInt("coin"),
                        rs.getInt("hp"),
                        rs.getInt("is_registered") == 1
                );
                return Optional.of(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public int saveOrUpdateUser(String nickname, String character) throws SQLException {
        Optional<User> existing = loadFirstUser();
        if (existing.isPresent()) {
            String sql = "UPDATE user SET nickname = ?, character = ?, is_registered = 1 WHERE id = ?";
            try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, nickname);
                ps.setString(2, character);
                ps.setInt(3, existing.get().id());
                ps.executeUpdate();
                return existing.get().id();
            }
        }

        String insert = "INSERT INTO user (nickname, character, is_registered) VALUES (?, ?, 1)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nickname);
            ps.setString(2, character);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }
        }
        return -1;
    }

    public void updateUserStats(int userId, int coins, int hp, double weight, double height, int age) {
        String sql = "UPDATE user SET coin = ?, hp = ?, weight = ?, height = ?, age = ? WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, coins);
            ps.setInt(2, hp);
            ps.setDouble(3, weight);
            ps.setDouble(4, height);
            ps.setInt(5, age);
            ps.setInt(6, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertCalorieLog(int userId, LocalDate date, int kcal) {
        String sql = "INSERT INTO calorie_log (user_id, date, food, kcal) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, date.toString());
            ps.setString(3, "기록");
            ps.setInt(4, kcal);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertExerciseLog(int userId, LocalDate date, String content, int minutes) {
        String sql = "INSERT INTO exercise_log (user_id, date, content, minutes) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, date.toString());
            ps.setString(3, content);
            ps.setInt(4, minutes);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertSleepLog(int userId, LocalDate date, int hours) {
        String sql = "INSERT INTO sleep_log (user_id, date, hours) VALUES (?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, date.toString());
            ps.setInt(3, hours);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
