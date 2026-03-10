package com.hypermall.shipping.service;

import com.hypermall.common.exception.BadRequestException;
import com.hypermall.common.exception.ResourceNotFoundException;
import com.hypermall.shipping.dto.request.CalculateShippingRequest;
import com.hypermall.shipping.dto.request.CreateShipmentRequest;
import com.hypermall.shipping.dto.response.ShipmentResponse;
import com.hypermall.shipping.dto.response.ShippingOptionResponse;
import com.hypermall.shipping.dto.response.TrackingResponse;
import com.hypermall.shipping.entity.*;
import com.hypermall.shipping.mapper.ShippingMapper;
import com.hypermall.shipping.repository.ShipmentRepository;
import com.hypermall.shipping.repository.TrackingEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShippingService {

    private final ShipmentRepository shipmentRepository;
    private final TrackingEventRepository trackingEventRepository;
    private final ShippingMapper shippingMapper;

    public List<ShippingOptionResponse> calculateShipping(CalculateShippingRequest request) {
        List<ShippingOptionResponse> options = new ArrayList<>();

        // Calculate for each provider (mock calculation)
        // In production, call actual provider APIs

        // GHN
        options.add(calculateGHN(request));

        // GHTK
        options.add(calculateGHTK(request));

        // Viettel Post
        options.add(calculateViettelPost(request));

        return options;
    }

    private ShippingOptionResponse calculateGHN(CalculateShippingRequest request) {
        // Mock calculation - in production call GHN API
        BigDecimal baseFee = BigDecimal.valueOf(22000);
        BigDecimal weightFee = BigDecimal.valueOf(request.getWeight() / 500 * 5000);
        BigDecimal shippingFee = baseFee.add(weightFee);

        BigDecimal insuranceFee = BigDecimal.ZERO;
        if (request.getInsuranceValue() != null && request.getInsuranceValue().compareTo(BigDecimal.ZERO) > 0) {
            insuranceFee = request.getInsuranceValue().multiply(BigDecimal.valueOf(0.005));
        }

        return ShippingOptionResponse.builder()
                .provider(ShippingProvider.GHN)
                .providerName(ShippingProvider.GHN.getDisplayName())
                .serviceName("Express")
                .shippingFee(shippingFee)
                .insuranceFee(insuranceFee)
                .totalFee(shippingFee.add(insuranceFee))
                .estimatedDays(2)
                .estimatedDelivery("2-3 days")
                .build();
    }

    private ShippingOptionResponse calculateGHTK(CalculateShippingRequest request) {
        BigDecimal baseFee = BigDecimal.valueOf(18000);
        BigDecimal weightFee = BigDecimal.valueOf(request.getWeight() / 500 * 4000);
        BigDecimal shippingFee = baseFee.add(weightFee);

        BigDecimal insuranceFee = BigDecimal.ZERO;
        if (request.getInsuranceValue() != null && request.getInsuranceValue().compareTo(BigDecimal.ZERO) > 0) {
            insuranceFee = request.getInsuranceValue().multiply(BigDecimal.valueOf(0.004));
        }

        return ShippingOptionResponse.builder()
                .provider(ShippingProvider.GHTK)
                .providerName(ShippingProvider.GHTK.getDisplayName())
                .serviceName("Standard")
                .shippingFee(shippingFee)
                .insuranceFee(insuranceFee)
                .totalFee(shippingFee.add(insuranceFee))
                .estimatedDays(3)
                .estimatedDelivery("3-4 days")
                .build();
    }

    private ShippingOptionResponse calculateViettelPost(CalculateShippingRequest request) {
        BigDecimal baseFee = BigDecimal.valueOf(20000);
        BigDecimal weightFee = BigDecimal.valueOf(request.getWeight() / 500 * 4500);
        BigDecimal shippingFee = baseFee.add(weightFee);

        BigDecimal insuranceFee = BigDecimal.ZERO;
        if (request.getInsuranceValue() != null && request.getInsuranceValue().compareTo(BigDecimal.ZERO) > 0) {
            insuranceFee = request.getInsuranceValue().multiply(BigDecimal.valueOf(0.005));
        }

        return ShippingOptionResponse.builder()
                .provider(ShippingProvider.VIETTEL_POST)
                .providerName(ShippingProvider.VIETTEL_POST.getDisplayName())
                .serviceName("Standard")
                .shippingFee(shippingFee)
                .insuranceFee(insuranceFee)
                .totalFee(shippingFee.add(insuranceFee))
                .estimatedDays(3)
                .estimatedDelivery("2-4 days")
                .build();
    }

    @Transactional
    public ShipmentResponse createShipment(Long sellerId, CreateShipmentRequest request) {
        // Check if shipment already exists for this order
        if (shipmentRepository.findByOrderId(request.getOrderId()).isPresent()) {
            throw new BadRequestException("Shipment already exists for this order");
        }

        // Generate tracking number (in production, get from provider API)
        String trackingNumber = generateTrackingNumber(request.getProvider());

        // Calculate shipping fee
        CalculateShippingRequest calcRequest = CalculateShippingRequest.builder()
                .fromProvince(request.getSenderProvince())
                .fromDistrict(request.getSenderDistrict())
                .toProvince(request.getReceiverProvince())
                .toDistrict(request.getReceiverDistrict())
                .weight(request.getWeight())
                .insuranceValue(request.getInsuranceValue())
                .build();

        ShippingOptionResponse option = switch (request.getProvider()) {
            case GHN -> calculateGHN(calcRequest);
            case GHTK -> calculateGHTK(calcRequest);
            case VIETTEL_POST -> calculateViettelPost(calcRequest);
            default -> calculateGHN(calcRequest);
        };

        Shipment shipment = Shipment.builder()
                .orderId(request.getOrderId())
                .sellerId(sellerId)
                .provider(request.getProvider())
                .trackingNumber(trackingNumber)
                .status(ShipmentStatus.PENDING)
                .senderName(request.getSenderName())
                .senderPhone(request.getSenderPhone())
                .senderAddress(request.getSenderAddress())
                .senderProvince(request.getSenderProvince())
                .senderDistrict(request.getSenderDistrict())
                .senderWard(request.getSenderWard())
                .receiverName(request.getReceiverName())
                .receiverPhone(request.getReceiverPhone())
                .receiverAddress(request.getReceiverAddress())
                .receiverProvince(request.getReceiverProvince())
                .receiverDistrict(request.getReceiverDistrict())
                .receiverWard(request.getReceiverWard())
                .weight(request.getWeight())
                .length(request.getLength())
                .width(request.getWidth())
                .height(request.getHeight())
                .codAmount(request.getCodAmount() != null ? request.getCodAmount() : BigDecimal.ZERO)
                .shippingFee(option.getShippingFee())
                .insuranceFee(option.getInsuranceFee())
                .note(request.getNote())
                .expectedDeliveryDate(LocalDateTime.now().plusDays(option.getEstimatedDays()))
                .build();

        shipment = shipmentRepository.save(shipment);

        // Create initial tracking event
        addTrackingEvent(shipment, ShipmentStatus.PENDING, "Shipment created, waiting for pickup", null);

        log.info("Shipment created for order {} with tracking {}", request.getOrderId(), trackingNumber);

        return shippingMapper.toShipmentResponse(shipment);
    }

    @Transactional(readOnly = true)
    public ShipmentResponse getShipmentByOrderId(Long orderId) {
        Shipment shipment = shipmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found for order: " + orderId));
        return shippingMapper.toShipmentResponse(shipment);
    }

    @Transactional(readOnly = true)
    public TrackingResponse trackShipment(String trackingNumber) {
        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found: " + trackingNumber));

        List<TrackingEvent> events = trackingEventRepository.findByShipmentIdOrderByEventTimeDesc(shipment.getId());

        return TrackingResponse.builder()
                .trackingNumber(shipment.getTrackingNumber())
                .provider(shipment.getProvider())
                .providerName(shipment.getProvider().getDisplayName())
                .currentStatus(shipment.getStatus())
                .receiverName(shipment.getReceiverName())
                .receiverAddress(shipment.getReceiverAddress())
                .expectedDeliveryDate(shipment.getExpectedDeliveryDate())
                .deliveredAt(shipment.getDeliveredAt())
                .events(shippingMapper.toTrackingEventResponseList(events))
                .build();
    }

    @Transactional(readOnly = true)
    public Page<ShipmentResponse> getSellerShipments(Long sellerId, Pageable pageable) {
        Page<Shipment> shipments = shipmentRepository.findBySellerId(sellerId, pageable);
        return shipments.map(shippingMapper::toShipmentResponse);
    }

    @Transactional
    public ShipmentResponse updateShipmentStatus(Long shipmentId, ShipmentStatus newStatus, String description, String location) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found"));

        shipment.setStatus(newStatus);

        if (newStatus == ShipmentStatus.PICKED_UP) {
            shipment.setPickedUpAt(LocalDateTime.now());
        } else if (newStatus == ShipmentStatus.DELIVERED) {
            shipment.setDeliveredAt(LocalDateTime.now());
        }

        shipment = shipmentRepository.save(shipment);

        addTrackingEvent(shipment, newStatus, description, location);

        log.info("Shipment {} status updated to {}", shipmentId, newStatus);

        return shippingMapper.toShipmentResponse(shipment);
    }

    private void addTrackingEvent(Shipment shipment, ShipmentStatus status, String description, String location) {
        TrackingEvent event = TrackingEvent.builder()
                .shipment(shipment)
                .status(status)
                .description(description)
                .location(location)
                .eventTime(LocalDateTime.now())
                .build();
        trackingEventRepository.save(event);
    }

    private String generateTrackingNumber(ShippingProvider provider) {
        String prefix = switch (provider) {
            case GHN -> "GHN";
            case GHTK -> "GHTK";
            case VIETTEL_POST -> "VTP";
            default -> "HM";
        };
        return prefix + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }
}
