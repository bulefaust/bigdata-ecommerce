# Kafka Topic 创建脚本
# 在虚拟机上执行

# 创建用户行为主题
kafka-topics.sh --create \
  --bootstrap-server localhost:9092 \
  --replication-factor 1 \
  --partitions 3 \
  --topic user-behavior

# 创建实时热榜主题
kafka-topics.sh --create \
  --bootstrap-server localhost:9092 \
  --replication-factor 1 \
  --partitions 3 \
  --topic hot-products

# 创建异常告警主题
kafka-topics.sh --create \
  --bootstrap-server localhost:9092 \
  --replication-factor 1 \
  --partitions 1 \
  --topic alert-events

# 查看所有主题
kafka-topics.sh --list --bootstrap-server localhost:9092

# 消费测试
kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic user-behavior \
  --from-beginning
