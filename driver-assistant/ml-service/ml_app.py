# api-service/app/main.py
from fastapi import FastAPI, Depends, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.orm import Session
from typing import List, Optional
from datetime import date
from pydantic import BaseModel
import httpx
from app.database import get_db, Driver
# Модели данных
class DriverResponse(BaseModel):
    id: int
    name: str
    birth_date: date
    speed: float
    balance: float
    offered_subscription: Optional[str] = None
    trips_per_month: int
    avg_speed_on_section: float
    fines_count: int
    car_number: str
    rating: float
    
    class Config:
        from_attributes = True
class TripUpdateRequest(BaseModel):
    distance_km: float
    avg_speed: float
    fuel_cost: float
# Создаем приложение
app = FastAPI(title="Driver Assistant API")
# CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)
ML_SERVICE_URL = "http://ml-service:5000"
@app.get("/")
def root():
    return {"status": "ok", "service": "Driver Assistant API"}
@app.get("/drivers/{driver_id}", response_model=DriverResponse)
def get_driver(driver_id: int, db: Session = Depends(get_db)):
    driver = db.query(Driver).filter(Driver.id
 == driver_id).first()
    if not driver:
        raise HTTPException(status_code=404, detail="Водитель не найден")
    return driver
@app.post("/drivers/{driver_id}/add-balance")
def add_balance(driver_id: int, amount: float, db: Session = Depends(get_db)):
    driver = db.query(Driver).filter(Driver.id
 == driver_id).first()
    if not driver:
        raise HTTPException(status_code=404, detail="Водитель не найден")
    driver.balance += amount
    db.commit()
    return {"new_balance": driver.balance}
@app.post("/drivers/{driver_id}/add-fine")
def add_fine(driver_id: int, db: Session = Depends(get_db)):
    driver = db.query(Driver).filter(Driver.id
 == driver_id).first()
    if not driver:
        raise HTTPException(status_code=404, detail="Водитель не найден")
    driver.fines_count += 1
    driver.rating = max(1.0, driver.rating - 0.5)
    db.commit()
    return {"fines_count": driver.fines_count, "rating": driver.rating}
@app.post("/drivers/{driver_id}/update-trip")
def update_trip(driver_id: int, trip_data: TripUpdateRequest, db: Session = Depends(get_db)):
    driver = db.query(Driver).filter(Driver.id
 == driver_id).first()
    if not driver:
        raise HTTPException(status_code=404, detail="Водитель не найден")
    
    old_trips = driver.trips_per_month
    driver.trips_per_month += 1
    
    if old_trips > 0:
        new_avg = (driver.avg_speed_on_section * old_trips + trip_data.avg_speed) / driver.trips_per_month
    else:
        new_avg = trip_data.avg_speed
    
    driver.avg_speed_on_section = round(new_avg, 1)
    driver.speed = driver.avg_speed_on_section
    
    if driver.balance >= trip_data.fuel_cost:
        driver.balance -= trip_data.fuel_cost
    
    db.commit()
    return {"trips_per_month": driver.trips_per_month, "balance": driver.balance}
@app.get("/drivers/{driver_id}/predict")
async def predict(driver_id: int, db: Session = Depends(get_db)):
    driver = db.query(Driver).filter(Driver.id
 == driver_id).first()
    if not driver:
        raise HTTPException(status_code=404, detail="Водитель не найден")
    
    try:
        async with httpx.AsyncClient(timeout=30.0) as client:
            response = await client.post
(
                f"{ML_SERVICE_URL}/predict_expenses",
                json={
                    "driver_id": driver_id,
                    "trips_per_month": driver.trips_per_month,
                    "avg_speed": driver.avg_speed_on_section,
                    "fines_count": driver.fines_count
                }
            )
            if response.status_code == 200:
                result = response.json()
                if result.get("offered_subscription"):
                    driver.offered_subscription = r
11:7
esult["offered_subscription"]
                    db.commit()
                return result
    except Exception as e:
        print(f"Ошибка ML: {e}")
    
    return {
        "predicted_expenses": driver.trips_per_month * 500,
        "recommendations": ["ML сервис временно недоступен"],
        "offered_subscription": None
    }
@app.get("/mobile/driver/{driver_id}")
def mobile_get_driver(driver_id: int, db: Session = Depends(get_db)):
    driver = db.query(Driver).filter(Driver.id
 == driver_id).first()
    if not driver:
        raise HTTPException(status_code=404, detail="Водитель не найден")
    return {
        "id": driver.id
,
        "name": driver.name
,
        "balance": driver.balance,
        "rating": driver.rating,
        "fines_count": driver.fines_count,
        "trips_per_month": driver.trips_per_month,
        "offered_subscription": driver.offered_subscription or "Нет"
    }