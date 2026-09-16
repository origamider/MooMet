import MetricCard from "../components/MetricCard";
import MentalScoreForm from "../components/MentalScoreForm";
import { getLatestRecord } from "@/lib/backend";

export default async function Home() {
  const latest_data = await getLatestRecord();

  const metrics = latest_data
    ? [
        { title: "睡眠時間", value: `${latest_data.sleep_hours}時間`, emoji: "💤" },
        { title: "SNS利用時間", value: `${latest_data.daily_social_media_hours}時間`, emoji: "📱" },
        { title: "AI利用時間", value: `${latest_data.daily_ai_tool_usage_hours}時間`, emoji: "🤖" },
        { title: "運動時間", value: `${latest_data.physical_activity_hours}時間`, emoji: "🏃" },
      ]
    : [];

  return (
    <main className="mx-auto flex min-h-full w-full max-w-5xl flex-col gap-6 p-6">
      <div className="flex items-center justify-between">
        <span className="text-xl font-extrabold tracking-tight text-foreground">
          Moo<span className="text-accent">Met</span>
        </span>
        <button className="rounded-full border border-border bg-transparent px-5 py-2 text-sm font-medium text-foreground">
          ログイン
        </button>
      </div>

      <div className="flex flex-col gap-6 md:flex-row md:items-start">
        <section className="flex flex-col gap-5 md:basis-7/10">
          <div className="rounded-[28px] border border-border bg-surface p-7">
            <p className="text-sm tracking-wide text-foreground-muted">今日のコンディション</p>
            {latest_data ? (
              <>
                <div className="mt-2 flex items-end gap-3">
                  <span className="text-6xl font-black leading-none text-accent tabular-nums">
                    {Math.round(latest_data.mental_health_score)}
                  </span>
                  <span className="pb-2 text-lg font-bold text-foreground-muted">点</span>
                </div>
                <p className="mt-3 text-sm text-foreground-muted">{latest_data.record_date}</p>
              </>
            ) : (
              <p className="mt-3 text-sm text-foreground-muted">
                まだ記録がありません。右のフォームから入力してください。
              </p>
            )}
          </div>

          {latest_data && (
            <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
              {metrics.map((metric) => (
                <MetricCard
                  key={metric.title}
                  title={metric.title}
                  value={metric.value}
                  emoji={metric.emoji}
                />
              ))}
            </div>
          )}
        </section>

        <section className="rounded-[28px] border border-border bg-surface p-6 md:basis-3/10">
          <h2 className="text-lg font-bold text-foreground">今日の記録</h2>
          <p className="mb-5 mt-1 text-xs text-foreground-muted">4つの項目を入力してください</p>
          <MentalScoreForm />
        </section>
      </div>
    </main>
  );
}

