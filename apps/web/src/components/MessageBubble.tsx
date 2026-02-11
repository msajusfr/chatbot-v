import { MarkdownBlock } from './MarkdownBlock';
import { SqlArtifactRenderer } from './SqlArtifactRenderer';
import { SqlArtifact } from '@/lib/types';

function getRoleLabel(role: string) {
  return role === 'user' ? 'You' : 'AI Assistant';
}

export function MessageBubble({ role, parts }: { role: string; parts: any[] }) {
  return (
    <article className={`message-bubble ${role === 'user' ? 'message-user' : 'message-assistant'}`}>
      <p className="message-role">{getRoleLabel(role)}</p>
      <div className="message-body">
        {parts.map((part, idx) => {
          if (part.type === 'text') return <MarkdownBlock key={idx} content={part.text ?? ''} />;
          if (part.type === 'data-sql') return <SqlArtifactRenderer key={idx} artifact={part.data as SqlArtifact} />;
          return null;
        })}
      </div>
    </article>
  );
}
