package com.hypermall.payment.repository;

import com.hypermall.payment.entity.Payment;
import com.hypermall.payment.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByOrderId(Long orderId);

    Optional<Payment> findByOrderNumber(String orderNumber);

    Optional<Payment> findByTransactionId(String transactionId);

    Page<Payment> findByUserId(Long userId, Pageable pageable);

    Optional<Payment> findTopByOrderIdOrderByCreatedAtDesc(Long orderId);

    List<Payment> findByOrderIdAndStatus(Long orderId, PaymentStatus status);
}
