/*rolesテーブル*/
INSERT INTO roles (id, name) VALUES (1,'ROLE_GENERAL') ON CONFLICT (id) DO NOTHING;
INSERT INTO roles (id, name) VALUES (2,'ROLE_ADMIN') ON CONFLICT (id) DO NOTHING;

/* usersテーブル */
INSERT INTO users
(name, date_of_birth, gender, email, password, role_id, enabled)
VALUES
('Taro Yamada', '1990-05-10', 0, 'taro@example.com', '$2a$10$.3lD/Q3LgmdBKCo1KzECPu2VfjUzp2jnT.LFKWx.jXKHSYSj/QEPm', 1, true),
('Hanako Suzuki', '1993-11-25', 1, 'hanako@example.com', '$2a$10$2JNjTwZBwo7fprL2X4sv.OEKqxnVtsVQvuXDkI8xVGix.U3W5B7CO', 2, true)
ON CONFLICT (email) DO NOTHING;

/* qualificationsテーブル */
INSERT INTO qualifications
(id, root_qualification_id, name, estimated_minutes)
VALUES
(1,1,'FP3級',9000),-- 150h
(2,2,'FP2級',18000),-- 300h
(3,3,'日商簿記3級',9000),-- 150h
(4,4,'日商簿記2級',21000),-- 350h
(5,5,'ITパスポート',6000),-- 100h
(6,6,'基本情報技術者',12000),-- 200h
(7,7,'医療事務認定実務者',12000),-- 200h
(8,8,'インテリアコーディネーター',21000),-- 350h
(9,9,'宅地建物取引士',30000),-- 500h
(10,10,'行政書士試験',60000)-- 1000h
ON CONFLICT (id) DO NOTHING;
