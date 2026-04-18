import { useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';

import Loading from '@components/common/Loading';
import { paymentService } from '@services/payment.service';
import { getErrorMessage } from '@/utils';

export default function VNPayCallbackPage() {
  const location = useLocation();
  const navigate = useNavigate();

  useEffect(() => {
    let mounted = true;

    const handleCallback = async () => {
      try {
        const payment = await paymentService.handleVnpayCallback(location.search);
        if (!mounted) {
          return;
        }

        if (payment.status === 'SUCCESS') {
          toast.success('Payment completed successfully');
        } else {
          toast.error(payment.failureReason || 'Payment was not completed successfully');
        }
        navigate(`/order-success/${payment.orderId}`, { replace: true });
      } catch (error: unknown) {
        if (!mounted) {
          return;
        }

        toast.error(getErrorMessage(error, 'Unable to verify the VNPay payment callback'));
        navigate('/orders', { replace: true });
      }
    };

    void handleCallback();

    return () => {
      mounted = false;
    };
  }, [location.search, navigate]);

  return (
    <div className="flex min-h-[60vh] items-center justify-center">
      <div className="text-center">
        <Loading size="lg" />
        <p className="mt-4 text-sm text-gray-500">Verifying your VNPay payment...</p>
      </div>
    </div>
  );
}
