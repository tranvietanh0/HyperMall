import { useEffect, useMemo, useState, type ComponentType, type SVGProps } from 'react';
import {
  ArrowRightIcon,
  BoltIcon,
  ComputerDesktopIcon,
  DevicePhoneMobileIcon,
  HomeModernIcon,
  SparklesIcon,
  SwatchIcon,
} from '@heroicons/react/24/outline';
import { Link } from 'react-router-dom';
import useScrollReveal from '@hooks/useScrollReveal';
import { flashSaleService } from '@/services/flash-sale.service';
import { productService } from '@/services/product.service';
import { formatCurrency } from '@utils/format';
import type { FlashSale, FlashSaleProduct, Product } from '@/types';

type IconComponent = ComponentType<SVGProps<SVGSVGElement>>;

interface CategoryItem {
  id: number;
  name: string;
  icon: IconComponent;
}

interface ProductCardItem {
  id: number;
  name: string;
  price: string;
  sold: string;
  imageUrl?: string;
}

const categoryItems: CategoryItem[] = [
  { id: 1, name: 'Fashion', icon: SwatchIcon },
  { id: 2, name: 'Mobiles', icon: DevicePhoneMobileIcon },
  { id: 3, name: 'Computing', icon: ComputerDesktopIcon },
  { id: 4, name: 'Home', icon: HomeModernIcon },
  { id: 5, name: 'Beauty', icon: SparklesIcon },
  { id: 6, name: 'Electronics', icon: DevicePhoneMobileIcon },
  { id: 7, name: 'Gaming', icon: ComputerDesktopIcon },
  { id: 8, name: 'Accessories', icon: SwatchIcon },
  { id: 9, name: 'Sports', icon: BoltIcon },
  { id: 10, name: 'Luxury', icon: SparklesIcon },
];

const fallbackDailyDiscoveryItems: ProductCardItem[] = Array.from({ length: 12 }, (_, index) => ({
  id: index + 1,
  name: `Premium Lifestyle Product ${index + 1}`,
  price: `$${(24 + index * 5).toFixed(2)}`,
  sold: `${0.8 + index / 5}k sold`,
  imageUrl: `https://placehold.co/600x600?text=Product+${index + 1}`,
}));

function SectionHeading({ title, description, actionLabel, actionTo }: { title: string; description: string; actionLabel?: string; actionTo?: string }) {
  return (
    <div className="mb-8 flex items-end justify-between gap-4 border-b border-secondary-100 pb-5">
      <div>
        <h2 className="text-3xl font-black tracking-tighter text-primary-900 uppercase md:text-4xl">{title}</h2>
        <p className="mt-2 text-sm font-medium text-secondary-500">{description}</p>
      </div>
      {actionLabel && actionTo ? (
        <Link to={actionTo} className="group hidden items-center gap-2 text-sm font-bold text-primary-900 transition hover:text-accent-600 md:flex">
          {actionLabel}
          <ArrowRightIcon className="h-4 w-4 transition-transform duration-300 group-hover:translate-x-1.5" />
        </Link>
      ) : null}
    </div>
  );
}

function CategoryTile({ item, index }: { item: CategoryItem; index: number }) {
  const Icon = item.icon;

  return (
    <Link
      to={`/search?q=${encodeURIComponent(item.name)}`}
      className="group flex flex-col items-center rounded-3xl bg-white p-6 ring-1 ring-secondary-100 transition-all duration-300 hover:-translate-y-2 hover:shadow-xl"
      data-reveal="up"
      data-reveal-delay={String(index + 1)}
    >
      <div className="flex h-16 w-16 items-center justify-center rounded-[22px] bg-secondary-50 text-secondary-900 transition-all duration-300 group-hover:bg-primary-900 group-hover:text-white group-hover:shadow-lg group-hover:shadow-primary-900/20 group-hover:scale-110">
        <Icon className="h-8 w-8" />
      </div>
      <span className="mt-4 text-center text-xs font-bold uppercase tracking-widest text-secondary-400 transition-colors group-hover:text-primary-900">
        {item.name}
      </span>
    </Link>
  );
}

