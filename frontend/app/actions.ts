"use server";
import { revalidatePath } from "next/cache";
import { submitRecord } from "@/lib/backend";
export type FormState = {
  error?: string;
};



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
  revalidatePath("/");
  return {};
}