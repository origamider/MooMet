import sqlite3
from pathlib import Path
from datetime import date, datetime

DB_PATH = Path(__file__).resolve().parents[1] / "data" / "moomet.db"

DEFAULT_USER_ID = "default_user"

def init_db():
    """mental_score_recordsテーブルを作成する（存在すれば何もしない）。
    アプリ起動時（lifespan）に1回呼び出す想定。
    """
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    # ユーザー、日付ごとに日々の記録がどんどんDBに溜まっていく設計。
    # そうすることで、期間内の可視化もできるようになる。
    # ユーザーと記録日のペアで重複保存されないようにUNIQUE制約を使用。
    conn.execute("""
        CREATE TABLE IF NOT EXISTS mental_score_records (
            id INTEGER PRIMARY KEY,
            user_id TEXT NOT NULL DEFAULT 'default-user',
            record_date TEXT NOT NULL,
            daily_social_media_hours REAL NOT NULL,
            daily_ai_tool_usage_hours REAL NOT NULL,
            sleep_hours REAL NOT NULL,
            physical_activity_hours REAL NOT NULL,
            mental_health_score REAL NOT NULL,
            updated_at TEXT NOT NULL,
            UNIQUE (user_id, record_date)
            )
    """
    )
    conn.commit()
    conn.close()

def upsert_record(
    daily_social_media_hours: float,
    daily_ai_tool_usage_hours: float,
    sleep_hours: float,
    physical_activity_hours: float,
    mental_health_score: float,
    user_id: str = DEFAULT_USER_ID,
    record_date: date | None = None,
) -> sqlite3.Row:
    """
    指定ユーザー・日付のレコードを追加、既存なら上書きする。
    同じ(user_id, record_date)の組み合わせが既に存在する場合、
    daily_*系の入力値とmental_health_scoreを最新の値で上書きする。

    Args:
        daily_social_media_hours: 1日のSNS利用時間（時間）。
        daily_ai_tool_usage_hours: 1日のAIツール利用時間（時間）。
        sleep_hours: 1日の睡眠時間（時間）。
        physical_activity_hours: 1日の運動時間（時間）。
        mental_health_score: モデルによる予測済みのメンタルスコア。
        user_id: レコードの持ち主のユーザーID。省略時はDEFAULT_USER_ID。
        record_date: 記録日。省略時は実行時点の今日の日付を使う。
    
    Returns:
        sqlite3.Row: 保存後のレコード（1行）。
    """
    record_date = record_date or date.today()
    conn = sqlite3.connect(DB_PATH)
    # これをすることで、row["user_id"]のように名前で参照可能になる
    conn.row_factory = sqlite3.Row
    # SQliteにおけるUpsertの書き方参考。
    conn.execute(
        """
            INSERT INTO mental_score_records (
                user_id, record_date, daily_social_media_hours,
                daily_ai_tool_usage_hours, sleep_hours,
                physical_activity_hours, mental_health_score, updated_at
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (user_id, record_date)
            DO UPDATE SET
                daily_social_media_hours = excluded.daily_social_media_hours,
                daily_ai_tool_usage_hours = excluded.daily_ai_tool_usage_hours,
                sleep_hours = excluded.sleep_hours,
                physical_activity_hours = excluded.physical_activity_hours,
                mental_health_score = excluded.mental_health_score,
                updated_at = excluded.updated_at
        """,
        (
            user_id,
            record_date,
            daily_social_media_hours,
            daily_ai_tool_usage_hours,
            sleep_hours,
            physical_activity_hours,
            mental_health_score,
            datetime.now(),
        ),
    )
    results = conn.execute(
        """
            SELECT * FROM mental_score_records
            WHERE user_id = ? AND record_date = ?
        """,
        (user_id, record_date),
    )
    conn.commit()
    row = results.fetchone()
    conn.close()
    return row

def get_latest_record(user_id: str = DEFAULT_USER_ID) -> sqlite3.Row | None:
    """指定ユーザーの最新（record_dateが最も新しい）レコードを1件取得する。

    Args:
        user_id: 取得対象のユーザーID。省略時はDEFAULT_USER_ID。

    Returns:
        sqlite3.Row | None: 最新のレコード。1件も存在しない場合はNone。
    """
    
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    # プレイスフォルダー部分はsequenceとして渡す。
    cursor = conn.execute(
        """
        SELECT * FROM mental_score_records
        WHERE user_id = ?
        ORDER BY record_date DESC
        LIMIT 1
        """,
        (user_id,),
    )
    conn.commit()
    conn.close()
    return cursor.fetchone()