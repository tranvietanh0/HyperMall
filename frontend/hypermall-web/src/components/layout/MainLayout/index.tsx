import { Outlet } from 'react-router-dom';
import CartDrawer from '@components/cart/CartDrawer';
import AiChatWidget from '@components/features/ai/AiChatWidget';
import PageTransition from '@components/common/PageTransition';
import Footer from '../Footer';
import Header from '../Header';

export default function MainLayout() {
  const isAiEnabled = import.meta.env.VITE_ENABLE_AI_CHAT !== 'false';

  return (
    <div className="min-h-screen bg-[#f5f5f5]">
      <Header />
      <main className="pb-8">
        <PageTransition>
          <Outlet />
        </PageTransition>
      </main>
      <Footer />
      <CartDrawer />
      {isAiEnabled ? <AiChatWidget /> : null}
    </div>
  );
}
