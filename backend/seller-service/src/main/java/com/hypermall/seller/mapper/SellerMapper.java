package com.hypermall.seller.mapper;

import com.hypermall.seller.dto.response.SellerResponse;
import com.hypermall.seller.entity.Seller;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SellerMapper {

    SellerResponse toSellerResponse(Seller seller);

    List<SellerResponse> toSellerResponseList(List<Seller> sellers);
}