function FlashSaleCard({ item, index }: { item: FlashSaleProduct; index: number }) {
  const soldPercent = item.stockLimit > 0
    ? Math.min(100, Math.max(0, Math.round((item.soldCount / item.stockLimit) * 100)))
    : 0;

  return (
    <Link
      to={`/products/${item.productId}`}
      className="group block"
      data-reveal="up"
      data-reveal-delay={String(index + 1)}
    >
      <div className="relative aspect-[3/4] overflow-hidden rounded-3xl bg-slate-100 transition-all duration-500 group-hover:scale-[1.02] group-hover:shadow-xl">
        <img
          src={item.productImage || 'https://placehold.co/600x800?text=Flash+Sale'}
          alt={item.productName}
          className="h-full w-full object-cover transition-transform duration-500 group-hover:scale-105"
          onError={(e) => { (e.target as HTMLImageElement).src = 'https://placehold.co/600x800?text=Flash+Sale'; }}
        />
        <div className="absolute right-3 top-3 rounded-xl bg-primary-900 px-3 py-1.5 text-[10px] font-black text-white shimmer-badge">-{item.discountPercent}%</div>
        <div className="absolute inset-0 bg-gradient-to-b from-transparent via-transparent to-black/10 opacity-0 transition-opacity duration-500 group-hover:opacity-100" />
        <div className="absolute inset-x-4 bottom-4">
          <div className="overflow-hidden rounded-full bg-white/20 backdrop-blur-md">
            <div className="h-1.5 bg-white transition-all duration-700 group-hover:bg-accent-400" style={{ width: `${soldPercent}%` }} />
          </div>
          <p className="mt-2 text-[10px] font-bold uppercase tracking-widest text-white/80">{soldPercent}% Sold</p>
        </div>
      </div>
      <div className="mt-4 px-2">
        <h3 className="truncate text-sm font-bold text-primary-900 transition-colors duration-300 group-hover:text-accent-600">{item.productName}</h3>
        <div className="mt-2 flex items-baseline gap-2">
          <span className="text-lg font-black text-primary-900">{formatCurrency(item.flashSalePrice)}</span>
          <span className="text-xs font-medium text-secondary-400 line-through">{formatCurrency(item.originalPrice)}</span>
        </div>
      </div>
    </Link>
  );
}

function DiscoveryCard({ item, index }: { item: ProductCardItem; index: number }) {
  return (
    <Link
      to={`/products/${item.id}`}
      className="group block"
      data-reveal="up"
      data-reveal-delay={String(index + 1)}
    >
      <div className="aspect-square overflow-hidden rounded-[32px] bg-slate-100 transition-all duration-500 group-hover:-translate-y-2 group-hover:rounded-2xl group-hover:shadow-2xl">
        <img
          src={item.imageUrl || 'https://placehold.co/600x600?text=Daily+Discovery'}
          alt={item.name}
          className="h-full w-full object-cover transition-transform duration-700 ease-spring group-hover:scale-110"
          onError={(event) => {
            event.currentTarget.src = 'https://placehold.co/600x600?text=Daily+Discovery';
          }}
        />
      </div>
      <div className="mt-6 px-1">
        <div className="mb-2 flex items-center gap-2">
          <span className="rounded-md bg-secondary-100 px-2 py-0.5 text-[9px] font-bold uppercase tracking-widest text-secondary-500 transition-colors duration-300 group-hover:bg-accent-100 group-hover:text-accent-700">In Stock</span>
        </div>
        <h3 className="line-clamp-2 text-sm font-bold leading-relaxed text-primary-900 transition-colors duration-300 group-hover:text-accent-600">{item.name}</h3>
        <div className="mt-4 flex items-center justify-between">
          <p className="text-xl font-black text-primary-900">{item.price}</p>
          <p className="text-[10px] font-bold uppercase tracking-widest text-secondary-400">{item.sold}</p>
        </div>
      </div>
    </Link>
  );
}

