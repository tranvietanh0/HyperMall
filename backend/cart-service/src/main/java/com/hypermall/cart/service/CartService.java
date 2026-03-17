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
        validateCartCapacity(cart);

        // TODO: [Product-Service Integration] Validate product exists and is available
        // Call product-service via Feign client or WebClient to verify:
        // - Product exists and is active
        // - Variant exists (if variantId is provided)
        // - Get seller information
        // Example: ProductResponse product = productServiceClient.getProduct(request.getProductId());

        // TODO: [Product-Service Integration] Fetch current price from product-service
        // The price should be fetched in real-time to ensure accuracy
        // Example: BigDecimal currentPrice = productServiceClient.getPrice(request.getProductId(), request.getVariantId());

        // TODO: [Inventory-Service Integration] Check inventory availability
        // Validate that requested quantity is available in stock
        // Example: inventoryServiceClient.checkAvailability(request.getProductId(), request.getVariantId(), request.getQuantity());

        // Check if same product+variant already in cart
        Optional<CartItem> existing = findExistingItem(cart, request);

        if (existing.isPresent()) {
            CartItem existingItem = existing.get();
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
            log.debug("Updated quantity for product {} in cart for user {}", request.getProductId(), userId);
        } else {
            CartItem newItem = buildCartItem(request);
            cart.getItems().add(newItem);
            log.info("Added product {} to cart for user {}", request.getProductId(), userId);
        }

        saveCart(userId, cart);
        return toCartResponse(cart);
    }

    public CartResponse updateItem(Long userId, String itemId, UpdateCartItemRequest request) {
        Cart cart = loadCart(userId);

        // TODO: [Inventory-Service Integration] Validate quantity against available stock
        // Before updating quantity, check if the new quantity is available
        // Example: inventoryServiceClient.checkAvailability(item.getProductId(), item.getVariantId(), request.getQuantity());

        CartItem item = findCartItemById(cart, itemId);

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

    public CartResponse removeItem(Long userId, String itemId) {
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
        validateSelectedItemsNotEmpty(selectedItems);

        // TODO: [Product-Service Integration] Fetch latest prices for all selected items
        // Prices may have changed since items were added to cart
        // Example: Map<Long, BigDecimal> currentPrices = productServiceClient.getPrices(productIds);

        // TODO: [Inventory-Service Integration] Validate stock availability for all selected items
        // Before checkout, ensure all items are still in stock with requested quantities
        // Example: inventoryServiceClient.validateBulkAvailability(selectedItems);

        // TODO: [Promotion-Service Integration] Apply applicable promotions and discounts
        // Fetch and apply vouchers, seller discounts, platform promotions
        // Example: DiscountResult discounts = promotionServiceClient.calculateDiscounts(selectedItems, userId);

        BigDecimal subtotal = calculateSubtotal(selectedItems);

        BigDecimal shippingFee = BigDecimal.valueOf(30000); // flat rate, in production would call shipping service
        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal total = subtotal.add(shippingFee).subtract(discount);

        Map<Long, List<CartItemResponse>> itemsBySeller = selectedItems.stream()
                .map(this::toCartItemResponse)
                .collect(Collectors.groupingBy(CartItemResponse::getSellerId));

        return toCheckoutPreviewResponse(selectedItems, subtotal, shippingFee, discount, total, itemsBySeller);
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

    private void validateCartCapacity(Cart cart) {
        if (cart.getItems().size() >= maxItems) {
            throw new BadRequestException("Cart is full. Maximum " + maxItems + " items allowed.");
        }
    }

    private Optional<CartItem> findExistingItem(Cart cart, AddCartItemRequest request) {
        return cart.getItems().stream()
                .filter(item -> item.getProductId().equals(request.getProductId())
                        && Objects.equals(item.getVariantId(), request.getVariantId()))
                .findFirst();
    }

    private CartItem buildCartItem(AddCartItemRequest request) {
        return CartItem.builder()
                .id(UUID.randomUUID().toString())
                .productId(request.getProductId())
                .variantId(request.getVariantId())
                .sellerId(request.getSellerId())
                .productName(resolveProductName(request))
                .variantName(request.getVariantName())
                .thumbnail(resolveThumbnail(request))
                .quantity(request.getQuantity())
                .price(request.getPrice() != null ? request.getPrice() : BigDecimal.ZERO)
                .selected(true)
                .build();
    }

    private String resolveProductName(AddCartItemRequest request) {
        return request.getProductName() != null && !request.getProductName().isBlank()
                ? request.getProductName()
                : "Product #" + request.getProductId();
    }

    private String resolveThumbnail(AddCartItemRequest request) {
        return request.getThumbnail() != null && !request.getThumbnail().isBlank()
                ? request.getThumbnail()
                : "https://placehold.co/80x80?text=?";
    }

    private CartItem findCartItemById(Cart cart, String itemId) {
        return cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + itemId));
    }

    private void validateSelectedItemsNotEmpty(List<CartItem> selectedItems) {
        if (selectedItems.isEmpty()) {
            throw new BadRequestException("No items selected for checkout");
        }
    }

    private BigDecimal calculateSubtotal(List<CartItem> selectedItems) {
        return selectedItems.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private CheckoutPreviewResponse toCheckoutPreviewResponse(
            List<CartItem> selectedItems,
            BigDecimal subtotal,
            BigDecimal shippingFee,
            BigDecimal discount,
            BigDecimal total,
            Map<Long, List<CartItemResponse>> itemsBySeller
    ) {
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
