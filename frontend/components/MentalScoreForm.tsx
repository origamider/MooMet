"use client";

import { useActionState } from "react";
import {submitMentalScoreForm, FormState} from "../app/actions";

export default function MentalScoreForm(){
  const [state, formAction, isPending] = useActionState(submitMentalScoreForm, {})
  return (
    <form action={formAction}>
      <label>
        SNS利用時間
        <input name="daily_social_media_hours"></input>
      </label>
      <label>
        AI利用時間
        <input name="daily_ai_tool_usage_hours"></input>
      </label>
      <label>
        睡眠時間
        <input name="sleep_hours"></input>
      </label>
      <label>
        運動時間
        <input name="physical_activity_hours"></input>
      </label>
      {state.error && <p>{state.error}</p>}
      <button type="submit" disabled={isPending}>
        {isPending ? "送信中..." : "更新する"}
      </button>
    </form>
  )
}