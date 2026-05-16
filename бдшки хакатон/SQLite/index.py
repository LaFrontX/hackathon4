"""
Генерация тестовых водителей и связанных поездок/транзакций в drivers.db.
Зависимость: pip install faker
"""

from __future__ import annotations

import random
import sqlite3
import string
import sys
from datetime import date, datetime, timedelta
from pathlib import Path

from faker import Faker

fake = Faker("ru_RU")

REFERENCE_DATE = date(2026, 5, 15)
MIN_AGE = 18
MAX_AGE = 67
DRIVERS_COUNT = 30
MAX_FINES = 30
ID_LENGTH = 8

PLATE_LETTERS = "АВЕКМНОРСТУХ"

REGION_CODES = (
    [f"{i:02d}" for i in range(1, 100)]
    + [
        "102",
        "113",
        "116",
        "121",
        "122",
        "123",
        "124",
        "125",
        "126",
        "134",
        "136",
        "138",
        "142",
        "150",
        "152",
        "154",
        "159",
        "161",
        "163",
        "164",
        "173",
        "177",
        "178",
        "186",
        "190",
        "196",
        "197",
        "198",
        "199",
        "250",
        "550",
        "602",
        "702",
        "716",
        "725",
        "750",
        "761",
        "763",
        "774",
        "777",
        "790",
        "797",
        "799",
    ]
)

DRIVER_CHARACTERISTICS = (
    "Аккуратный городской водитель",
    "Частые длинные поездки по МКАД",
    "Предпочитает ночные рейсы",
    "Осторожный стиль, без резких манёвров",
    "Часто ездит в час пик",
    "Много коротких заказов в центре",
    "Регулярные межгородские рейсы",
    "Новичок на платформе",
    "Опытный водитель такси",
)

SUBSCRIPTION_LEVELS = ("Базовый", "Стандартный", "Премиум", "Корпоративный")


def db_path() -> Path:
    return Path(__file__).resolve().parent / "drivers.db"


def generate_unique_id(existing_ids: set[str]) -> str:
    alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    while True:
        new_id = "".join(random.choices(alphabet, k=ID_LENGTH))
        if new_id not in existing_ids:
            existing_ids.add(new_id)
            return new_id


def random_birth_date(ref: date, min_age: int, max_age: int) -> date:
    youngest_birth = ref.replace(year=ref.year - min_age)
    oldest_birth = ref.replace(year=ref.year - max_age)
    days_range = (youngest_birth - oldest_birth).days
    return oldest_birth + timedelta(days=random.randint(0, days_range))


def recommended_subscription_for_driver(trips_per_month: int, balance: float) -> str:
    score = trips_per_month + (balance / 1000.0)
    if score < 25:
        return "Базовый"
    if score < 60:
        return "Стандартный"
    if score < 110:
        return "Премиум"
    return "Корпоративный"


def generate_plate_number() -> dict[str, str]:
    letter1 = random.choice(PLATE_LETTERS)
    digits = f"{random.randint(0, 999):03d}"
    letters2 = "".join(random.choices(PLATE_LETTERS, k=2))
    region = random.choice(REGION_CODES)
    return {
        "plate_letter1": letter1,
        "plate_number": digits,
        "plate_letters2": letters2,
        "plate_region": region,
        "plate_full": f"{letter1}{digits}{letters2}{region}",
        "plate_formatted": f"{letter1} {digits} {letters2} {region}",
    }


def generate_driver(existing_ids: set[str]) -> dict:
    birth_date = random_birth_date(REFERENCE_DATE, MIN_AGE, MAX_AGE)
    age_on_ref = (REFERENCE_DATE - birth_date).days // 365
    plate = generate_plate_number()
    trips_per_month = random.randint(2, 120)
    balance = round(random.uniform(500, 5000), 2)

    return {
        "driver_id": generate_unique_id(existing_ids),
        "full_name": fake.name(),
        "birth_date": birth_date.isoformat(),
        "age_on_2026_05_15": age_on_ref,
        "balance": balance,
        "trips_per_month": trips_per_month,
        "avg_speed_kph": round(random.uniform(25, 95), 1),
        "fines_count": random.randint(0, MAX_FINES),
        "driver_characteristic": random.choice(DRIVER_CHARACTERISTICS),
        "recommended_subscription": recommended_subscription_for_driver(
            trips_per_month, balance
        ),
        **plate,
    }


def generate_drivers(count: int = DRIVERS_COUNT) -> list[dict]:
    ids: set[str] = set()
    return [generate_driver(ids) for _ in range(count)]


def random_trip_window(ref: date) -> tuple[date, datetime, datetime]:
    """Случайная поездка за последние ~120 дней до ref."""
    day_offset = random.randint(0, 120)
    trip_day = ref - timedelta(days=day_offset)
    start_h = random.randint(6, 22)
    start_m = random.choice([0, 15, 30, 45])
    duration_min = random.randint(15, 240)
    start_dt = datetime.combine(trip_day, datetime.min.time()) + timedelta(
        hours=start_h, minutes=start_m
    )
    end_dt = start_dt + timedelta(minutes=duration_min)
    return trip_day, start_dt, end_dt


def distance_from_trip(start: datetime, end: datetime, avg_speed_kph: float) -> float:
    hours = max((end - start).total_seconds() / 3600.0, 1 / 60)
    base = avg_speed_kph * hours * random.uniform(0.55, 1.05)
    return round(max(0.5, min(base, 600.0)), 2)


