# Spark Structured Streaming 实时分析
# 替代 Flink 的方案

from pyspark.sql import SparkSession
from pyspark.sql.functions import *
from pyspark.sql.types import *

spark = SparkSession.builder \
    .appName("ECommerceRealtimeAnalytics") \
    .getOrCreate()

# ========== 1. 实时热榜 ==========
# 从Kafka读取用户行为，5分钟窗口统计热门商品

kafka_df = spark.readStream \
    .format("kafka") \
    .option("kafka.bootstrap.servers", "localhost:9092") \
    .option("subscribe", "user-behavior") \
    .option("startingOffsets", "latest") \
    .load()

schema = StructType([
    StructField("userId", LongType()),
    StructField("eventType", StringType()),
    StructField("productId", LongType()),
    StructField("category", StringType()),
    StructField("keyword", StringType()),
    StructField("duration", IntegerType()),
    StructField("timestamp", LongType()),
    StructField("sessionId", StringType()),
])

behavior_df = kafka_df.select(
    from_json(col("value").cast("string"), schema).alias("data")
).select("data.*")

# 5分钟滚动窗口 - 实时热榜
hot_products = behavior_df \
    .filter(col("eventType").isin("CLICK", "PURCHASE", "CART")) \
    .withWatermark("timestamp", "10 minutes") \
    .groupBy(
        window(col("timestamp"), "5 minutes"),
        col("productId"),
        col("category")
    ) \
    .agg(
        count("*").alias("action_count"),
        sum(when(col("eventType") == "CLICK", 1).otherwise(0)).alias("clicks"),
        sum(when(col("eventType") == "PURCHASE", 1).otherwise(0)).alias("purchases")
    ) \
    .withColumn("hot_score",
        col("clicks") * 1.0 + col("purchases") * 5.0
    )

# 写入MySQL (需要JDBC) 或 Console (调试用)
def write_to_mysql(batch_df, batch_id):
    batch_df.write \
        .format("jdbc") \
        .option("url", "jdbc:mysql://192.168.100.100:3306/ecommerce") \
        .option("dbtable", "t_realtime_hot") \
        .option("user", "root") \
        .option("password", "root") \
        .mode("overwrite") \
        .save()

hot_products.writeStream \
    .foreachBatch(write_to_mysql) \
    .outputMode("update") \
    .trigger(processingTime="1 minute") \
    .start()

# ========== 2. 实时异常检测 ==========
# 同一用户1分钟内点击超过50次 -> 疑似刷单
suspicious_users = behavior_df \
    .filter(col("eventType") == "CLICK") \
    .withWatermark("timestamp", "2 minutes") \
    .groupBy(
        window(col("timestamp"), "1 minute"),
        col("userId")
    ) \
    .agg(count("*").alias("click_count")) \
    .filter(col("click_count") > 50)

suspicious_users.writeStream \
    .format("console") \
    .outputMode("update") \
    .trigger(processingTime="30 seconds") \
    .start()

spark.streams.awaitAnyTermination()
