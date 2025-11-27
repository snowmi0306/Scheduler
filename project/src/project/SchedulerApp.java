package project;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;

public class SchedulerApp extends JFrame {
    private CardLayout cardLayout;
    private JPanel cards;
    private RegistrationPanel registrationPanel;
    private MainPanel mainPanel;
    private HealthcarePanel healthcarePanel;
    private Shop shopPanel;

    private final DatabaseManager databaseManager;
    private int userId = -1;

    public SchedulerApp() {
        // 창 제목 통일
        setTitle("HealthcarePanel");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(880, 640);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);

        Path storageDir = Paths.get(System.getProperty("user.home"), ".health_scheduler");
        try {
            Files.createDirectories(storageDir);
        } catch (Exception e) {
            throw new RuntimeException("데이터 디렉터리를 준비하는 중 문제가 발생했습니다", e);
        }

        Path dbFile = storageDir.resolve("health_scheduler.db");
        Path legacyDb = Paths.get("health_scheduler.db").toAbsolutePath();
        try {
            if (Files.exists(legacyDb) && Files.notExists(dbFile)) {
                Files.move(legacyDb, dbFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            throw new RuntimeException("기존 데이터베이스를 이동하는 중 문제가 발생했습니다", e);
        }

        databaseManager = new DatabaseManager(dbFile.toAbsolutePath().toString());
        try {
            databaseManager.initialize();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "데이터베이스 초기화에 실패했습니다: " + e.getMessage());
        }

        registrationPanel = new RegistrationPanel(this);
        mainPanel = new MainPanel(databaseManager);
        healthcarePanel = new HealthcarePanel(this, mainPanel, databaseManager);
        shopPanel = new Shop(this, mainPanel);

        cards.add(registrationPanel, "register");
        cards.add(mainPanel, "main");
        cards.add(healthcarePanel, "healthcare");
        cards.add(shopPanel, "shop");

        add(cards);
        loadUserOrShowRegistration();

        // 헬스케어 이동
        mainPanel.getHealthcareButton().addActionListener(e -> {
            healthcarePanel.setCharacterText(mainPanel.getNickname() + " (" + mainPanel.getAvatarIcon() + ")");
            showCard("healthcare");
        });

        // 상점 이동 (기존 상점 버튼 사용)
        mainPanel.getShopButton().addActionListener(e -> {
            shopPanel.refreshInfo();
            showCard("shop");
        });

        // 뒤로가기
        healthcarePanel.getBackButton().addActionListener(e -> showCard("main"));
        shopPanel.getBackButton().addActionListener(e -> showCard("main"));
    }

    private void loadUserOrShowRegistration() {
        databaseManager.loadFirstUser().ifPresentOrElse(user -> {
            if (user.registered()) {
                userId = user.id();
                mainPanel.setUserContext(userId);
                mainPanel.setUserInfo(user.nickname(), user.character());
                mainPanel.setStats(user.coins(), user.hp());
                healthcarePanel.applyStoredBodyInfo(user.weight(), user.height(), user.age());
                healthcarePanel.setUserContext(userId);
                showCard("main");
            } else {
                showCard("register");
            }
        }, () -> showCard("register"));
    }

    public void showCard(String name) {
        cardLayout.show(cards, name);
    }

    public void showMain(String nickname, String avatarIcon) {
        mainPanel.setUserInfo(nickname, avatarIcon);
        showCard("main");
    }

    public void completeRegistration(String nickname, String avatarIcon) {
        try {
            userId = databaseManager.saveOrUpdateUser(nickname, avatarIcon);
            mainPanel.setUserContext(userId);
            mainPanel.setUserInfo(nickname, avatarIcon);
            healthcarePanel.setUserContext(userId);
            showCard("main");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "사용자 저장 중 오류가 발생했습니다: " + ex.getMessage());
        }
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}
