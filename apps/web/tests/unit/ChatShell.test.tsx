import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { vi } from 'vitest';
import { ChatShell } from '@/components/ChatShell';

const useChatMock = vi.fn();

vi.mock('ai/react', () => ({
  useChat: (...args: unknown[]) => useChatMock(...args)
}));

it('shows an actionable error message when the assistant request fails', async () => {
  const reloadMock = vi.fn();

  useChatMock.mockReturnValue({
    messages: [],
    append: vi.fn(),
    status: 'ready',
    error: new Error('Backend unavailable'),
    reload: reloadMock
  });

  render(<ChatShell />);

  expect(screen.getByRole('alert')).toHaveTextContent(/unable to fetch an assistant response/i);

  await userEvent.click(screen.getByRole('button', { name: /retry/i }));
  expect(reloadMock).toHaveBeenCalledTimes(1);
});
