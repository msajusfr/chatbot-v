import { NextRequest } from 'next/server';

const DEFAULT_BACKEND_URL = 'http://localhost:8080';

function normalizeToken(rawToken?: string) {
  if (!rawToken) {
    return '';
  }

  const trimmed = rawToken.trim();
  if (!trimmed) {
    return '';
  }

  return trimmed.replace(/^Bearer\s+/i, '');
}

export async function POST(req: NextRequest) {
  const backendUrl = process.env.BACKEND_URL ?? process.env.NEXT_PUBLIC_BACKEND_URL ?? DEFAULT_BACKEND_URL;
  const token = normalizeToken(
    process.env.CHATBOTV_INTERNAL_TOKEN ??
    process.env.BACKEND_INTERNAL_TOKEN ??
    process.env.INTERNAL_TOKEN
  );
  const incomingAuth = req.headers.get('Authorization');

  const headers = new Headers({
    'Content-Type': 'application/json'
  });

  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  } else if (incomingAuth) {
    headers.set('Authorization', incomingAuth);
  }

  const upstream = await fetch(`${backendUrl}/api/v1/chat/stream`, {
    method: 'POST',
    headers,
    body: await req.text(),
    cache: 'no-store'
  });

  return new Response(upstream.body, {
    status: upstream.status,
    headers: {
      'Content-Type': upstream.headers.get('Content-Type') ?? 'text/event-stream; charset=utf-8',
      'Cache-Control': upstream.headers.get('Cache-Control') ?? 'no-cache',
      Connection: upstream.headers.get('Connection') ?? 'keep-alive',
      'x-vercel-ai-ui-message-stream': upstream.headers.get('x-vercel-ai-ui-message-stream') ?? 'v1'
    }
  });
}
