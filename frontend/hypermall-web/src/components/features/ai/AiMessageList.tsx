import { Link, useNavigate } from 'react-router-dom';
import clsx from 'clsx';
import type { AiChatUiMessage } from '@/types';
import AiSuggestedProducts from './AiSuggestedProducts';

type AiMessageListProps = {
  messages: AiChatUiMessage[];
  isTyping?: boolean;
};

export default function AiMessageList({ messages, isTyping = false }: AiMessageListProps) {
  const navigate = useNavigate();

  return (
    <div className="flex flex-col gap-3">
      {messages.map((message) => (
        <div
          key={message.id}
          className={clsx('flex', message.role === 'user' ? 'justify-end' : 'justify-start')}
        >
          <div
            className={clsx(
              'max-w-[88%] rounded-2xl px-4 py-3 text-sm shadow-sm',
              message.role === 'user'
                ? 'bg-primary-900 text-white'
                : 'border border-secondary-100 bg-white text-secondary-700'
            )}
          >
            <p className="whitespace-pre-line leading-6">{message.content}</p>
            {message.degraded ? (
              <p className="mt-2 text-[11px] font-semibold uppercase tracking-wide text-accent-600">
                fallback mode
              </p>
            ) : null}

            {message.suggestedActions?.length ? (
              <div className="mt-3 flex flex-wrap gap-2">
                {message.suggestedActions.map((action) =>
                  action.type === 'link' ? (
                    <Link
                      key={`${message.id}-${action.label}`}
                      to={action.value}
                      className="rounded-full border border-primary-200 bg-primary-50 px-3 py-1 text-xs font-semibold text-primary-700 transition hover:bg-primary-100"
                    >
                      {action.label}
                    </Link>
                  ) : action.type === 'search' ? (
                    <button
                      key={`${message.id}-${action.label}`}
                      type="button"
                      onClick={() => navigate(`/search?q=${encodeURIComponent(action.value)}`)}
                      className="rounded-full border border-secondary-200 bg-secondary-50 px-3 py-1 text-xs font-semibold text-secondary-700 transition hover:bg-secondary-100"
                    >
                      {action.label}
                    </button>
                  ) : action.type === 'category' ? (
                    <button
                      key={`${message.id}-${action.label}`}
                      type="button"
                      onClick={() => navigate(`/products?category=${encodeURIComponent(action.value)}`)}
                      className="rounded-full border border-secondary-200 bg-secondary-50 px-3 py-1 text-xs font-semibold text-secondary-700 transition hover:bg-secondary-100"
                    >
                      {action.label}
                    </button>
                  ) : null
                )}
              </div>
            ) : null}

            <AiSuggestedProducts products={message.productSuggestions || []} />
          </div>
        </div>
      ))}

      {isTyping ? (
        <div className="flex justify-start">
          <div className="max-w-[88%] rounded-2xl border border-secondary-100 bg-white px-4 py-3 text-sm text-secondary-700 shadow-sm">
            <div className="flex items-center gap-3" aria-live="polite" aria-label="AI is typing">
              <div className="flex items-center gap-1.5">
                <span className="h-2.5 w-2.5 animate-bounce rounded-full bg-primary-400 [animation-delay:-0.3s]" />
                <span className="h-2.5 w-2.5 animate-bounce rounded-full bg-primary-500 [animation-delay:-0.15s]" />
                <span className="h-2.5 w-2.5 animate-bounce rounded-full bg-primary-600" />
              </div>
              <span className="text-xs font-medium text-secondary-500">AI is replying...</span>
            </div>
          </div>
        </div>
      ) : null}
    </div>
  );
}
