package com.hypermall.user.service;

import com.hypermall.common.exception.BadRequestException;
import com.hypermall.common.exception.ResourceNotFoundException;
import com.hypermall.user.dto.AddressRequest;
import com.hypermall.user.dto.AddressResponse;
import com.hypermall.user.entity.Address;
import com.hypermall.user.entity.User;
import com.hypermall.user.mapper.UserMapper;
import com.hypermall.user.repository.AddressRepository;
import com.hypermall.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    private static final int MAX_ADDRESSES_PER_USER = 10;

    @Transactional(readOnly = true)
    public List<AddressResponse> getAddresses(Long userId) {
        List<Address> addresses = addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
        return userMapper.toAddressResponseList(addresses);
    }

    @Transactional(readOnly = true)
    public AddressResponse getAddress(Long userId, Long addressId) {
        Address address = findAddressByIdAndUserId(addressId, userId);
        return userMapper.toAddressResponse(address);
    }

    @Transactional
    public AddressResponse createAddress(Long userId, AddressRequest request) {
        long count = addressRepository.countByUserId(userId);
        if (count >= MAX_ADDRESSES_PER_USER) {
            throw new BadRequestException("Maximum number of addresses reached (" + MAX_ADDRESSES_PER_USER + ")");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Address address = Address.builder()
                .user(user)
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .province(request.getProvince())
                .district(request.getDistrict())
                .ward(request.getWard())
                .addressDetail(request.getAddressDetail())
                .isDefault(request.getIsDefault() != null ? request.getIsDefault() : false)
                .type(request.getType())
                .build();

        // If this is the first address or set as default, handle default logic
        if (address.getIsDefault() || count == 0) {
            address.setIsDefault(true);
            addressRepository.clearDefaultAddress(userId, -1L);
        }

        address = addressRepository.save(address);
        log.info("Address created for user {}: {}", userId, address.getId());

        return userMapper.toAddressResponse(address);
    }

    @Transactional
    public AddressResponse updateAddress(Long userId, Long addressId, AddressRequest request) {
        Address address = findAddressByIdAndUserId(addressId, userId);

        address.setFullName(request.getFullName());
        address.setPhone(request.getPhone());
        address.setProvince(request.getProvince());
        address.setDistrict(request.getDistrict());
        address.setWard(request.getWard());
        address.setAddressDetail(request.getAddressDetail());

        if (request.getType() != null) {
            address.setType(request.getType());
        }

        if (Boolean.TRUE.equals(request.getIsDefault()) && !address.getIsDefault()) {
            addressRepository.clearDefaultAddress(userId, addressId);
            address.setIsDefault(true);
        }

        address = addressRepository.save(address);
        log.info("Address updated for user {}: {}", userId, addressId);

        return userMapper.toAddressResponse(address);
    }

    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        Address address = findAddressByIdAndUserId(addressId, userId);

        boolean wasDefault = address.getIsDefault();
        addressRepository.delete(address);

        // If deleted address was default, set another address as default
        if (wasDefault) {
            List<Address> remainingAddresses = addressRepository
                    .findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
            if (!remainingAddresses.isEmpty()) {
                Address newDefault = remainingAddresses.get(0);
                newDefault.setIsDefault(true);
                addressRepository.save(newDefault);
            }
        }

        log.info("Address deleted for user {}: {}", userId, addressId);
    }

    @Transactional
    public AddressResponse setDefaultAddress(Long userId, Long addressId) {
        Address address = findAddressByIdAndUserId(addressId, userId);

        addressRepository.clearDefaultAddress(userId, addressId);
        address.setIsDefault(true);
        address = addressRepository.save(address);

        log.info("Default address set for user {}: {}", userId, addressId);
        return userMapper.toAddressResponse(address);
    }

    private Address findAddressByIdAndUserId(Long addressId, Long userId) {
        return addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
    }
}
