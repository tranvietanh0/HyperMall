package com.hypermall.shipping.repository;

import com.hypermall.shipping.entity.Shipment;
import com.hypermall.shipping.entity.ShipmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    Optional<Shipment> findByOrderId(Long orderId);

    Optional<Shipment> findByTrackingNumber(String trackingNumber);

    Page<Shipment> findBySellerId(Long sellerId, Pageable pageable);

    Page<Shipment> findBySellerIdAndStatus(Long sellerId, ShipmentStatus status, Pageable pageable);
}
