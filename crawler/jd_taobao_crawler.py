import requests
from bs4 import BeautifulSoup
import pymysql
import time
import random
import json
import re
import os

DB_CONFIG = {
    'host': 'localhost',
    'port': 3306,
    'user': 'root',
    'password': 'root',
    'database': 'ecommerce',
    'charset': 'utf8mb4'
}

JD_HEADERS = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
    'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8',
    'Accept-Language': 'zh-CN,zh;q=0.9,en;q=0.8',
    'Accept-Encoding': 'gzip, deflate',
    'Referer': 'https://www.jd.com/',
    'Connection': 'keep-alive',
}

CATEGORY_KEYWORDS = {
    '手机': ['iPhone', '华为手机', '小米手机', 'OPPO手机', 'vivo手机', '三星手机', '荣耀手机', '一加手机', '魅族手机', 'realme手机'],
    '电脑': ['笔记本电脑', '游戏本', '轻薄本', '台式机', 'MacBook', '联想电脑', '戴尔电脑', '华硕电脑', '惠普电脑', '机械革命'],
    '耳机': ['蓝牙耳机', '降噪耳机', '头戴式耳机', 'AirPods', '索尼耳机', '漫步者耳机', 'BOSE耳机', '铁三角耳机', 'JBL耳机', '森海塞尔'],
    '平板': ['iPad', '安卓平板', '华为平板', '小米平板', '三星平板', '联想平板', '荣耀平板', 'Surface', '学习平板', '绘画平板'],
    '穿戴': ['智能手表', 'Apple Watch', '华为手表', '小米手环', '运动手环', '智能手环', '儿童手表', 'Garmin', '三星手表', 'OPPO手表'],
    '游戏': ['PS5', 'Switch', 'Xbox', '游戏手柄', '电竞鼠标', '机械键盘', '游戏耳机', '电竞椅', '游戏显示器', 'Steam Deck'],
    '摄影': ['单反相机', '微单相机', '拍立得', '运动相机', '无人机', '镜头', '三脚架', '相机包', '补光灯', 'GoPro'],
    '配件': ['手机壳', '充电器', '数据线', '充电宝', '钢化膜', '手机支架', '蓝牙音箱', '鼠标垫', 'U盘', '移动硬盘'],
}

