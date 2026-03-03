package com.hypermall.cart.service;

import com.hypermall.cart.dto.*;
import com.hypermall.cart.model.Cart;
import com.hypermall.cart.model.CartItem;
import com.hypermall.common.exception.BadRequestException;
import com.hypermall.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${app.cart.max-items:50}")
    private int maxItems;

    @Value("${app.cart.item-expiration-days:30}")
    private int expirationDays;

    private static final String CART_KEY_PREFIX = "cart:";

    private String getCartKey(Long userId) {
        return CART_KEY_PREFIX + userId;
    }

    public CartResponse getCart(Long userId) {
        Cart cart = loadCart(userId);
        return toCartResponse(cart);
    }

    public CartResponse addItem(Long userId, AddCartItemRequest request) {
        Cart cart = loadCart(userId);

        if (cart.getItems().size() >= maxItems) {
            throw new BadRequestException("Cart is full. Maximum " + maxItems + " items allowed.");
        }

        // Check if same product+variant already in cart
        Optional<CartItem> existing = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(request.getProductId())
                        && Objects.equals(item.getVariantId(), request.getVariantId()))
                .findFirst();

        if (existing.isPresent()) {
            existing.get().setQuantity(existing.get().getQuantity() + request.getQuantity());
            log.debug("Updated quantity for product {} in cart for user {}", request.getProductId(), userId);
        } else {
            CartItem newItem = CartItem.builder()
                    .id(System.currentTimeMillis())
                    .productId(request.getProductId())
                    .variantId(request.getVariantId())
                    .sellerId(request.getSellerId())
                    .quantity(request.getQuantity())
                    .price(BigDecimal.ZERO) // price will be fetched from product service in a real scenario
                    .selected(true)
                    .build();
            cart.getItems().add(newItem);
            log.info("Added product {} to cart for user {}", request.getProductId(), userId);
        }

        saveCart(userId, cart);
        return toCartResponse(cart);
    }

    public CartResponse updateItem(Long userId, Long itemId, UpdateCartItemRequest request) {
        Cart cart = loadCart(userId);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + itemId));

        if (request.getQuantity() != null) {
            item.setQuantity(request.getQuantity());
        }
        if (request.getSelected() != null) {
            item.setSelected(request.getSelected());
        }

        saveCart(userId, cart);
        log.debug("Updated cart item {} for user {}", itemId, userId);
        return toCartResponse(cart);
    }

    public CartResponse removeItem(Long userId, Long itemId) {
        Cart cart = loadCart(userId);

        boolean removed = cart.getItems().removeIf(item -> item.getId().equals(itemId));
        if (!removed) {
            throw new ResourceNotFoundException("Cart item not found with id: " + itemId);
        }

        saveCart(userId, cart);
        log.info("Removed cart item {} for user {}", itemId, userId);
        return toCartResponse(cart);
    }

    public void clearCart(Long userId) {
        String key = getCartKey(userId);
        redisTemplate.delete(key);
        log.info("Cleared cart for user {}", userId);
    }

    public CheckoutPreviewResponse getCheckoutPreview(Long userId) {
        Cart cart = loadCart(userId);

        List<CartItem> selectedItems = cart.getItems().stream()
                .filter(CartItem::getSelected)
                .collect(Collectors.toList());

        if (selectedItems.isEmpty()) {
            throw new BadRequestException("No items selected for checkout");
        }

        BigDecimal subtotal = selectedItems.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal shippingFee = BigDecimal.valueOf(30000); // flat rate, in production would call shipping service
        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal total = subtotal.add(shippingFee).subtract(discount);

        Map<Long, List<CartItemResponse>> itemsBySeller = selectedItems.stream()
                .map(this::toCartItemResponse)
                .collect(Collectors.groupingBy(CartItemResponse::getSellerId));

        CheckoutPreviewResponse response = new CheckoutPreviewResponse();
        response.setSelectedItems(selectedItems.stream().map(this::toCartItemResponse).collect(Collectors.toList()));
        response.setTotalItems(selectedItems.stream().mapToInt(CartItem::getQuantity).sum());
        response.setSubtotal(subtotal);
        response.setShippingFee(shippingFee);
        response.setDiscount(discount);
        response.setTotal(total);
        response.setItemsBySeller(itemsBySeller);

        return response;
    }

    public CartResponse selectAll(Long userId, boolean selected) {
        Cart cart = loadCart(userId);
        cart.getItems().forEach(item -> item.setSelected(selected));
        saveCart(userId, cart);
        return toCartResponse(cart);
    }

    private Cart loadCart(Long userId) {
        String key = getCartKey(userId);
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached instanceof Cart) {
            return (Cart) cached;
        }
        return Cart.builder().userId(userId).items(new ArrayList<>()).build();
    }

    private void saveCart(Long userId, Cart cart) {
        String key = getCartKey(userId);
        redisTemplate.opsForValue().set(key, cart, expirationDays, TimeUnit.DAYS);
    }

    private CartResponse toCartResponse(Cart cart) {
        CartResponse response = new CartResponse();
        response.setUserId(cart.getUserId());
        response.setItems(cart.getItems().stream().map(this::toCartItemResponse).collect(Collectors.toList()));
        response.setTotalItems(cart.getTotalItems());
        response.setSelectedCount(cart.getSelectedCount());
        response.setSubtotal(cart.getSubtotal());
        return response;
    }

    private CartItemResponse toCartItemResponse(CartItem item) {
        CartItemResponse response = new CartItemResponse();
        response.setId(item.getId());
        response.setProductId(item.getProductId());
        response.setVariantId(item.getVariantId());
        response.setSellerId(item.getSellerId());
        response.setProductName(item.getProductName());
        response.setVariantName(item.getVariantName());
        response.setThumbnail(item.getThumbnail());
        response.setQuantity(item.getQuantity());
        response.setPrice(item.getPrice());
        response.setSubtotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        response.setSelected(item.getSelected());
        return response;
    }
}
