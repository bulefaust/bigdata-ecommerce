import pymysql
import random
import json
from datetime import datetime, timedelta

DB_CONFIG = {
    'host': 'localhost',
    'port': 3306,
    'user': 'root',
    'password': 'root',
    'database': 'ecommerce',
    'charset': 'utf8mb4'
}

SPEC_TEMPLATES = {
    '手机': [
        ('品牌', '{brand}'), ('型号', '{name}'), ('上市时间', '2024年'), ('操作系统', '{os}'),
        ('CPU型号', '{cpu}'), ('CPU核心数', '八核'), ('运行内存', '{ram}'), ('存储容量', '{storage}'),
        ('屏幕尺寸', '{screen}'), ('屏幕分辨率', '{resolution}'), ('屏幕刷新率', '120Hz'),
        ('后置摄像头', '{rear_cam}'), ('前置摄像头', '{front_cam}'), ('电池容量', '{battery}'),
        ('充电功率', '{charge}'), ('NFC', '支持'), ('5G', '支持'), ('防水等级', 'IP68'),
        ('机身重量', '{weight}'), ('SIM卡类型', 'Nano SIM'),
    ],
    '电脑': [
        ('品牌', '{brand}'), ('型号', '{name}'), ('上市时间', '2024年'), ('产品类型', '{type}'),
        ('操作系统', '{os}'), ('CPU型号', '{cpu}'), ('CPU核心数', '{cores}'),
        ('内存容量', '{ram}'), ('硬盘容量', '{storage}'), ('硬盘类型', 'SSD'),
        ('显卡型号', '{gpu}'), ('显卡类型', '{gpu_type}'), ('屏幕尺寸', '{screen}'),
        ('屏幕分辨率', '{resolution}'), ('刷新率', '{refresh}'), ('电池续航', '{battery}'),
        ('USB接口', 'USB-C ×2, USB-A ×1'), ('重量', '{weight}'),
    ],
    '耳机': [
        ('品牌', '{brand}'), ('型号', '{name}'), ('产品类型', '{type}'), ('佩戴方式', '{wear}'),
        ('连接方式', '蓝牙5.3'), ('降噪类型', '{anc}'), ('驱动单元', '{driver}'),
        ('频率响应', '20Hz-20kHz'), ('阻抗', '{impedance}'), ('灵敏度', '{sensitivity}'),
        ('续航时间', '{battery}'), ('充电接口', 'USB-C'), ('防水等级', 'IPX4'),
        ('重量', '{weight}'), ('是否支持APP', '支持'),
    ],
    '平板': [
        ('品牌', '{brand}'), ('型号', '{name}'), ('上市时间', '2024年'), ('操作系统', '{os}'),
        ('CPU型号', '{cpu}'), ('运行内存', '{ram}'), ('存储容量', '{storage}'),
        ('屏幕尺寸', '{screen}'), ('屏幕分辨率', '{resolution}'), ('屏幕刷新率', '{refresh}'),
        ('摄像头', '{camera}'), ('电池容量', '{battery}'), ('充电接口', 'USB-C'),
        ('是否支持通话', '{call}'), ('重量', '{weight}'),
    ],
    '穿戴': [
        ('品牌', '{brand}'), ('型号', '{name}'), ('产品类型', '{type}'), ('表盘形状', '圆形'),
        ('屏幕尺寸', '{screen}'), ('屏幕类型', 'AMOLED'), ('防水等级', '{waterproof}'),
        ('续航时间', '{battery}'), ('心率监测', '支持'), ('血氧监测', '支持'),
        ('睡眠监测', '支持'), ('NFC', '{nfc}'), ('GPS', '支持'),
        ('连接方式', '蓝牙5.2'), ('重量', '{weight}'),
    ],
    '游戏': [
        ('品牌', '{brand}'), ('型号', '{name}'), ('产品类型', '{type}'),
        ('CPU/GPU', '{cpu}'), ('内存', '{ram}'), ('存储', '{storage}'),
        ('屏幕/输出', '{screen}'), ('分辨率', '{resolution}'), ('刷新率', '{refresh}'),
        ('连接方式', '{connect}'), ('续航', '{battery}'), ('重量', '{weight}'),
    ],
    '摄影': [
        ('品牌', '{brand}'), ('型号', '{name}'), ('产品类型', '{type}'),
        ('有效像素', '{pixel}'), ('传感器类型', '{sensor}'), ('传感器尺寸', '{sensor_size}'),
        ('镜头 mount', '{mount}'), ('对焦方式', '{af}'), ('最高连拍速度', '{fps}'),
        ('视频分辨率', '{video}'), ('防抖方式', '{is}'), ('存储介质', 'SD/CFexpress'),
        ('电池类型', '{battery}'), ('重量', '{weight}'),
    ],
    '配件': [
        ('品牌', '{brand}'), ('型号', '{name}'), ('产品类型', '{type}'),
        ('适用机型', '{compat}'), ('材质', '{material}'), ('颜色', '{color}'),
        ('功率', '{power}'), ('接口', '{interface}'), ('容量', '{capacity}'),
        ('重量', '{weight}'), ('认证', '{cert}'),
    ],
}

