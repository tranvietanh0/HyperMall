import { useEffect, useMemo, useState } from 'react';
import { aiService } from '@/services/ai.service';
import type {
  AiChatContext,
  AiChatHistoryItem,
  AiChatRequest,
  AiChatResponse,
  AiChatUiMessage,
} from '@/types';
import { getErrorMessage } from '@/utils';

type PersistedState = {
  sessionId?: string;
  messages: AiChatUiMessage[];
};

const DEFAULT_WELCOME_MESSAGE: AiChatUiMessage = {
  id: 'welcome',
  role: 'assistant',
  content: 'Hi! I can help you find products, explain the current product features, or guide you through shopping on HyperMall.',
};

const STORAGE_KEY = import.meta.env.VITE_AI_CHAT_STORAGE_KEY || 'hypermall_ai_chat_v1';

export function useAiChat() {
  const [isOpen, setIsOpen] = useState(false);
  const [isSending, setIsSending] = useState(false);
  const [sessionId, setSessionId] = useState<string | undefined>();
  const [messages, setMessages] = useState<AiChatUiMessage[]>([DEFAULT_WELCOME_MESSAGE]);

  useEffect(() => {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) {
      return;
    }

    try {
      const parsed = JSON.parse(raw) as PersistedState;
      setSessionId(parsed.sessionId);
      setMessages(parsed.messages?.length ? parsed.messages : [DEFAULT_WELCOME_MESSAGE]);
    } catch {
      setMessages([DEFAULT_WELCOME_MESSAGE]);
    }
  }, []);

  useEffect(() => {
    const payload: PersistedState = { sessionId, messages };
    localStorage.setItem(STORAGE_KEY, JSON.stringify(payload));
  }, [messages, sessionId]);

  const history = useMemo<AiChatHistoryItem[]>(() => {
    return messages
      .filter((message) => message.id !== DEFAULT_WELCOME_MESSAGE.id)
      .slice(-6)
      .map((message) => ({ role: message.role, content: message.content }));
  }, [messages]);

  const sendMessage = async (content: string, context: AiChatContext) => {
    const trimmed = content.trim();
    if (!trimmed) {
      return;
    }

    const userMessage: AiChatUiMessage = {
      id: `user-${Date.now()}`,
      role: 'user',
      content: trimmed,
    };

    setMessages((current) => [...current, userMessage]);
    setIsSending(true);

    const request: AiChatRequest = {
      message: trimmed,
      sessionId,
      history,
      context,
    };

    try {
      const response: AiChatResponse = await aiService.chat(request);
      setSessionId(response.sessionId);
      setMessages((current) => [
        ...current,
        {
          id: `assistant-${Date.now()}`,
          role: 'assistant',
          content: response.message,
          degraded: response.degraded,
          suggestedActions: response.suggestedActions,
          productSuggestions: response.productSuggestions,
        },
      ]);
    } catch (error: unknown) {
      setMessages((current) => [
        ...current,
        {
          id: `assistant-fallback-${Date.now()}`,
          role: 'assistant',
          content: getErrorMessage(error, 'AI assistant is temporarily unavailable. Try searching for products or open a product detail page for more recommendations.'),
          degraded: true,
          suggestedActions: [
            { type: 'search', label: 'Search products', value: trimmed },
            { type: 'link', label: 'Browse catalog', value: '/products' },
          ],
        },
      ]);
    } finally {
      setIsSending(false);
    }
  };

  const clearConversation = () => {
    setSessionId(undefined);
    setMessages([DEFAULT_WELCOME_MESSAGE]);
    localStorage.removeItem(STORAGE_KEY);
  };

  return {
    isOpen,
    isSending,
    messages,
    open: () => setIsOpen(true),
    close: () => setIsOpen(false),
    toggle: () => setIsOpen((current) => !current),
    sendMessage,
    clearConversation,
  };
}
