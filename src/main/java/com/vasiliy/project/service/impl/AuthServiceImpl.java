package com.vasiliy.project.service.impl;

import com.vasiliy.project.entity.User;
import com.vasiliy.project.repository.UserRepository;
import com.vasiliy.project.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;


    @Override
    public String getCurrentUserUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByEmail(authentication.getName());

        return user.getName();
    }
}
