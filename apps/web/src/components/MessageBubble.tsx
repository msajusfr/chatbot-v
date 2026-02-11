import { MarkdownBlock } from './MarkdownBlock';
import { SqlArtifactRenderer } from './SqlArtifactRenderer';
import { SqlArtifact } from '@/lib/types';

export function MessageBubble({ role, parts }: { role: string; parts: any[] }) {
  return (
    <article className={`card ${role === 'user' ? 'message-user' : 'message-assistant'}`}>
      <strong>{role === 'user' ? 'Vous' : 'Assistant'}</strong>
      {parts.map((part, idx) => {
        if (part.type === 'text') return <MarkdownBlock key={idx} content={part.text ?? ''} />;
        if (part.type === 'data-sql') return <SqlArtifactRenderer key={idx} artifact={part.data as SqlArtifact} />;
        return null;
      })}
    </article>
  );
}
