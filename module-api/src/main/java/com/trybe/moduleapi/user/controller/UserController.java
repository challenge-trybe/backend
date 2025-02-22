package com.trybe.moduleapi.user.controller;

import com.trybe.moduleapi.user.dto.request.UserRequest;
import com.trybe.moduleapi.user.dto.response.UserResponse;
import com.trybe.moduleapi.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public UserResponse findById(@PathVariable Long id){
        return userService.findById(id);
    }

    @PostMapping
    public UserResponse save(@Valid @RequestBody UserRequest.Create userRequest) {
        return userService.save(userRequest);
    }

    @PutMapping("/{id}")
    public UserResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest.Update userRequest) {
        return userService.update(id, userRequest);
    }

    @PutMapping("/{id}/change-password")
    public UserResponse updatePassword(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest.UpdatePassword userRequest) {
        return userService.updatePassword(id, userRequest);
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
