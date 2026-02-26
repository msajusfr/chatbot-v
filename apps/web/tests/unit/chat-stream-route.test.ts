import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { NextRequest } from 'next/server';
import { POST } from '@/app/api/v1/chat/stream/route';

describe('POST /api/v1/chat/stream proxy route', () => {
  const originalEnv = process.env;

  beforeEach(() => {
    vi.restoreAllMocks();
    process.env = { ...originalEnv };
    delete process.env.BACKEND_URL;
    delete process.env.NEXT_PUBLIC_BACKEND_URL;
  });

  afterEach(() => {
    process.env = originalEnv;
  });

  it('falls back to IPv4 localhost when localhost is unreachable', async () => {
    const fetchMock = vi
      .fn()
      .mockRejectedValueOnce(new Error('connect ECONNREFUSED ::1:8080'))
      .mockResolvedValueOnce(
        new Response(JSON.stringify({ response: { answerMarkdown: 'ok' } }), {
          status: 200,
          headers: { 'Content-Type': 'application/json; charset=utf-8' }
        })
      );

    vi.stubGlobal('fetch', fetchMock);

    const req = new NextRequest('http://localhost/api/v1/chat/stream', {
      method: 'POST',
      body: JSON.stringify({ messages: [] })
    });

    const res = await POST(req);

    expect(fetchMock).toHaveBeenNthCalledWith(
      1,
      'http://localhost:8080/api/v1/chat',
      expect.objectContaining({ method: 'POST' })
    );
    expect(fetchMock).toHaveBeenNthCalledWith(
      2,
      'http://127.0.0.1:8080/api/v1/chat',
      expect.objectContaining({ method: 'POST' })
    );
    expect(res.status).toBe(200);
    await expect(res.text()).resolves.toBe('ok');
  });

  it('falls back to localhost defaults when configured backend is unreachable', async () => {
    process.env.BACKEND_URL = 'http://backend:8080';

    const fetchMock = vi
      .fn()
      .mockRejectedValueOnce(new Error('connect ECONNREFUSED backend:8080'))
      .mockResolvedValueOnce(
        new Response(JSON.stringify({ response: { answerMarkdown: 'ok' } }), {
          status: 200,
          headers: { 'Content-Type': 'application/json; charset=utf-8' }
        })
      );

    vi.stubGlobal('fetch', fetchMock);

    const req = new NextRequest('http://localhost/api/v1/chat/stream', {
      method: 'POST',
      body: JSON.stringify({ messages: [] })
    });

    const res = await POST(req);

    expect(fetchMock).toHaveBeenNthCalledWith(
      1,
      'http://backend:8080/api/v1/chat',
      expect.objectContaining({ method: 'POST' })
    );
    expect(fetchMock).toHaveBeenNthCalledWith(
      2,
      'http://localhost:8080/api/v1/chat',
      expect.objectContaining({ method: 'POST' })
    );
    expect(res.status).toBe(200);
    await expect(res.text()).resolves.toBe('ok');
  });

  it('returns 503 when no backend candidate is reachable', async () => {
    vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new Error('connection refused')));

    const req = new NextRequest('http://localhost/api/v1/chat/stream', {
      method: 'POST',
      body: JSON.stringify({ messages: [] })
    });

    const res = await POST(req);

    expect(res.status).toBe(503);
    expect(res.headers.get('x-proxy-error')).toContain('connection refused');
  });
});
