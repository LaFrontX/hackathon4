# init_db.py
import sqlite3
import os
DB_PATH = os.path.join(os.path.dirname(__file__), "data", "driver.db")
os.makedirs(os.path.dirname(DB_PATH), exist_ok=True)
conn = sqlite3.connect(DB_PATH)
cursor = conn.cursor()
cursor.execute('''
CREATE TABLE IF NOT EXISTS drivers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT,
    birth_date TEXT,
    car_number TEXT,
    speed REAL DEFAULT 0,
    balance REAL DEFAULT 0,
    trips_per_month INTEGER DEFAULT 0,
    avg_speed_on_section REAL DEFAULT 0,
    fines_count INTEGER DEFAULT 0,
    rating REAL DEFAULT 5,
    offered_subscription TEXT
)
''')
# Добавляем тестового водителя
cursor.execute("SELECT COUNT(*) FROM drivers")
if cursor.fetchone()[0] == 0:
    cursor.execute('''
    INSERT INTO drivers (name, birth_date, car_number, balance, trips_per_month)
    VALUES (?, ?, ?, ?, ?)
    ''', ("Тестовый Водитель", "1990-01-01", "А123БВ", 10000, 15))
    print("✅ Тестовый водитель создан")
conn.commit()
conn.close()
print("✅ БД инициализирована")