MOCK_PRODUCTS = {
    '手机': [
        ('iPhone 15 Pro Max 256GB', 9999.00, 'Apple iPhone 15 Pro Max 钛金属设计 A17 Pro芯片'),
        ('iPhone 15 Pro 128GB', 7999.00, 'Apple iPhone 15 Pro 钛金属 超视网膜XDR显示屏'),
        ('iPhone 15 128GB', 5999.00, 'Apple iPhone 15 融色玻璃设计 A16仿生芯片'),
        ('iPhone 14 Pro Max 256GB', 8499.00, 'Apple iPhone 14 Pro Max 灵动岛 A16芯片'),
        ('华为 Mate 60 Pro 512GB', 7999.00, '华为Mate60 Pro 昆仑玻璃 鸿蒙4.0 卫星通话'),
        ('华为 Mate 60 256GB', 5999.00, '华为Mate60 鸿蒙操作系统 超光变摄像头'),
        ('华为 P60 Pro 256GB', 5988.00, '华为P60 Pro 超聚光夜视长杆 昆仑玻璃'),
        ('华为 nova 12 Ultra', 3999.00, '华为nova12 Ultra 鸿蒙智慧通信 6000万追焦双摄'),
        ('小米14 Ultra 16GB+512GB', 6499.00, '小米14 Ultra 徕卡光学 Summilux 骁龙8Gen3'),
        ('小米14 Pro 12GB+256GB', 4999.00, '小米14 Pro 徕卡专业光学 骁龙8Gen3 2K屏幕'),
        ('小米14 12GB+256GB', 3999.00, '小米14 徕卡光学 骁龙8Gen3 小屏旗舰'),
        ('小米13 Ultra 16GB+512GB', 5499.00, '小米13 Ultra 徕卡一英寸可变光圈 影像旗舰'),
        ('OPPO Find X7 Ultra', 5999.00, 'OPPO Find X7 Ultra 哈苏影像 双潜望长焦'),
        ('OPPO Reno11 Pro+', 3499.00, 'OPPO Reno11 Pro+ 超级闪充 人像旗舰'),
        ('vivo X100 Pro 16GB+256GB', 4999.00, 'vivo X100 Pro 蔡司APO超级长焦 天玑9300'),
        ('vivo iQOO 12 Pro', 4399.00, 'iQOO 12 Pro 电竞旗舰 骁龙8Gen3 2K E7屏'),
        ('三星 Galaxy S24 Ultra', 9699.00, '三星S24 Ultra AI智能手机 钛金属 S Pen'),
        ('三星 Galaxy Z Fold5', 12999.00, '三星折叠屏手机 Z Fold5 大屏沉浸体验'),
        ('荣耀 Magic6 Pro', 4999.00, '荣耀Magic6 Pro 鸿燕通信 鹰眼相机'),
        ('荣耀 100 Pro', 2999.00, '荣耀100 Pro 单反级写真相机 骁龙8Gen2'),
        ('一加 12 16GB+512GB', 4599.00, '一加12 哈苏影像 2K东方屏 骁龙8Gen3'),
        ('一加 Ace 3', 2599.00, '一加Ace 3 骁龙8Gen2 超长续航 旗舰质感'),
        ('魅族 21 PRO', 4299.00, '魅族21 PRO AI旗舰 无界设计 骁龙8Gen3'),
        ('realme GT5 Pro', 3299.00, 'realme真我GT5 Pro 骁龙8Gen3 旗舰影像'),
        ('Redmi K70 Pro', 2999.00, 'Redmi红米K70 Pro 骁龙8Gen3 2K中国屏'),
        ('Redmi Note 13 Pro+', 1899.00, 'Redmi Note13 Pro+ 2亿像素 120W秒充'),
        ('摩托罗拉 edge 40 Pro', 3299.00, 'motorola edge 40 Pro 骁龙8Gen2 165Hz高刷'),
        ('努比亚 Z60 Ultra', 3999.00, '努比亚Z60 Ultra 屏下摄像 骁龙8Gen3'),
        ('索尼 Xperia 1 V', 7999.00, '索尼Xperia 1 V 4K OLED屏 专业影像手机'),
        ('ROG 游戏手机8 Pro', 5999.00, 'ROG游戏手机8 Pro 骁龙8Gen3 165Hz电竞屏'),
    ],
    '电脑': [
        ('MacBook Pro 16 M3 Max', 27999.00, 'Apple MacBook Pro 16英寸 M3 Max芯片 36GB内存'),
        ('MacBook Pro 14 M3 Pro', 16999.00, 'Apple MacBook Pro 14英寸 M3 Pro芯片 18GB内存'),
        ('MacBook Air 15 M3', 10499.00, 'Apple MacBook Air 15英寸 M3芯片 轻薄本'),
        ('MacBook Air 13 M3', 8999.00, 'Apple MacBook Air 13英寸 M3芯片 午夜色'),
        ('联想 ThinkPad X1 Carbon', 11999.00, 'ThinkPad X1 Carbon 14英寸 i7-1365U 16G'),
        ('联想 小新Pro 16', 5999.00, '联想小新Pro16 锐龙7 32G 1TB 2.5K屏'),
        ('联想 拯救者 Y9000P', 8999.00, '联想拯救者Y9000P i9-14900HX RTX4060'),
        ('联想 拯救者 R9000P', 7999.00, '联想拯救者R9000P R9-7945HX RTX4060'),
        ('戴尔 XPS 15', 12999.00, '戴尔XPS 15 i7-13700H OLED触控屏'),
        ('戴尔 游匣 G16', 7499.00, '戴尔游匣G16 i7-13650HX RTX4060'),
        ('华硕 天选5 Pro', 7499.00, '华硕天选5 Pro i9-14900HX RTX4060 2.5K'),
        ('华硕 灵耀14', 5999.00, '华硕灵耀14 OLED屏 i7-1360P 轻薄本'),
        ('惠普 暗影精灵9', 6999.00, '惠普暗影精灵9 i7-13700HX RTX4060'),
        ('惠普 战66六代', 4499.00, '惠普战66六代 i5-1340P 16G 商务本'),
        ('机械革命 蛟龙16S', 5999.00, '机械革命蛟龙16S R7-7840H RTX4060'),
        ('机械革命 无界16 Pro', 4999.00, '机械革命无界16Pro i7-13700H RTX4050'),
        ('神舟 战神Z8', 5499.00, '神舟战神Z8 i7-13620H RTX4060'),
        ('华为 MateBook X Pro', 11999.00, '华为MateBook X Pro 酷睿Ultra 3.1K触控屏'),
        ('华为 MateBook 14', 5999.00, '华为MateBook 14 i5-1340P 2K触控屏'),
        ('小米 RedmiBook Pro 15', 4299.00, 'RedmiBook Pro15 锐龙7 3.2K 120Hz'),
        ('微软 Surface Pro 9', 8988.00, '微软Surface Pro 9 i5 8G 256G 二合一'),
        ('宏碁 掠夺者擎Neo', 6999.00, '宏碁掠夺者擎Neo i7-13700HX RTX4060'),
        ('雷蛇 灵刃14', 15999.00, 'Razer Blade 14 i7-13700H RTX4070 电竞本'),
        ('微星 强袭GE78', 12999.00, '微星强袭GE78 i9-13980HX RTX4080'),
        ('苹果 iMac 24 M3', 10999.00, 'Apple iMac 24英寸 M3芯片 8核GPU 一体机'),
    ],
    '耳机': [
        ('Apple AirPods Pro 2', 1899.00, 'Apple AirPods Pro 第二代 主动降噪 MagSafe充电盒'),
        ('Apple AirPods 3', 1399.00, 'Apple AirPods 第三代 空间音频 抗汗抗水'),
        ('Apple AirPods Max', 3999.00, 'Apple AirPods Max 高保真降噪头戴式耳机'),
        ('索尼 WH-1000XM5', 2299.00, '索尼头戴式降噪耳机 WH-1000XM5 30小时续航'),
        ('索尼 WF-1000XM5', 1999.00, '索尼真无线降噪耳机 WF-1000XM5 小巧轻便'),
        ('索尼 LinkBuds S', 999.00, '索尼LinkBuds S 舒适佩戴 智能降噪'),
        ('漫步者 W820NB', 399.00, '漫步者头戴式主动降噪耳机 49小时续航'),
        ('漫步者 NeoBuds Pro 2', 599.00, '漫步者真无线降噪耳机 Hi-Res认证'),
        ('漫步者 LolliPods Pro 2', 349.00, '漫步者LolliPods Pro2 主动降噪 低延迟'),
        ('BOSE QuietComfort Ultra', 2999.00, 'BOSE消噪耳机Ultra 沉浸式空间音频'),
        ('BOSE QuietComfort 45', 1999.00, 'BOSE QC45 头戴式降噪耳机 Aware模式'),
        ('铁三角 ATH-M50x', 1299.00, '铁三角专业监听耳机 45mm大口径驱动'),
        ('JBL 音乐战鼓3', 899.00, 'JBL头戴式无线蓝牙耳机 纯净低音'),
        ('JBL TUNE 230NC', 499.00, 'JBL真无线降噪耳机 主动降噪 智能环境音'),
        ('森海塞尔 Momentum 4', 2399.00, '森海塞尔Momentum 4 60小时续航 自适应降噪'),
        ('森海塞尔 HD 660S2', 3299.00, '森海塞尔HD660S2 开放式高保真耳机'),
        ('华为 FreeBuds Pro 3', 1299.00, '华为FreeBuds Pro 3 超感知原声双单元'),
        ('华为 FreeBuds 5', 899.00, '华为FreeBuds 5 水滴造型 超磁感澎湃单元'),
        ('小米 Buds 4 Pro', 699.00, '小米Buds 4 Pro 48dB主动降噪 空间音频'),
        ('OPPO Enco X2', 799.00, 'OPPO Enco X2 丹拿联合调音 45dB降噪'),
        ('vivo TWS 4', 499.00, 'vivo TWS4 55dB深海降噪 45小时续航'),
        ('B&O Beoplay EX', 2999.00, 'B&O铂傲真无线耳机 丹麦声学 主动降噪'),
        ('三星 Galaxy Buds2 Pro', 999.00, '三星Galaxy Buds2 Pro 24bit高保真音频'),
        ('万魔 EVO', 599.00, '1MORE万魔EVO 空间音频 主动降噪'),
        ('QCY ArcBuds HT07', 249.00, 'QCY头戴式降噪耳机 65dB主动降噪'),
    ],
    '平板': [
        ('iPad Pro 12.9 M2', 8999.00, 'Apple iPad Pro 12.9英寸 M2芯片 Liquid视网膜XDR屏'),
        ('iPad Pro 11 M2', 6799.00, 'Apple iPad Pro 11英寸 M2芯片 ProMotion'),
        ('iPad Air M2', 4799.00, 'Apple iPad Air M2芯片 11英寸 全面屏'),
        ('iPad 10', 3599.00, 'Apple iPad 第10代 10.9英寸 A14芯片'),
        ('iPad mini 6', 3799.00, 'Apple iPad mini 8.3英寸 A15芯片'),
        ('华为 MatePad Pro 13.2', 5699.00, '华为MatePad Pro 13.2英寸 星闪连接 鸿蒙4.0'),
        ('华为 MatePad 11.5 S', 2599.00, '华为MatePad 11.5S 柔光屏 鸿蒙智慧体验'),
        ('华为 MatePad Air', 2899.00, '华为MatePad Air 11.5英寸 2K屏 鸿蒙系统'),
        ('小米平板6 Pro', 2399.00, '小米平板6 Pro 骁龙870 2.8K 144Hz'),
        ('小米平板6 Max', 2999.00, '小米平板6 Max 14英寸 骁龙860 大屏办公'),
        ('OPPO Pad 2', 2499.00, 'OPPO Pad2 11.61英寸 天玑9000 144Hz'),
        ('vivo Pad3 Pro', 2999.00, 'vivo Pad3 Pro 13英寸 天玑9300 144Hz'),
        ('荣耀平板9', 1599.00, '荣耀平板9 12.1英寸 2K护眼屏 8扬声器'),
        ('荣耀 MagicPad 13', 2699.00, '荣耀MagicPad 13英寸 2K IMAX Enhanced'),
        ('三星 Galaxy Tab S9 Ultra', 8999.00, '三星Tab S9 Ultra 14.6英寸 AMOLED 骁龙8Gen2'),
        ('三星 Galaxy Tab S9', 5499.00, '三星Tab S9 11英寸 AMOLED S Pen'),
        ('联想 小新Pad Pro 12.7', 1999.00, '联想小新Pad Pro 12.7英寸 骁龙870 2.9K'),
        ('联想 拯救者 Y700', 2399.00, '联想拯救者Y700 8.8英寸 游戏平板 骁龙8+Gen1'),
        ('微软 Surface Pro 9', 7988.00, '微软Surface Pro 9 i5 二合一平板电脑'),
        ('酷比魔方 iPlay 50 Mini', 699.00, '酷比魔方iPlay50 Mini 8.4英寸 安卓平板'),
    ],
    '穿戴': [
        ('Apple Watch Ultra 2', 5999.00, 'Apple Watch Ultra2 钛金属 双频GPS 精准寻路'),
        ('Apple Watch Series 9', 2999.00, 'Apple Watch S9 GPS+蜂窝 双指互点手势'),
        ('Apple Watch SE 2', 1799.00, 'Apple Watch SE 第二代 健康监测 运动追踪'),
        ('华为 Watch GT 4', 1488.00, '华为Watch GT4 46mm 两周续航 健康管理'),
        ('华为 Watch 4 Pro', 2699.00, '华为Watch 4 Pro eSIM独立通话 微体检'),
        ('华为 Watch Ultimate', 5999.00, '华为Watch Ultimate 非凡大师 百米深潜'),
        ('华为 Band 8', 249.00, '华为手环8 轻薄舒适 96种运动模式'),
        ('小米手环8 Pro', 299.00, '小米手环8 Pro 1.74AMOLED 150+运动模式'),
        ('小米 Watch S3', 999.00, '小米Watch S3 可换表圈 eSIM独立通话'),
        ('OPPO Watch 4 Pro', 1999.00, 'OPPO Watch4 Pro eSIM 双芯长续航'),
        ('vivo Watch 3', 1299.00, 'vivo Watch3 蓝牙通话 健康监测'),
        ('三星 Galaxy Watch6', 1899.00, '三星Galaxy Watch6 蓝牙版 BIA身体成分'),
        ('Garmin Forerunner 265', 3180.00, '佳明265 GPS运动手表 多频多星定位'),
        ('Garmin Venu 3', 3480.00, '佳明Venu3 AMOLED屏 智能健康运动手表'),
        ('荣耀 Watch 4', 999.00, '荣耀手表4 eSIM通话 超长续航'),
        ('Amazfit GTR 4', 1199.00, '华米Amazfit GTR4 双频GPS 150+运动模式'),
        ('Amazfit Bip 5', 599.00, '华米Amazfit Bip5 超长续航 蓝牙通话'),
        ('小天才 Z9', 1998.00, '小天才电话手表Z9 旗舰版 视频通话'),
        ('360 儿童手表 X9 Pro', 1299.00, '360儿童手表X9 Pro AI语音 视频通话'),
        ('颂拓 Suunto Race', 3999.00, '颂拓Race AMOLED屏 双频GPS 离线地图'),
    ],
    '游戏': [
        ('PlayStation 5 光驱版', 3899.00, '索尼PS5光驱版 次世代游戏主机 4K 120fps'),
        ('PlayStation 5 数字版', 3299.00, '索尼PS5数字版 无光驱 超高速SSD'),
        ('Nintendo Switch OLED', 2349.00, '任天堂Switch OLED版 7英寸鲜艳屏幕'),
        ('Nintendo Switch Lite', 1399.00, '任天堂Switch Lite 便携式掌机 轻巧'),
        ('Xbox Series X', 3799.00, '微软Xbox Series X 4K游戏 1TB SSD'),
        ('Xbox Series S', 1999.00, '微软Xbox Series S 数字版 512GB'),
        ('Steam Deck OLED', 4999.00, 'Valve Steam Deck OLED版 掌上PC游戏机'),
        ('ROG Ally X', 5999.00, '华硕ROG Ally X 掌机 Z1Extreme 24GB'),
        ('雷蛇 八岐大蛇V2', 599.00, 'Razer游戏鼠标 5G传感器 59g轻量'),
        ('罗技 G Pro X Superlight2', 899.00, '罗技GPW2 超轻无线游戏鼠标 63g'),
        ('樱桃 MX Board 3.0S', 599.00, 'Cherry机械键盘 MX红轴 全键无冲'),
        ('雷蛇 黑寡妇V4', 799.00, 'Razer黑寡妇V4 绿轴 RGB机械键盘'),
        ('罗技 G915 TKL', 1299.00, '罗技G915 TKL 无线矮轴机械键盘'),
        ('索尼 INZONE H9', 1999.00, '索尼INZONE H9 无线降噪游戏耳机 360空间音效'),
        ('雷蛇 旋风黑鲨V2 Pro', 1299.00, 'Razer旋风黑鲨V2Pro 无线游戏耳机 HyperSpeed'),
        ('HyperX 飓风3', 699.00, '金士顿HyperX飓风3 游戏耳机 DTS空间音效'),
        ('Secretlab Titan Evo', 4299.00, 'Secretlab电竞椅 Titan Evo 2024 冷泡棉'),
        ('保友 优旗舰2代', 2999.00, '保友优旗舰2代 人体工学椅 4D扶手'),
        ('三星 Odyssey G7 32', 3299.00, '三星玄龙骑士G7 32寸 2K 240Hz 曲面'),
        ('LG 27GP950', 4999.00, 'LG 27英寸 4K 144Hz NanoIPS HDMI2.1'),
    ],
    '摄影': [
        ('索尼 A7M4 全画幅微单', 15999.00, '索尼A7M4 3300万像素 BIONZ XR 4K60p'),
        ('索尼 A7R5 全画幅微单', 23999.00, '索尼A7R5 6100万像素 AI对焦 8K视频'),
        ('佳能 EOS R6 Mark II', 15999.00, '佳能R6II 2420万像素 全画幅 40fps连拍'),
        ('佳能 EOS R5', 24999.00, '佳能R5 4500万像素 8K RAW 全画幅'),
        ('尼康 Z8', 25999.00, '尼康Z8 4571万像素 EXPEED7 8K视频'),
        ('尼康 Z6 III', 15999.00, '尼康Z6III 部分堆栈式CMOS 6K视频'),
        ('富士 X-T5', 11999.00, '富士X-T5 4020万像素 胶片模拟 5轴防抖'),
        ('富士 X100VI', 11390.00, '富士X100VI 4020万像素 复古旁轴 IBIS防抖'),
        ('松下 S5M2', 11998.00, '松下S5M2 全画幅 混合相位AF 6K视频'),
        ('徕卡 Q3', 42999.00, '徕卡Q3 6000万像素 Summilux 28mm f/1.7'),
        ('哈苏 X2D 100C', 73999.00, '哈苏X2D 100C 中画幅 1亿像素 自然色彩'),
        ('大疆 Mavic 3 Pro', 13888.00, '大疆Mavic3 Pro 三摄系统 哈苏主摄 43分钟续航'),
        ('大疆 Mini 4 Pro', 5788.00, '大疆Mini4 Pro 4K/60fps 全向避障 249g'),
        ('GoPro HERO12 Black', 3298.00, 'GoPro HERO12 5.3K60 HyperSmooth 6.0'),
        ('Insta360 X4', 3998.00, '影石X4 8K全景运动相机 2.5K隐形自拍杆'),
        ('富士 instax mini 12', 599.00, '富士拍立得mini12 即拍即得 自动曝光'),
        ('索尼 FE 24-70mm F2.8 GM2', 14999.00, '索尼24-70mm F2.8 GM2 G大师镜头 II代'),
        ('佳能 RF 70-200mm F2.8L', 17999.00, '佳能RF70-200mm F2.8 L IS USM 白色长焦'),
        ('曼富图 190go 铝合金三脚架', 1299.00, '曼富图190go 铝合金4节 90°中轴横置'),
        ('神牛 AD600pro 外拍灯', 3999.00, '神牛AD600pro 600W外拍闪光灯 TTL HSS'),
    ],
    '配件': [
        ('苹果 MagSafe充电器', 399.00, 'Apple MagSafe 15W无线充电器 磁吸对齐'),
        ('安克 100W氮化镓充电器', 299.00, 'Anker 100W四口氮化镓 GaN快充'),
        ('贝尔金 100W氮化镓', 399.00, 'Belkin 100W 4口氮化镓充电器'),
        ('紫米 20000mAh充电宝', 199.00, '紫米20000mAh 22.5W快充移动电源'),
        ('安克 10000mAh充电宝', 149.00, 'Anker 10000mAh 22.5W 超薄移动电源'),
        ('苹果 Lightning数据线', 149.00, 'Apple Lightning to USB 1米 MFi认证'),
        ('绿联 Type-C数据线 100W', 39.00, '绿联USB-C to C 100W快充线 1.5米'),
        ('苹果 iPhone 15手机壳', 199.00, 'Apple iPhone15 硅胶壳 MagSafe兼容'),
        ('闪魔 钢化膜 iPhone15', 29.00, '闪魔iPhone15钢化膜 高清防指纹 贴膜神器'),
        ('绿联 iPhone15手机壳', 49.00, '绿联iPhone15液态硅胶壳 全包防摔'),
        ('倍思 车载手机支架', 69.00, '倍思车载出风口支架 磁吸导航 通用'),
        ('绿联 Type-C扩展坞', 199.00, '绿联10合1 Type-C扩展坞 4K HDMI 100W PD'),
        ('贝尔金 雷电4扩展坞', 1999.00, 'Belkin雷电4扩展坞 12合1 双8K'),
        ('JBL Charge 5 蓝牙音箱', 899.00, 'JBL音乐战鼓5 IP67防水 20小时续航'),
        ('马歇尔 Emberton II', 1299.00, 'Marshall Emberton2 蓝牙音箱 30小时续航'),
        ('Bose SoundLink Flex', 999.00, 'BOSE蓝牙音箱 IP67防水 PositionIQ'),
        ('罗技 MX Master 3S', 699.00, '罗技MX Master3S 无线鼠标 8000DPI 静音'),
        ('赛睿 QcK Large鼠标垫', 99.00, 'SteelSeries QcK大号 布面游戏鼠标垫'),
        ('三星 T7 Shield 1TB', 699.00, '三星T7 Shield 1TB 移动固态硬盘 IP65'),
        ('闪迪 128GB U盘', 79.00, 'SanDisk 128GB USB3.1 U盘 双接口Type-C'),
    ],
}


