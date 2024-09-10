import psycopg2
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

chrome_options = Options()
chrome_options.add_argument("--headless")
chrome_options.add_argument("--no-sandbox")
chrome_options.add_argument("--disable-dev-shm-usage")
chrome_options.add_argument("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")


def init_driver():
    return webdriver.Chrome(options=chrome_options)


def get_category(driver, cafe_id):
    try:
        url = f"https://place.map.kakao.com/{cafe_id}"
        driver.get(url)
        
        categories = []

        # 카페 이름을 기다리며 추출
        wait = WebDriverWait(driver, 10)
        meta_element = wait.until(
            EC.presence_of_element_located((By.CSS_SELECTOR, "meta[name='twitter:title']"))
        )
        cafename = meta_element.get_attribute("content")
        
        #태그 추출
        tags = []
        try:
            tag_elements = wait.until(EC.presence_of_all_elements_located((By.CSS_SELECTOR, ".tag_g a[data-logevent]")))
            for element in tag_elements:
                tags.append(element.text.strip())
        except Exception:
            tags = []

        print(f"{cafe_id}: {cafename} / {tags}")
        #카테고리 지정 
        if "#테이크아웃" in tags or cafename.endswith("점") or "#포장" in tags or "#테이크아웃가능" in tags:
            categories.append(1)
        if "#분위기좋은" in tags or "#앤틱한" in tags or "#브런치" in tags or  "#한옥카페" in tags or "#브런치카페" in tags or "#로스팅전문" in tags or "#감성카페" in tags or "#디저트" in tags or "#디저트카페" in tags or "#루프탑" in tags or "#루프탑있는" in tags:
            categories.append(2)
        if cafename.endswith("점"):
            categories.append(3)
        if "#대형카페" in tags or cafename.find("스타벅스") != -1 or cafename.find("투썸플레이스") != -1 or "#스터디카페" in tags or "#조용한" in tags or "#WIFI" in tags or "#Wifi" in tags or "#공부하기좋은" in tags or "#노트북하기좋은" in tags:
            categories.append(4)
        if cafename.find("만화") != -1 or "#보드게임카페" in tags or "#룸카페" in tags or cafename.find("벌툰") != -1 or "#드로잉카페" in tags or cafename.find("키즈") != -1 or "#보드게임" in tags or "#만화카페" in tags or "#실내놀거리" in tags or "#공방카페" in tags or "#북카페" in tags or "#뮤직카페" in tags or "#이색카페" in tags or "#테마카페" in tags or cafename.find("공방") != -1 or "#공방" in tags or "#애견동반카페" in tags or "#실내놀이터" in tags:
            categories.append(5)
        if "#무인카페" in tags or cafename.find("무인") != -1 or "#셀프" in tags:
            categories.append(6)

        return categories
    except Exception as e:
        print(f"Error occurred while scraping cafe {cafe_id}: {e}")
        return []



def main():
    conn_read = psycopg2.connect(host="localhost", dbname="postgres", user="postgres", password="1234", port="5432")
    cur = conn_read.cursor()

    conn_write = psycopg2.connect(host="localhost", dbname="postgres", user="postgres", password="1234", port="5432")
    cursor = conn_write.cursor()

    try:
        driver = init_driver()

        cur.execute("SELECT * FROM cafe")
        rows = cur.fetchall()

        cnt = 0
        for row in rows:
            link = row[9]
            cafe_id = row[0]
            if link:
                cnt += 1
                print(f"{cnt} 회 반복 중")
                
                # Get categories for the cafe
                categories = get_category(driver, cafe_id)

                if categories:
                    print(f"{cafe_id}를 {categories}로 진행하겠습니다.")
                    for category in categories:
                        cursor.execute('INSERT INTO category (cafeid, category) VALUES (%s, %s)', (cafe_id, category))
                    conn_write.commit()
                else:
                    print(f"{cafe_id}: 예상 카테고리 정보가 없습니다.")

    except Exception as e:
        print(f"Error occurred during execution: {e}")
    finally:
        driver.quit()
        cur.close()
        cursor.close()
        conn_read.close()
        conn_write.close()


if __name__ == "__main__":
    main()