BRAND_MAP = {
    'iPhone': 'Apple', 'iPad': 'Apple', 'MacBook': 'Apple', 'AirPods': 'Apple',
    'Apple Watch': 'Apple', '华为': '华为', '小米': '小米', 'Redmi': '小米',
    'OPPO': 'OPPO', 'vivo': 'vivo', '三星': '三星', '荣耀': '荣耀',
    '一加': '一加', '魅族': '魅族', 'realme': 'realme', '索尼': '索尼',
    '联想': '联想', '戴尔': '戴尔', '华硕': '华硕', '惠普': '惠普',
    '漫步者': '漫步者', 'BOSE': 'BOSE', 'JBL': 'JBL', '森海塞尔': '森海塞尔',
    '铁三角': '铁三角', 'B&O': 'B&O', '佳能': '佳能', '尼康': '尼康',
    '富士': '富士', '松下': '松下', '徕卡': '徕卡', '大疆': '大疆',
    'GoPro': 'GoPro', 'Insta360': 'Insta360', 'PlayStation': '索尼',
    'Nintendo': '任天堂', 'Xbox': '微软', 'Steam': 'Valve', 'ROG': '华硕',
    '雷蛇': 'Razer', '罗技': '罗技', '樱桃': 'Cherry', '安克': 'Anker',
    '贝尔金': 'Belkin', '紫米': '紫米', '绿联': '绿联', '闪魔': '闪魔',
    '倍思': '倍思', 'Garmin': 'Garmin', 'Amazfit': 'Amazfit',
    '小天才': '小天才', '360': '360', '颂拓': '颂拓', 'Secretlab': 'Secretlab',
    '保友': '保友', 'LG': 'LG', 'HyperX': '金士顿', '微软 Surface': '微软',
    '神舟': '神舟', '机械革命': '机械革命', '宏碁': '宏碁', '微星': '微星',
    '摩托罗拉': '摩托罗拉', '努比亚': '努比亚', 'ROG 游戏手机': '华硕',
    '哈苏': '哈苏', '曼富图': '曼富图', '神牛': '神牛', '马歇尔': 'Marshall',
    'QCY': 'QCY', '万魔': '1MORE', '酷比魔方': '酷比魔方',
}

def get_brand(name):
    for key, val in BRAND_MAP.items():
        if key in name:
            return val
    return name.split()[0] if name else '未知'

def generate_specs(name, category):
    brand = get_brand(name)
    template = SPEC_TEMPLATES.get(category, SPEC_TEMPLATES['配件'])
    specs = {}
    for label, value_template in template:
        value = value_template
        if '{brand}' in value:
            value = value.replace('{brand}', brand)
        elif '{name}' in value:
            value = value.replace('{name}', name)
        else:
            value = fill_value(label, name, category, brand)
        specs[label] = value
    return specs