def scrape_jd(keyword, page=1):
    url = 'https://search.jd.com/Search'
    params = {
        'keyword': keyword,
        'enc': 'utf-8',
        'page': page,
    }
    try:
        resp = requests.get(url, params=params, headers=JD_HEADERS, timeout=10)
        resp.encoding = 'utf-8'
        if resp.status_code != 200:
            return []
        soup = BeautifulSoup(resp.text, 'lxml')
        items = soup.select('li.gl-item')
        if not items:
            items = soup.select('.J-goods-list .gl-item')
        results = []
        for item in items[:20]:
            try:
                name_tag = item.select_one('.p-name a em')
                price_tag = item.select_one('.p-price strong i')
                img_tag = item.select_one('.p-img img')
                name = name_tag.get_text(strip=True) if name_tag else ''
                price_str = price_tag.get_text(strip=True) if price_tag else '0'
                price = float(price_str) if price_str else 0.0
                img_src = ''
                if img_tag:
                    img_src = img_tag.get('data-lazy-img') or img_tag.get('src', '')
                    if img_src and not img_src.startswith('http'):
                        img_src = 'https:' + img_src
                if name and price > 0:
                    results.append({
                        'name': name[:200],
                        'price': price,
                        'image_url': img_src[:255] if img_src else '',
                        'description': name[:200],
                    })
            except Exception:
                continue
        return results
    except Exception as e:
        print(f'  [JD爬取失败] {keyword}: {e}')
        return []


