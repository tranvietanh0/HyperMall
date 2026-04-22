import { useEffect, useState } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import Loading from '@/components/common/Loading';
import { sellerService } from '@/services/seller.service';
import type { SellerProfile } from '@/types';

interface SellerGuardProps {
  children: React.ReactElement;
}

export default function SellerGuard({ children }: SellerGuardProps) {
  const location = useLocation();
  const [profile, setProfile] = useState<SellerProfile | null | undefined>(undefined);

  useEffect(() => {
    let active = true;

    sellerService
      .getMySellerProfile()
      .then((response) => {
        if (active) {
          setProfile(response);
        }
      })
      .catch(() => {
        if (active) {
          setProfile(null);
        }
      });

    return () => {
      active = false;
    };
  }, []);

  if (profile === undefined) {
    return (
      <div className="flex min-h-[40vh] items-center justify-center">
        <Loading size="lg" text="Loading seller workspace..." />
      </div>
    );
  }

  if (!profile && location.pathname !== '/seller/onboarding') {
    return <Navigate to="/seller/onboarding" replace />;
  }

  if (profile && location.pathname === '/seller/onboarding') {
    return <Navigate to="/seller" replace />;
  }

  return children;
}
