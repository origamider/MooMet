import joblib
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from pathlib import Path
import numpy as np
from fastapi.middleware.cors import CORSMiddleware

ml_models = {}
MODEL_PATH = Path(__file__).resolve().parent.parent / "ml" / "model.sklearn"

class PredictionRequest(BaseModel):
    use_sns_hours: float
    use_ai_hours: float
    sleep_hours: float
    physical_activity_hours: float

class PredictionResponse(BaseModel):
    mental_health_score: float

async def lifespan(app: FastAPI):
    ml_models['linear_regression'] = joblib.load(MODEL_PATH)
    yield
    ml_models.clear()

app = FastAPI(lifespan=lifespan)
origins = [
    "http://localhost:3000"
]
app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.post("/predict", response_model=PredictionResponse)
def predict(request: PredictionRequest):
    try:
        # scikit-learnは(n_samples, n_features)を入力とするため2重で囲む。
        features = np.array([[
            request.use_sns_hours,
            request.use_ai_hours,
            request.sleep_hours,
            request.physical_activity_hours
        ]])
        predicted_mental_score = ml_models['linear_regression'].predict(features)
        return PredictionResponse(mental_health_score=float(predicted_mental_score[0]))
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
