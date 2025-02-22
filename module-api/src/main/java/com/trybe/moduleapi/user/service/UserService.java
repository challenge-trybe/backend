package com.trybe.moduleapi.user.service;

import com.trybe.moduleapi.user.dto.request.UserRequest;
import com.trybe.moduleapi.user.dto.response.UserResponse;
import com.trybe.moduleapi.user.exception.DuplicatedUserException;
import com.trybe.moduleapi.user.exception.NotFoundUserException;
import com.trybe.moduleapi.user.exception.UpdatePasswordFailException;
import com.trybe.modulecore.user.entity.User;
import com.trybe.modulecore.user.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse save(UserRequest.Create userRequest){
        User user = userRequest.toEntity();

        String bcryptPassword = passwordEncoder.encode(userRequest.password());
        user.updatePassword(bcryptPassword);

        User savedUser = userRepository.save(user);
        return UserResponse.from(savedUser);
    }

    @Transactional
    public UserResponse findById(Long id){
        User user = getUserById(id);
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse findByUserId(String userId){
        User user = getUserByUserId(userId);
        return UserResponse.from(user);
    }

    @Transactional
    public void delete(Long id){
        userRepository.deleteById(id);
    }

    @Transactional
    public UserResponse update(Long id, UserRequest.Update userRequest){
        User user = getUserById(id);
        user.update(userRequest.nickname(), userRequest.email(), userRequest.gender(), userRequest.birth());
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updatePassword(Long id, UserRequest.UpdatePassword updatePassword){
        checkedUpdatePassword(id, updatePassword);
        User user = getUserById(id);
        user.updatePassword(updatePassword.newPassword());
        return UserResponse.from(user);
    }

    @Transactional
    public void checkDuplicatedUserId(String userId) {
        userRepository.findByUserId(userId).ifPresent((user) -> {
            throw new DuplicatedUserException("중복된 유저 아이디입니다. userId = " + user.getUserId());
        });
    }

    @Transactional
    public void checkDuplicatedEmail(String email) {
        userRepository.findByEmail(email).ifPresent((user) -> {
            throw new DuplicatedUserException("중복된 이메일입니다. email = " + user.getEmail());
        });
    }

    private void checkedUpdatePassword(Long id, UserRequest.UpdatePassword updatePassword) {
        User user = getUserById(id);

        if  (updatePassword.oldPassword().equals(updatePassword.newPassword())) {
            throw new UpdatePasswordFailException("현재 비밀번호와 새로운 비밀번호가 동일합니다.");
        }

        if (!passwordEncoder.matches(updatePassword.oldPassword(), user.getEncodedPassword())){
            throw new UpdatePasswordFailException("현재 비밀번호를 잘못 입력하셨습니다.");
        }

        if (!updatePassword.newPassword().equals(updatePassword.confirmPassword())) {
            throw new UpdatePasswordFailException("새 비빌번호와 비밀번호 확인이 일치하지 않습니다.");
        }
    }


    private User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(NotFoundUserException::new);
    }

    private User getUserByUserId(String userId) {
        return userRepository.findByUserId(userId).orElseThrow(NotFoundUserException::new);
    }
}
