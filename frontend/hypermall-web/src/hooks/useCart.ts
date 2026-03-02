import { useCallback, useEffect } from 'react';
import { useAppDispatch, useAppSelector } from '@/store/hooks';
import {
  fetchCart,
  addToCart,
  updateCartItem,
  removeCartItem,
  clearCart,
} from '@/store/slices/cartSlice';
import { setCartDrawerOpen } from '@/store/slices/uiSlice';
import type { AddToCartRequest } from '@/types';
import toast from 'react-hot-toast';

export const useCart = () => {
  const dispatch = useAppDispatch();
  const { cart, isLoading, error } = useAppSelector((state) => state.cart);
  const { isAuthenticated } = useAppSelector((state) => state.auth);

  useEffect(() => {
    if (isAuthenticated) {
      dispatch(fetchCart());
    }
  }, [dispatch, isAuthenticated]);

  const handleAddToCart = useCallback(
    async (data: AddToCartRequest) => {
      const result = await dispatch(addToCart(data));
      if (addToCart.fulfilled.match(result)) {
        toast.success('Added to cart');
        dispatch(setCartDrawerOpen(true));
        return true;
      } else {
        toast.error('Failed to add to cart');
        return false;
      }
    },
    [dispatch]
  );

  const handleUpdateQuantity = useCallback(
    async (itemId: string, quantity: number) => {
      if (quantity < 1) return;
      await dispatch(updateCartItem({ itemId, quantity }));
    },
    [dispatch]
  );

  const handleRemoveItem = useCallback(
    async (itemId: string) => {
      const result = await dispatch(removeCartItem(itemId));
      if (removeCartItem.fulfilled.match(result)) {
        toast.success('Item removed');
      }
    },
    [dispatch]
  );

  const handleClearCart = useCallback(async () => {
    await dispatch(clearCart());
    toast.success('Cart cleared');
  }, [dispatch]);

  const totalItems = cart?.items.reduce((sum, item) => sum + item.quantity, 0) || 0;
  const selectedItems = cart?.items.filter((item) => item.selected) || [];
  const selectedTotal = selectedItems.reduce(
    (sum, item) => sum + item.price * item.quantity,
    0
  );

  return {
    cart,
    items: cart?.items || [],
    totalItems,
    selectedItems,
    selectedTotal,
    isLoading,
    error,
    addToCart: handleAddToCart,
    updateQuantity: handleUpdateQuantity,
    removeItem: handleRemoveItem,
    clearCart: handleClearCart,
  };
};
