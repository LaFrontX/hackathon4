# api-service/app/database.py
import os
from sqlalchemy import create_engine, Column, Integer, String, Float
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
# Путь к БД (локальная папка data)
DB_PATH = os.getenv("DB_PATH", "/data/driver.db")
DATABASE_URL = f"sqlite:///{DB_PATH}"
engine = create_engine(DATABASE_URL, connect_args={"check_same_thread": False})
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()
class Driver(Base):
    __tablename__ = "drivers"
    
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String)
    birth_date = Column(String)
    car_number = Column(String)
    speed = Column(Float, default=0)
    balance = Column(Float, default=0)
    trips_per_month = Column(Integer, default=0)
    avg_speed_on_section = Column(Float, default=0)
    fines_count = Column(Integer, default=0)
    rating = Column(Float, default=5.0)
    offered_subscription = Column(String, nullable=True)
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
def init_db():
    Base.metadata.create_all(bind=engine)