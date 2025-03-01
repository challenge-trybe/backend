package com.trybe.moduleapi.annotation;

import com.trybe.moduleapi.auth.CustomUserDetails;
import com.trybe.modulecore.user.entity.User;
import com.trybe.modulecore.user.enums.Gender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithCustomMockUserSecurityContextFactory implements WithSecurityContextFactory<WithCustomMockUser> {
    @Override
    public SecurityContext createSecurityContext(WithCustomMockUser annotation) {
        String username = annotation.username();
        String email = annotation.email();
        String nickname = annotation.nickname();

        User user = new User(username, email, nickname, Gender.NULL, null);
        CustomUserDetails userDetails = new CustomUserDetails(user);

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);

        return context;
    }
}
