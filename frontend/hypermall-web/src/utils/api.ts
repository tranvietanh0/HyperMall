import type { ErrorResponse } from '@/types';

type ApiLikeError = {
  response?: {
    data?: Partial<ErrorResponse>;
  };
};

export function getErrorMessage(error: unknown, fallback: string): string {
  if (typeof error === 'object' && error !== null) {
    const apiError = error as ApiLikeError;
    const message = apiError.response?.data?.message;
    if (typeof message === 'string' && message.trim()) {
      return message;
    }
  }

  return fallback;
}
