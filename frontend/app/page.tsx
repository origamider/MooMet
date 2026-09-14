import MetricCard from "../components/MetricCard";
import { mockDashboardData } from "../data/mockDashboardData";
import { formatMinutes } from "../utils/formatMinutes";

export default function Home() {
  return (
    <main>
      <h1>本日のコンディション</h1>
      <p>{mockDashboardData.date}</p>

      <section>
        <MetricCard
          title="メンタルスコア"
          value={`${mockDashboardData.mentalHealthScore}点`}
        />

        <MetricCard
          title="睡眠時間"
          value={formatMinutes(mockDashboardData.sleepMinutes)}
        />

        <MetricCard
          title="SNS利用時間"
          value={formatMinutes(mockDashboardData.socialMediaMinutes)}
        />

        <MetricCard
          title="AI利用時間"
          value={formatMinutes(mockDashboardData.aiUsageMinutes)}
        />

        <MetricCard
          title="歩数"
          value={`${mockDashboardData.stepCount}歩`}
        />
        
      </section>
    </main>
  );
}