function CountdownUnit({ value, label }: { value: string; label: string }) {
  return (
    <div className="flex flex-col items-center">
      <span className="text-2xl font-black tracking-tight text-primary-900 animate-countdown-pulse">{value}</span>
      <span className="text-[10px] font-bold uppercase tracking-widest text-secondary-400">{label}</span>
    </div>
  );
}

export default function HomePage() {
  const [flashSale, setFlashSale] = useState<FlashSale | null>(null);
  const [dailyDiscoveryItems, setDailyDiscoveryItems] = useState<ProductCardItem[]>(fallbackDailyDiscoveryItems);
  const [springCollectionProducts, setSpringCollectionProducts] = useState<Product[]>([]);
  const [remainingSeconds, setRemainingSeconds] = useState(0);
  const revealRef = useScrollReveal<HTMLDivElement>({
    deps: [flashSale?.products?.length ?? 0, dailyDiscoveryItems.length, springCollectionProducts.length],
  });

  useEffect(() => {
    let mounted = true;

    const loadDiscoveryProducts = async () => {
      try {
        const page = await productService.getProducts({ page: 0, size: 12, sortBy: 'newest' });
        if (!mounted) return;

        const products = page.content ?? [];
        if (!products.length) return;

        setDailyDiscoveryItems(
          products.map((product) => ({
            id: product.id,
            name: product.name,
            price: formatCurrency(product.salePrice ?? product.basePrice),
            sold: `${product.totalSold.toLocaleString('en-US')} sold`,
            imageUrl: product.thumbnail,
          }))
        );
        setSpringCollectionProducts(products.slice(0, 4));
      } catch {
        if (!mounted) return;
        setDailyDiscoveryItems(fallbackDailyDiscoveryItems);
        setSpringCollectionProducts([]);
      }
    };

    const loadFlashSale = async () => {
      try {
        const currentFlashSale = await flashSaleService.getCurrentFlashSale();
        if (!mounted) return;
        setFlashSale(currentFlashSale);
        setRemainingSeconds(currentFlashSale?.remainingSeconds ?? 0);
      } catch {
        if (!mounted) return;
        setFlashSale(null);
        setRemainingSeconds(0);
      }
    };

    void loadDiscoveryProducts();
    void loadFlashSale();

    return () => {
      mounted = false;
    };
  }, []);

  useEffect(() => {
    if (remainingSeconds <= 0) return;

    const timer = window.setInterval(() => {
      setRemainingSeconds((current) => (current > 0 ? current - 1 : 0));
    }, 1000);

    return () => {
      window.clearInterval(timer);
    };
  }, [remainingSeconds]);

  const countdown = useMemo(() => {
    const hours = Math.floor(remainingSeconds / 3600);
    const minutes = Math.floor((remainingSeconds % 3600) / 60);
    const seconds = remainingSeconds % 60;

    return {
      hours: String(hours).padStart(2, '0'),
      minutes: String(minutes).padStart(2, '0'),
      seconds: String(seconds).padStart(2, '0'),
    };
  }, [remainingSeconds]);

  const springCollectionTiles = useMemo(() => {
    if (springCollectionProducts.length >= 4) {
      return springCollectionProducts.slice(0, 4).map((product) => ({
        id: product.id,
        imageUrl: product.thumbnail,
        name: product.name,
      }));
    }

    return Array.from({ length: 4 }, (_, index) => ({
      id: index + 1,
      imageUrl: `https://placehold.co/800x1000?text=Collection+${index + 1}`,
      name: `Collection ${index + 1}`,
    }));
  }, [springCollectionProducts]);

  return (
    <div ref={revealRef} className="space-y-12 pb-16 md:space-y-20 md:pb-24">
      {/* ── Hero Banner ── */}
      <section className="container pt-6 md:pt-10">
        <div className="relative overflow-hidden rounded-[32px] bg-primary-900 shadow-2xl animate-blur-in">
          <div className="absolute inset-0 bg-[url('https://images.unsplash.com/photo-1441986300917-64674bd600d8?auto=format&fit=crop&q=80')] bg-cover bg-center opacity-40 mix-blend-overlay" />
          <div className="relative z-10 grid gap-12 px-8 py-16 md:grid-cols-2 md:items-center md:px-16 md:py-24">
            <div className="max-w-xl animate-fade-up">
              <span className="inline-flex items-center rounded-full bg-accent-600/20 px-4 py-1.5 text-xs font-bold uppercase tracking-widest text-accent-400 shimmer-badge">
                New Spring Collection
              </span>
              <h1 className="mt-8 text-5xl font-black leading-[1.1] tracking-tighter text-white md:text-7xl">
                Experience <br />
                <span className="text-accent-400">Precision</span> Commerce.
              </h1>
              <p className="mt-8 text-lg font-medium leading-relaxed text-secondary-300">
                Curated premium products from global brands, delivered to your doorstep with unmatched reliability and care.
              </p>
              <div className="mt-10 flex flex-wrap gap-4">
                <Link to="/products" className="btn-accent px-8 py-4 text-sm font-bold shadow-xl shadow-accent-600/20 hover:shadow-2xl hover:shadow-accent-600/30 transition-shadow duration-300">
                  Shop Collection
                </Link>
                <Link to="/search?q=trending" className="btn-outline border-white/20 bg-white/5 px-8 py-4 text-sm font-bold text-white backdrop-blur-md hover:bg-white/10 hover:border-white/30 transition-all duration-300">
                  View Trending
                </Link>
              </div>
            </div>

            <div className="hidden grid-cols-2 gap-4 md:grid animate-fade-up" style={{ animationDelay: '120ms' }}>
              <div className="space-y-4 pt-12">
                <div className="aspect-[4/5] rounded-3xl bg-white/10 p-6 backdrop-blur-xl ring-1 ring-white/20 animate-float transition-all duration-500 hover:bg-white/15 hover:ring-white/30">
                  <img
                    src={springCollectionTiles[0]?.imageUrl}
                    alt={springCollectionTiles[0]?.name || 'Collection item'}
                    className="h-full w-full rounded-2xl object-cover"
                    onError={(event) => {
                      event.currentTarget.src = 'https://placehold.co/800x1000?text=Collection+1';
                    }}
                  />
                </div>
                <div className="aspect-square rounded-3xl bg-accent-600/20 p-6 backdrop-blur-xl ring-1 ring-accent-400/30 animate-float-delayed transition-all duration-500 hover:bg-accent-600/30">
                  <img
                    src={springCollectionTiles[1]?.imageUrl}
                    alt={springCollectionTiles[1]?.name || 'Collection item'}
                    className="h-full w-full rounded-2xl object-cover"
                    onError={(event) => {
                      event.currentTarget.src = 'https://placehold.co/800x800?text=Collection+2';
                    }}
                  />
                </div>
              </div>
              <div className="space-y-4">
                <div className="aspect-square rounded-3xl bg-white/10 p-6 backdrop-blur-xl ring-1 ring-white/20 animate-float-delayed transition-all duration-500 hover:bg-white/15 hover:ring-white/30">
                  <img
                    src={springCollectionTiles[2]?.imageUrl}
                    alt={springCollectionTiles[2]?.name || 'Collection item'}
                    className="h-full w-full rounded-2xl object-cover"
                    onError={(event) => {
                      event.currentTarget.src = 'https://placehold.co/800x800?text=Collection+3';
                    }}
                  />
                </div>
                <div className="aspect-[4/5] rounded-3xl bg-secondary-400/10 p-6 backdrop-blur-xl ring-1 ring-secondary-400/20 animate-float transition-all duration-500 hover:bg-secondary-400/15">
                  <img
                    src={springCollectionTiles[3]?.imageUrl}
                    alt={springCollectionTiles[3]?.name || 'Collection item'}
                    className="h-full w-full rounded-2xl object-cover"
                    onError={(event) => {
                      event.currentTarget.src = 'https://placehold.co/800x1000?text=Collection+4';
                    }}
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* ── Categories ── */}
      <section className="container" data-reveal="fade">
        <div className="flex flex-col items-center gap-12 md:flex-row md:justify-between">
          <div className="max-w-xs" data-reveal="left">
            <h2 className="text-3xl font-black tracking-tighter text-primary-900">Explore by Category</h2>
            <p className="mt-4 text-sm font-medium text-secondary-500">Find exactly what you need through our curated departments.</p>
          </div>
          <div className="grid flex-1 grid-cols-2 gap-4 sm:grid-cols-3 md:grid-cols-5">
            {categoryItems.slice(0, 5).map((item, index) => (
              <CategoryTile key={item.id} item={item} index={index} />
            ))}
          </div>
        </div>
      </section>

      {/* ── Flash Sale ── */}
      <section className="container" data-reveal="fade">
        <div className="rounded-[40px] border border-secondary-100 bg-white p-8 md:p-12 transition-shadow duration-500 hover:shadow-xl">
          <div className="mb-12 flex flex-col gap-6 md:flex-row md:items-end md:justify-between">
            <div data-reveal="up">
              <div className="flex items-center gap-3">
                <BoltIcon className="h-8 w-8 text-accent-600 animate-pulse-glow rounded-full" />
                <h2 className="text-3xl font-black tracking-tighter text-primary-900 uppercase">Flash Sale</h2>
              </div>
              <p className="mt-2 text-sm font-medium text-secondary-500">
                {flashSale?.description || 'Limited time offers. Ending soon.'}
              </p>
            </div>
            <div className="flex items-center gap-8" data-reveal="right">
              <div className="flex gap-4">
                <CountdownUnit value={countdown.hours} label="Hrs" />
                <CountdownUnit value={countdown.minutes} label="Min" />
                <CountdownUnit value={countdown.seconds} label="Sec" />
              </div>
              <Link to="/search?q=flash-sale" className="btn-secondary rounded-xl py-3 text-xs font-bold uppercase tracking-widest transition-all duration-300 hover:shadow-md">
                View All
              </Link>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-6 sm:grid-cols-3 lg:grid-cols-6">
            {(flashSale?.products ?? []).map((item, index) => (
              <FlashSaleCard key={item.id} item={item} index={index} />
            ))}
          </div>

          {!flashSale?.products?.length ? (
            <p className="mt-6 text-sm font-medium text-secondary-500">There is no active flash sale right now.</p>
          ) : null}
        </div>
      </section>

      {/* ── Daily Discovery ── */}
      <section className="container" data-reveal="fade">
        <SectionHeading
          title="Daily Discovery"
          description="Personalized selection updated every hour."
          actionLabel="See More Products"
          actionTo="/products"
        />

        <div className="grid grid-cols-2 gap-6 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-6">
          {dailyDiscoveryItems.map((item, index) => (
            <DiscoveryCard key={item.id} item={item} index={index} />
          ))}
        </div>
      </section>

      {/* ── Newsletter CTA ── */}
      <section className="container" data-reveal="up">
        <div className="rounded-[40px] bg-secondary-50 px-8 py-16 text-center md:px-16 md:py-20 transition-shadow duration-500 hover:shadow-lg">
          <h2 className="text-3xl font-black tracking-tighter text-primary-900 uppercase md:text-5xl">Join the HyperMall Elite</h2>
          <p className="mx-auto mt-6 max-w-lg text-sm font-medium leading-relaxed text-secondary-500">
            Subscribe to our newsletter and get early access to collections, exclusive discounts, and professional styling tips.
          </p>
          <form className="mx-auto mt-10 flex max-w-md gap-3" data-reveal="up" data-reveal-delay="2">
            <input
              type="email"
              placeholder="Enter your email address"
              className="input border-none bg-white shadow-sm focus:ring-accent-100 transition-shadow duration-300 focus:shadow-lg focus:shadow-accent-100/50"
            />
            <button type="submit" className="btn-primary whitespace-nowrap px-8">
              Join Now
            </button>
          </form>
        </div>
      </section>
    </div>
  );
}
