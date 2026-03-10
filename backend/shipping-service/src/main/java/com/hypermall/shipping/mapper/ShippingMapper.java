package com.hypermall.shipping.mapper;

import com.hypermall.shipping.dto.response.ShipmentResponse;
import com.hypermall.shipping.dto.response.TrackingResponse;
import com.hypermall.shipping.entity.Shipment;
import com.hypermall.shipping.entity.TrackingEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ShippingMapper {

    @Mapping(target = "providerName", expression = "java(shipment.getProvider().getDisplayName())")
    ShipmentResponse toShipmentResponse(Shipment shipment);

    List<ShipmentResponse> toShipmentResponseList(List<Shipment> shipments);

    TrackingResponse.TrackingEventResponse toTrackingEventResponse(TrackingEvent event);

    List<TrackingResponse.TrackingEventResponse> toTrackingEventResponseList(List<TrackingEvent> events);
}
