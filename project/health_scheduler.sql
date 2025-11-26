PRAGMA foreign_keys = ON;

-- 기본 정보
-- user (nickname, character, weight, height, age, coin)
CREATE TABLE IF NOT EXISTS user (
    id          INTEGER PRIMARY KEY AUTOINCREMENT, -- 고유 번호
    nickname    TEXT NOT NULL,          -- 닉네임
    character   TEXT,                   -- 캐릭터 이름
    weight      REAL,                   -- 몸무게 (kg)
    height      REAL,                   -- 키 (cm)
    age         INTEGER NOT NULL DEFAULT 0, -- 나이 (년)
    coin        INTEGER NOT NULL DEFAULT 0,  -- 보유 코인
    hp          INTEGER NOT NULL DEFAULT 100, -- 캐릭터 HP
    is_registered INTEGER NOT NULL DEFAULT 1     -- 가입 여부 0: 미가입/1: 가입 -> 최초 생성 창 출력할 시 필요!
);


-- 운동량 기록
-- exercise_log (user_id, date, content, minutes)
CREATE TABLE IF NOT EXISTS exercise_log (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id     INTEGER NOT NULL,
    date        TEXT NOT NULL,     -- ex) 2025-11-11
    content     TEXT NOT NULL,     -- 운동 내용
    minutes     INTEGER NOT NULL,  -- 운동 시간(분)
    FOREIGN KEY (user_id) REFERENCES user(id)
);

-- 칼로리 기록
-- calorie_log (user_id, date, food, kcal)
CREATE TABLE IF NOT EXISTS calorie_log (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id     INTEGER NOT NULL,
    date        TEXT NOT NULL,     -- ex) 2025-11-11
    food        TEXT NOT NULL,     -- 음식 이름
    kcal        INTEGER NOT NULL,  -- 칼로리
    FOREIGN KEY (user_id) REFERENCES user(id)
);

-- 수면 기록
-- sleep_log (user_id, date, hours)
CREATE TABLE IF NOT EXISTS sleep_log (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id     INTEGER NOT NULL,
    date        TEXT NOT NULL,     -- ex) 2025-11-11
    hours       REAL NOT NULL,     -- 수면 시간
    FOREIGN KEY (user_id) REFERENCES user(id)
);

-- 상점 아이템
-- shop_item (name, price)
CREATE TABLE IF NOT EXISTS shop_item (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    name        TEXT NOT NULL,     -- 아이템 이름
    price       INTEGER NOT NULL   -- 가격
);

-- 유저가 가진 아이템
CREATE TABLE IF NOT EXISTS user_item (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id     INTEGER NOT NULL,   -- 유저 
    item_id     INTEGER NOT NULL,   -- 상점 아이템 
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (item_id) REFERENCES shop_item(id)
);