def fill_value(label, name, category, brand):
    fills = {
        '操作系统': {'手机': 'Android 14', '平板': 'Android 14', '电脑': 'Windows 11', '穿戴': '定制OS'},
        'CPU型号': {'手机': '骁龙8 Gen3', '电脑': 'Intel Core i7-13700H', '平板': '骁龙8 Gen2', '游戏': '定制芯片'},
        '运行内存': {'手机': '12GB', '电脑': '16GB', '平板': '8GB', '穿戴': '2GB'},
        '存储容量': {'手机': '256GB', '电脑': '512GB SSD', '平板': '256GB', '穿戴': '32GB'},
        '屏幕尺寸': {'手机': '6.7英寸', '电脑': '15.6英寸', '平板': '11英寸', '穿戴': '1.43英寸'},
        '屏幕分辨率': {'手机': '2796×1290', '电脑': '2560×1600', '平板': '2560×1600', '穿戴': '466×466'},
        '电池容量': {'手机': '4422mAh', '电脑': '70Wh', '平板': '7600mAh', '穿戴': '485mAh'},
        '后置摄像头': {'手机': '4800万像素主摄+1200万超广角'},
        '前置摄像头': {'手机': '1200万像素'},
        '充电功率': {'手机': '27W'},
        '机身重量': {'手机': '221g', '电脑': '1.9kg', '平板': '500g', '穿戴': '52g', '耳机': '5g', '游戏': '350g', '摄影': '650g', '配件': '50g'},
        '产品类型': {'电脑': '笔记本', '耳机': '头戴式耳机', '穿戴': '智能手表', '游戏': '游戏主机', '摄影': '微单相机', '配件': '配件'},
        '显卡型号': {'电脑': 'NVIDIA RTX 4060'},
        '显卡类型': {'电脑': '独立显卡'},
        '刷新率': {'电脑': '165Hz', '平板': '120Hz'},
        '佩戴方式': {'耳机': '头戴式'},
        '降噪类型': {'耳机': '主动降噪'},
        '驱动单元': {'耳机': '40mm'},
        '续航时间': {'耳机': '30小时', '穿戴': '14天'},
        '防水等级': {'穿戴': '5ATM'},
        'NFC': {'穿戴': '支持'},
        '有效像素': {'摄影': '3300万像素'},
        '传感器类型': {'摄影': 'CMOS'},
        '传感器尺寸': {'摄影': '全画幅'},
        '视频分辨率': {'摄影': '4K 60fps'},
        '适用机型': {'配件': '通用'},
        '材质': {'配件': '铝合金+PC'},
        '颜色': {'配件': '黑色'},
        '功率': {'配件': '100W'},
        '接口': {'配件': 'USB-C'},
        '容量': {'配件': '20000mAh'},
        '认证': {'配件': '3C认证'},
    }
    for key, val_map in fills.items():
        if label == key:
            if isinstance(val_map, dict):
                return val_map.get(category, '标准配置')
            return val_map
    return '标准'

REVIEW_USERS = [
    '数码达人小王', '科技爱好者', '购物狂魔', '品质生活家', '极客玩家',
    '小明同学', '大壮评测', '阿花爱购物', '老张说数码', '小白入门',
    '资深果粉', '安卓党代表', '性价比之王', '颜值控', '实用主义者',
    '学生党', '白领丽人', '数码博主', '摄影发烧友', '游戏狂人',
]

REVIEW_TEMPLATES = {
    '手机': [
        ('手机用起来非常流畅，拍照效果超赞！', 5),
        ('续航比上一代好很多，一天一充没问题', 5),
        ('屏幕显示效果很细腻，色彩还原度高', 4),
        ('手感不错，重量可以接受', 4),
        ('性价比一般，但是体验确实好', 4),
        ('信号比之前好，5G网速很快', 5),
        ('发热控制得不错，玩游戏也不烫', 4),
        ('系统很流畅，动画丝滑', 5),
        ('充电速度还行，希望能更快', 3),
        ('外观设计很漂亮，朋友都说好看', 5),
    ],
    '电脑': [
        ('性能很强，跑大型软件毫无压力', 5),
        ('屏幕素质很好，做设计很舒服', 5),
        ('键盘手感不错，打字很舒服', 4),
        ('散热表现一般，长时间高负载会热', 3),
        ('轻薄便携，出差带着很方便', 5),
        ('续航表现超出预期，办公8小时没问题', 4),
        ('开机速度很快，SSD就是快', 5),
        ('接口够用，不用带扩展坞了', 4),
        ('风扇声音有点大，其他都好', 3),
        ('做工精致，对得起这个价格', 4),
    ],
    '耳机': [
        ('降噪效果非常好，地铁上完全安静', 5),
        ('音质很棒，低频有力高频通透', 5),
        ('佩戴舒适，戴几个小时也不累', 4),
        ('续航很给力，一周充一次电', 5),
        ('连接很稳定，没出现过断连', 4),
        ('通话质量不错，对方听得很清楚', 4),
        ('APP功能丰富，可以自定义EQ', 4),
        ('外观好看，做工精致', 5),
        ('价格有点贵，但物有所值', 4),
        ('漏音控制得不错', 3),
    ],
    '平板': [
        ('屏幕大看着舒服，追剧神器', 5),
        ('手写笔延迟很低，记笔记很流畅', 5),
        ('性能足够，玩游戏也不卡', 4),
        ('分屏功能很实用，一边看视频一边记笔记', 5),
        ('续航不错，一天使用没问题', 4),
        ('轻便好携带，比笔记本方便', 4),
        ('扬声器效果很好，看片不用外接音箱', 4),
        ('系统生态丰富，APP适配不错', 4),
        ('希望能有更大的存储版本', 3),
        ('性价比很高，推荐购买', 5),
    ],
    '穿戴': [
        ('运动监测很准确，跑步数据靠谱', 5),
        ('心率监测和医院测的差不多', 4),
        ('续航真的长，两周不用充电', 5),
        ('表盘很多可以换，每天不重样', 4),
        ('防水做得好，游泳也能戴', 5),
        ('睡眠监测很详细，帮助改善作息', 4),
        ('消息提醒很方便，不用掏手机', 4),
        ('表带舒适，睡觉戴也不硌', 4),
        ('GPS定位很快很准', 5),
        ('功能很全面，基本满足日常需求', 4),
    ],
    '游戏': [
        ('游戏体验非常棒，画面流畅', 5),
        ('手柄手感很好，按键反馈清晰', 5),
        ('独占游戏太好玩了，值了', 5),
        ('加载速度很快，几乎不用等', 4),
        ('画质模式很震撼，4K真香', 5),
        ('散热不错，长时间玩也不热', 4),
        ('游戏库丰富，总有想玩的', 4),
        ('多人在线体验好，匹配快', 4),
        ('希望能出更多独占大作', 3),
        ('性价比很高，推荐入手', 5),
    ],
    '摄影': [
        ('画质非常出色，细节丰富', 5),
        ('对焦速度很快，抓拍利器', 5),
        ('高感表现好，夜景也能拍', 4),
        ('视频功能强大，Vlog够用', 4),
        ('机身做工扎实，手感好', 4),
        ('镜头群丰富，选择多', 4),
        ('菜单逻辑清晰，上手快', 4),
        ('电池续航还行，建议备一块', 3),
        ('色彩科学很棒，直出就好看', 5),
        ('防抖效果明显，手持也能拍', 4),
    ],
    '配件': [
        ('质量很好，做工精致', 5),
        ('和描述一致，很满意', 5),
        ('性价比高，推荐购买', 4),
        ('使用方便，即插即用', 4),
        ('充电速度很快，不发热', 5),
        ('外观好看，和设备很搭', 4),
        ('包装很好，物流也快', 4),
        ('用了一段时间了，很稳定', 4),
        ('价格实惠，功能齐全', 5),
        ('比想象中好，超出预期', 4),
    ],
}

