package com.network.shopping.web.rest;

import com.network.shopping.config.Constants;
import com.network.shopping.service.UserService;
import com.network.shopping.service.dto.UserDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import javax.validation.Valid;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * TODO:
 * 1 add service that register admin (use same implementation)
 * 2 handle security access restriction by role
 * 3  add service to activate user
 * 4 rattache user and account
 */
@RestController
@RequestMapping("/api")
@Slf4j
@Api("User management services")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * {@code POST  /register}  : register a new user platform.
     * <p>
     * Creates a new user if the login and email are not already used, and sends an
     * mail with an activation link.
     * The user needs to be activated on creation.
     *
     * @param userDTO the user to create.
     *                status {@code 201 (Created)} and with body the new user,
     *                or with status {@code 400 (Bad Request)} if the login or email is already in use.
     * @throws HttpClientErrorException.BadRequest {@code 400 (Bad Request)} if the login or email is already in use.
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation("Register a new user account")
    //@PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public void createUser(@Valid @RequestBody @NonNull @ApiParam(value = "user data information")
                                   UserDTO userDTO) {
        log.debug("REST request to save User : {}", userDTO);
        if (!this.checkPasswordLength(userDTO.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }
        this.userService.createUser(userDTO);
    }

    private boolean checkPasswordLength(String password) {
        return !isEmpty(password) &&
                password.length() >= Constants.PASSWORD_MIN_LENGTH &&
                password.length() <= Constants.PASSWORD_MAX_LENGTH;
    }
}
