package com.hypermall.notification.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hypermall.notification.dto.NotificationPreferenceResponse;
import com.hypermall.notification.dto.NotificationResponse;
import com.hypermall.notification.entity.Notification;
import com.hypermall.notification.entity.NotificationPreference;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(target = "data", source = "data", qualifiedByName = "jsonToMap")
    NotificationResponse toResponse(Notification notification);

    NotificationPreferenceResponse toPreferenceResponse(NotificationPreference preference);

    @Named("jsonToMap")
    default Map<String, Object> jsonToMap(String json) {
        if (json == null || json.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            return Collections.emptyMap();
        }
    }

    @Named("mapToJson")
    default String mapToJson(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
