package com.network.shopping.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.network.shopping.config.Constants;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
@JsonIgnoreProperties(allowSetters = true, value = "password")
@ApiModel(description = "All user details ")
public class UserDTO implements Serializable {
    private static final long serialVersionUID = -2624326677631113L;
    @ApiModelProperty(required = true, name = "user login credential")
    @Pattern(regexp = Constants.LOGIN_REGEX, message = "Invalid login format")
    @Size(min = 1, max = 50)
    @NotBlank(message = "user name is mandatory")
    private String username;

    @ApiModelProperty(required = true, name = "user password credential")
    @NotBlank(message = "password is mandatory")
    @Size(max = Constants.PASSWORD_MAX_LENGTH, min = Constants.PASSWORD_MIN_LENGTH,
            message = "Password don't match required size")
    private String password;

    @ApiModelProperty(required = true, name = "user first name")
    @Size(max = 50)
    @Pattern(regexp = Constants.USER_NAME_REGEX, message = "Only letters are accepted for first name")
    @NotBlank(message = "first name is mandatory")
    private String firstName;

    @ApiModelProperty(required = true, name = "user last name")
    @Pattern(regexp = Constants.USER_NAME_REGEX, message = "Only letters are accepted for last name")
    @NotBlank(message = "last name is mandatory")
    private String lastName;

    @ApiModelProperty(name = "user email", required = true)
    @Email(message = "invalid mail address")
    @NotBlank(message = "email address is mandatory to activate your account")
    @Size(min = 5, max = 254)
    private String email;

    @JsonIgnore
    private boolean administrator = false;
}
