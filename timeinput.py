import psycopg2
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from datetime import datetime

chrome_options = Options()
chrome_options.add_argument("--headless")
chrome_options.add_argument("--no-sandbox")
chrome_options.add_argument("--disable-dev-shm-usage")
chrome_options.add_argument("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")

def init_driver():
    return webdriver.Chrome(options=chrome_options)

def time_update():
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
            link = row[5]
            cafe_id = row[0]
            if link:
                cnt += 1
                # Get categories for the cafe
                times = get_time(driver, cafe_id)
                
                if times:
                    cursor.execute("UPDATE cafe SET time = %s WHERE cafeid = %s", (times, cafe_id))
                    conn_write.commit()
                    print(f"Updated {cafe_id} with time {times}")

    except Exception as e:
        print(f"Error occurred during execution: {e}")
    finally:
        driver.quit()
        cur.close()
        cursor.close()
        conn_read.close()
        conn_write.close()


def get_time(driver, cafeid):
    try:
        url = f"https://place.map.kakao.com/{cafeid}"
        driver.get(url)

        wait = WebDriverWait(driver, 10)
        operation_element = wait.until(EC.presence_of_element_located((By.CLASS_NAME, 'txt_operation'))).text
        time_element = wait.until(EC.presence_of_element_located((By.CLASS_NAME, 'time_operation'))).text

        time = f"{operation_element}"
        return time
    
    except Exception as e:
        print(f"Error occurred while scraping cafe {cafeid}: {e}")
        return ""


if __name__ == "__main__":
    #while(True):
        #current = datetime.now()
        #if current.hour == 0 and current.minute == 0:
        time_update()
        #else:
            #continue