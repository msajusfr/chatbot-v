'use client';

import { useMemo, useState } from 'react';
import { useChat } from 'ai/react';
import { MessageList } from './MessageList';

export function ChatShell() {
  const backendUrl = process.env.NEXT_PUBLIC_BACKEND_URL ?? 'http://localhost:8080';
  const [text, setText] = useState('');
  const api = useMemo(() => `${backendUrl}/api/v1/chat/stream`, [backendUrl]);

  const { messages, append, status, error, reload } = useChat({
    api,
    headers: { 'Content-Type': 'application/json' }
  });

  const isStreaming = status === 'streaming' || status === 'submitted';

  return (
    <main className="chat-layout">
      <section className="chat-window" aria-label="Professional assistant chat interface">
        <header className="chat-header">
          <p className="chat-eyebrow">Virtual Assistant</p>
          <h1>AI Assistant</h1>
          <p className="chat-subtitle">Ask focused questions and get responses streamed in real time.</p>
        </header>

        <MessageList messages={messages} isStreaming={isStreaming} />

        {error ? (
          <p className="chat-error" role="alert">
            Unable to fetch an assistant response. Please verify that the backend is reachable, then try again.
            <button type="button" className="chat-error-retry" onClick={() => reload()}>
              Retry
            </button>
          </p>
        ) : null}

        <form
          className="chat-form"
          onSubmit={(e) => {
            e.preventDefault();
            if (!text.trim()) return;
            append({ role: 'user', content: text });
            setText('');
          }}
        >
          <label htmlFor="q" className="sr-only">Ask your question</label>
          <input
            id="q"
            value={text}
            onChange={(e) => setText(e.target.value)}
            placeholder="Ask a question..."
            autoComplete="off"
            disabled={isStreaming}
          />
          <button type="submit" disabled={isStreaming || !text.trim()}>
            {isStreaming ? 'Streaming…' : 'Send'}
          </button>
        </form>
      </section>
    </main>
  );
}
