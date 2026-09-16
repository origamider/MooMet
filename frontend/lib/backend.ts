import { RecordInput, RecordResult } from "@/types/record";

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

export async function getLatestRecord(userId: string = "default_user"): Promise<RecordResult | null>{
  const query = new URLSearchParams({ user_id : userId });
  const response = await fetch(
    `${process.env.BACKEND_URL}/get_latest_data?${query}`,
    {
      method: "GET",
      cache: "no-store"
    }
  );
  if (response.status === 404) {
    return null;
  }
  if (!response.ok) {
    throw new Error('MooMet記録情報の取得に失敗しました');
  }
  return response.json();
}
