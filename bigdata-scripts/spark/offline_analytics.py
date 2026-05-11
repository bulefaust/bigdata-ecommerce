# Spark 离线分析脚本
# 在虚拟机上用 spark-submit 提交

from pyspark.sql import SparkSession
from pyspark.sql.functions import *
from pyspark.sql.window import Window

spark = SparkSession.builder \
    .appName("ECommerceOfflineAnalytics") \
    .enableHiveSupport() \
    .getOrCreate()

dt = spark.conf.get("spark.analysis.dt", "20240101")

# ========== 1. 漏斗分析 ==========
# 浏览 -> 加购 -> 下单 -> 支付 的转化率
funnel = spark.sql(f"""
    SELECT
        COUNT(DISTINCT CASE WHEN event_type='BROWSE' THEN user_id END) AS browse_users,
        COUNT(DISTINCT CASE WHEN event_type='CART' THEN user_id END) AS cart_users,
        COUNT(DISTINCT CASE WHEN event_type='PURCHASE' THEN user_id END) AS purchase_users
    FROM dwd.dwd_user_behavior
    WHERE dt='{dt}'
""")
funnel.show()

# ========== 2. 商品推荐 (协同过滤) ==========
# 基于用户-商品交互矩阵，计算商品相似度
user_product = spark.sql(f"""
    SELECT
        user_id,
        product_id,
        SUM(CASE WHEN event_type='CLICK' THEN 1 ELSE 0 END) * 1 +
        SUM(CASE WHEN event_type='BROWSE' THEN 1 ELSE 0 END) * 0.5 +
        SUM(CASE WHEN event_type='CART' THEN 2 ELSE 0 END) +
        SUM(CASE WHEN event_type='PURCHASE' THEN 5 ELSE 0 END) AS score
    FROM dwd.dwd_user_behavior
    WHERE dt='{dt}' AND product_id IS NOT NULL
    GROUP BY user_id, product_id
""")
user_product.show()

# ========== 3. 销量预测特征 ==========
# 统计近7天/30天销量趋势
sales_trend = spark.sql(f"""
    SELECT
        product_id,
        category,
        COUNT(*) AS daily_sales,
        SUM(CASE WHEN dt >= date_sub('{dt}', 7) THEN 1 ELSE 0 END) AS sales_7d,
        SUM(CASE WHEN dt >= date_sub('{dt}', 30) THEN 1 ELSE 0 END) AS sales_30d
    FROM dwd.dwd_user_behavior
    WHERE event_type='PURCHASE' AND product_id IS NOT NULL
    GROUP BY product_id, category
""")
sales_trend.show()

# ========== 4. 评论情感分析 (如果有评论数据) ==========
# 此处预留，后续接入NLP模型

spark.stop()
