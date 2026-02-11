import { SqlArtifact } from '@/lib/types';

export function VizRenderer({ artifact }: { artifact: SqlArtifact }) {
  return (
    <div className="card" aria-label="Visualisation recommandée">
      <strong>Visualisation recommandée:</strong> {artifact.presentationHints.primary}
      <p>Ce mock remplace un composant de charting réel.</p>
    </div>
  );
}
