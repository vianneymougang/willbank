package com.willbank.userservice.dto;

import com.willbank.userservice.model.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for partial user updates.
 * All fields are optional - only non-null fields will be updated.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDto implements Serializable {

    @Size(min = 1, max = 20, message = "Name should be between 1 and 20 characters")
    private String name;

    @Size(min = 1, max = 20, message = "Surname should be between 1 and 20 characters")
    private String surname;

    @Size(min = 1, max = 30, message = "Email should not exceed 30 characters")
    @Email(message = "Email should be valid")
    private String email;

    @Size(min = 8, message = "Password should be at least 8 characters")
    private String password;

    @Size(min = 9, max = 13, message = "Phone should be between 9 and 13 characters")
    private String phone;

    private String address;

    private UserStatus status;
}