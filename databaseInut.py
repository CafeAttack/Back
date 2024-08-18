import requests
import json
import psycopg2

db_config = {
    'host': 'localhost',
    'database': 'postgres',
    'user': 'postgres',
    'password': '1234',
    'port': '5432'
}

def connect_db(config):
    try:
        conn = psycopg2.connect(**config)
        return conn
    except Exception as e:
        print(e)
        return None

def fetch_kakao_data(api_url, headers, param):
    response = requests.get(api_url, headers=headers, params=param)
    if(response.status_code == 200):
        return response.json()
    else:
        response.raise_for_status()

def insert_data_to_db(cursor, data):
    cursor = conn.cursor()
    place = data.get('documents', [])
    for p in place:
        id = p.get('id')
        place_name = p.get('place_name')
        phone = p.get('phone')
        x = p.get('x')
        y = p.get('y')
        address_name = p.get('road_address_name')

        cursor.execute('SELECT 1 FROM cafe WHERE cafeid = %s', (id,))
        if cursor.fetchone() is not None:
            print(f"Skipping {id} / {place_name} as it already exists")
            continue
        
        cursor.execute('INSERT INTO cafe (cafeid, cafename, latitude, longitude, address, phone) VALUES (%s, %s, %s, %s, %s, %s)', (id, place_name, y, x, address_name, phone))
        print(f"Inserting {id} / {place_name} into the database")
    conn.commit()

if __name__ == "__main__":
    for page in range(1, 46):  # 페이지를 4부터 45까지 반복합니다.
        conn = connect_db(db_config)
        if conn is not None:
            api_url = 'https://dapi.kakao.com/v2/local/search/category.json'
            headers = {
                'Authorization': 'KakaoAK 1cb221b04b1df970647b9e7a7b9846d3'
            }
            params = {
                'category_group_code': 'CE7',
                'longitude': '127.05982',
                'latitude': '37.619427',
                'radius': '10000',
                'page': page  # page 변수를 삽입합니다.
            }
            print(f"Fetching data from page {page}")
            kakao_response = fetch_kakao_data(api_url, headers, params)

            try:
                insert_data_to_db(conn, kakao_response)
                print(f"Successfully inserted data from page {page}")
            except Exception as e:
                print(f"Error inserting data: {e}")
                conn.rollback()
            finally:
                conn.close()