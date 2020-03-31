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
    private String password;

    @ApiModelProperty(required = true, name = "user first name")
    @Size(max = 50)
    @Pattern(regexp = Constants.USER_NAME_REGEX)
    @NotBlank(message = "first name is mandatory")
    private String firstName;

    @ApiModelProperty(required = true, name = "user last name")
    @Pattern(regexp = Constants.USER_NAME_REGEX)
    @NotBlank(message = "last name is mandatory")
    private String lastName;

    @ApiModelProperty(name = "user email")
    @Email(message = "invalid mail address")
    @Size(min = 5, max = 254)
    private String email;

    @JsonIgnore
    private boolean administrator = false;
}
