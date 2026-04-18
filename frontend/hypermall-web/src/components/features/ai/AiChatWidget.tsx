import { FormEvent, useMemo, useState } from 'react';
import { useLocation } from 'react-router-dom';
import { SparklesIcon, XMarkIcon } from '@heroicons/react/24/solid';
import { ArrowPathIcon } from '@heroicons/react/24/outline';
import { useAiChat } from '@/hooks/useAiChat';
import { useAppSelector } from '@/store/hooks';
import Button from '@components/common/Button';
import AiChatLauncher from './AiChatLauncher';
import AiMessageList from './AiMessageList';
import type { AiChatContext } from '@/types';

const starterPrompts = [
  'Recommend wireless earbuds under $100',
  'Who is this product best for?',
  'How can I track my order?',
];

export default function AiChatWidget() {
  const location = useLocation();
  const { currentProduct } = useAppSelector((state) => state.product);
  const { isOpen, isSending, messages, open, close, sendMessage, clearConversation } = useAiChat();
  const [draft, setDraft] = useState('');

  const context = useMemo<AiChatContext>(() => {
    if (location.pathname.startsWith('/products/') && currentProduct) {
      return {
        pageType: 'product-detail',
        path: `${location.pathname}${location.search}`,
        productId: currentProduct.id,
        productName: currentProduct.name,
      };
    }

    if (location.pathname.startsWith('/search')) {
      return { pageType: 'search', path: `${location.pathname}${location.search}` };
    }

    if (location.pathname.startsWith('/products')) {
      return { pageType: 'product-list', path: `${location.pathname}${location.search}` };
    }

    return { pageType: 'other', path: `${location.pathname}${location.search}` };
  }, [currentProduct, location.pathname, location.search]);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!draft.trim()) {
      return;
    }

    const nextMessage = draft;
    setDraft('');
    await sendMessage(nextMessage, context);
  };

  return (
    <>
      {isOpen ? (
        <div className="fixed inset-x-0 bottom-0 z-50 flex justify-end p-3 md:bottom-6 md:right-6 md:left-auto md:w-[26rem] md:p-0">
          <section className="flex h-[70vh] w-full max-w-[26rem] flex-col overflow-hidden rounded-[1.75rem] border border-secondary-100 bg-[#fffdf8] shadow-2xl shadow-primary-900/15">
            <div className="relative overflow-hidden border-b border-secondary-100 bg-primary-900 px-5 py-4 text-white">
              <div className="absolute inset-0 bg-[radial-gradient(circle_at_top_right,rgba(255,255,255,0.16),transparent_40%)]" />
              <div className="relative flex items-start justify-between gap-4">
                <div>
                  <div className="flex items-center gap-2 text-[11px] uppercase tracking-[0.24em] text-white/60">
                    <SparklesIcon className="h-4 w-4" />
                    HyperMall AI
                  </div>
                  <h2 className="mt-2 text-lg font-semibold">Your shopping assistant</h2>
                  <p className="mt-1 text-sm text-white/75">
                    Ask about products, get buying recommendations, or request help navigating the store.
                  </p>
                </div>
                <button
                  type="button"
                  onClick={close}
                  className="rounded-full bg-white/10 p-2 transition hover:bg-white/20"
                  aria-label="Close AI assistant"
                >
                  <XMarkIcon className="h-5 w-5" />
                </button>
              </div>
            </div>

            <div className="flex-1 overflow-y-auto bg-gradient-to-b from-[#fffaf0] to-white px-4 py-4">
              <AiMessageList messages={messages} />
            </div>

            <div className="border-t border-secondary-100 bg-white px-4 py-4">
              <div className="mb-3 flex flex-wrap gap-2">
                {starterPrompts.map((prompt) => (
                  <button
                    key={prompt}
                    type="button"
                    onClick={() => setDraft(prompt)}
                    className="rounded-full bg-secondary-50 px-3 py-1.5 text-xs font-semibold text-secondary-700 transition hover:bg-secondary-100"
                  >
                    {prompt}
                  </button>
                ))}
              </div>

              <form onSubmit={handleSubmit} className="space-y-3">
                <textarea
                  value={draft}
                  onChange={(event) => setDraft(event.target.value)}
                  placeholder="Example: find me a wireless mouse under $50"
                  rows={3}
                  className="w-full resize-none rounded-2xl border border-secondary-200 bg-secondary-50 px-4 py-3 text-sm text-primary-900 placeholder:text-secondary-400 focus:border-primary-500 focus:outline-none focus:ring-4 focus:ring-primary-50"
                />

                <div className="flex items-center justify-between gap-3">
                  <button
                    type="button"
                    onClick={clearConversation}
                    className="inline-flex items-center gap-2 text-xs font-semibold text-secondary-500 transition hover:text-primary-700"
                  >
                    <ArrowPathIcon className="h-4 w-4" />
                    Clear conversation
                  </button>
                  <Button type="submit" isLoading={isSending} className="rounded-full px-5">
                    Send message
                  </Button>
                </div>
              </form>
            </div>
          </section>
        </div>
      ) : null}

      <AiChatLauncher onClick={open} />
    </>
  );
}
