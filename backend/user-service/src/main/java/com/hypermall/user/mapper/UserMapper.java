package com.hypermall.user.mapper;

import com.hypermall.user.dto.AddressResponse;
import com.hypermall.user.dto.UserResponse;
import com.hypermall.user.entity.Address;
import com.hypermall.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    UserResponse toUserResponse(User user);

    List<UserResponse> toUserResponseList(List<User> users);

    AddressResponse toAddressResponse(Address address);

    List<AddressResponse> toAddressResponseList(List<Address> addresses);
}
