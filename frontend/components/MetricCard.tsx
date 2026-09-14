type MetricCardProps = {
    title: string;
    value: string;
};

export default function MetricCard({
    title,
    value,
}: MetricCardProps) {
    return (
        <article>
            <p>{title}</p>
            <p>{value}</p>
        </article>
    );
}