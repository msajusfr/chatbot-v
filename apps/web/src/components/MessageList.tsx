import { MessageBubble } from './MessageBubble';

export function MessageList({ messages }: { messages: any[] }) {
  return (
    <section aria-live="polite" aria-atomic="false">
      {messages.map((m) => (
        <MessageBubble key={m.id} role={m.role} parts={m.parts ?? [{ type: 'text', text: m.content }]} />
      ))}
    </section>
  );
}
