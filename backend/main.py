import joblib
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field
from pathlib import Path
import numpy as np
from fastapi.middleware.cors import CORSMiddleware
from datetime import date
from db import init_db, upsert_record

ml_models = {}
MODEL_PATH = Path(__file__).resolve().parent.parent / "ml" / "model.sklearn"

class RecordRequest(BaseModel):
    """メンタルヘルススコア予測&記録の入力型
    ge -> 最小値設定
    """
    daily_social_media_hours: float = Field(ge=0, description="1日のSNS利用時間")
    daily_ai_tool_usage_hours: float = Field(ge=0, description="1日のAIツール利用時間")
    sleep_hours: float = Field(ge=0, description="1日の睡眠時間")
    physical_activity_hours: float = Field(ge=0, description="1日の運動時間")

class RecordResponse(BaseModel):
    """メンタルヘルススコア予測&記録の出力型
    ge -> 最小値設定
    """
    record_date: date
    daily_social_media_hours: float
    daily_ai_tool_usage_hours: float
    sleep_hours: float
    physical_activity_hours: float
    mental_health_score: float

    

async def lifespan(app: FastAPI):
    ml_models['linear_regression'] = joblib.load(MODEL_PATH)
    init_db()
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

@app.post("/records", response_model=RecordResponse)
def record(request: RecordRequest):
    """入力値からメンタルスコアを予測し、DEFAULT_USER_IDの当日レコードとして保存する。

    「予測」と「保存」を1回の呼び出しで完結させる。手入力フォーム・将来の自動計測
    のどちらのユースケースでも、予測結果を保存せずに使うことはないため、予測専用の
    エンドポイントは設けない。

    同日に複数回呼び出された場合は、直近の呼び出し内容で上書き（upsert）する。

    Args:
        request: SNS/AI/睡眠/運動の各時間を含むリクエストボディ。

    Returns:
        RecordResponse: 保存後の入力値と予測済みスコア。
    """
    try:
        # scikit-learnは(n_samples, n_features)を入力とするため2重で囲む。
        features = np.array([[
            request.daily_social_media_hours,
            request.daily_ai_tool_usage_hours,
            request.sleep_hours,
            request.physical_activity_hours
        ]])
        predicted_mental_score = float(ml_models['linear_regression'].predict(features)[0])
        row = upsert_record(
            daily_social_media_hours=request.daily_social_media_hours,
            daily_ai_tool_usage_hours=request.daily_ai_tool_usage_hours,
            sleep_hours=request.sleep_hours,
            physical_activity_hours=request.physical_activity_hours,
            mental_health_score=predicted_mental_score
        )
        return RecordResponse(**dict(row))
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
