import clsx from 'clsx';

interface LoadingProps {
  size?: 'sm' | 'md' | 'lg';
  fullScreen?: boolean;
  text?: string;
}

export default function Loading({ size = 'md', fullScreen = false, text }: LoadingProps) {
  const sizeClasses = {
    sm: 'w-4 h-4 border-2',
    md: 'w-8 h-8 border-3',
    lg: 'w-12 h-12 border-4',
  };

  const glowClasses = {
    sm: 'w-6 h-6',
    md: 'w-12 h-12',
    lg: 'w-16 h-16',
  };

  const spinner = (
    <div className="flex flex-col items-center justify-center gap-3">
      <div className="relative">
        {/* Glow ring */}
        <div
          className={clsx(
            'absolute inset-0 m-auto rounded-full bg-accent-400/20 animate-pulse-glow',
            glowClasses[size]
          )}
        />
        {/* Spinner */}
        <div
          className={clsx(
            'relative animate-spin rounded-full border-accent-600 border-t-transparent',
            sizeClasses[size]
          )}
        />
      </div>
      {text && <p className="text-gray-600 text-sm animate-fade-in">{text}</p>}
    </div>
  );

  if (fullScreen) {
    return (
      <div className="fixed inset-0 flex items-center justify-center bg-white/80 backdrop-blur-sm z-50 animate-fade-in">
        {spinner}
      </div>
    );
  }

  return spinner;
}
