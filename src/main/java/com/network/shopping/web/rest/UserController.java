package com.network.shopping.web.rest;

import com.network.shopping.dto.UserDTO;
import com.network.shopping.service.UserService;
import com.network.shopping.service.utils.RestRequestUtils;
import io.swagger.annotations.*;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/v1")
@Slf4j
@Api("User management services")
public class UserController {

    private final UserService userService;

    @Value("${application.name}")
    private String applicationName;

    @Autowired
    public UserController(final UserService userService) {
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
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation("Register a new user account")
    public void createUser(@Valid @RequestBody @NonNull @ApiParam(value = "user data information") final
                           UserDTO userDTO) {
        log.debug("REST request to save User : {}", userDTO);
        this.userService.createUserAccount(userDTO);
    }

    @PostMapping("/users/admin")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation("Register a new administrator")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully added new administrator account"),
            @ApiResponse(code = 400, message = "Validation failed for data")})
    public void createAdministratorUser(@Valid @RequestBody @ApiParam(value = "admin information") final
                                        UserDTO userDTO) {
        log.debug("REST request to save administrator : {}", userDTO);
        userDTO.setAdministrator(true);
        this.userService.createUserAccount(userDTO);
    }

    /**
     * {@code GET /users} : get all users.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body all users.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<Page<UserDTO>> getAllUsers(final Pageable pageable) {
        final Page<UserDTO> page = this.userService.getAllManagedUsers(pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * {@code DELETE /users/:username} : delete the "username" User.
     *
     * @param username the username of the user to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/users/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable final String username) {
        log.debug("REST request to delete User: {}", username);
        this.userService.deleteUser(username);
        return ResponseEntity.noContent().headers(RestRequestUtils.createAlert(
                this.applicationName, "usersManagement.deleted", username)).build();
    }

    /**
     * {@code GET /activate} : Confirm account mail & activate account.
     *
     * @param key the identifier of created account.
     * @return void with status {@code 200 (ok)}.
     */
    @GetMapping("/activate")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Activate user account by token", notes = "token must be valid and not expired")
    public void activateUserAccount(@Valid @NotBlank(message = "Activation key is invalid or broken!")
                                    @RequestParam final String key) {
        this.userService.activateRegistration(key);
    }
}