def ensure_trips_has_distance_km(conn: sqlite3.Connection) -> None:
    cur = conn.execute("PRAGMA table_info(trips)")
    cols = [row[1] for row in cur.fetchall()]
    if "distance_km" not in cols:
        conn.execute("ALTER TABLE trips ADD COLUMN distance_km REAL")
        conn.commit()


def clear_demo_data(conn: sqlite3.Connection) -> None:
    conn.execute("PRAGMA foreign_keys = OFF")
    conn.execute("DELETE FROM transactions")
    conn.execute("DELETE FROM trips")
    conn.execute("DELETE FROM drivers")
    conn.commit()
    conn.execute("PRAGMA foreign_keys = ON")


def persist_dataset(conn: sqlite3.Connection, drivers: list[dict]) -> None:
    """
    Записывает водителей, для каждого — несколько поездок и транзакций.
    Имя колонки в БД: recommeneded_subscription (как в sqlite-схеме).
    """
    ensure_trips_has_distance_km(conn)
    clear_demo_data(conn)

    insert_driver = """
        INSERT INTO drivers (
            driver_id,
            full_name,
            birth_date,
            balance,
            trips_per_month,
            avg_speed_kph,
            plate_letter1,
            plate_number,
            plate_letters2,
            plate_region,
            plate_full,
            driver_characteristic,
            recommeneded_subscription,
            fines_count
        ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)
    """

    insert_trip = """
        INSERT INTO trips (
            driver_id,
            trip_date,
            start_datetime,
            end_datetime,
            distance_km
        ) VALUES (?,?,?,?,?)
    """

    insert_tx = """
        INSERT INTO transactions (
            driver_id,
            trip_id,
            amount,
            operation_datetime
        ) VALUES (?,?,?,?)
    """

    cur = conn.cursor()

    for d in drivers:
        cur.execute(
            insert_driver,
            (
                d["driver_id"],
                d["full_name"],
                d["birth_date"],
                d["balance"],
                d["trips_per_month"],
                d["avg_speed_kph"],
                d["plate_letter1"],
                d["plate_number"],
                d["plate_letters2"],
                d["plate_region"],
                d["plate_full"],
                d["driver_characteristic"],
                d["recommended_subscription"],
                d["fines_count"],
            ),
        )

        trips_for_driver = random.randint(2, 7)
        for _ in range(trips_for_driver):
            trip_day, start_dt, end_dt = random_trip_window(REFERENCE_DATE)
            distance_km = distance_from_trip(start_dt, end_dt, d["avg_speed_kph"])

            cur.execute(
                insert_trip,
                (
                    d["driver_id"],
                    trip_day.isoformat(),
                    start_dt.isoformat(sep=" ", timespec="seconds"),
                    end_dt.isoformat(sep=" ", timespec="seconds"),
                    distance_km,
                ),
            )
            trip_id = cur.lastrowid

            pay_dt = end_dt + timedelta(minutes=random.randint(0, 45))
            amount = round(random.uniform(120, 4500), 2)
            cur.execute(
                insert_tx,
                (
                    d["driver_id"],
                    trip_id,
                    amount,
                    pay_dt.isoformat(sep=" ", timespec="seconds"),
                ),
            )

    conn.commit()


def print_db_summary(conn: sqlite3.Connection, path: Path) -> None:
    cur = conn.cursor()
    drivers_n = cur.execute("SELECT COUNT(*) FROM drivers").fetchone()[0]
    trips_n = cur.execute("SELECT COUNT(*) FROM trips").fetchone()[0]
    tx_n = cur.execute("SELECT COUNT(*) FROM transactions").fetchone()[0]
    print(f"\nБаза: {path}")
    print(f"  drivers: {drivers_n}, trips: {trips_n}, transactions: {tx_n}")


def configure_console_utf8() -> None:
    """Чтобы кириллица и символ рубля не ломали вывод в Windows-терминале."""
    if hasattr(sys.stdout, "reconfigure"):
        try:
            sys.stdout.reconfigure(encoding="utf-8")
        except (OSError, ValueError):
            pass


if __name__ == "__main__":
    configure_console_utf8()
    path = db_path()
    drivers = generate_drivers()

    with sqlite3.connect(path) as conn:
        persist_dataset(conn, drivers)
        print_db_summary(conn, path)

    for i, d in enumerate(drivers, 1):
        print(
            f"{i:02d}. {d['driver_id']} | {d['full_name']} | "
            f"номер {d['plate_formatted']} | "
            f"род. {d['birth_date']} ({d['age_on_2026_05_15']} лет) | "
            f"баланс {d['balance']:.2f} руб. | "
            f"{d['trips_per_month']} поезд./мес | "
            f"{d['avg_speed_kph']} км/ч | штрафов: {d['fines_count']} | "
            f"{d['driver_characteristic']} | подписка: {d['recommended_subscription']}"
        )

    assert len({d["driver_id"] for d in drivers}) == len(drivers)
    assert all(500 <= d["balance"] <= 5000 for d in drivers)
    assert all(0 <= d["fines_count"] <= MAX_FINES for d in drivers)
    for d in drivers:
        assert len(d["plate_letter1"]) == 1
        assert len(d["plate_number"]) == 3
        assert len(d["plate_letters2"]) == 2
        assert d["recommended_subscription"] in SUBSCRIPTION_LEVELS

    print(f"\nГотово. Данные сохранены в:\n  {path}")
