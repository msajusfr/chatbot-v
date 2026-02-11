import { SqlArtifact } from '@/lib/types';

const PIE_COLORS = ['#3b82f6', '#f59e0b', '#10b981', '#ec4899', '#8b5cf6', '#06b6d4', '#ef4444', '#84cc16'];

type PieSlice = {
  label: string;
  value: number;
  color: string;
};

function extractPieData(artifact: SqlArtifact): PieSlice[] {
  const [labelIndex, valueIndex] = artifact.result.columns.length >= 2 ? [0, 1] : [0, -1];
  if (valueIndex < 0) return [];

  return artifact.result.rows
    .map((row, idx) => {
      const label = String(row[labelIndex] ?? `Item ${idx + 1}`);
      const rawValue = row[valueIndex];
      const value = typeof rawValue === 'number' ? rawValue : Number(rawValue);
      if (!Number.isFinite(value) || value <= 0) {
        return null;
      }
      return {
        label,
        value,
        color: PIE_COLORS[idx % PIE_COLORS.length]
      };
    })
    .filter((entry): entry is PieSlice => entry !== null);
}

function PieChart({ data }: { data: PieSlice[] }) {
  const total = data.reduce((acc, item) => acc + item.value, 0);
  if (!total) {
    return <p>Données insuffisantes pour afficher un pie chart.</p>;
  }

  let running = 0;
  const segments = data
    .map((item) => {
      const start = (running / total) * 360;
      running += item.value;
      const end = (running / total) * 360;
      return `${item.color} ${start}deg ${end}deg`;
    })
    .join(', ');

  return (
    <div>
      <div
        aria-label="Pie chart"
        style={{
          width: 160,
          height: 160,
          borderRadius: '50%',
          marginBottom: 12,
          background: `conic-gradient(${segments})`
        }}
      />
      <ul style={{ margin: 0, paddingLeft: 18 }}>
        {data.map((item) => (
          <li key={item.label}>
            <span
              aria-hidden="true"
              style={{
                display: 'inline-block',
                width: 10,
                height: 10,
                borderRadius: 2,
                marginRight: 8,
                backgroundColor: item.color
              }}
            />
            {item.label}: {item.value}
          </li>
        ))}
      </ul>
    </div>
  );
}

export function VizRenderer({ artifact }: { artifact: SqlArtifact }) {
  const isPie = artifact.presentationHints.primary === 'pie';
  const pieData = isPie ? extractPieData(artifact) : [];

  return (
    <div className="card" aria-label="Visualisation recommandée">
      <strong>Visualisation recommandée:</strong> {artifact.presentationHints.primary}
      {isPie ? <PieChart data={pieData} /> : <p>Ce mock remplace un composant de charting réel.</p>}
    </div>
  );
}
