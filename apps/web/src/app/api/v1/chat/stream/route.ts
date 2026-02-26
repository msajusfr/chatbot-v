import { NextRequest } from 'next/server';

const DEFAULT_BACKEND_URL = 'http://localhost:8080';
const IPV4_LOCALHOST_BACKEND_URL = 'http://127.0.0.1:8080';

type BackendChatResponse = {
  response?: {
    answerMarkdown?: string;
  };
};

function buildBackendCandidates(configuredBackendUrl?: string) {
  const candidates = [configuredBackendUrl, DEFAULT_BACKEND_URL, IPV4_LOCALHOST_BACKEND_URL]
    .map((candidate) => candidate?.trim())
    .filter((candidate): candidate is string => Boolean(candidate));

  return [...new Set(candidates)];
}

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
  const configuredBackendUrl = process.env.BACKEND_URL ?? process.env.NEXT_PUBLIC_BACKEND_URL;
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

  const backendCandidates = buildBackendCandidates(configuredBackendUrl);

  const payload = await req.text();
  let lastError: unknown;

  for (const backendUrl of backendCandidates) {
    try {
      const upstream = await fetch(`${backendUrl}/api/v1/chat`, {
        method: 'POST',
        headers,
        body: payload,
        cache: 'no-store'
      });

      if (!upstream.ok) {
        return new Response(await upstream.text(), {
          status: upstream.status,
          headers: {
            'Content-Type': 'application/json; charset=utf-8',
            'Cache-Control': 'no-cache'
          }
        });
      }

      const response = (await upstream.json()) as BackendChatResponse;
      const answer = response.response?.answerMarkdown ?? '';

      return new Response(answer, {
        status: 200,
        headers: {
          'Content-Type': 'text/plain; charset=utf-8',
          'Cache-Control': 'no-cache'
        }
      });
    } catch (error) {
      lastError = error;
    }
  }

  return new Response(
    JSON.stringify({ error: 'Upstream chat backend unreachable' }),
    {
      status: 503,
      headers: {
        'Content-Type': 'application/json; charset=utf-8',
        'Cache-Control': 'no-cache',
        'x-proxy-error': lastError instanceof Error ? lastError.message : 'Upstream chat backend unreachable'
      }
    }
  );
}
