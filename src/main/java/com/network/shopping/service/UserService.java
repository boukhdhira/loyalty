package com.network.shopping.service;

import com.network.shopping.service.dto.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserDTO createUserAccount(UserDTO userDTO);

    void deleteUser(String username);

    Page<UserDTO> getAllManagedUsers(Pageable pageable);

    void activateRegistration(String key);
}
