package project;

import javax.swing.*;
import java.awt.*;

public class SchedulerApp extends JFrame {
    private CardLayout cardLayout;
    private JPanel cards;
    private RegistrationPanel registrationPanel;
    private MainPanel mainPanel;
    private HealthcarePanel healthcarePanel;
    private Shop shopPanel;

    public SchedulerApp() {
        // 창 제목 통일
        setTitle("HealthcarePanel");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(880, 640);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);

        registrationPanel = new RegistrationPanel(this);
        mainPanel = new MainPanel();
        healthcarePanel = new HealthcarePanel(this, mainPanel);
        shopPanel = new Shop(this, mainPanel);

        cards.add(registrationPanel, "register");
        cards.add(mainPanel, "main");
        cards.add(healthcarePanel, "healthcare");
        cards.add(shopPanel, "shop");

        add(cards);
        cardLayout.show(cards, "register");

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

    public void showCard(String name) {
        cardLayout.show(cards, name);
    }

    public void showMain(String nickname, String avatarIcon) {
        mainPanel.setUserInfo(nickname, avatarIcon);
        showCard("main");
    }
}
