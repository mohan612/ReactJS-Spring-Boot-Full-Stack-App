package backend.hobbiebackend.service;

import backend.hobbiebackend.model.dto.AppClientSignUpDto;
import backend.hobbiebackend.model.dto.BusinessRegisterDto;
import backend.hobbiebackend.model.entities.AppClient;
import backend.hobbiebackend.model.entities.BusinessOwner;
import backend.hobbiebackend.model.entities.Hobby;
import backend.hobbiebackend.model.entities.UserEntity;
import backend.hobbiebackend.service.impl.UserServiceImpl;
import backend.hobbiebackend.repository.AppClientRepository;
import backend.hobbiebackend.repository.BusinessOwnerRepository;
import backend.hobbiebackend.repository.UserRepository;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AppClientRepository appClientRepository;

    @Mock
    private BusinessOwnerRepository businessOwnerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private AppClientSignUpDto mockUserDto;
    private BusinessRegisterDto mockBusinessDto;
    private UserEntity mockUserEntity;
    private AppClient mockAppClient;
    private BusinessOwner mockBusinessOwner;

    @BeforeEach
    void setUp() {
        mockUserDto = new AppClientSignUpDto();
        mockUserDto.setUsername("testuser");
        mockUserDto.setEmail("test@example.com");
        mockUserDto.setPassword("password123");

        mockBusinessDto = new BusinessRegisterDto();
        mockBusinessDto.setUsername("businessuser");
        mockBusinessDto.setEmail("business@example.com");
        mockBusinessDto.setPassword("businesspass123");
        mockBusinessDto.setBusinessName("Test Business");

        mockUserEntity = new UserEntity();
        mockUserEntity.setId(1L);
        mockUserEntity.setUsername("testuser");
        mockUserEntity.setEmail("test@example.com");

        mockAppClient = new AppClient();
        mockAppClient.setId(1L);
        mockAppClient.setUsername("testuser");
        mockAppClient.setEmail("test@example.com");

        mockBusinessOwner = new BusinessOwner();
        mockBusinessOwner.setId(1L);
        mockBusinessOwner.setUsername("businessuser");
        mockBusinessOwner.setEmail("business@example.com");
        mockBusinessOwner.setBusinessName("Test Business");
    }

    @Test
    void testSeedUsersAndUserRoles() {
        // Given
        when(userRepository.saveAll(anyList())).thenReturn(Arrays.asList(mockUserEntity));

        // When
        List<UserEntity> result = userService.seedUsersAndUserRoles();

        // Then
        assertNotNull(result);
        verify(userRepository, times(1)).saveAll(anyList());
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
        verify(passwordEncoder, times(1)).encode("password123");
        verify(appClientRepository, times(1)).save(any(AppClient.class));
    }

    @Test
    void testRegisterAppClient_EmailAlreadyExists() {
        // Given
        when(appClientRepository.existsByEmail(anyString())).thenReturn(true);

        // When & Then
        assertThrows(RuntimeException.class, () -> userService.register(mockUserDto));
        verify(appClientRepository, never()).save(any(AppClient.class));
    }

    @Test
    void testRegisterBusiness_Success() {
        // Given
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(businessOwnerRepository.save(any(BusinessOwner.class))).thenReturn(mockBusinessOwner);

        // When
        BusinessOwner result = userService.register(mockBusinessDto);

        // Then
        assertNotNull(result);
        verify(passwordEncoder, times(1)).encode("businesspass123");
        verify(businessOwnerRepository, times(1)).save(any(BusinessOwner.class));
    }

    @Test
    void testRegisterBusiness_BusinessNameAlreadyExists() {
        // Given
        when(businessOwnerRepository.existsByBusinessName(anyString())).thenReturn(true);

        // When & Then
        assertThrows(RuntimeException.class, () -> userService.register(mockBusinessDto));
        verify(businessOwnerRepository, never()).save(any(BusinessOwner.class));
    }

    @Test
    void testSaveUpdatedUserClient() {
        // Given
        when(appClientRepository.save(any(AppClient.class))).thenReturn(mockAppClient);

        // When
        AppClient result = userService.saveUpdatedUserClient(mockAppClient);

        // Then
        assertNotNull(result);
        verify(appClientRepository, times(1)).save(mockAppClient);
    }

    @Test
    void testSaveUpdatedUserBusiness() {
        // Given
        when(businessOwnerRepository.save(any(BusinessOwner.class))).thenReturn(mockBusinessOwner);

        // When
        BusinessOwner result = userService.saveUpdatedUser(mockBusinessOwner);

        // Then
        assertNotNull(result);
        verify(businessOwnerRepository, times(1)).save(mockBusinessOwner);
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
    void testDeleteUser_Success() {
        // Given
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        // When
        boolean result = userService.deleteUser(1L);

        // Then
        assertTrue(result);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteUser_NotFound() {
        // Given
        when(userRepository.existsById(1L)).thenReturn(false);

        // When
        boolean result = userService.deleteUser(1L);

        // Then
        assertFalse(result);
        verify(userRepository, never()).deleteById(1L);
    }

    @Test
    void testUserExists_True() {
        // Given
        when(userRepository.existsByUsernameOrEmail("testuser", "test@example.com")).thenReturn(true);

        // When
        boolean result = userService.userExists("testuser", "test@example.com");

        // Then
        assertTrue(result);
        verify(userRepository, times(1)).existsByUsernameOrEmail("testuser", "test@example.com");
    }

    @Test
    void testUserExists_False() {
        // Given
        when(userRepository.existsByUsernameOrEmail("nonexistent", "nonexistent@example.com")).thenReturn(false);

        // When
        boolean result = userService.userExists("nonexistent", "nonexistent@example.com");

        // Then
        assertFalse(result);
        verify(userRepository, times(1)).existsByUsernameOrEmail("nonexistent", "nonexistent@example.com");
    }

    @Test
    void testFindAppClientById_Success() {
        // Given
        when(appClientRepository.findById(1L)).thenReturn(Optional.of(mockAppClient));

        // When
        AppClient result = userService.findAppClientById(1L);

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(appClientRepository, times(1)).findById(1L);
    }

    @Test
    void testFindAppClientById_NotFound() {
        // Given
        when(appClientRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        AppClient result = userService.findAppClientById(1L);

        // Then
        assertNull(result);
        verify(appClientRepository, times(1)).findById(1L);
    }

    @Test
    void testFindBusinessOwnerById_Success() {
        // Given
        when(businessOwnerRepository.findById(1L)).thenReturn(Optional.of(mockBusinessOwner));

        // When
        BusinessOwner result = userService.findBusinessOwnerById(1L);

        // Then
        assertNotNull(result);
        assertEquals("businessuser", result.getUsername());
        verify(businessOwnerRepository, times(1)).findById(1L);
    }

    @Test
    void testFindBusinessOwnerById_NotFound() {
        // Given
        when(businessOwnerRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        BusinessOwner result = userService.findBusinessOwnerById(1L);

        // Then
        assertNull(result);
        verify(businessOwnerRepository, times(1)).findById(1L);
    }

    @Test
    void testBusinessExists_True() {
        // Given
        when(businessOwnerRepository.existsByBusinessName("Test Business")).thenReturn(true);

        // When
        boolean result = userService.businessExists("Test Business");

        // Then
        assertTrue(result);
        verify(businessOwnerRepository, times(1)).existsByBusinessName("Test Business");
    }

    @Test
    void testBusinessExists_False() {
        // Given
        when(businessOwnerRepository.existsByBusinessName("Nonexistent Business")).thenReturn(false);

        // When
        boolean result = userService.businessExists("Nonexistent Business");

        // Then
        assertFalse(result);
        verify(businessOwnerRepository, times(1)).existsByBusinessName("Nonexistent Business");
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
    void testFindBusinessByUsername_Success() {
        // Given
        when(businessOwnerRepository.findByUsername("businessuser")).thenReturn(Optional.of(mockBusinessOwner));

        // When
        BusinessOwner result = userService.findBusinessByUsername("businessuser");

        // Then
        assertNotNull(result);
        assertEquals("businessuser", result.getUsername());
        verify(businessOwnerRepository, times(1)).findByUsername("businessuser");
    }
}
