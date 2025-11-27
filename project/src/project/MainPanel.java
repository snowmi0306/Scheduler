package project;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

class MainPanel extends JPanel {
    // 사용자 정보
    private String nickname = "사용자";
    private String avatarIcon = "여자1";
    private int userId = -1;
    private final DatabaseManager databaseManager;

    // 상단 UI
    private JLabel avatarLabel;
    private JLabel nameLabel;
    private JProgressBar hpBar;
    private JLabel dateLabel;
    private JLabel timeLabel;
    private JLabel coinLabel;
    private JComboBox<LocalDate> dateSelector;

    // 일정 데이터/UI
    private DefaultListModel<String> scheduleModel;
    private JList<String> scheduleList;
    private Map<LocalDate, List<ScheduleEntry>> scheduleStore = new HashMap<>();

    // 건강 기록 데이터/UI
    private DefaultListModel<String> healthModel;
    private JList<String> healthList;
    private Map<LocalDate, String> healthStore = new HashMap<>();

    // 일정 등록 UI
    private JTextField timeInput;
    private JTextField taskInput;
    private JPanel regCard;

    // 네비게이션 버튼
    private JButton healthcareBtn;
    private JButton shopBtn;

    // 헬스케어 데이터 저장
    private Map<LocalDate, Integer> foodCalories = new HashMap<>();
    private Map<LocalDate, Integer> exerciseCalories = new HashMap<>();
    private Map<LocalDate, Integer> sleepHours = new HashMap<>();

    // 코인 및 보상
    private int coins = 0;
    private LocalDate lastCalorieRewardDate = null;

    // 배경 원복용
    private Color originalBackground = null;

    public MainPanel(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(12, 12, 12, 12));

        // 상단: 캐릭터, 날짜/시간, HP, 코인
        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.setPreferredSize(new Dimension(0, 120));

        JPanel charCard = new JPanel(new BorderLayout());
        charCard.setBorder(new TitledBorder("캐릭터"));
        avatarLabel = new JLabel(avatarIcon, SwingConstants.CENTER);
        avatarLabel.setFont(new Font("SansSerif", Font.PLAIN, 32));
        nameLabel = new JLabel("닉네임: " + nickname, SwingConstants.CENTER);
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        charCard.add(avatarLabel, BorderLayout.CENTER);
        charCard.add(nameLabel, BorderLayout.SOUTH);
        charCard.setPreferredSize(new Dimension(220, 0));
        top.add(charCard, BorderLayout.WEST);

