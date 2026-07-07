package com.donaton.auth;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.donaton.auth.dto.RoleUpdateRequestDTO;
import com.donaton.auth.dto.TokenResponseDTO;
import com.donaton.auth.dto.UserSummaryDTO;
import com.donaton.auth.model.Role;
import com.donaton.auth.model.User;
import com.donaton.auth.repository.UserRepositoryPattern;
import com.donaton.auth.security.IJwtService;
import com.donaton.auth.service.UserService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepositoryPattern repository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    @Test
    public void testRegistrarPublicExitoso() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setName("Test User");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail("test@example.com");
        savedUser.setPassword("password123");
        savedUser.setName("Test User");
        savedUser.setRole(Role.USER);

        when(repository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(repository.save(any(User.class))).thenReturn(savedUser);

        UserSummaryDTO resultado = userService.registrarPublic(user);

        assertNotNull(resultado);
        assertEquals(1L, resultado.id());
        assertEquals("test@example.com", resultado.email());
        verify(repository, times(1)).save(any(User.class));
    }

    @Test
    public void testRegistrarPublicEmailVacioDebeFallar() {
        User user = new User();
        user.setEmail("");
        user.setPassword("password123");

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.registrarPublic(user);
        });

        assertEquals("Credenciales inválidas", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    public void testRegistrarPublicPasswordVacioDebeFallar() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("");

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.registrarPublic(user);
        });

        assertEquals("Credenciales inválidas", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    public void testRegistrarPublicEmailYaRegistradoDebeFallar() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password123");

        when(repository.findByEmail("test@example.com")).thenReturn(Optional.of(new User()));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.registrarPublic(user);
        });

        assertEquals("El correo ya está registrado", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    public void testListarUsuariosComoAdmin() {
        User user1 = new User();
        user1.setId(1L);
        user1.setEmail("user1@example.com");

        User user2 = new User();
        user2.setId(2L);
        user2.setEmail("user2@example.com");

        when(repository.findAll()).thenReturn(List.of(user1, user2));

        List<UserSummaryDTO> resultado = userService.listarUsuarios("ADMIN");

        assertEquals(2, resultado.size());
        verify(repository, times(1)).findAll();
    }

    @Test
    public void testListarUsuariosComoNoAdminDebeFallar() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.listarUsuarios("USER");
        });

        assertEquals("Solo un administrador puede realizar esta acción", exception.getMessage());
        verify(repository, never()).findAll();
    }

    @Test
    public void testCrearUsuarioComoAdmin() {
        User user = new User();
        user.setEmail("new@example.com");
        user.setPassword("password123");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail("new@example.com");

        when(repository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(repository.save(any(User.class))).thenReturn(savedUser);

        UserSummaryDTO resultado = userService.crearUsuario("ADMIN", user);

        assertNotNull(resultado);
        verify(repository, times(1)).save(any(User.class));
    }

    @Test
    public void testCambiarRolExitoso() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setRole(Role.USER);

        RoleUpdateRequestDTO request = new RoleUpdateRequestDTO("ADMIN");

        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(repository.save(any(User.class))).thenReturn(user);

        UserSummaryDTO resultado = userService.cambiarRol(1L, request, "ADMIN");

        assertNotNull(resultado);
        verify(repository, times(1)).save(any(User.class));
    }

    @Test
    public void testCambiarRolRolInvalidoDebeFallar() {
        RoleUpdateRequestDTO request = new RoleUpdateRequestDTO("");

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.cambiarRol(1L, request, "ADMIN");
        });

        assertEquals("Rol inválido", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    public void testEliminarUsuarioComoAdmin() {
        User user = new User();
        user.setId(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(user));
        doNothing().when(repository).delete(any());

        userService.eliminarUsuario(1L, "ADMIN");

        verify(repository, times(1)).delete(user);
    }

    @Test
    public void testLoginExitoso() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setRole(Role.USER);

        when(repository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken("test@example.com", "USER")).thenReturn("access-token");
        when(jwtService.generateRefreshToken("test@example.com", "USER")).thenReturn("refresh-token");

        TokenResponseDTO resultado = userService.login("test@example.com", "password123");

        assertNotNull(resultado);
        assertEquals("access-token", resultado.getAccessToken());
        assertEquals("refresh-token", resultado.getRefreshToken());
    }

    @Test
    public void testLoginPasswordIncorrectoDebeFallar() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("correct-password");

        when(repository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.login("test@example.com", "wrong-password");
        });

        assertEquals("Credenciales inválidas", exception.getMessage());
    }

    @Test
    public void testRefreshTokenExitoso() {
        when(jwtService.isRefreshToken("valid-refresh-token")).thenReturn(true);
        when(jwtService.extractEmail("valid-refresh-token")).thenReturn("test@example.com");
        when(jwtService.extractRole("valid-refresh-token")).thenReturn("USER");
        when(jwtService.generateAccessToken("test@example.com", "USER")).thenReturn("new-access-token");

        TokenResponseDTO resultado = userService.refresh("valid-refresh-token");

        assertNotNull(resultado);
        assertEquals("new-access-token", resultado.getAccessToken());
        assertEquals("valid-refresh-token", resultado.getRefreshToken());
    }

    @Test
    public void testRefreshTokenInvalidoDebeFallar() {
        when(jwtService.isRefreshToken("invalid-token")).thenReturn(false);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.refresh("invalid-token");
        });

        assertEquals("Token de refresco inválido", exception.getMessage());
    }
}
