import pymysql
import random
from datetime import datetime, timedelta

conn = pymysql.connect(host='localhost', port=3306, user='root', password='root', database='ecommerce', charset='utf8mb4')
cursor = conn.cursor()

cursor.execute("""
CREATE TABLE IF NOT EXISTS t_user_behavior_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    event_type VARCHAR(32),
    product_id BIGINT,
    category VARCHAR(64),
    keyword VARCHAR(255),
    duration INT,
    timestamp BIGINT,
    INDEX idx_user_id (user_id),
    INDEX idx_product_id (product_id),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
""")

cursor.execute("""
CREATE TABLE IF NOT EXISTS t_flash_sale (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT,
    original_price DECIMAL(10,2),
    sale_price DECIMAL(10,2),
    total_stock INT,
    remain_stock INT,
    start_time DATETIME,
    end_time DATETIME,
    title VARCHAR(255),
    status INT DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
""")

cursor.execute("SELECT id, category, price FROM t_product WHERE deleted = 0")
products = cursor.fetchall()

if products:
    event_types = ['CLICK', 'BROWSE', 'CART', 'PURCHASE', 'SEARCH']
    user_ids = list(range(1, 21))

    behavior_count = 0
    for _ in range(2000):
        user_id = random.choice(user_ids)
        product = random.choice(products)
        event_type = random.choice(event_types)

        if event_type == 'SEARCH':
            product_id = None
            category = None
            keyword = random.choice(['iPhone', '华为', 'MacBook', '耳机', 'PS5', '无人机', '平板', '手表'])
        else:
            product_id = product[0]
            category = product[1]
            keyword = None

        duration = random.randint(5, 300) if event_type == 'BROWSE' else None
        ts = int((datetime.now() - timedelta(days=random.randint(0, 7), hours=random.randint(0, 23), minutes=random.randint(0, 59))).timestamp() * 1000)

        cursor.execute(
            "INSERT INTO t_user_behavior_log (user_id, event_type, product_id, category, keyword, duration, timestamp) VALUES (%s, %s, %s, %s, %s, %s, %s)",
            (user_id, event_type, product_id, category, keyword, duration, ts)
        )
        behavior_count += 1

    print(f"Generated {behavior_count} behavior logs")

cursor.execute("DELETE FROM t_flash_sale")

now = datetime.now()
flash_sale_data = [
    ("限时秒杀·手机专场", now - timedelta(hours=1), now + timedelta(hours=5)),
    ("午间特惠·数码好物", now - timedelta(minutes=30), now + timedelta(hours=3, minutes=30)),
    ("晚间抢购·耳机狂欢", now + timedelta(hours=2), now + timedelta(hours=8)),
    ("明日预告·电脑盛典", now + timedelta(hours=10), now + timedelta(hours=22)),
]

sale_count = 0
for title, start, end in flash_sale_data:
    available = [p for p in products if p[1] in ['手机', '电脑', '耳机', '平板', '穿戴', '游戏']]
    if not available:
        continue
    sale_products = random.sample(available, min(4, len(available)))
    for product in sale_products:
        product_id, category, price = product
        original_price = float(price)
        sale_price = round(original_price * random.uniform(0.3, 0.7), 2)
        total_stock = random.randint(50, 200)
        remain_stock = random.randint(int(total_stock * 0.3), total_stock)

        cursor.execute(
            "INSERT INTO t_flash_sale (product_id, original_price, sale_price, total_stock, remain_stock, start_time, end_time, title, status) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)",
            (product_id, original_price, sale_price, total_stock, remain_stock, start, end, title, 1)
        )
        sale_count += 1

print(f"Generated {sale_count} flash sale items")

conn.commit()
cursor.close()
conn.close()
print("Done!")