        JPanel centerCard = new JPanel(new BorderLayout(6, 6));
        centerCard.setBorder(new TitledBorder("날짜 및 시간"));
        JPanel dtPanel = new JPanel(new GridLayout(2, 1));
        dateLabel = new JLabel("날짜: " + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), SwingConstants.CENTER);
        dateLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        timeLabel = new JLabel("시간: " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")), SwingConstants.CENTER);
        timeLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        dtPanel.add(dateLabel);
        dtPanel.add(timeLabel);
        centerCard.add(dtPanel, BorderLayout.CENTER);

        LocalDate today = LocalDate.now();
        dateSelector = new JComboBox<>();
        for (int i = -30; i <= 30; i++) {
            dateSelector.addItem(today.plusDays(i));
        }
        dateSelector.setSelectedItem(today);
        dateSelector.addActionListener(e -> {
            loadSchedulesForSelectedDate();
            loadHealthForSelectedDate();
        });
        JPanel selPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        selPanel.add(new JLabel("날짜 선택:"));
        selPanel.add(dateSelector);
        centerCard.add(selPanel, BorderLayout.SOUTH);

        top.add(centerCard, BorderLayout.CENTER);

        JPanel statusCard = new JPanel();
        statusCard.setLayout(new BoxLayout(statusCard, BoxLayout.Y_AXIS));
        statusCard.setBorder(new TitledBorder("상태"));
        hpBar = new JProgressBar(0, 100);
        hpBar.setValue(100);
        hpBar.setStringPainted(true);
        hpBar.setForeground(Color.RED);
        JLabel hpLabel = new JLabel("HP", SwingConstants.CENTER);
        hpLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusCard.add(hpLabel);
        statusCard.add(Box.createRigidArea(new Dimension(0, 6)));
        statusCard.add(hpBar);

        coinLabel = new JLabel("Coins: " + coins, SwingConstants.CENTER);
        coinLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        coinLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusCard.add(Box.createRigidArea(new Dimension(0, 6)));
        statusCard.add(coinLabel);

        statusCard.setPreferredSize(new Dimension(220, 0));
        top.add(statusCard, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);

        // 중앙: 일정 목록 + 버튼
        JPanel center = new JPanel(new BorderLayout(8, 8));
        scheduleModel = new DefaultListModel<>();
        scheduleList = new JList<>(scheduleModel);
        scheduleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scheduleList.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JScrollPane scroll = new JScrollPane(scheduleList);
        scroll.setBorder(new TitledBorder("일정 목록"));
        center.add(scroll, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
        actionPanel.setBorder(new EmptyBorder(6, 6, 6, 6));
        JButton completeBtn = new JButton("완료");
        JButton failBtn = new JButton("실패");
        JButton deleteBtn = new JButton("삭제");
        completeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        failBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        deleteBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        actionPanel.add(Box.createVerticalGlue());
        actionPanel.add(completeBtn);
        actionPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        actionPanel.add(failBtn);
        actionPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        actionPanel.add(deleteBtn);
        actionPanel.add(Box.createVerticalGlue());
        center.add(actionPanel, BorderLayout.EAST);

        add(center, BorderLayout.CENTER);

        // 하단: 건강 기록 + 일정 등록 + 네비게이션
        JPanel bottom = new JPanel(new BorderLayout(8, 8));

        healthModel = new DefaultListModel<>();
        healthList = new JList<>(healthModel);
        healthList.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JScrollPane healthScroll = new JScrollPane(healthList);
        healthScroll.setBorder(new TitledBorder("건강 기록"));
        bottom.add(healthScroll, BorderLayout.NORTH);

        regCard = new JPanel(new BorderLayout(6, 6));
        regCard.setBorder(new TitledBorder("일정 등록"));
        regCard.setVisible(true); // 기본 표시로 변경
        JPanel fields = new JPanel(new GridLayout(2, 2, 6, 6));
        fields.add(new JLabel("시간 (HH:mm):"));
        timeInput = new JTextField();
        fields.add(timeInput);
        fields.add(new JLabel("내용:"));
        taskInput = new JTextField();
        fields.add(taskInput);
        regCard.add(fields, BorderLayout.CENTER);
        JButton addBtn = new JButton("등록");
        regCard.add(addBtn, BorderLayout.EAST);
        bottom.add(regCard, BorderLayout.CENTER);

        JPanel nav = new JPanel(new GridLayout(1, 3, 12, 12));
        nav.setBorder(new EmptyBorder(6, 6, 6, 6));
        healthcareBtn = new JButton("헬스케어");
        JButton schedulerBtn = new JButton("스케줄러");
        shopBtn = new JButton("상점");
        nav.add(healthcareBtn);
        nav.add(schedulerBtn);
        nav.add(shopBtn);
        bottom.add(nav, BorderLayout.SOUTH);

        add(bottom, BorderLayout.SOUTH);

        // 일정 등록 이벤트 (입력 검증 강화)
        addBtn.addActionListener(e -> {
            LocalDate selectedDate = (LocalDate) dateSelector.getSelectedItem();
            String t = timeInput.getText().trim();
            String task = taskInput.getText().trim();

            if (t.length() != 5 || t.charAt(2) != ':') {
                JOptionPane.showMessageDialog(this, "시간은 HH:mm 형식으로 입력해주세요. 예: 09:00");
                return;
            }
            if (task.isEmpty()) {
                JOptionPane.showMessageDialog(this, "내용을 입력해주세요.");
                return;
            }

            try {
                LocalTime lt = LocalTime.parse(t, DateTimeFormatter.ofPattern("HH:mm"));
                scheduleStore.putIfAbsent(selectedDate, new ArrayList<>());
                int scheduleId = -1;
                if (userId > 0) {
                    scheduleId = databaseManager.insertSchedule(userId, selectedDate, lt, task, ScheduleStatus.TODO.label);
                }
                scheduleStore.get(selectedDate).add(new ScheduleEntry(scheduleId, lt, task, ScheduleStatus.TODO));
                ensureScheduleOrder(selectedDate);
                loadSchedulesForSelectedDate();
                timeInput.setText("");
                taskInput.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "시간 형식이 올바르지 않습니다. 예: 14:00");
            }
        });

        // 완료 버튼: 완료로 변경 + 코인 10 지급
        completeBtn.addActionListener(e -> {
            int sel = scheduleList.getSelectedIndex();
            if (sel == -1) return;
            LocalDate selectedDate = (LocalDate) dateSelector.getSelectedItem();
            List<ScheduleEntry> list = scheduleStore.get(selectedDate);
            if (list == null || sel >= list.size()) return;
            ScheduleEntry entry = list.get(sel);
            if (entry.status != ScheduleStatus.DONE) {
                entry.status = ScheduleStatus.DONE;
                if (entry.id > 0) {
                    databaseManager.updateScheduleStatus(entry.id, entry.status.label);
                }
                awardCoins(10);
                loadSchedulesForSelectedDate();
            }
        });

        // 실패 버튼: 상태 변경 + HP 15 감소 (일정 실패 패널 규칙 유지)
        failBtn.addActionListener(e -> {
            int sel = scheduleList.getSelectedIndex();
            if (sel == -1) return;
            LocalDate selectedDate = (LocalDate) dateSelector.getSelectedItem();
            List<ScheduleEntry> list = scheduleStore.get(selectedDate);
            if (list == null || sel >= list.size()) return;
            ScheduleEntry entry = list.get(sel);
            if (entry.status != ScheduleStatus.FAILED) {
                entry.status = ScheduleStatus.FAILED;
                if (entry.id > 0) {
                    databaseManager.updateScheduleStatus(entry.id, entry.status.label);
                }
                hpBar.setValue(Math.max(0, hpBar.getValue() - 15));
                persistStats();
                loadSchedulesForSelectedDate();
            }
        });

        // 삭제 버튼
        deleteBtn.addActionListener(e -> {
            int sel = scheduleList.getSelectedIndex();
            if (sel == -1) return;
            int confirm = JOptionPane.showConfirmDialog(this, "선택한 일정을 삭제하시겠습니까?", "일정 삭제", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
            LocalDate selectedDate = (LocalDate) dateSelector.getSelectedItem();
            List<ScheduleEntry> list = scheduleStore.get(selectedDate);
            if (list != null && sel < list.size()) {
                ScheduleEntry removed = list.remove(sel);
                if (removed != null && removed.id > 0) {
                    databaseManager.deleteSchedule(removed.id);
                }
                loadSchedulesForSelectedDate();
            }
        });

        // 스케줄러 버튼: 등록 카드 토글
        schedulerBtn.addActionListener(e -> regCard.setVisible(!regCard.isVisible()));

        // 시간 라벨 갱신 및 23:59 보상 체크
        new javax.swing.Timer(1000, e -> {
            LocalTime now = LocalTime.now();
            timeLabel.setText("시간: " + now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            dateLabel.setText("날짜: " + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            if (now.getHour() == 23 && now.getMinute() == 59 && now.getSecond() == 0) {
                processDailyCalorieReward();
            }
        }).start();

        loadSchedulesForSelectedDate();
        loadHealthForSelectedDate();
    }

    // 코인 지급 공통
    private void awardCoins(int amount) {
        if (amount <= 0) return;
        coins += amount;
        coinLabel.setText("Coins: " + coins);
        JOptionPane.showMessageDialog(this, amount + " 코인이 지급되었습니다. 현재 코인: " + coins);
        persistStats();
    }

    // 23:59 기준 섭취 칼로리 보상(50 코인)
    private void processDailyCalorieReward() {
        LocalDate today = LocalDate.now();
        if (today.equals(lastCalorieRewardDate)) return;

        int food = foodCalories.getOrDefault(today, 0);
        int exercise = exerciseCalories.getOrDefault(today, 0);

        boolean isMale = avatarIcon.startsWith("남자");
        double weight = HealthcarePanel.getWeight();
        double height = HealthcarePanel.getUserHeight();
        int age = HealthcarePanel.getAge();

        int bmr = calculateBMR(isMale, weight, height, age);
        int allowed = bmr + exercise;

        if (food <= allowed) {
            awardCoins(50);
            lastCalorieRewardDate = today;
        }
    }

    // 사용자 정보 적용
    public void setUserInfo(String nickname, String avatarIcon) {
        this.nickname = nickname;
        this.avatarIcon = avatarIcon;
        avatarLabel.setText(this.avatarIcon);
        nameLabel.setText("닉네임: " + this.nickname);
    }

    public void setStats(int coins, int hp) {
        this.coins = coins;
        coinLabel.setText("Coins: " + coins);
        hpBar.setValue(hp);
    }

    public void setUserContext(int userId) {
        this.userId = userId;
        loadSchedulesFromDatabase();
    }

    // 일정 목록 로딩
    private void loadSchedulesForSelectedDate() {
        LocalDate selectedDate = (LocalDate) dateSelector.getSelectedItem();
        scheduleModel.clear();
        List<ScheduleEntry> list = scheduleStore.getOrDefault(selectedDate, new ArrayList<>());
        ensureScheduleOrder(selectedDate);
        for (ScheduleEntry entry : list) {
            scheduleModel.addElement(entry.time.format(DateTimeFormatter.ofPattern("HH:mm")) + " - " + entry.content + " [" + entry.status.label + "]");
        }
    }

    private void ensureScheduleOrder(LocalDate date) {
        List<ScheduleEntry> list = scheduleStore.get(date);
        if (list != null) {
            list.sort(Comparator.comparing(s -> s.time));
        }
    }

    // 건강 요약 로딩
    private void loadHealthForSelectedDate() {
        LocalDate selectedDate = (LocalDate) dateSelector.getSelectedItem();
        healthModel.clear();
        String summary = healthStore.getOrDefault(selectedDate, "기록 없음");
        healthModel.addElement(summary);
    }

    // 버튼 getter
    public JButton getHealthcareButton() { return healthcareBtn; }
    public JButton getShopButton() { return shopBtn; }
    public String getNickname() { return nickname; }
    public String getAvatarIcon() { return avatarIcon; }
    public int getHpValue() { return hpBar.getValue(); }

    // 헬스케어 데이터 추가
    public void addFoodCalories(LocalDate date, int kcal) {
        foodCalories.put(date, foodCalories.getOrDefault(date, 0) + kcal);
        updateHealthStore(date);
        checkDailyBalance(date);
    }

    public void addExerciseCalories(LocalDate date, int kcal) {
        exerciseCalories.put(date, exerciseCalories.getOrDefault(date, 0) + kcal);
        updateHealthStore(date);
        checkDailyBalance(date);
    }

    public void addSleepHours(LocalDate date, int hours) {
        sleepHours.put(date, sleepHours.getOrDefault(date, 0) + hours);
        updateHealthStore(date);
    }

    // 캡슐화된 조회
    public int getFoodCaloriesForDate(LocalDate date) {
        return foodCalories.getOrDefault(date, 0);
    }

    public int getExerciseCaloriesForDate(LocalDate date) {
        return exerciseCalories.getOrDefault(date, 0);
    }

    public int getSleepHoursForDate(LocalDate date) {
        return sleepHours.getOrDefault(date, 0);
    }

    // 건강 기록 갱신
    private void updateHealthStore(LocalDate date) {
        int food = foodCalories.getOrDefault(date, 0);
        int exercise = exerciseCalories.getOrDefault(date, 0);
        int sleep = sleepHours.getOrDefault(date, 0);
        String summary = "음식: " + food + " kcal, 운동: " + exercise + " kcal, 수면: " + sleep + " 시간";
        healthStore.put(date, summary);
        loadHealthForSelectedDate();
    }

    // 칼로리 초과시 HP 10 감소
    private void checkDailyBalance(LocalDate date) {
        int food = foodCalories.getOrDefault(date, 0);
        int exercise = exerciseCalories.getOrDefault(date, 0);

        boolean isMale = avatarIcon.startsWith("남자");
        double weight = HealthcarePanel.getWeight();
        double height = HealthcarePanel.getUserHeight();
        int age = HealthcarePanel.getAge();

        int bmr = calculateBMR(isMale, weight, height, age);
        int allowed = bmr + exercise;

        if (food > allowed) {
            int hpLoss = 10;
            hpBar.setValue(Math.max(0, hpBar.getValue() - hpLoss));
            persistStats();
            JOptionPane.showMessageDialog(this, "칼로리 초과! HP -" + hpLoss);
        }
    }

    // 상점 연동 메서드들
    public int getCoins() { return coins; }

    public boolean spendCoins(int amount) {
        if (amount <= 0) return true;
        if (coins < amount) return false;
        coins -= amount;
        coinLabel.setText("Coins: " + coins);
        persistStats();
        return true;
    }

    public void addCoins(int amount) {
        if (amount <= 0) return;
        coins += amount;
        coinLabel.setText("Coins: " + coins);
        persistStats();
    }

    public void restoreHPBy(int amount) {
        if (amount <= 0) return;
        hpBar.setValue(Math.min(100, hpBar.getValue() + amount));
        persistStats();
    }

    public void changeBackground(Color color) {
        if (originalBackground == null) {
            originalBackground = this.getBackground();
        }
        this.setBackground(color);
        repaint();
    }

    public void restoreOriginalBackground() {
        if (originalBackground != null) {
            this.setBackground(originalBackground);
            repaint();
        }
    }

    private void loadSchedulesFromDatabase() {
        if (userId <= 0 || databaseManager == null) return;

        scheduleStore.clear();
        List<DatabaseManager.ScheduleRow> rows = databaseManager.loadAllSchedules(userId);
        for (DatabaseManager.ScheduleRow row : rows) {
            ScheduleStatus status = ScheduleStatus.fromLabel(row.status());
            scheduleStore.computeIfAbsent(row.date(), k -> new ArrayList<>())
                    .add(new ScheduleEntry(row.id(), row.time(), row.content(), status));
        }
        scheduleStore.values().forEach(list -> list.sort(Comparator.comparing(s -> s.time)));
        loadSchedulesForSelectedDate();
    }

    private void persistStats() {
        if (userId > 0 && databaseManager != null) {
            databaseManager.updateUserStats(userId, coins, hpBar.getValue(), HealthcarePanel.getWeight(), HealthcarePanel.getUserHeight(), HealthcarePanel.getAge());
        }
    }

    // BMR 계산
    public static int calculateBMR(boolean isMale, double weight, double height, int age) {
        if (isMale) {
            return (int)Math.round(66.47 + (13.75 * weight) + (5 * height) - (6.76 * age));
        } else {
            return (int)Math.round(655.1 + (9.56 * weight) + (1.85 * height) - (4.68 * age));
        }
    }

    private enum ScheduleStatus {
        TODO("미완료"),
        DONE("완료"),
        FAILED("실패");

        private final String label;

        ScheduleStatus(String label) {
            this.label = label;
        }

        static ScheduleStatus fromLabel(String label) {
            for (ScheduleStatus status : values()) {
                if (status.label.equals(label)) return status;
            }
            return TODO;
        }
    }

    private static class ScheduleEntry {
        private final int id;
        private final LocalTime time;
        private final String content;
        private ScheduleStatus status;

        private ScheduleEntry(int id, LocalTime time, String content, ScheduleStatus status) {
            this.id = id;
            this.time = time;
            this.content = content;
            this.status = status;
        }
    }
}