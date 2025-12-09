package com.willbank.userservice.service;

import com.willbank.userservice.dto.UserRegisterDto;
import com.willbank.userservice.dto.UserResponse;
import com.willbank.userservice.dto.UserUpdateDto;
import com.willbank.userservice.model.*;
import com.willbank.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private UserRegisterDto adminDto;
    private UserRegisterDto customerDto;
    private Admin testAdmin;
    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        adminDto = new UserRegisterDto(
                "Amadou",
                "Nkotto",
                "admin@willbank.cm",
                "SecurePass2025!",
                "+237677123456",
                "Avenue Kennedy, Douala",
                UserRole.ADMIN);

        customerDto = new UserRegisterDto(
                "Marie",
                "Fotso",
                "student@example.com",
                "MySecurePass123!",
                "+237698765432",
                "Quartier Bastos, Yaoundé",
                UserRole.CUSTOMER);

        testAdmin = new Admin();
        testAdmin.setId(1L);
        testAdmin.setName("Amadou");
        testAdmin.setSurname("Nkotto");
        testAdmin.setEmail("admin@willbank.cm");
        testAdmin.setPassword(passwordEncoder.encode("SecurePass2025!"));
        testAdmin.setPhone("+237677123456");
        testAdmin.setAddress("Avenue Kennedy, Douala");
        testAdmin.setRole(UserRole.ADMIN);
        testAdmin.setStatus(UserStatus.ACTIVE);

        testCustomer = new Customer();
        testCustomer.setId(2L);
        testCustomer.setName("Marie");
        testCustomer.setSurname("Fotso");
        testCustomer.setEmail("student@example.com");
        testCustomer.setPassword(passwordEncoder.encode("MySecurePass123!"));
        testCustomer.setPhone("+237698765432");
        testCustomer.setAddress("Quartier Bastos, Yaoundé");
        testCustomer.setRole(UserRole.CUSTOMER);
        testCustomer.setStatus(UserStatus.ACTIVE);
        testCustomer.setKycStatus(KycStatus.PENDING);
    }

    @Test
    void registerUser_ShouldCreateAdmin_WhenRoleIsAdmin() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByPhone(anyString())).thenReturn(false);
        when(userRepository.save(any(Admin.class))).thenReturn(testAdmin);

        // When
        UserResponse response = userService.registerUser(adminDto);

        // Then
        assertNotNull(response);
        assertEquals("Amadou", response.getName());
        assertEquals("Nkotto", response.getSurname());
        assertEquals(UserRole.ADMIN, response.getRole());
        assertEquals("ADMIN", response.getUserType());
        assertNull(response.getKycStatus()); // Admin should not have KYC status
        verify(userRepository, times(1)).save(any(Admin.class));
    }

    @Test
    void registerUser_ShouldCreateCustomer_WhenRoleIsCustomer() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByPhone(anyString())).thenReturn(false);
        when(userRepository.save(any(Customer.class))).thenReturn(testCustomer);

        // When
        UserResponse response = userService.registerUser(customerDto);

        // Then
        assertNotNull(response);
        assertEquals("Marie", response.getName());
        assertEquals("Fotso", response.getSurname());
        assertEquals(UserRole.CUSTOMER, response.getRole());
        assertEquals("CUSTOMER", response.getUserType());
        assertEquals(KycStatus.PENDING, response.getKycStatus()); // Customer should have PENDING KYC status
        verify(userRepository, times(1)).save(any(Customer.class));
    }

    @Test
    void registerUser_ShouldHashPassword() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByPhone(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            // Verify password is hashed (BCrypt hashes start with $2a$)
            assertTrue(savedUser.getPassword().startsWith("$2a$"));
            assertNotEquals("SecurePass2025!", savedUser.getPassword());
            return savedUser;
        });

        // When
        userService.registerUser(adminDto);

        // Then
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_ShouldThrowException_WhenEmailExists() {
        // Given
        when(userRepository.existsByEmail(adminDto.getEmail())).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.registerUser(adminDto));
        assertTrue(exception.getMessage().contains("Email already in use"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_ShouldThrowException_WhenPhoneExists() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByPhone(adminDto.getPhone())).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.registerUser(adminDto));
        assertTrue(exception.getMessage().contains("Phone number already in use"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_ShouldReturnUser_WhenExists() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testAdmin));

        // When
        UserResponse response = userService.getUserById(1L);

        // Then
        assertNotNull(response);
        assertEquals(testAdmin.getId(), response.getId());
        assertEquals(testAdmin.getEmail(), response.getEmail());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUserById_ShouldThrowException_WhenNotExists() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> userService.getUserById(999L));
        verify(userRepository, times(1)).findById(999L);
    }

    @Test
    void getAllUsers_ShouldReturnListOfUsers() {
        // Given
        List<User> users = Arrays.asList(testAdmin, testCustomer);
        when(userRepository.findAll()).thenReturn(users);

        // When
        List<UserResponse> responses = userService.getAllUsers();

        // Then
        assertNotNull(responses);
        assertEquals(2, responses.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getUsersByRole_ShouldReturnFilteredUsers() {
        // Given
        when(userRepository.findByRole(UserRole.ADMIN)).thenReturn(Arrays.asList(testAdmin));

        // When
        List<UserResponse> responses = userService.getUsersByRole(UserRole.ADMIN);

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(UserRole.ADMIN, responses.get(0).getRole());
        verify(userRepository, times(1)).findByRole(UserRole.ADMIN);
    }

    @Test
    void getUsersByStatus_ShouldReturnFilteredUsers() {
        // Given
        when(userRepository.findByStatus(UserStatus.ACTIVE)).thenReturn(Arrays.asList(testAdmin, testCustomer));

        // When
        List<UserResponse> responses = userService.getUsersByStatus(UserStatus.ACTIVE);

        // Then
        assertNotNull(responses);
        assertEquals(2, responses.size());
        verify(userRepository, times(1)).findByStatus(UserStatus.ACTIVE);
    }

    @Test
    void updateUser_ShouldUpdateFields() {
        // Given
        UserUpdateDto updateDto = new UserUpdateDto(
                "Pierre",
                "Nkotto",
                "admin@willbank.cm",
                "newPassword",
                "+237677123456",
                "New Address",
                UserStatus.ACTIVE);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testAdmin));
        when(userRepository.save(any(User.class))).thenReturn(testAdmin);

        // When
        UserResponse response = userService.updateUser(1L, updateDto);

        // Then
        assertNotNull(response);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUserStatus_ShouldChangeStatus() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testAdmin));
        when(userRepository.save(any(User.class))).thenReturn(testAdmin);

        // When
        UserResponse response = userService.updateUserStatus(1L, UserStatus.SUSPENDED);

        // Then
        assertNotNull(response);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void deleteUser_ShouldDeleteUser_WhenExists() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testAdmin));

        // When
        userService.deleteUser(1L);

        // Then
        verify(userRepository, times(1)).delete(testAdmin);
    }

    @Test
    void deleteUser_ShouldThrowException_WhenNotExists() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> userService.deleteUser(999L));
        verify(userRepository, never()).delete(any(User.class));
    }
}
