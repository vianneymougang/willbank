package com.willbank.userservice.dto;

import com.willbank.userservice.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link com.willbank.userservice.model.User}
 */
@Value
public class UserRegisterDto implements Serializable {

    @Size(message = "Name should be valid.", min = 1, max = 20)
    @NotBlank(message = "Name is required.")
    String name;

    @Size(message = "Surname should be valid.", min = 1, max = 20)
    @NotBlank(message = "Surname is required.")
    String surname;

    @Size(message = "Email should not exceed 30 characters.", min = 1, max = 30)
    @Email(message = "Email should be valid.")
    @NotBlank(message = "Email is required.")
    String email;

    @Size(message = "Password should at least have 8 characters.", min = 8)
    @NotBlank(message = "Password is required.")
    String password;

    @Size(message = "Phone should be valid.", min = 9, max = 13)
    @NotBlank(message = "Phone is required.")
    String phone;

    String address;

    @NotNull(message = "Role is required.")
    UserRole role;
}