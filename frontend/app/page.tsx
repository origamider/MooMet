import MetricCard from "../components/MetricCard";
import MentalScoreForm from "../components/MentalScoreForm";
import { formatMinutes } from "../utils/formatMinutes";
import { getLatestRecord } from "@/lib/backend";

export default async function Home() {
  const latest_data = await getLatestRecord();

  console.log(latest_data);
  // 三項演算子で配列化しておく。(mapでまとめて描画するため)
  const metrics = latest_data ?
  [
    {
      title: "メンタルスコア",
      value: `${Math.round(latest_data.mental_health_score)}点`,
    },
    {
      title: "睡眠時間",
      value: `${latest_data.sleep_hours}時間`,
    },
    {
      title: "SNS利用時間",
      value: `${latest_data.daily_social_media_hours}時間`,
    },
    {
      title: "AI利用時間",
      value: `${latest_data.daily_ai_tool_usage_hours}時間`,
    },
    {
      title: "運動時間",
      value: `${latest_data.physical_activity_hours}時間`,
    }
  ] : [];
  return (
    <main>
      <h1>本日のコンディション</h1>
      {
        latest_data ? (
          <>
            <p>{latest_data.record_date}</p>
            <section>
              {metrics.map((metric) => (
                <MetricCard key={metric.title} title={metric.title} value={metric.value} />
              ))}
            </section>
          </>
        ) : (
          <p>まだ記録がありません。下のフォームから入力してください。</p>
        )
      }
      <section>
        <h2>現在の記録を入力</h2>
        <MentalScoreForm/>
      </section>
    </main>
  );
}

