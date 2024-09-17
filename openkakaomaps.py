import psycopg2
import webbrowser

if __name__ == "__main__":
    conn_read = psycopg2.connect(host="localhost", dbname="postgres", user="postgres", password="1234", port="5432")
    cur = conn_read.cursor()

    try:
        cur.execute("SELECT * FROM cafe")
        rows = cur.fetchall()

        cnt = 0
        for row in rows:
            link = row[5]
            cafe_id = row[0]
            if link:
                cnt += 1
                print(f"{cnt}번째 카페")
                webbrowser.open(link)
                input("다음 페이지를 열려면 엔터를 누르세요.")

    except Exception as e:
        print(f"Error occurred during execution: {e}")
    finally:
        cur.close()
        conn_read.close()