package project;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class Shop extends JPanel {
    private SchedulerApp parent;
    private MainPanel mainPanel;

    private JTextArea infoArea;
    private JButton backBtn;

    public Shop(SchedulerApp parent, MainPanel mainPanel) {
        this.parent = parent;
        this.mainPanel = mainPanel;

        setLayout(new BorderLayout(12,12));
        setBorder(new EmptyBorder(12,12,12,12));

        JLabel title = new JLabel("Shop", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8,8,8,8);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0; c.gridy = 0;

        // 상품: HP 회복
        JButton buyHpBtn = new JButton("HP 회복 +30 (30 코인)");
        buyHpBtn.addActionListener(e -> buyHp());
        center.add(buyHpBtn, c);

        // 상품: 배경색 변경 3개
        c.gridy++;
        JButton bg1 = new JButton("배경색 변경 - 블루 (50 코인)");
        bg1.addActionListener(e -> buyBackground(Color.decode("#DCEEFF")));
        center.add(bg1, c);

        c.gridy++;
        JButton bg2 = new JButton("배경색 변경 - 그린 (50 코인)");
        bg2.addActionListener(e -> buyBackground(Color.decode("#E6FFE6")));
        center.add(bg2, c);

        c.gridy++;
        JButton bg3 = new JButton("배경색 변경 - 라일락 (50 코인)");
        bg3.addActionListener(e -> buyBackground(Color.decode("#F0E6FF")));
        center.add(bg3, c);

        // 원래색 되돌리기 (무료)
        c.gridy++;
        JButton restoreBtn = new JButton("원래색 되돌리기 (0 코인)");
        restoreBtn.addActionListener(e -> {
            mainPanel.restoreOriginalBackground();
            JOptionPane.showMessageDialog(this, "배경색이 원래대로 복원되었습니다.");
            refreshInfo();
        });
        center.add(restoreBtn, c);

        // 정보 영역
        c.gridy++;
        infoArea = new JTextArea(6, 30);
        infoArea.setEditable(false);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setText(buildInfoText());
        JScrollPane sp = new JScrollPane(infoArea);
        center.add(sp, c);

        add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        backBtn = new JButton("뒤로가기");
        backBtn.addActionListener(e -> parent.showCard("main"));
        bottom.add(backBtn);
        add(bottom, BorderLayout.SOUTH);
    }

    private String buildInfoText() {
        StringBuilder sb = new StringBuilder();
        sb.append("상점 안내\n");
        sb.append("- HP 회복: 즉시 HP를 30 회복합니다 (최대 100). 가격 30 코인.\n");
        sb.append("- 배경색 변경: 메인 화면 배경을 변경합니다. 가격 50 코인.\n");
        sb.append("- 원래색 되돌리기: 무료로 원래 배경으로 복원합니다.\n\n");
        sb.append("현재 코인: ").append(mainPanel.getCoins()).append("\n");
        return sb.toString();
    }

    public void refreshInfo() {
        infoArea.setText(buildInfoText());
    }

    private void buyHp() {
        int price = 30;
        if (!mainPanel.spendCoins(price)) {
            JOptionPane.showMessageDialog(this, "코인이 부족합니다.");
            return;
        }
        mainPanel.restoreHPBy(30);
        JOptionPane.showMessageDialog(this, "HP가 30 회복되었습니다.");
        refreshInfo();
    }

    private void buyBackground(Color color) {
        int price = 50;
        if (!mainPanel.spendCoins(price)) {
            JOptionPane.showMessageDialog(this, "코인이 부족합니다.");
            return;
        }
        mainPanel.changeBackground(color);
        JOptionPane.showMessageDialog(this, "배경색이 변경되었습니다.");
        refreshInfo();
    }

    public JButton getBackButton() { return backBtn; }
}