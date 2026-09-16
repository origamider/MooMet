"use client";

import { useActionState } from "react";
import {submitMentalScoreForm, FormState} from "../app/actions";

export default function MentalScoreForm(){
  const [state, formAction, isPending] = useActionState(submitMentalScoreForm, {})
  return (
    <form action={formAction}>
      <label>
        SNS利用時間
        <input
          type="number"
          name="daily_social_media_hours"
          min="0"
          max="24"
          step="0.1"
          required
        />
      </label>
      <label>
        AI利用時間
        <input
          type="number"
          name="daily_ai_tool_usage_hours"
          min="0"
          max="24"
          step="0.1"
          required
        />
      </label>
      <label>
        睡眠時間
        <input
          type="number"
          name="sleep_hours"
          min="0"
          max="24"
          step="0.1"
          required
        />
      </label>
      <label>
        運動時間
        <input
          type="number"
          name="physical_activity_hours"
          min="0"
          max="24"
          step="0.1"
          required
        />
      </label>
      {state.error && <p>{state.error}</p>}
      <button type="submit" disabled={isPending}>
        {isPending ? "送信中..." : "更新する"}
      </button>
    </form>
  )
}