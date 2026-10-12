
package com.example.vendorcrm.service;

import com.example.vendorcrm.dto.VendorDto;
import com.example.vendorcrm.entity.User;
import com.example.vendorcrm.repository.UserRepository;
import com.example.vendorcrm.repository.VendorRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class VendorService {

    private final VendorRepository vendorRepository;
    private final UserRepository userRepository;

    public VendorService(
            VendorRepository vendorRepository,
            UserRepository userRepository) {
        this.vendorRepository = vendorRepository;
        this.userRepository = userRepository;
    }

    public List<VendorDto> findAll() {
        return vendorRepository.findAll()
                .stream()
                .map(VendorDto::from)
                .toList();
    }

    public VendorDto findByUserEmail(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new AccessDeniedException("User not found"));

        if (user.getVendor() == null) {
            throw new AccessDeniedException(
                    "No vendor linked to this account");
        }

        return VendorDto.from(user.getVendor());
    }
}
