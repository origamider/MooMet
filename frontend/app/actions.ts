"use server";

export type FormState = {
  error?: string;
};

export type RecordInput = {
  daily_social_media_hours: number;
  daily_ai_tool_usage_hours: number;
  sleep_hours: number;
  physical_activity_hours: number;
};

export type RecordResult = {
  daily_social_media_hours: number;
  daily_ai_tool_usage_hours: number;
  sleep_hours: number;
  physical_activity_hours: number;
  record_date: string;
  mental_health_score: number;
};

export async function submitRecord(input: RecordInput): Promise<RecordResult> {
  const res = await fetch(
    `${process.env.BACKEND_URL}/records`,
    {
      method: "POST",
      body: JSON.stringify(input),
      headers: {
        'Content-Type': 'application/json'
      }
    }
  );
  if(!res.ok) {
    throw new Error('Mental Health Score record Error');
  }
  return res.json()
}



export async function submitMentalScoreForm(previousState: any, formData: FormData){
  // 引数および返り値の型注釈を指定した関数。
  function checkInputValue(key: string): number {
    const raw = formData.get(key);// 指定したidまたはnameを使ってformから取得した値。
    const value = Number(raw);
    if(raw === null || Number.isNaN(value) || value < 0) {
      throw new Error(`invalid error for ${key}`); // テンプレートリテラルを使って変数を埋める。
    }
    return value;
  };
  try{
    const input = {
      daily_social_media_hours: checkInputValue('daily_social_media_hours'),
      daily_ai_tool_usage_hours: checkInputValue("daily_ai_tool_usage_hours"),
      sleep_hours: checkInputValue("sleep_hours"),
      physical_activity_hours: checkInputValue("physical_activity_hours")
    };
    await submitRecord(input);
  } catch (e) {
    return {error: "送信に失敗しました"};
  }
  return {};
}