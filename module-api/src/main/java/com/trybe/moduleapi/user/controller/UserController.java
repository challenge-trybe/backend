package com.trybe.moduleapi.user.controller;

import com.trybe.moduleapi.auth.CustomUserDetails;
import com.trybe.moduleapi.user.dto.request.UserRequest;
import com.trybe.moduleapi.user.dto.response.UserResponse;
import com.trybe.moduleapi.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public UserResponse.Detail findById(@PathVariable Long id){
        return userService.findById(id);
    }

    @PostMapping
    public void save(@Valid @RequestBody UserRequest.Create userRequest) {
        userService.save(userRequest);
    }

    @PutMapping
    public UserResponse.Detail update(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UserRequest.Update userRequest) {
        return userService.updateProfile(userDetails, userRequest);
    }

    @PutMapping("/change-password")
    public void updatePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UserRequest.UpdatePassword userRequest) {
        userService.updatePassword(userDetails, userRequest);
    }

    @DeleteMapping
    public void delete(@AuthenticationPrincipal CustomUserDetails userDetails) {
        userService.delete(userDetails);
    }

    @GetMapping("/check-user-id")
    public void checkDuplicatedUserId(@RequestParam("userId") String userId){
        userService.checkDuplicatedUserId(userId);
    }

    @GetMapping("/check-email")
    public void checkDuplicatedEmail(@RequestParam("email") String email){
        userService.checkDuplicatedEmail(email);
    }


}
