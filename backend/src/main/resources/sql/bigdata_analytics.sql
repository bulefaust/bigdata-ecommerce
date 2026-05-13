-- 实时热榜表
CREATE TABLE IF NOT EXISTS t_realtime_hot (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_name VARCHAR(200) DEFAULT NULL COMMENT '商品名称',
    category VARCHAR(100) DEFAULT NULL COMMENT '商品分类',
    image_url VARCHAR(500) DEFAULT NULL COMMENT '商品图片',
    price DECIMAL(10,2) DEFAULT NULL COMMENT '商品价格',
    clicks INT DEFAULT 0 COMMENT '点击数',
    purchases INT DEFAULT 0 COMMENT '购买数',
    carts INT DEFAULT 0 COMMENT '加购数',
    favorites INT DEFAULT 0 COMMENT '收藏数',
    hot_score DOUBLE DEFAULT 0 COMMENT '热度评分',
    rank_num INT DEFAULT 0 COMMENT '排名',
    time_window VARCHAR(20) DEFAULT '1h' COMMENT '统计时间窗口',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_product_id (product_id),
    INDEX idx_hot_score (hot_score DESC),
    INDEX idx_time_window (time_window)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实时热榜表';
