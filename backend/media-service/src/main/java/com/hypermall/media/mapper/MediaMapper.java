package com.hypermall.media.mapper;

import com.hypermall.media.dto.response.MediaResponse;
import com.hypermall.media.entity.Media;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MediaMapper {

    MediaResponse toResponse(Media media);

    List<MediaResponse> toResponseList(List<Media> mediaList);
}
