import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { cartService } from '@/services/cart.service';
import type { Cart, CartItem, AddToCartRequest } from '@/types';

interface CartState {
  cart: Cart | null;
  isLoading: boolean;
  error: string | null;
}

const initialState: CartState = {
  cart: null,
  isLoading: false,
  error: null,
};

export const fetchCart = createAsyncThunk('cart/fetch', async (_, { rejectWithValue }) => {
  try {
    return await cartService.getCart();
  } catch (error: unknown) {
    const err = error as { response?: { data?: { message?: string } } };
    return rejectWithValue(err.response?.data?.message || 'Failed to fetch cart');
  }
});

export const addToCart = createAsyncThunk(
  'cart/addItem',
  async (data: AddToCartRequest, { rejectWithValue }) => {
    try {
      return await cartService.addItem(data);
    } catch (error: unknown) {
      const err = error as { response?: { data?: { message?: string } } };
      return rejectWithValue(err.response?.data?.message || 'Failed to add item');
    }
  }
);

export const updateCartItem = createAsyncThunk(
  'cart/updateItem',
  async ({ itemId, quantity }: { itemId: string; quantity: number }, { rejectWithValue }) => {
    try {
      return await cartService.updateItem(itemId, quantity);
    } catch (error: unknown) {
      const err = error as { response?: { data?: { message?: string } } };
      return rejectWithValue(err.response?.data?.message || 'Failed to update item');
    }
  }
);

export const removeCartItem = createAsyncThunk(
  'cart/removeItem',
  async (itemId: string, { rejectWithValue }) => {
    try {
      return await cartService.removeItem(itemId);
    } catch (error: unknown) {
      const err = error as { response?: { data?: { message?: string } } };
      return rejectWithValue(err.response?.data?.message || 'Failed to remove item');
    }
  }
);

export const clearCart = createAsyncThunk('cart/clear', async (_, { rejectWithValue }) => {
  try {
    await cartService.clearCart();
  } catch (error: unknown) {
    const err = error as { response?: { data?: { message?: string } } };
    return rejectWithValue(err.response?.data?.message || 'Failed to clear cart');
  }
});

const cartSlice = createSlice({
  name: 'cart',
  initialState,
  reducers: {
    setCart: (state, action: PayloadAction<Cart>) => {
      state.cart = action.payload;
    },
    updateItemLocally: (state, action: PayloadAction<{ itemId: number; changes: Partial<CartItem> }>) => {
      if (state.cart) {
        const item = state.cart.items.find((i) => i.id === action.payload.itemId);
        if (item) {
          Object.assign(item, action.payload.changes);
        }
      }
    },
  },
  extraReducers: (builder) => {
    builder
      // Fetch cart
      .addCase(fetchCart.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(fetchCart.fulfilled, (state, action) => {
        state.isLoading = false;
        state.cart = action.payload;
      })
      .addCase(fetchCart.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })
      // Add to cart
      .addCase(addToCart.fulfilled, (state, action) => {
        state.cart = action.payload;
      })
      // Update cart item
      .addCase(updateCartItem.fulfilled, (state, action) => {
        state.cart = action.payload;
      })
      // Remove cart item
      .addCase(removeCartItem.fulfilled, (state, action) => {
        state.cart = action.payload;
      })
      // Clear cart
      .addCase(clearCart.fulfilled, (state) => {
        state.cart = null;
      });
  },
});

export const { setCart, updateItemLocally } = cartSlice.actions;
export default cartSlice.reducer;
