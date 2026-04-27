package backend.hobbiebackend.integration;

import backend.hobbiebackend.model.dto.AppClientSignUpDto;
import backend.hobbiebackend.model.dto.LoginDto;
import backend.hobbiebackend.repository.AppClientRepository;
import backend.hobbiebackend.repository.BusinessOwnerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private AppClientRepository appClientRepository;

    @Autowired
    private BusinessOwnerRepository businessOwnerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        appClientRepository.deleteAll();
        businessOwnerRepository.deleteAll();
    }

    @Test
    void testCompleteUserRegistrationAndAuthenticationFlow() throws Exception {
        // Step 1: Register a new user
        AppClientSignUpDto signUpDto = new AppClientSignUpDto();
        signUpDto.setUsername("integrationuser");
        signUpDto.setEmail("integration@example.com");
        signUpDto.setPassword("password123");

        mockMvc.perform(post("/api/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("integrationuser"))
                .andExpect(jsonPath("$.email").value("integration@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist());

        // Step 2: Authenticate the user
        LoginDto loginDto = new LoginDto();
        loginDto.setUsername("integrationuser");
        loginDto.setPassword("password123");

        mockMvc.perform(post("/api/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.token").length(greaterThan(10)))
                .andExpect(jsonPath("$.type").value("Bearer"));
    }

    @Test
    void testUserRegistrationWithDuplicateEmail() throws Exception {
        // Register first user
        AppClientSignUpDto firstUser = new AppClientSignUpDto();
        firstUser.setUsername("user1");
        firstUser.setEmail("duplicate@example.com");
        firstUser.setPassword("password123");

        mockMvc.perform(post("/api/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstUser)))
                .andExpect(status().isCreated());

        // Try to register second user with same email
        AppClientSignUpDto secondUser = new AppClientSignUpDto();
        secondUser.setUsername("user2");
        secondUser.setEmail("duplicate@example.com");
        secondUser.setPassword("password456");

        mockMvc.perform(post("/api/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(secondUser)))
                .andExpect(status().isConflict());
    }

    @Test
    void testAuthenticationWithInvalidCredentials() throws Exception {
        // Register a user first
        AppClientSignUpDto signUpDto = new AppClientSignUpDto();
        signUpDto.setUsername("testuser");
        signUpDto.setEmail("test@example.com");
        signUpDto.setPassword("correctpassword");

        mockMvc.perform(post("/api/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpDto)))
                .andExpect(status().isCreated());

        // Try to authenticate with wrong password
        LoginDto loginDto = new LoginDto();
        loginDto.setUsername("testuser");
        loginDto.setPassword("wrongpassword");

        mockMvc.perform(post("/api/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testAuthenticationWithNonExistentUser() throws Exception {
        LoginDto loginDto = new LoginDto();
        loginDto.setUsername("nonexistent");
        loginDto.setPassword("password123");

        mockMvc.perform(post("/api/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testRegistrationValidation() throws Exception {
        // Test with empty username
        AppClientSignUpDto invalidUser = new AppClientSignUpDto();
        invalidUser.setUsername("");
        invalidUser.setEmail("test@example.com");
        invalidUser.setPassword("password123");

        mockMvc.perform(post("/api/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());

        // Test with invalid email
        invalidUser.setUsername("testuser");
        invalidUser.setEmail("invalid-email");
        invalidUser.setPassword("password123");

        mockMvc.perform(post("/api/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());

        // Test with short password
        invalidUser.setUsername("testuser");
        invalidUser.setEmail("test@example.com");
        invalidUser.setPassword("123");

        mockMvc.perform(post("/api/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAuthenticationValidation() throws Exception {
        // Test with empty username
        LoginDto invalidLogin = new LoginDto();
        invalidLogin.setUsername("");
        invalidLogin.setPassword("password123");

        mockMvc.perform(post("/api/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidLogin)))
                .andExpect(status().isBadRequest());

        // Test with empty password
        invalidLogin.setUsername("testuser");
        invalidLogin.setPassword("");

        mockMvc.perform(post("/api/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidLogin)))
                .andExpect(status().isBadRequest());
    }
}
