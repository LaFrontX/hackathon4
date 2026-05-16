"""
FastAPI — контракт из инструкции для Android (Retrofit ApiService).
Swagger: http://localhost:8000/docs
"""

from __future__ import annotations

import os
from typing import Any

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field

app = FastAPI(title="BlatPlat Mobile API", version="1.0.0")

DRIVERS: dict[int, dict[str, Any]] = {
    1: {
        "id": 1,
        "name": "Иванов Алексей",
        "balance": 1520.0,
        "rating": 4.7,
        "fines_count": int(os.getenv("DEMO_FINES_COUNT", "35")),
        "trips_per_month": 143,
        "offered_subscription": "М-11 «Нева» — сутки",
    },
}


class DriverResponse(BaseModel):
    id: int
    name: str
    balance: float
    rating: float
    fines_count: int = Field(serialization_alias="fines_count")
    trips_per_month: int = Field(serialization_alias="trips_per_month")
    offered_subscription: str = Field(serialization_alias="offered_subscription")


class PredictionResponse(BaseModel):
    driver_id: int = Field(serialization_alias="driver_id")
    predicted_expenses: float = Field(serialization_alias="predicted_expenses")
    confidence: float
    recommendations: list[str]
    offered_subscription: str | None = Field(serialization_alias="offered_subscription")
    discount_percent: int = Field(serialization_alias="discount_percent")


class TripRequest(BaseModel):
    distance_km: float = Field(serialization_alias="distance_km")
    avg_speed: float = Field(serialization_alias="avg_speed")
    fuel_cost: float = Field(serialization_alias="fuel_cost")


class BalanceResponse(BaseModel):
    new_balance: float = Field(serialization_alias="new_balance")


class FineResponse(BaseModel):
    fines_count: int = Field(serialization_alias="fines_count")
    rating: float


class TripResponse(BaseModel):
    trips_per_month: int = Field(serialization_alias="trips_per_month")
    balance: float


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok"}


@app.get("/mobile/driver/{driver_id}", response_model=DriverResponse)
def get_driver(driver_id: int) -> DriverResponse:
    row = DRIVERS.get(driver_id)
    if row is None:
        raise HTTPException(status_code=404, detail="driver not found")
    return DriverResponse(**row)


@app.get("/mobile/prediction/{driver_id}", response_model=PredictionResponse)
def get_prediction(driver_id: int) -> PredictionResponse:
    if driver_id not in DRIVERS:
        raise HTTPException(status_code=404, detail="driver not found")
    d = DRIVERS[driver_id]
    return PredictionResponse(
        driver_id=driver_id,
        predicted_expenses=890.0,
        confidence=0.87,
        recommendations=[
            "Оформите суточный абонемент М-11",
            "Снизьте среднюю скорость на 5 км/ч",
        ],
        offered_subscription=d["offered_subscription"],
        discount_percent=15,
    )


@app.post("/drivers/{driver_id}/add-balance", response_model=BalanceResponse)
def add_balance(driver_id: int, amount: float) -> BalanceResponse:
    row = DRIVERS.get(driver_id)
    if row is None:
        raise HTTPException(status_code=404, detail="driver not found")
    new_balance = float(row["balance"]) + amount
    if new_balance < 0:
        raise HTTPException(status_code=400, detail="insufficient funds")
    row["balance"] = new_balance
    return BalanceResponse(new_balance=new_balance)


@app.post("/drivers/{driver_id}/add-fine", response_model=FineResponse)
def add_fine(driver_id: int) -> FineResponse:
    row = DRIVERS.get(driver_id)
    if row is None:
        raise HTTPException(status_code=404, detail="driver not found")
    row["fines_count"] = int(row["fines_count"]) + 1
    row["rating"] = max(1.0, float(row["rating"]) - 0.1)
    return FineResponse(fines_count=int(row["fines_count"]), rating=float(row["rating"]))


@app.post("/drivers/{driver_id}/update-trip", response_model=TripResponse)
def update_trip(driver_id: int, trip_data: TripRequest) -> TripResponse:
    row = DRIVERS.get(driver_id)
    if row is None:
        raise HTTPException(status_code=404, detail="driver not found")
    row["trips_per_month"] = int(row["trips_per_month"]) + 1
    row["balance"] = float(row["balance"]) - trip_data.fuel_cost
    return TripResponse(trips_per_month=int(row["trips_per_month"]), balance=float(row["balance"]))
