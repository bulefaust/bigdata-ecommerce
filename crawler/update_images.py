import pymysql
import urllib.parse

DB_CONFIG = {
    'host': 'localhost',
    'port': 3306,
    'user': 'root',
    'password': 'root',
    'database': 'ecommerce',
    'charset': 'utf8mb4'
}

IMG_API = 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image'

def generate_image_url(product_name, category):
    prompts = {
        '手机': f'Professional product photo of {product_name}, smartphone on clean white background, studio lighting, high-end commercial photography, 4K',
        '电脑': f'Professional product photo of {product_name}, laptop on clean white background, studio lighting, commercial product photography, 4K',
        '耳机': f'Professional product photo of {product_name}, headphones on clean white background, studio lighting, commercial product photography, 4K',
        '平板': f'Professional product photo of {product_name}, tablet on clean white background, studio lighting, commercial product photography, 4K',
        '穿戴': f'Professional product photo of {product_name}, smartwatch on clean white background, studio lighting, commercial product photography, 4K',
        '游戏': f'Professional product photo of {product_name}, gaming device on clean white background, studio lighting, commercial product photography, 4K',
        '摄影': f'Professional product photo of {product_name}, camera on clean white background, studio lighting, commercial product photography, 4K',
        '配件': f'Professional product photo of {product_name}, tech accessory on clean white background, studio lighting, commercial product photography, 4K',
    }
    prompt = prompts.get(category, f'Professional product photo of {product_name}, on clean white background, studio lighting, 4K')
    encoded = urllib.parse.quote(prompt)
    return f'{IMG_API}?prompt={encoded}&image_size=square'

def main():
    conn = pymysql.connect(**DB_CONFIG)
    try:
        cursor = conn.cursor()
        cursor.execute('SELECT id, name, category FROM t_product')
        products = cursor.fetchall()
        print(f'共 {len(products)} 件商品需要更新图片')

        success = 0
        for pid, name, category in products:
            img_url = generate_image_url(name, category)
            try:
                cursor.execute('UPDATE t_product SET image_url = %s WHERE id = %s', (img_url, pid))
                success += 1
            except Exception as e:
                print(f'  更新失败 id={pid}: {e}')

        conn.commit()
        print(f'\n更新完成！成功 {success}/{len(products)} 件')
    finally:
        conn.close()

if __name__ == '__main__':
    main()
