package com.bojan.bootcamp_01.dto;

import lombok.Data;
import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;

@Data
public class UserUpdateDto {
    @Pattern(regexp = "^[a-zA-Z0-9]{3,20}$", message = "Username must be alphanumeric and between 3 and 20 characters long")
    private String username;

    @Email(message = "Email should be valid")
    private String email;
}