def generate_reviews(product_id, category, count=5):
    templates = REVIEW_TEMPLATES.get(category, REVIEW_TEMPLATES['配件'])
    reviews = []
    for i in range(min(count, len(templates))):
        content, rating = templates[i]
        username = random.choice(REVIEW_USERS)
        days_ago = random.randint(1, 90)
        created = (datetime.now() - timedelta(days=days_ago)).strftime('%Y-%m-%d %H:%M:%S')
        reviews.append({
            'product_id': product_id,
            'username': username,
            'rating': rating,
            'content': content,
            'create_time': created,
        })
    return reviews

def main():
    conn = pymysql.connect(**DB_CONFIG)
    try:
        cursor = conn.cursor()

        cursor.execute('''CREATE TABLE IF NOT EXISTS t_product_spec (
            id BIGINT AUTO_INCREMENT PRIMARY KEY,
            product_id BIGINT NOT NULL,
            spec_name VARCHAR(100) NOT NULL,
            spec_value VARCHAR(500) NOT NULL,
            INDEX idx_product_id (product_id)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4''')

        cursor.execute('''CREATE TABLE IF NOT EXISTS t_product_review (
            id BIGINT AUTO_INCREMENT PRIMARY KEY,
            product_id BIGINT NOT NULL,
            username VARCHAR(50) NOT NULL,
            rating TINYINT NOT NULL DEFAULT 5,
            content TEXT NOT NULL,
            create_time DATETIME NOT NULL,
            INDEX idx_product_id (product_id)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4''')

        cursor.execute('DELETE FROM t_product_spec')
        cursor.execute('DELETE FROM t_product_review')

        cursor.execute('SELECT id, name, category FROM t_product')
        products = cursor.fetchall()

        spec_count = 0
        review_count = 0

        for pid, name, category in products:
            specs = generate_specs(name, category)
            for spec_name, spec_value in specs.items():
                cursor.execute(
                    'INSERT INTO t_product_spec (product_id, spec_name, spec_value) VALUES (%s, %s, %s)',
                    (pid, spec_name, str(spec_value))
                )
                spec_count += 1

            reviews = generate_reviews(pid, category, random.randint(3, 8))
            for r in reviews:
                cursor.execute(
                    'INSERT INTO t_product_review (product_id, username, rating, content, create_time) VALUES (%s, %s, %s, %s, %s)',
                    (r['product_id'], r['username'], r['rating'], r['content'], r['create_time'])
                )
                review_count += 1

        conn.commit()
        print(f'完成！生成 {spec_count} 条规格参数，{review_count} 条用户评价')
    finally:
        conn.close()

if __name__ == '__main__':
    main()
