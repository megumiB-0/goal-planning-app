/*rolesテーブル*/
INSERT IGNORE INTO roles (id, name) VALUES (1,'ROLE_GENERAL');
INSERT IGNORE INTO roles (id, name) VALUES (2,'ROLE_ADMIN');

/* usersテーブル */
INSERT IGNORE INTO users
(name, date_of_birth, gender, email, password, role_id, enabled)
VALUES
('Taro Yamada', '1990-05-10', 0, 'taro@example.com', '$2a$10$2JNjTwZBwo7fprL2X4sv.OEKqxnVtsVQvuXDkI8xVGix.U3W5B7CO', 1, 1),
('Hanako Suzuki', '1993-11-25', 1, 'hanako@example.com', '$2a$10$2JNjTwZBwo7fprL2X4sv.OEKqxnVtsVQvuXDkI8xVGix.U3W5B7CO', 2, 1);