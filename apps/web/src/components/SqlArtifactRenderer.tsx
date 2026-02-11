import { SqlArtifact } from '@/lib/types';
import { DataTable } from './DataTable';
import { KpiCards } from './KpiCards';
import { VizRenderer } from './VizRenderer';

export function SqlArtifactRenderer({ artifact }: { artifact: SqlArtifact }) {
  return (
    <section className="card">
      <h4>{artifact.title}</h4>
      <pre>{artifact.sql}</pre>
      {artifact.presentationHints.primary === 'kpi' ? <KpiCards artifact={artifact} /> : <VizRenderer artifact={artifact} />}
      <DataTable artifact={artifact} />
      {artifact.result.isFictional && <p><em>Données fictives (démo)</em></p>}
    </section>
  );
}
