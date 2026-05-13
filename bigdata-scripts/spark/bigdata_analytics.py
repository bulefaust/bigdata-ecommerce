"""
电商平台大数据分析脚本
使用 PySpark 进行离线数据分析

功能模块:
1. 用户行为分析 - DAU、留存率、用户活跃时段
2. 转化漏斗分析 - 浏览->点击->加购->下单->支付
3. 销售预测 - 基于历史数据的销量预测
4. 实时热榜计算 - Kafka流式处理热门商品

依赖:
pip install pyspark findspark

运行:
spark-submit --master local[*] offline_analytics.py
"""

from pyspark.sql import SparkSession
from pyspark.sql.functions import *
from pyspark.sql.types import *
from pyspark.sql.window import Window
import findspark
from datetime import datetime, timedelta

findspark.init()

spark = SparkSession.builder \
    .appName("ECommerceBigDataAnalytics") \
    .config("spark.sql.shuffle.partitions", "8") \
    .getOrCreate()

spark.sparkContext.setLogLevel("WARN")

KAFKA_BOOTSTRAP = "192.168.100.110:9092"
MYSQL_URL = "jdbc:mysql://localhost:3306/ecommerce?useUnicode=true&characterEncoding=utf8"
MYSQL_PROPS = {"user": "root", "password": "root", "driver": "com.mysql.cj.jdbc.Driver"}

def load_behavior_data():
    """从MySQL加载用户行为数据"""
    df = spark.read.jdbc(MYSQL_URL, "t_user_behavior_log", properties=MYSQL_PROPS)
    return df

def analyze_dau(df):
    """日活跃用户(DAU)分析"""
    print("\n" + "="*60)
    print("📊 日活跃用户(DAU)分析")
    print("="*60)
    
    today = datetime.now()
    start_date = (today - timedelta(days=30)).strftime("%Y-%m-%d")
    end_date = today.strftime("%Y-%m-%d")
    
    dau_df = df.filter(
        (col("create_time") >= start_date) & 
        (col("create_time") < end_date)
    ).groupBy(date_format("create_time", "yyyy-MM-dd").alias("date")) \
     .agg(countDistinct("user_id").alias("dau")) \
     .orderBy("date")
    
    dau_df.show(30, truncate=False)
    
    avg_dau = dau_df.agg(avg("dau")).collect()[0][0]
    max_dau = dau_df.agg(max("dau")).collect()[0][0]
    min_dau = dau_df.agg(min("dau")).collect()[0][0]
    
    print(f"\n📈 DAU统计:")
    print(f"   平均日活: {avg_dau:.0f}")
    print(f"   最高日活: {max_dau:.0f}")
    print(f"   最低日活: {min_dau:.0f}")
    
    return dau_df

def analyze_hourly_distribution(df):
    """用户活跃时段分析"""
    print("\n" + "="*60)
    print("⏰ 用户活跃时段分析")
    print("="*60)
    
    hourly_df = df.groupBy(hour("create_time").alias("hour")) \
                  .agg(count("*").alias("behavior_count")) \
                  .orderBy("hour")
    
    hourly_df.show(24, truncate=False)
    
    peak_hour = hourly_df.orderBy(desc("behavior_count")).first()
    print(f"\n🏆 活跃高峰: {peak_hour['hour']}点 (行为数: {peak_hour['behavior_count']})")
    
    return hourly_df

def analyze_retention(df):
    """用户留存分析"""
    print("\n" + "="*60)
    print("📉 用户留存分析")
    print("="*60)
    
    user_first_login = df.groupBy("user_id") \
                         .agg(min("create_time").alias("first_login")) \
                         .withColumn("first_date", date_format("first_login", "yyyy-MM-dd"))
    
    today = datetime.now().strftime("%Y-%m-%d")
    
    retention_data = []
    for days in [1, 3, 7, 14, 30]:
        check_date = (datetime.now() - timedelta(days=days)).strftime("%Y-%m-%d")
        
        retained_users = user_first_login.filter(
            col("first_date") == check_date
        ).join(
            df.filter(date_format("create_time", "yyyy-MM-dd") == today),
            "user_id"
        ).count()
        
        new_users = user_first_login.filter(col("first_date") == check_date).count()
        
        retention_rate = (retained_users / new_users * 100) if new_users > 0 else 0
        retention_data.append({
            "days": f"D{days}",
            "new_users": new_users,
            "retained": retained_users,
            "retention_rate": f"{retention_rate:.2f}%"
        })
    
    for r in retention_data:
        print(f"   {r['days']}留存: 新用户{r['new_users']}, 回访{r['retained']}, 留存率{r['retention_rate']}")
    
    return retention_data

def analyze_conversion_funnel(df):
    """转化漏斗分析"""
    print("\n" + "="*60)
    print("🔄 转化漏斗分析")
    print("="*60)
    
    today = datetime.now()
    start_date = (today - timedelta(days=30)).strftime("%Y-%m-%d")
    end_date = today.strftime("%Y-%m-%d")
    
    recent_df = df.filter(
        (col("create_time") >= start_date) & 
        (col("create_time") < end_date)
    )
    
    funnel_stages = ["VIEW", "CLICK", "CART", "PURCHASE", "PAY"]
    funnel_data = []
    
    prev_count = None
    for stage in funnel_stages:
        stage_df = recent_df.filter(col("event_type") == stage)
        
        if stage == "VIEW":
            count = stage_df.select(countDistinct("user_id")).collect()[0][0]
        else:
            count = stage_df.count()
        
        funnel_data.append({
            "stage": stage,
            "stage_name": {"VIEW": "浏览", "CLICK": "点击", "CART": "加购", "PURCHASE": "下单", "PAY": "支付"}[stage],
            "count": count
        })
        
        if prev_count:
            conversion_rate = (count / prev_count * 100) if prev_count > 0 else 0
            print(f"   {funnel_data[-1]['stage_name']}: {count} (转化率: {conversion_rate:.1f}%)")
        else:
            print(f"   {funnel_data[-1]['stage_name']}: {count}")
        
        prev_count = count
    
    return funnel_data

