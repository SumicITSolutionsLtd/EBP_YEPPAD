package com.youthconnect.edge_functions.client;

import com.youthconnect.edge_functions.dto.UserProfileDTO;
import com.youthconnect.edge_functions.dto.UssdRegistrationRequestDTO; // Correct import
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @GetMapping("/api/users/{userId}")
    UserProfileDTO getUserById(@PathVariable Long userId);

    @GetMapping("/api/users/phone/{phoneNumber}")
    UserProfileDTO getUserByPhoneNumber(@PathVariable String phoneNumber);

    @GetMapping("/api/users/exists")
    Boolean checkUserExistsByPhone(@RequestParam String phoneNumber);

    // Add this if you need to create users from USSD
    @PostMapping("/api/users/ussd-register")
    UserProfileDTO registerUssdUser(@RequestBody UssdRegistrationRequestDTO request); // Use correct class name
}