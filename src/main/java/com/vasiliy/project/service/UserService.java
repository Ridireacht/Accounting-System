package com.vasiliy.project.service;

import com.vasiliy.project.dto.UserDto;
import com.vasiliy.project.entity.User;

import java.util.List;

public interface UserService {
    void saveUser(UserDto userDto);

    User findUserByEmail(String email);

    List<UserDto> findAllUsers();
}