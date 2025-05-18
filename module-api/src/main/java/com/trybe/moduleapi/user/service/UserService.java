package com.trybe.moduleapi.user.service;

import com.trybe.moduleapi.auth.CustomUserDetails;
import com.trybe.moduleapi.file.dto.FileResponse;
import com.trybe.moduleapi.file.service.FileManager;
import com.trybe.moduleapi.user.dto.request.UserRequest;
import com.trybe.moduleapi.user.dto.response.UserResponse;
import com.trybe.moduleapi.user.exception.DuplicatedUserException;
import com.trybe.moduleapi.user.exception.NotFoundUserException;
import com.trybe.moduleapi.user.exception.UpdatePasswordFailException;
import com.trybe.modulecore.file.entity.File;
import com.trybe.modulecore.user.entity.User;
import com.trybe.modulecore.user.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final FileManager fileManager;

    private static final String USER_PROFILE_BASE_PATH = "profile/user";

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, FileManager fileManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.fileManager = fileManager;
    }

    @Transactional
    public void save(UserRequest.Create userRequest){
        checkDuplicatedUserId(userRequest.userId());
        checkDuplicatedEmail(userRequest.email());

        User user = userRequest.toEntity();

        String bcryptPassword = passwordEncoder.encode(userRequest.password());
        user.updatePassword(bcryptPassword);

        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserResponse.Detail findById(Long id){
        User user = getUserById(id);
        FileResponse fileResponse = toFileResponse(user.getProfileImage());
        return UserResponse.Detail.from(user, fileResponse);
    }

    @Transactional(readOnly = true)
    public List<User> findByUserIdIn(Set<String> userIds){
        return userRepository.findByUserIdIn(userIds);
    }

    @Transactional
    public void delete(CustomUserDetails userDetails){
        User user = getUserById(userDetails.getUser().getId());

        File profileImage = user.getProfileImage();
        fileManager.deleteFile(profileImage);

        userRepository.deleteById(user.getId());
    }

    @Transactional
    public UserResponse.Detail updateProfile(CustomUserDetails userDetails,
                                             UserRequest.Update userRequest){
        User user = getUserById(userDetails.getUser().getId());
        if (user.getEmail() != userRequest.email()) {
            checkDuplicatedEmail(userRequest.email());
        }
        user.updateProfile(userRequest.nickname(), userRequest.email(), userRequest.gender(), userRequest.birth());
        FileResponse fileResponse = toFileResponse(user.getProfileImage());

        return UserResponse.Detail.from(user, fileResponse);
    }

    @Transactional
    public UserResponse.Detail updateProfileImage(User user, MultipartFile newProfileImage) {

        File file = (user.getProfileImage() != null) ?
                fileManager.updateFile(user.getProfileImage(), newProfileImage, USER_PROFILE_BASE_PATH) :
                fileManager.uploadFile(newProfileImage, USER_PROFILE_BASE_PATH);

        user.updateProfileImage(file);
        userRepository.save(user);

        FileResponse fileResponse = toFileResponse(file);
        return UserResponse.Detail.from(user, fileResponse);
    }

    @Transactional
    public void updatePassword(CustomUserDetails userDetails, UserRequest.UpdatePassword updatePassword){
        User user = userDetails.getUser();
        checkUpdatePassword(user, updatePassword);

        String bcryptPassword = passwordEncoder.encode(updatePassword.newPassword());
        user.updatePassword(bcryptPassword);
    }

    @Transactional
    public void checkDuplicatedUserId(String userId) {
        boolean exists = userRepository.existsByUserId(userId);
        if (exists) {
            throw new DuplicatedUserException("이미 존재하는 아이디입니다.");
        }
    }

    @Transactional
    public void checkDuplicatedEmail(String email) {
        boolean exists = userRepository.existsByEmail(email);
        if (exists) {
            throw new DuplicatedUserException("이미 존재하는 이메일입니다.");
        }
    }

    private void checkUpdatePassword(User user, UserRequest.UpdatePassword updatePassword) {

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

    private FileResponse toFileResponse(File file) {
        return file == null ? null : FileResponse.from(file.getOriginalName(), fileManager.getFileUrl(file.getFilePath()));
    }
}
