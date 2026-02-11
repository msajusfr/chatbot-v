'use client';

import { useMemo, useState } from 'react';
import { useChat } from 'ai/react';
import { MessageList } from './MessageList';

export function ChatShell() {
  const backendUrl = process.env.NEXT_PUBLIC_BACKEND_URL ?? process.env.BACKEND_URL ?? 'http://localhost:8080';
  const [text, setText] = useState('');
  const transport = useMemo(() => ({
    api: `${backendUrl}/api/v1/chat/stream`,
    headers: { 'Content-Type': 'application/json' }
  }), [backendUrl]);

  const { messages, sendMessage, status } = useChat({ transport });

  return (
    <main>
      <h1>chatbot-v</h1>
      <MessageList messages={messages} />
      <form
        onSubmit={(e) => {
          e.preventDefault();
          if (!text.trim()) return;
          sendMessage({ text });
          setText('');
        }}
      >
        <label htmlFor="q">Message</label>
        <input id="q" value={text} onChange={(e) => setText(e.target.value)} />
        <button type="submit" disabled={status === 'streaming'}>Envoyer</button>
      </form>
    </main>
  );
}
