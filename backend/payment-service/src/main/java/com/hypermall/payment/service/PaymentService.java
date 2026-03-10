package com.hypermall.payment.service;

import com.hypermall.common.exception.BadRequestException;
import com.hypermall.common.exception.ResourceNotFoundException;
import com.hypermall.payment.dto.*;
import com.hypermall.payment.entity.Payment;
import com.hypermall.payment.entity.PaymentMethod;
import com.hypermall.payment.entity.PaymentStatus;
import com.hypermall.payment.exception.PaymentMethodNotImplementedException;
import com.hypermall.payment.gateway.MoMoGateway;
import com.hypermall.payment.gateway.VNPayGateway;
import com.hypermall.payment.gateway.ZaloPayGateway;
import com.hypermall.payment.mapper.PaymentMapper;
import com.hypermall.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final VNPayGateway vnPayGateway;
    private final MoMoGateway moMoGateway;
    private final ZaloPayGateway zaloPayGateway;

    @Transactional
    public PaymentResponse createPayment(Long userId, CreatePaymentRequest request) {
        // Check if active payment already exists for this order
        paymentRepository.findByOrderIdAndStatus(request.getOrderId(), PaymentStatus.PENDING)
                .stream().findFirst().ifPresent(p -> {
                    throw new BadRequestException("Payment already in progress for order: " + request.getOrderNumber());
                });

        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .orderNumber(request.getOrderNumber())
                .userId(userId)
                .amount(request.getAmount())
                .method(request.getMethod())
                .build();

        // COD - no gateway needed, mark as pending
        if (request.getMethod() == PaymentMethod.COD) {
            payment.setStatus(PaymentStatus.PENDING);
            payment.setPaymentUrl(null);
            Payment saved = paymentRepository.save(payment);
            log.info("COD payment created for order: {}", request.getOrderNumber());
            return paymentMapper.toPaymentResponse(saved);
        }

        // Generate payment URL based on method
        String paymentUrl = generatePaymentUrl(request);
        payment.setPaymentUrl(paymentUrl);

        Payment saved = paymentRepository.save(payment);
        log.info("Payment created for order: {} via {}", request.getOrderNumber(), request.getMethod());
        return paymentMapper.toPaymentResponse(saved);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));
        return paymentMapper.toPaymentResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findTopByOrderIdOrderByCreatedAtDesc(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order id: " + orderId));
        return paymentMapper.toPaymentResponse(payment);
    }

    @Transactional
    public PaymentResponse handleVNPayCallback(Map<String, String> params) {
        if (!vnPayGateway.verifyCallback(params)) {
            log.warn("VNPay callback signature verification failed");
            throw new BadRequestException("Invalid VNPay callback signature");
        }

        String orderNumber = params.get("vnp_TxnRef");
        String transactionId = params.get("vnp_TransactionNo");

        // Idempotency check: create unique callback reference ID
        String callbackReferenceId = "vnpay_" + orderNumber + "_" + transactionId;
        if (paymentRepository.existsByCallbackReferenceId(callbackReferenceId)) {
            log.info("VNPay callback already processed for reference: {}", callbackReferenceId);
            Payment existingPayment = paymentRepository.findByCallbackReferenceId(callbackReferenceId)
                    .orElseThrow(() -> new ResourceNotFoundException("Payment not found for callback reference: " + callbackReferenceId));
            return paymentMapper.toPaymentResponse(existingPayment);
        }

        boolean success = vnPayGateway.isPaymentSuccess(params);

        Payment payment = paymentRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order: " + orderNumber));

        payment.setTransactionId(transactionId);
        payment.setGatewayResponse(params.toString());
        payment.setCallbackReferenceId(callbackReferenceId);
        payment.setCallbackProcessedAt(LocalDateTime.now());

        if (success) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setPaidAt(LocalDateTime.now());
            log.info("VNPay payment SUCCESS for order: {}", orderNumber);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("VNPay response code: " + params.get("vnp_ResponseCode"));
            log.warn("VNPay payment FAILED for order: {}", orderNumber);
        }

        return paymentMapper.toPaymentResponse(paymentRepository.save(payment));
    }

    @Transactional
    public Map<String, Object> handleMoMoCallback(MoMoCallbackRequest request) {
        Map<String, String> params = new HashMap<>();
        params.put("partnerCode", request.getPartnerCode());
        params.put("orderId", request.getOrderId());
        params.put("requestId", request.getRequestId());
        params.put("amount", String.valueOf(request.getAmount()));
        params.put("orderInfo", request.getOrderInfo());
        params.put("orderType", request.getOrderType());
        params.put("transId", String.valueOf(request.getTransId()));
        params.put("resultCode", String.valueOf(request.getResultCode()));
        params.put("message", request.getMessage());
        params.put("payType", request.getPayType());
        params.put("responseTime", String.valueOf(request.getResponseTime()));
        params.put("extraData", request.getExtraData());
        params.put("signature", request.getSignature());

        if (!moMoGateway.verifyCallback(params)) {
            log.warn("MoMo callback signature verification failed");
            return Map.of("resultCode", 1, "message", "Invalid signature");
        }

        // Idempotency check: create unique callback reference ID
        String callbackReferenceId = "momo_" + request.getOrderId() + "_" + request.getTransId();
        if (paymentRepository.existsByCallbackReferenceId(callbackReferenceId)) {
            log.info("MoMo callback already processed for reference: {}", callbackReferenceId);
            return Map.of("resultCode", 0, "message", "Callback already processed");
        }

        Payment payment = paymentRepository.findByOrderNumber(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order: " + request.getOrderId()));

        payment.setTransactionId(String.valueOf(request.getTransId()));
        payment.setGatewayResponse(params.toString());
        payment.setCallbackReferenceId(callbackReferenceId);
        payment.setCallbackProcessedAt(LocalDateTime.now());

        if (moMoGateway.isPaymentSuccess(params)) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setPaidAt(LocalDateTime.now());
            log.info("MoMo payment SUCCESS for order: {}", request.getOrderId());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("MoMo result code: " + request.getResultCode());
            log.warn("MoMo payment FAILED for order: {}", request.getOrderId());
        }

        paymentRepository.save(payment);
        return Map.of("resultCode", 0, "message", "Callback received");
    }

    @Transactional
    public Map<String, Object> handleZaloPayCallback(ZaloPayCallbackRequest request) {
        if (!zaloPayGateway.verifyCallback(request.getData(), request.getMac())) {
            log.warn("ZaloPay callback signature verification failed");
            return Map.of("return_code", 0, "return_message", "Invalid mac");
        }

        Map<String, Object> data = request.getData();
        String appTransId = (String) data.get("app_trans_id");
        String zpTransId = String.valueOf(data.get("zp_trans_id"));

        // Idempotency check: create unique callback reference ID
        String callbackReferenceId = "zalopay_" + appTransId + "_" + zpTransId;
        if (paymentRepository.existsByCallbackReferenceId(callbackReferenceId)) {
            log.info("ZaloPay callback already processed for reference: {}", callbackReferenceId);
            return Map.of("return_code", 1, "return_message", "Callback already processed");
        }

        // Extract order number from app_trans_id format: "yyMMdd_orderNumber"
        String orderNumber = appTransId.contains("_") ? appTransId.substring(appTransId.indexOf("_") + 1) : appTransId;

        Payment payment = paymentRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order: " + orderNumber));

        payment.setTransactionId(zpTransId);
        payment.setGatewayResponse(data.toString());
        payment.setCallbackReferenceId(callbackReferenceId);
        payment.setCallbackProcessedAt(LocalDateTime.now());

        if (zaloPayGateway.isPaymentSuccess(data)) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setPaidAt(LocalDateTime.now());
            log.info("ZaloPay payment SUCCESS for order: {}", orderNumber);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("ZaloPay type: " + request.getType());
            log.warn("ZaloPay payment FAILED for order: {}", orderNumber);
        }

        paymentRepository.save(payment);
        return Map.of("return_code", 1, "return_message", "success");
    }

    private String generatePaymentUrl(CreatePaymentRequest request) {
        return switch (request.getMethod()) {
            case VNPAY -> vnPayGateway.createPaymentUrl(
                    request.getOrderId(),
                    request.getOrderNumber(),
                    request.getAmount(),
                    request.getClientIp() != null ? request.getClientIp() : "127.0.0.1"
            );
            case MOMO -> moMoGateway.createPaymentUrl(
                    request.getOrderId(),
                    request.getOrderNumber(),
                    request.getAmount()
            );
            case ZALOPAY -> zaloPayGateway.createPaymentUrl(
                    request.getOrderId(),
                    request.getOrderNumber(),
                    request.getAmount()
            );
            case BANK_TRANSFER -> throw new PaymentMethodNotImplementedException("BANK_TRANSFER");
            case WALLET -> throw new PaymentMethodNotImplementedException("WALLET");
            default -> throw new BadRequestException("Unsupported payment method for online payment: " + request.getMethod());
        };
    }

    /**
     * Initiates a refund for a completed payment.
     * This is a stub implementation that will need to be expanded
     * to integrate with each payment gateway's refund API.
     *
     * @param paymentId the ID of the payment to refund
     * @param amount the amount to refund (null for full refund)
     * @param reason the reason for the refund
     * @return the updated payment response
     */
    @Transactional
    public PaymentResponse refundPayment(Long paymentId, BigDecimal amount, String reason) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new BadRequestException("Cannot refund a payment that is not successful. Current status: " + payment.getStatus());
        }

        BigDecimal refundAmount = amount != null ? amount : payment.getAmount();
        if (refundAmount.compareTo(payment.getAmount()) > 0) {
            throw new BadRequestException("Refund amount cannot exceed the payment amount");
        }

        // TODO: Implement actual gateway-specific refund logic
        // For now, mark the payment as refunded
        log.info("Initiating refund for payment {} (order: {}), amount: {}, reason: {}",
                paymentId, payment.getOrderNumber(), refundAmount, reason);

        switch (payment.getMethod()) {
            case VNPAY:
                // TODO: Call VNPay refund API
                log.warn("VNPay refund API integration not yet implemented");
                break;
            case MOMO:
                // TODO: Call MoMo refund API
                log.warn("MoMo refund API integration not yet implemented");
                break;
            case ZALOPAY:
                // TODO: Call ZaloPay refund API
                log.warn("ZaloPay refund API integration not yet implemented");
                break;
            case COD:
                // COD refunds are handled manually
                log.info("COD refund processed manually");
                break;
            default:
                throw new BadRequestException("Refund not supported for payment method: " + payment.getMethod());
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefundedAt(LocalDateTime.now());
        payment.setFailureReason("Refund reason: " + reason);

        return paymentMapper.toPaymentResponse(paymentRepository.save(payment));
    }
}
