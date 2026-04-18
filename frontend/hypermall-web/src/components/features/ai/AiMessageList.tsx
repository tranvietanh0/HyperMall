import { Link, useNavigate } from 'react-router-dom';
import clsx from 'clsx';
import type { AiChatUiMessage } from '@/types';
import AiSuggestedProducts from './AiSuggestedProducts';

type AiMessageListProps = {
  messages: AiChatUiMessage[];
};

export default function AiMessageList({ messages }: AiMessageListProps) {
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
    </div>
  );
}
