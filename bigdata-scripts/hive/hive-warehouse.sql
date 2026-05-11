-- Hive 数仓建表脚本
-- 分层: ODS(原始) -> DWD(清洗) -> DWS(汇总) -> ADS(应用)

-- ============ ODS 层 (原始数据层) ============
CREATE DATABASE IF NOT EXISTS ods;
USE ods;

CREATE EXTERNAL TABLE IF NOT EXISTS ods_user_behavior (
    user_id BIGINT,
    event_type STRING,
    product_id BIGINT,
    category STRING,
    keyword STRING,
    duration INT,
    ts BIGINT,
    session_id STRING
)
PARTITIONED BY (dt STRING)
ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t'
STORED AS TEXTFILE
LOCATION '/data/ecommerce/user_behavior';

-- ============ DWD 层 (明细数据层) ============
CREATE DATABASE IF NOT EXISTS dwd;
USE dwd;

CREATE TABLE IF NOT EXISTS dwd_user_behavior (
    user_id BIGINT,
    event_type STRING,
    product_id BIGINT,
    category STRING,
    keyword STRING,
    duration INT,
    session_id STRING,
    event_time TIMESTAMP
)
PARTITIONED BY (dt STRING)
STORED AS ORC;

INSERT OVERWRITE TABLE dwd.dwd_user_behavior PARTITION(dt='${dt}')
SELECT
    user_id,
    event_type,
    product_id,
    category,
    keyword,
    duration,
    session_id,
    FROM_UNIXTIME(ts/1000) AS event_time
FROM ods.ods_user_behavior
WHERE dt='${dt}'
  AND user_id IS NOT NULL
  AND event_type IS NOT NULL;

-- ============ DWS 层 (汇总数据层) ============
CREATE DATABASE IF NOT EXISTS dws;
USE dws;

-- 每日用户行为汇总
CREATE TABLE IF NOT EXISTS dws_user_daily (
    user_id BIGINT,
    click_count INT,
    browse_count INT,
    search_count INT,
    cart_count INT,
    purchase_count INT,
    browse_duration INT
)
PARTITIONED BY (dt STRING)
STORED AS ORC;

INSERT OVERWRITE TABLE dws.dws_user_daily PARTITION(dt='${dt}')
SELECT
    user_id,
    SUM(CASE WHEN event_type='CLICK' THEN 1 ELSE 0 END) AS click_count,
    SUM(CASE WHEN event_type='BROWSE' THEN 1 ELSE 0 END) AS browse_count,
    SUM(CASE WHEN event_type='SEARCH' THEN 1 ELSE 0 END) AS search_count,
    SUM(CASE WHEN event_type='CART' THEN 1 ELSE 0 END) AS cart_count,
    SUM(CASE WHEN event_type='PURCHASE' THEN 1 ELSE 0 END) AS purchase_count,
    SUM(CASE WHEN event_type='BROWSE' THEN COALESCE(duration,0) ELSE 0 END) AS browse_duration
FROM dwd.dwd_user_behavior
WHERE dt='${dt}'
GROUP BY user_id;

-- 每日商品热度汇总
CREATE TABLE IF NOT EXISTS dws_product_daily (
    product_id BIGINT,
    category STRING,
    click_count INT,
    browse_count INT,
    cart_count INT,
    purchase_count INT,
    avg_browse_duration INT
)
PARTITIONED BY (dt STRING)
STORED AS ORC;

INSERT OVERWRITE TABLE dws.dws_product_daily PARTITION(dt='${dt}')
SELECT
    product_id,
    category,
    SUM(CASE WHEN event_type='CLICK' THEN 1 ELSE 0 END),
    SUM(CASE WHEN event_type='BROWSE' THEN 1 ELSE 0 END),
    SUM(CASE WHEN event_type='CART' THEN 1 ELSE 0 END),
    SUM(CASE WHEN event_type='PURCHASE' THEN 1 ELSE 0 END),
    CAST(AVG(CASE WHEN event_type='BROWSE' THEN duration END) AS INT)
FROM dwd.dwd_user_behavior
WHERE dt='${dt}' AND product_id IS NOT NULL
GROUP BY product_id, category;

-- ============ ADS 层 (应用数据层) ============
CREATE DATABASE IF NOT EXISTS ads;
USE ads;

-- 热门商品排行
CREATE TABLE IF NOT EXISTS ads_hot_products (
    product_id BIGINT,
    category STRING,
    hot_score DOUBLE,
    rank_num INT
)
PARTITIONED BY (dt STRING)
STORED AS ORC;

INSERT OVERWRITE TABLE ads.ads_hot_products PARTITION(dt='${dt}')
SELECT
    product_id,
    category,
    click_count * 1.0 + browse_count * 0.5 + cart_count * 2.0 + purchase_count * 5.0 AS hot_score,
    ROW_NUMBER() OVER (ORDER BY click_count * 1.0 + browse_count * 0.5 + cart_count * 2.0 + purchase_count * 5.0 DESC) AS rank_num
FROM dws.dws_product_daily
WHERE dt='${dt}';

-- 用户画像
CREATE TABLE IF NOT EXISTS ads_user_profile (
    user_id BIGINT,
    prefer_category STRING,
    active_level STRING,
    purchase_power STRING
)
PARTITIONED BY (dt STRING)
STORED AS ORC;

INSERT OVERWRITE TABLE ads.ads_user_profile PARTITION(dt='${dt}')
SELECT
    user_id,
    category AS prefer_category,
    CASE
        WHEN click_count > 50 THEN '高活跃'
        WHEN click_count > 10 THEN '中活跃'
        ELSE '低活跃'
    END AS active_level,
    CASE
        WHEN purchase_count > 5 THEN '高消费'
        WHEN purchase_count > 1 THEN '中消费'
        ELSE '低消费'
    END AS purchase_power
FROM (
    SELECT
        d.user_id,
        p.category,
        d.click_count,
        d.purchase_count,
        ROW_NUMBER() OVER (PARTITION BY d.user_id ORDER BY SUM(CASE WHEN dwd.event_type='CLICK' THEN 1 ELSE 0 END) DESC) AS rn
    FROM dws.dws_user_daily d
    LEFT JOIN dwd.dwd_user_behavior dwd ON d.user_id = dwd.user_id AND dwd.dt='${dt}'
    LEFT JOIN (SELECT DISTINCT product_id, category FROM dws.dws_product_daily WHERE dt='${dt}') p
        ON dwd.product_id = p.product_id
    WHERE d.dt='${dt}'
    GROUP BY d.user_id, p.category, d.click_count, d.purchase_count
) t
WHERE rn = 1;
