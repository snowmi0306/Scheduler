package project;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

class RegistrationPanel extends JPanel {
    private SchedulerApp parent;
    private JComboBox<String> avatarCombo;
    private JTextField nameField;

    public RegistrationPanel(SchedulerApp parent) {
        this.parent = parent;

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(40, 80, 40, 80));

        JLabel title = new JLabel("캐릭터 생성", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(10, 10));
        JLabel preview = new JLabel("여자1", SwingConstants.CENTER);
        preview.setFont(new Font("SansSerif", Font.PLAIN, 32));
        preview.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));
        preview.setPreferredSize(new Dimension(180, 180));
        center.add(preview, BorderLayout.WEST);

        JPanel form = new JPanel(new GridLayout(4, 1, 8, 8));
        form.setBorder(new EmptyBorder(10, 20, 10, 20));
        avatarCombo = new JComboBox<>(new String[] {"여자1", "여자2", "남자1", "남자2"});
        nameField = new JTextField();

        avatarCombo.addActionListener(e -> {
            String sel = (String) avatarCombo.getSelectedItem();
            preview.setText(sel);
        });

        form.add(new JLabel("캐릭터 선택"));
        form.add(avatarCombo);
        form.add(new JLabel("닉네임 입력"));
        form.add(nameField);

        center.add(form, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        JButton createBtn = new JButton("등록 완료");
        createBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        createBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String avatar = (String) avatarCombo.getSelectedItem();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "닉네임을 입력해주세요.");
                return;
            }
            parent.showMain(name, avatar);
        });
        add(createBtn, BorderLayout.SOUTH);
    }
}