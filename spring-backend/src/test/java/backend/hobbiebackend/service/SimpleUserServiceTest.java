package backend.hobbiebackend.service;

import backend.hobbiebackend.model.dto.AppClientSignUpDto;
import backend.hobbiebackend.model.entities.AppClient;
import backend.hobbiebackend.model.entities.UserEntity;
import backend.hobbiebackend.model.repostiory.AppClientRepository;
import backend.hobbiebackend.model.repostiory.UserRepository;
import backend.hobbiebackend.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SimpleUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AppClientRepository appClientRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private AppClientSignUpDto mockUserDto;
    private UserEntity mockUserEntity;
    private AppClient mockAppClient;

    @BeforeEach
    void setUp() {
        mockUserDto = new AppClientSignUpDto();
        mockUserDto.setUsername("testuser");
        mockUserDto.setEmail("test@example.com");
        mockUserDto.setPassword("password123");

        mockUserEntity = new UserEntity();
        mockUserEntity.setId(1L);
        mockUserEntity.setUsername("testuser");
        mockUserEntity.setEmail("test@example.com");

        mockAppClient = new AppClient();
        mockAppClient.setId(1L);
        mockAppClient.setUsername("testuser");
        mockAppClient.setEmail("test@example.com");
    }

    @Test
    void testRegisterAppClient_Success() {
        // Given
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(appClientRepository.save(any(AppClient.class))).thenReturn(mockAppClient);

        // When
        AppClient result = userService.register(mockUserDto);

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        verify(passwordEncoder, times(1)).encode("password123");
        verify(appClientRepository, times(1)).save(any(AppClient.class));
    }

    @Test
    void testFindUserById_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUserEntity));

        // When
        UserEntity result = userService.findUserById(1L);

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void testFindUserById_NotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        UserEntity result = userService.findUserById(1L);

        // Then
        assertNull(result);
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void testFindUserByEmail_Success() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUserEntity));

        // When
        UserEntity result = userService.findUserByEmail("test@example.com");

        // Then
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    void testFindUserByEmail_NotFound() {
        // Given
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When
        UserEntity result = userService.findUserByEmail("nonexistent@example.com");

        // Then
        assertNull(result);
        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
    }

    @Test
    void testFindAppClientByUsername_Success() {
        // Given
        when(appClientRepository.findByUsername("testuser")).thenReturn(Optional.of(mockAppClient));

        // When
        AppClient result = userService.findAppClientByUsername("testuser");

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(appClientRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void testFindAppClientByUsername_NotFound() {
        // Given
        when(appClientRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When
        AppClient result = userService.findAppClientByUsername("nonexistent");

        // Then
        assertNull(result);
        verify(appClientRepository, times(1)).findByUsername("nonexistent");
    }
}
