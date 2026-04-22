import { useEffect, useState } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import axios from 'axios';
import Loading from '@/components/common/Loading';
import { sellerService } from '@/services/seller.service';
import type { SellerProfile } from '@/types';

interface SellerGuardProps {
  children: React.ReactElement;
}

export default function SellerGuard({ children }: SellerGuardProps) {
  const location = useLocation();
  const [profile, setProfile] = useState<SellerProfile | null | undefined>(undefined);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;

    sellerService
      .getMySellerProfile()
      .then((response) => {
        if (active) {
          setProfile(response);
          setError('');
        }
      })
      .catch((err: unknown) => {
        if (!active) {
          return;
        }

        if (axios.isAxiosError(err) && err.response?.status === 404) {
          setProfile(null);
          setError('');
          return;
        }

        setError('Unable to load seller workspace. Please try again.');
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

  if (error) {
    return (
      <div className="rounded-2xl border border-red-200 bg-red-50 p-6 text-red-700">
        <p className="font-medium">{error}</p>
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
