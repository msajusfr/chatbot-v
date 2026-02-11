import { MessageBubble } from './MessageBubble';

export function MessageList({ messages, isStreaming }: { messages: any[]; isStreaming: boolean }) {
  return (
    <section className="message-list" aria-live="polite" aria-atomic="false">
      {messages.length === 0 ? (
        <article className="empty-state">
          <p>Start with a clear question to receive a professional answer with live streaming output.</p>
        </article>
      ) : (
        messages.map((m) => (
          <MessageBubble key={m.id} role={m.role} parts={m.parts ?? [{ type: 'text', text: m.content }]} />
        ))
      )}

      {isStreaming ? (
        <article className="streaming-indicator" aria-label="Assistant is currently responding">
          <span />
          <span />
          <span />
          AI Assistant is typing...
        </article>
      ) : null}
    </section>
  );
}