def analyze_sales_forecast(df):
    """销售预测分析"""
    print("\n" + "="*60)
    print("📈 销售预测分析")
    print("="*60)
    
    today = datetime.now()
    start_date = (today - timedelta(days=60)).strftime("%Y-%m-%d")
    end_date = today.strftime("%Y-%m-%d")
    
    daily_sales = df.filter(
        (col("create_time") >= start_date) & 
        (col("create_time") < end_date) &
        (col("event_type") == "PURCHASE")
    ).groupBy(date_format("create_time", "yyyy-MM-dd").alias("date")) \
     .agg(count("*").alias("orders")) \
     .orderBy("date") \
     .withColumn("type", lit("historical"))
    
    historical_data = daily_sales.collect()
    
    if len(historical_data) < 7:
        print("   数据不足，无法进行预测")
        return None
    
    recent_7_days = [h["orders"] for h in historical_data[-7:]]
    avg_daily = sum(recent_7_days) / len(recent_7_days)
    
    first_half = historical_data[:len(historical_data)//2]
    second_half = historical_data[len(historical_data)//2:]
    
    first_avg = sum([h["orders"] for h in first_half]) / len(first_half)
    second_avg = sum([h["orders"] for h in second_half]) / len(second_half)
    
    growth_rate = ((second_avg - first_avg) / first_avg * 100) if first_avg > 0 else 0
    
    print(f"\n   历史数据: {len(historical_data)}天")
    print(f"   日均订单: {avg_daily:.1f}")
    print(f"   增长趋势: {'↑ 上升' if growth_rate > 0 else '↓ 下降'} {abs(growth_rate):.1f}%")
    
    print("\n   📊 未来7天预测:")
    forecast_data = []
    for i in range(1, 8):
        predicted = avg_daily * ((1 + growth_rate/100) ** i)
        forecast_date = (today + timedelta(days=i)).strftime("%Y-%m-%d")
        forecast_data.append({
            "date": forecast_date,
            "predicted": int(predicted),
            "type": "forecast"
        })
        print(f"      {forecast_date}: 预测 {int(predicted)} 订单")
    
    return {"historical": historical_data, "forecast": forecast_data, "growth_rate": growth_rate}

def analyze_hot_products(df):
    """热门商品分析"""
    print("\n" + "="*60)
    print("🔥 热门商品分析")
    print("="*60)
    
    today = datetime.now()
    start_date = (today - timedelta(days=7)).strftime("%Y-%m-%d")
    
    hot_df = df.filter(col("create_time") >= start_date) \
               .groupBy("product_id", "category") \
               .agg(
                   sum(when(col("event_type") == "VIEW", 1).otherwise(0)).alias("views"),
                   sum(when(col("event_type") == "CLICK", 1).otherwise(0)).alias("clicks"),
                   sum(when(col("event_type") == "CART", 1).otherwise(0)).alias("carts"),
                   sum(when(col("event_type") == "PURCHASE", 1).otherwise(0)).alias("purchases")
               ) \
               .withColumn("hot_score", 
                   col("clicks") * 1.0 + 
                   col("carts") * 3.0 + 
                   col("purchases") * 5.0 +
                   col("views") * 0.5
               ) \
               .orderBy(desc("hot_score")) \
               .limit(10)
    
    window = Window.orderBy(desc("hot_score"))
    hot_df = hot_df.withColumn("rank", row_number().over(window))
    
    print("\n   排名 | 商品ID | 分类 | 浏览 | 点击 | 加购 | 购买 | 热度分")
    print("   " + "-"*65)
    
    for row in hot_df.collect():
        print(f"   #{row['rank']:2d}  | {row['product_id']:6d} | {row['category']:6s} | {row['views']:5d} | {row['clicks']:4d} | {row['carts']:4d} | {row['purchases']:3d} | {row['hot_score']:.1f}")
    
    return hot_df

def save_to_mysql(df, table_name, mode="overwrite"):
    """保存结果到MySQL"""
    df.write.jdbc(MYSQL_URL, table_name, mode=mode, properties=MYSQL_PROPS)
    print(f"\n✅ 结果已保存到MySQL表: {table_name}")

def main():
    print("\n" + "="*60)
    print("🚀 电商大数据分析系统")
    print("="*60)
    
    try:
        df = load_behavior_data()
        print(f"\n📦 已加载 {df.count()} 条行为数据")
        
        analyze_dau(df)
        
        analyze_hourly_distribution(df)
        
        analyze_retention(df)
        
        funnel = analyze_conversion_funnel(df)
        
        forecast = analyze_sales_forecast(df)
        
        hot = analyze_hot_products(df)
        
        print("\n" + "="*60)
        print("✨ 分析完成!")
        print("="*60)
        
    except Exception as e:
        print(f"\n❌ 分析出错: {str(e)}")
        import traceback
        traceback.print_exc()
    
    finally:
        spark.stop()

if __name__ == "__main__":
    main()
