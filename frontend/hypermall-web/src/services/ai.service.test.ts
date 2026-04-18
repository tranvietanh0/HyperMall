import { beforeEach, describe, expect, it, vi } from 'vitest';
import { aiService } from './ai.service';
import { api } from './api.service';
import { API_ENDPOINTS } from '@/config/api.config';

vi.mock('./api.service', () => ({
  api: {
    post: vi.fn(),
  },
}));

describe('aiService.chat', () => {
  beforeEach(() => {
    vi.mocked(api.post).mockReset();
  });

  it('posts chat payload to the ai chat endpoint', async () => {
    vi.mocked(api.post).mockResolvedValue({
      data: {
        message: 'Hello from AI',
        sessionId: 'session-1',
        suggestedActions: [],
        productSuggestions: [],
        degraded: false,
      },
    });

    const payload = {
      message: 'Suggest a gaming mouse',
      sessionId: 'session-1',
      history: [],
      context: {
        pageType: 'search' as const,
        path: '/search?q=mouse',
      },
    };

    const response = await aiService.chat(payload);

    expect(response.message).toBe('Hello from AI');
    expect(vi.mocked(api.post)).toHaveBeenCalledWith(API_ENDPOINTS.AI.CHAT, payload);
  });
});
