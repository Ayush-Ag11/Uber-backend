package com.demo.project.uber.advices;

import com.demo.project.uber.exceptions.BadCredentialsException;
import com.demo.project.uber.exceptions.InsufficientBalanceException;
import com.demo.project.uber.exceptions.ResourceNotFoundException;
import com.demo.project.uber.exceptions.RuntimeConflictExceptions;
import com.demo.project.uber.exceptions.ServiceCommunicationException;
import com.demo.project.uber.exceptions.UnauthorizedAccessException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;


    @Test
    void handleResourceNotFoundException_shouldReturn404() {
        ResourceNotFoundException ex =
                new ResourceNotFoundException("Resource not found");

        ResponseEntity<ApiResponse<?>> response =
                globalExceptionHandler.handleResourceNotFoundException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError().getMessage())
                .isEqualTo("Resource not found");
    }


    @Test
    void handleRuntimeConflictError_shouldReturn409() {
        RuntimeConflictExceptions ex =
                new RuntimeConflictExceptions("Conflict occurred");

        ResponseEntity<ApiResponse<?>> response =
                globalExceptionHandler.handleRuntimeConflictError(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getError().getMessage())
                .isEqualTo("Conflict occurred");
    }


    @Test
    void handleBadCredentialsException_shouldReturn401() {
        BadCredentialsException ex =
                new BadCredentialsException("Bad credentials");

        ResponseEntity<ApiResponse<?>> response =
                globalExceptionHandler.handleBadCredentialsException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().getError().getMessage())
                .isEqualTo("Bad credentials");
    }


    @Test
    void handleAuthenticationException_shouldReturn401() {
        AuthenticationException ex =
                new AuthenticationServiceException("Authentication failed");

        ResponseEntity<ApiResponse<?>> response =
                globalExceptionHandler.handleAuthenticationException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().getError().getMessage())
                .isEqualTo("Authentication failed");
    }


    @Test
    void handleJwtException_shouldReturn401() {
        JwtException ex = new JwtException("Invalid JWT token");

        ResponseEntity<ApiResponse<?>> response =
                globalExceptionHandler.handleJwtException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().getError().getMessage())
                .isEqualTo("Invalid JWT token");
    }


    @Test
    void handleAccessDeniedException_shouldReturn403() {
        AccessDeniedException ex =
                new AccessDeniedException("Access denied");

        ResponseEntity<ApiResponse<?>> response =
                globalExceptionHandler.handleAccessDeniedException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().getError().getMessage())
                .isEqualTo("Access denied");
    }


    @Test
    void handleInsufficientBalanceException_shouldReturn400() {
        InsufficientBalanceException ex =
                new InsufficientBalanceException("Insufficient balance");

        ResponseEntity<ApiResponse<?>> response =
                globalExceptionHandler.handleInsufficientBalanceException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getError().getMessage())
                .isEqualTo("Insufficient balance");
    }


    @Test
    void handleUnauthorizedAccessException_shouldReturn403() {
        UnauthorizedAccessException ex =
                new UnauthorizedAccessException("Unauthorized access");

        ResponseEntity<ApiResponse<?>> response =
                globalExceptionHandler.handleUnauthorizedAccessException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().getError().getMessage())
                .isEqualTo("Unauthorized access");
    }

    @Test
    void handleServiceCommunicationException_shouldReturn503() {
        ServiceCommunicationException ex =
                new ServiceCommunicationException("Service unavailable");

        ResponseEntity<ApiResponse<?>> response =
                globalExceptionHandler.handleServiceCommunicationException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody().getError().getMessage())
                .isEqualTo("Service unavailable");
    }

    @Test
    void handleInternalServerError_shouldReturn500() {
        Exception ex = new Exception("Unexpected error");

        ResponseEntity<ApiResponse<?>> response =
                globalExceptionHandler.handleInternalServerError(ex);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getError().getMessage())
                .isEqualTo("Unexpected error");
    }
}