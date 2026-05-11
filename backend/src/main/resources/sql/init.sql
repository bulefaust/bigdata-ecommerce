CREATE DATABASE IF NOT EXISTS ecommerce DEFAULT CHARACTER SET utf8mb4;
USE ecommerce;

CREATE TABLE t_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    gender TINYINT DEFAULT 0,
    age INT,
    avatar VARCHAR(255),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

CREATE TABLE t_product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    category VARCHAR(50) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    stock INT DEFAULT 0,
    description TEXT,
    image_url VARCHAR(255),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

CREATE TABLE t_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status TINYINT DEFAULT 0 COMMENT '0-pending 1-paid 2-shipped 3-completed 4-cancelled',
    payment_method VARCHAR(20),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

INSERT INTO t_user (username, password, phone, gender, age) VALUES
('zhangsan', '123456', '13800001111', 1, 25),
('lisi', '123456', '13800002222', 0, 30),
('wangwu', '123456', '13800003333', 1, 22);

INSERT INTO t_product (name, category, price, stock, description) VALUES
('iPhone 15 Pro', '手机', 8999.00, 100, 'Apple iPhone 15 Pro 256GB'),
('华为 Mate 60', '手机', 6999.00, 80, '华为 Mate 60 Pro'),
('MacBook Pro 14', '电脑', 14999.00, 50, 'Apple MacBook Pro 14寸 M3'),
('联想 ThinkPad X1', '电脑', 9999.00, 60, '联想 ThinkPad X1 Carbon'),
('AirPods Pro', '耳机', 1899.00, 200, 'Apple AirPods Pro 2'),
('索尼 WH-1000XM5', '耳机', 2499.00, 120, '索尼降噪耳机'),
('iPad Air', '平板', 4799.00, 90, 'Apple iPad Air M1'),
('华为 MatePad Pro', '平板', 3999.00, 70, '华为 MatePad Pro 12.6'),
('小米手环8', '穿戴', 249.00, 500, '小米手环8 NFC版'),
('Apple Watch S9', '穿戴', 2999.00, 150, 'Apple Watch Series 9');
