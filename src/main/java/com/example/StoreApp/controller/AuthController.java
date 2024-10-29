package com.example.StoreApp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.example.StoreApp.dto.ApiResponse;
import com.example.StoreApp.dto.LoginRequest;
import com.example.StoreApp.dto.LoginResponse;
import com.example.StoreApp.dto.UserDTO;
import com.example.StoreApp.model.Usuario;
import com.example.StoreApp.service.AuthService;
import com.example.StoreApp.service.UsuariosService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UsuariosService usuariosService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest, HttpSession session) {
        try {
            // Verificar si el usuario ya está autenticado
            if (authService.isCurrentUserAuthenticated()) {
                Usuario currentUser = authService.getCurrentUser();
                return ResponseEntity.ok(new LoginResponse(false, 
                    "Ya existe una sesión activa para el usuario: " + currentUser.getUsername(), 
                    new UserDTO(currentUser)));
            }

            // Buscar usuario
            Usuario usuario = usuariosService.findByUsername(loginRequest.getUsername());
            if (usuario == null) {
                return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse(false, "Usuario no encontrado"));
            }

            // Autenticar usuario
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );

            // Establecer la autenticación en el contexto
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Crear nueva sesión
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

            return ResponseEntity.ok(new LoginResponse(true, 
                "Inicio de sesión exitoso", 
                new UserDTO(usuario)));

        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new LoginResponse(false, "Credenciales inválidas"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getCurrentUser() {
        if (!authService.isCurrentUserAuthenticated()) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse(false, "No hay sesión activa. Por favor, inicie sesión."));
        }

        Usuario currentUser = authService.getCurrentUser();
        return ResponseEntity.ok(new ApiResponse(true, 
            "Usuario actual recuperado exitosamente", 
            new UserDTO(currentUser)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(HttpSession session) {
        if (!authService.isCurrentUserAuthenticated()) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse(false, "No hay sesión activa para cerrar"));
        }

        // Limpiar el contexto de seguridad
        SecurityContextHolder.clearContext();
        // Invalidar la sesión
        if (session != null) {
            session.invalidate();
        }

        return ResponseEntity.ok(new ApiResponse(true, "Sesión cerrada exitosamente"));
    }

    @GetMapping("/check-session")
    public ResponseEntity<ApiResponse> checkSession() {
        if (authService.isCurrentUserAuthenticated()) {
            Usuario currentUser = authService.getCurrentUser();
            return ResponseEntity.ok(new ApiResponse(true, 
                "Sesión activa", 
                new UserDTO(currentUser)));
        }
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(new ApiResponse(false, "No hay sesión activa"));
    }
}