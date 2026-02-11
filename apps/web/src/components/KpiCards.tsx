import { SqlArtifact } from '@/lib/types';

export function KpiCards({ artifact }: { artifact: SqlArtifact }) {
  const first = artifact.result.rows[0] ?? [];
  return (
    <div className="card" style={{ display: 'flex', gap: 12 }}>
      {artifact.result.columns.map((col, idx) => (
        <div key={col}>
          <strong>{col}</strong>
          <div>{String(first[idx] ?? '-') }</div>
        </div>
      ))}
    </div>
  );
}
