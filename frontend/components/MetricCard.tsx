type MetricCardProps = {
    title: string;
    value: string;
    emoji: string;
};

export default function MetricCard({
    title,
    value,
    emoji,
}: MetricCardProps) {
    return (
        <article className="flex flex-col gap-2 rounded-[20px] border border-border bg-surface p-4">
            <span className="text-2xl">{emoji}</span>
            <span className="text-2xl font-bold text-foreground tabular-nums">{value}</span>
            <span className="text-xs font-medium text-foreground-muted">{title}</span>
        </article>
    );
}