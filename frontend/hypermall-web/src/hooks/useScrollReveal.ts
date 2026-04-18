import { useEffect, useRef } from 'react';

interface ScrollRevealOptions {
  threshold?: number;
  rootMargin?: string;
  once?: boolean;
  deps?: ReadonlyArray<unknown>;
}

/**
 * Custom hook that uses IntersectionObserver to reveal elements
 * when they scroll into the viewport. Apply `data-reveal` attribute
 * to child elements you want to animate.
 *
 * CSS classes like `.reveal`, `.reveal-up`, `.reveal-left`, `.reveal-right`,
 * `.reveal-scale` are defined in globals.css and activate when
 * `[data-revealed="true"]` is set.
 */
export function useScrollReveal<T extends HTMLElement = HTMLDivElement>(
  options: ScrollRevealOptions = {}
) {
  const { threshold = 0.15, rootMargin = '0px 0px -40px 0px', once = true, deps = [] } = options;
  const containerRef = useRef<T>(null);

  useEffect(() => {
    const container = containerRef.current;
    if (!container) return;

    const elements = container.querySelectorAll('[data-reveal]');
    if (elements.length === 0) return;

    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            const el = entry.target as HTMLElement;
            el.setAttribute('data-revealed', 'true');
            if (once) {
              observer.unobserve(el);
            }
          } else if (!once) {
            const el = entry.target as HTMLElement;
            el.removeAttribute('data-revealed');
          }
        });
      },
      { threshold, rootMargin }
    );

    elements.forEach((el) => observer.observe(el));

    return () => observer.disconnect();
  }, [threshold, rootMargin, once, ...deps]);

  return containerRef;
}

export default useScrollReveal;
