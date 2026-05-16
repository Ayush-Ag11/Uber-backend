package com.demo.project.uber.services;

import com.demo.project.uber.dto.SignupDto;
import com.demo.project.uber.dto.UserDto;
import com.demo.project.uber.entities.Driver;
import com.demo.project.uber.entities.User;
import com.demo.project.uber.entities.enums.Role;
import com.demo.project.uber.exceptions.ResourceNotFoundException;
import com.demo.project.uber.exceptions.RuntimeConflictExceptions;
import com.demo.project.uber.repositories.UserRepository;
import com.demo.project.uber.security.JWTService;
import com.demo.project.uber.services.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RiderService riderService;

    @Mock
    private WalletService walletService;

    @Mock
    private DriverService driverService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JWTService jwtService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;
    private SignupDto signupDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");
        user.setName("Test User");
        user.setPassword("encodedPassword");
        user.setRoles(Set.of(Role.RIDER));

        signupDto = new SignupDto();
        signupDto.setEmail("test@test.com");
        signupDto.setName("Test User");
        signupDto.setPassword("plainPassword");
    }

    @Test
    void login_shouldReturnAccessAndRefreshTokens() {
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
        when(jwtService.generateAccessToken(user)).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(user)).thenReturn("refreshToken");

        String[] tokens = authService.login("test@test.com", "plainPassword");

        assertThat(tokens).hasSize(2);
        assertThat(tokens[0]).isEqualTo("accessToken");
        assertThat(tokens[1]).isEqualTo("refreshToken");
    }

    @Test
    void login_shouldCallAuthenticationManager() {
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
        when(jwtService.generateAccessToken(any())).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(any())).thenReturn("refreshToken");

        authService.login("test@test.com", "plainPassword");

        verify(authenticationManager, times(1)).authenticate(any());
    }

    @Test
    void signUp_shouldCreateUserSuccessfully() {
        when(userRepository.findByEmail(signupDto.getEmail())).thenReturn(Optional.empty());
        when(modelMapper.map(signupDto, User.class)).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(modelMapper.map(user, UserDto.class)).thenReturn(new UserDto());

        UserDto result = authService.signUp(signupDto);

        assertThat(result).isNotNull();
        verify(userRepository, times(1)).save(any(User.class));
        verify(riderService, times(1)).createNewRider(any(User.class));
        verify(walletService, times(1)).createNewWallet(any(User.class));
    }

    @Test
    void signUp_shouldEncodePassword() {
        when(userRepository.findByEmail(signupDto.getEmail())).thenReturn(Optional.empty());
        when(modelMapper.map(signupDto, User.class)).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(modelMapper.map(user, UserDto.class)).thenReturn(new UserDto());

        authService.signUp(signupDto);

        verify(passwordEncoder, times(1)).encode(anyString());
    }

    @Test
    void signUp_shouldAssignRiderRoleByDefault() {
        when(userRepository.findByEmail(signupDto.getEmail())).thenReturn(Optional.empty());
        when(modelMapper.map(signupDto, User.class)).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(modelMapper.map(user, UserDto.class)).thenReturn(new UserDto());

        authService.signUp(signupDto);

        assertThat(user.getRoles()).contains(Role.RIDER);
    }

    @Test
    void signUp_shouldThrowException_whenEmailAlreadyExists() {
        when(userRepository.findByEmail(signupDto.getEmail())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.signUp(signupDto))
                .isInstanceOf(RuntimeConflictExceptions.class)
                .hasMessageContaining("User already exists with email");
    }

    @Test
    void signUp_shouldCreateRiderAndWallet_whenUserCreated() {
        when(userRepository.findByEmail(signupDto.getEmail())).thenReturn(Optional.empty());
        when(modelMapper.map(signupDto, User.class)).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(modelMapper.map(user, UserDto.class)).thenReturn(new UserDto());

        authService.signUp(signupDto);

        verify(riderService, times(1)).createNewRider(user);
        verify(walletService, times(1)).createNewWallet(user);
    }

    @Test
    void onBoardNewDriver_shouldCreateDriverSuccessfully() {
        user.setRoles(new HashSet<>(Set.of(Role.RIDER)));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(driverService.createNewDriver(any(Driver.class))).thenReturn(new Driver());
        when(modelMapper.map(any(Driver.class), any())).thenReturn(null);

        authService.onBoardNewDriver(1L, "VH-1234");

        verify(driverService, times(1)).createNewDriver(any(Driver.class));
        verify(userRepository, times(1)).save(user);
        assertThat(user.getRoles()).contains(Role.DRIVER);
    }

    @Test
    void onBoardNewDriver_shouldThrowException_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.onBoardNewDriver(99L, "VH-1234"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id");
    }

    @Test
    void onBoardNewDriver_shouldThrowException_whenUserAlreadyDriver() {
        user.setRoles(Set.of(Role.DRIVER));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.onBoardNewDriver(1L, "VH-1234"))
                .isInstanceOf(RuntimeConflictExceptions.class)
                .hasMessageContaining("Driver already exists");
    }

    @Test
    void refreshToken_shouldReturnNewAccessToken() {
        when(jwtService.getUserIdFromToken("refreshToken")).thenReturn(1L);
        when(userService.getUserById(1L)).thenReturn(user);
        when(jwtService.generateAccessToken(user)).thenReturn("newAccessToken");

        String result = authService.refreshToken("refreshToken");

        assertThat(result).isEqualTo("newAccessToken");
    }

    @Test
    void refreshToken_shouldThrowException_whenUserNotFound() {
        when(jwtService.getUserIdFromToken("refreshToken")).thenReturn(99L);
        when(userService.getUserById(99L))
                .thenThrow(new ResourceNotFoundException("User not found with id 99"));

        assertThatThrownBy(() -> authService.refreshToken("refreshToken"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }
}