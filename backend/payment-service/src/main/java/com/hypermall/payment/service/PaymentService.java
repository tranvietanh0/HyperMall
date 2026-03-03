package com.hypermall.payment.service;

import com.hypermall.common.exception.BadRequestException;
import com.hypermall.common.exception.ResourceNotFoundException;
import com.hypermall.payment.dto.*;
import com.hypermall.payment.entity.Payment;
import com.hypermall.payment.entity.PaymentMethod;
import com.hypermall.payment.entity.PaymentStatus;
import com.hypermall.payment.gateway.MoMoGateway;
import com.hypermall.payment.gateway.VNPayGateway;
import com.hypermall.payment.gateway.ZaloPayGateway;
import com.hypermall.payment.mapper.PaymentMapper;
import com.hypermall.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        boolean success = vnPayGateway.isPaymentSuccess(params);

        Payment payment = paymentRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order: " + orderNumber));

        payment.setTransactionId(transactionId);
        payment.setGatewayResponse(params.toString());

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

        Payment payment = paymentRepository.findByOrderNumber(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order: " + request.getOrderId()));

        payment.setTransactionId(String.valueOf(request.getTransId()));
        payment.setGatewayResponse(params.toString());

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
        // Extract order number from app_trans_id format: "yyMMdd_orderNumber"
        String orderNumber = appTransId.contains("_") ? appTransId.substring(appTransId.indexOf("_") + 1) : appTransId;

        Payment payment = paymentRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order: " + orderNumber));

        payment.setTransactionId(String.valueOf(data.get("zp_trans_id")));
        payment.setGatewayResponse(data.toString());

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
            default -> throw new BadRequestException("Unsupported payment method for online payment: " + request.getMethod());
        };
    }
}
