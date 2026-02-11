import { SqlArtifact } from '@/lib/types';

export function DataTable({ artifact }: { artifact: SqlArtifact }) {
  return (
    <div className="card" aria-label="Résultats SQL tabulaires">
      <table>
        <thead>
          <tr>
            {artifact.result.columns.map((c) => (
              <th key={c}>{c}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {artifact.result.rows.map((row, idx) => (
            <tr key={idx}>
              {row.map((cell, cIdx) => (
                <td key={`${idx}-${cIdx}`}>{String(cell)}</td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