def generate_mock_data():
    products = []
    for category, items in MOCK_PRODUCTS.items():
        for name, price, desc in items:
            color_code = {
                '手机': '1a1a2e', '电脑': '0d1b2a', '耳机': '1b2838',
                '平板': '2d1b69', '穿戴': '1a3a2a', '游戏': '3a1a1a',
                '摄影': '2a2a1a', '配件': '1a2a3a',
            }.get(category, '333333')
            img_url = f'https://placehold.co/300x300/{color_code}/ffffff?text={requests.utils.quote(name[:12])}&font=roboto'
            stock = random.randint(20, 500)
            products.append({
                'name': name,
                'category': category,
                'price': price,
                'stock': stock,
                'description': desc,
                'image_url': img_url,
            })
    return products


def try_scrape_jd():
    all_products = []
    scraped_count = 0
    for category, keywords in CATEGORY_KEYWORDS.items():
        print(f'\n[爬取京东] 类目: {category}')
        for kw in keywords[:3]:
            print(f'  搜索: {kw}')
            items = scrape_jd(kw)
            if items:
                scraped_count += len(items)
                for item in items:
                    item['category'] = category
                    item['stock'] = random.randint(20, 500)
                    all_products.append(item)
            time.sleep(random.uniform(1.5, 3.0))
    print(f'\n[京东爬取结果] 成功获取 {scraped_count} 条商品')
    return all_products


