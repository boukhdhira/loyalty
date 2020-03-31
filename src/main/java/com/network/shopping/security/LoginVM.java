package com.network.shopping.security;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

import static com.network.shopping.config.Constants.PASSWORD_MAX_LENGTH;
import static com.network.shopping.config.Constants.PASSWORD_MIN_LENGTH;

@Data
@ApiModel(description = "Authentication form")
public class LoginVM implements Serializable {
    private static final long serialVersionUID = -5604186713664100138L;

    @ApiModelProperty(notes = " username or email address ", required = true)
    @NotBlank(message = "You must enter login")
    @Size(min = 1, max = 50)
    private String username;

    @ApiModelProperty(required = true)
    @NotBlank(message = "You must enter password")
    @Size(min = PASSWORD_MIN_LENGTH, max = PASSWORD_MAX_LENGTH)
    private String password;

    @ApiModelProperty(notes = "Keep logged in flag")
    private Boolean rememberMe;

    public Boolean isRememberMe() {
        return this.rememberMe;
    }
}
