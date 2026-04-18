import { SparklesIcon } from '@heroicons/react/24/solid';

type AiChatLauncherProps = {
  onClick: () => void;
};

export default function AiChatLauncher({ onClick }: AiChatLauncherProps) {
  return (
    <button
      type="button"
      onClick={onClick}
      className="group fixed bottom-5 right-5 z-40 flex items-center gap-3 rounded-full bg-primary-900 px-4 py-3 text-white shadow-2xl shadow-primary-900/20 transition hover:-translate-y-1 hover:bg-primary-800 md:bottom-7 md:right-7"
      aria-label="Open AI assistant"
    >
      <span className="flex h-10 w-10 items-center justify-center rounded-full bg-white/15 transition group-hover:rotate-6">
        <SparklesIcon className="h-5 w-5" />
      </span>
      <span className="hidden text-left md:block">
        <span className="block text-[11px] uppercase tracking-[0.24em] text-white/60">AI</span>
        <span className="block text-sm font-semibold">Shopping Assistant</span>
      </span>
    </button>
  );
}