def import_to_mysql(products):
    conn = pymysql.connect(**DB_CONFIG)
    try:
        cursor = conn.cursor()
        cursor.execute('DELETE FROM t_product')
        print(f'已清空 t_product 表')

        sql = '''INSERT INTO t_product (name, category, price, stock, description, image_url)
                 VALUES (%s, %s, %s, %s, %s, %s)'''
        success = 0
        for p in products:
            try:
                cursor.execute(sql, (
                    p['name'], p['category'], p['price'],
                    p['stock'], p['description'], p.get('image_url', '')
                ))
                success += 1
            except Exception as e:
                print(f'  插入失败: {p["name"]} - {e}')
        conn.commit()
        print(f'\n[导入完成] 成功导入 {success}/{len(products)} 条商品')
    finally:
        conn.close()


def generate_sql_file(products, filepath):
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write('USE ecommerce;\n\n')
        f.write('DELETE FROM t_product;\n\n')
        for p in products:
            name = p['name'].replace("'", "\\'")
            desc = p['description'].replace("'", "\\'")
            cat = p['category']
            img = p.get('image_url', '').replace("'", "\\'")
            f.write(f"INSERT INTO t_product (name, category, price, stock, description, image_url) VALUES "
                    f"('{name}', '{cat}', {p['price']:.2f}, {p['stock']}, '{desc}', '{img}');\n")
    print(f'SQL文件已生成: {filepath}')


def main():
    print('=' * 60)
    print('  京东/淘宝 商品数据爬取与导入工具')
    print('=' * 60)

    print('\n[步骤1] 尝试爬取京东商品数据...')
    jd_products = try_scrape_jd()

    print('\n[步骤2] 生成模拟商品数据(基于真实商品信息)...')
    mock_products = generate_mock_data()

    all_products = []
    jd_names = set()
    for p in jd_products:
        all_products.append(p)
        jd_names.add(p['name'])

    for p in mock_products:
        if p['name'] not in jd_names:
            all_products.append(p)

    print(f'\n[汇总] 京东爬取: {len(jd_products)} 条, 模拟数据: {len(mock_products)} 条, '
          f'去重后总计: {len(all_products)} 条')

    sql_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'products_import.sql')
    print('\n[步骤3] 生成SQL备份文件...')
    generate_sql_file(all_products, sql_path)

    print('\n[步骤4] 导入MySQL数据库...')
    import_to_mysql(all_products)

    print('\n' + '=' * 60)
    print('  完成！商品数据已导入 ecommerce.t_product')
    print(f'  SQL备份: {sql_path}')
    print('=' * 60)


if __name__ == '__main__':
    main()
