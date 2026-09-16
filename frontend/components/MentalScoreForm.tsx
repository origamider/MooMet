"use client";

import { useActionState } from "react";
import {submitMentalScoreForm, FormState} from "../app/actions";

const fields = [
  { name: "daily_social_media_hours", label: "SNS利用時間", emoji: "📱" },
  { name: "daily_ai_tool_usage_hours", label: "AI利用時間", emoji: "🤖" },
  { name: "sleep_hours", label: "睡眠時間", emoji: "💤" },
  { name: "physical_activity_hours", label: "運動時間", emoji: "🏃" },
] as const;

export default function MentalScoreForm(){
  const [state, formAction, isPending] = useActionState(submitMentalScoreForm, {})
  return (
    <form action={formAction} className="flex flex-col gap-4">
      {fields.map((field) => (
        <label key={field.name} className="flex flex-col gap-1.5 text-sm font-medium text-foreground">
          <span className="flex items-center gap-1.5">
            <span>{field.emoji}</span>
            {field.label}
          </span>
          <input
            type="number"
            name={field.name}
            min="0"
            max="24"
            step="0.1"
            required
            className="rounded-[14px] border border-border bg-background px-3.5 py-2.5 text-base text-foreground outline-accent"
          />
        </label>
      ))}
      {state.error && <p className="text-sm text-accent">{state.error}</p>}
      <button
        type="submit"
        disabled={isPending}
        className="mt-1 rounded-2xl bg-accent px-4 py-3 text-sm font-bold text-surface disabled:opacity-60"
      >
        {isPending ? "送信中..." : "記録する"}
      </button>
    </form>
  )
}