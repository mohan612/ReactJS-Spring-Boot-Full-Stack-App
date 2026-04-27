package backend.hobbiebackend.controller;

import backend.hobbiebackend.model.dto.AppClientSignUpDto;
import backend.hobbiebackend.model.dto.BusinessRegisterDto;
import backend.hobbiebackend.model.dto.LoginDto;
import backend.hobbiebackend.model.entities.AppClient;
import backend.hobbiebackend.model.entities.BusinessOwner;
import backend.hobbiebackend.service.UserService;
import backend.hobbiebackend.utility.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private AppClientSignUpDto mockUserDto;
    private BusinessRegisterDto mockBusinessDto;
    private LoginDto mockLoginDto;
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

        mockLoginDto = new LoginDto();
        mockLoginDto.setUsername("testuser");
        mockLoginDto.setPassword("password123");

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
    void testRegisterUser_Success() throws Exception {
        // Given
        when(userService.register(any(AppClientSignUpDto.class))).thenReturn(mockAppClient);

        // When & Then
        mockMvc.perform(post("/api/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockUserDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService, times(1)).register(any(AppClientSignUpDto.class));
    }

    @Test
    void testRegisterUser_ValidationFailure() throws Exception {
        // Given
        AppClientSignUpDto invalidDto = new AppClientSignUpDto();
        invalidDto.setUsername(""); // Invalid username

        // When & Then
        mockMvc.perform(post("/api/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).register(any(AppClientSignUpDto.class));
    }

    @Test
    void testRegisterBusiness_Success() throws Exception {
        // Given
        when(userService.register(any(BusinessRegisterDto.class))).thenReturn(mockBusinessOwner);

        // When & Then
        mockMvc.perform(post("/api/register-business")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockBusinessDto)))
                .andExpected(status().isCreated())
                .andExpect(jsonPath("$.username").value("businessuser"))
                .andExpect(jsonPath("$.businessName").value("Test Business"));

        verify(userService, times(1)).register(any(BusinessRegisterDto.class));
    }

    @Test
    void testRegisterBusiness_ValidationFailure() throws Exception {
        // Given
        BusinessRegisterDto invalidDto = new BusinessRegisterDto();
        invalidDto.setUsername(""); // Invalid username

        // When & Then
        mockMvc.perform(post("/api/register-business")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).register(any(BusinessRegisterDto.class));
    }

    @Test
    void testAuthenticate_Success() throws Exception {
        // Given
        Authentication mockAuthentication = mock(Authentication.class);
        when(mockAuthentication.isAuthenticated()).thenReturn(true);
        when(mockAuthentication.getName()).thenReturn("testuser");

        when(authenticationManager.authenticate(any())).thenReturn(mockAuthentication);
        when(jwtUtil.generateToken("testuser")).thenReturn("mock-jwt-token");

        // When & Then
        mockMvc.perform(post("/api/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockLoginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"));

        verify(authenticationManager, times(1)).authenticate(any());
        verify(jwtUtil, times(1)).generateToken("testuser");
    }

    @Test
    void testAuthenticate_InvalidCredentials() throws Exception {
        // Given
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // When & Then
        mockMvc.perform(post("/api/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockLoginDto)))
                .andExpect(status().isUnauthorized());

        verify(authenticationManager, times(1)).authenticate(any());
        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    void testAuthenticate_ValidationFailure() throws Exception {
        // Given
        LoginDto invalidDto = new LoginDto();
        invalidDto.setUsername(""); // Invalid username

        // When & Then
        mockMvc.perform(post("/api/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(authenticationManager, never()).authenticate(any());
        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    void testRegisterUser_EmailAlreadyExists() throws Exception {
        // Given
        when(userService.register(any(AppClientSignUpDto.class)))
                .thenThrow(new RuntimeException("Email already exists"));

        // When & Then
        mockMvc.perform(post("/api/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockUserDto)))
                .andExpect(status().isConflict());

        verify(userService, times(1)).register(any(AppClientSignUpDto.class));
    }

    @Test
    void testRegisterBusiness_BusinessNameAlreadyExists() throws Exception {
        // Given
        when(userService.register(any(BusinessRegisterDto.class)))
                .thenThrow(new RuntimeException("Business name already exists"));

        // When & Then
        mockMvc.perform(post("/api/register-business")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockBusinessDto)))
                .andExpect(status().isConflict());

        verify(userService, times(1)).register(any(BusinessRegisterDto.class));
    }
}
