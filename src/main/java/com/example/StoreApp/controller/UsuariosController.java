package com.example.StoreApp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import com.example.StoreApp.dto.ApiResponse;
import com.example.StoreApp.dto.LoginRequest;
import com.example.StoreApp.dto.LoginResponse;
import com.example.StoreApp.dto.RegisterRequest;
import com.example.StoreApp.dto.UserDTO;
import com.example.StoreApp.model.Despacho;
import com.example.StoreApp.model.Role;
import com.example.StoreApp.model.Usuario;
import com.example.StoreApp.service.AuthService;
import com.example.StoreApp.service.UsuariosService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.Valid;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.FieldError;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/usuarios")
public class UsuariosController {

    private static final Logger log = LoggerFactory.getLogger(UsuariosController.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private UsuariosService usuariosService;

  


    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public CollectionModel<EntityModel<Usuario>> getAllUsuarios() {
        List<Usuario> usuarios = usuariosService.getAllUsuarios();
        log.info("GET /usuarios");
        List<EntityModel<Usuario>> usuariosResources = usuarios.stream()
                .map(usuario -> EntityModel.of(usuario,
                        WebMvcLinkBuilder
                                .linkTo(WebMvcLinkBuilder.methodOn(this.getClass()).getUsuarioById(usuario.getId()))
                                .withSelfRel()))
                .collect(Collectors.toList());

        return CollectionModel.of(usuariosResources,
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(this.getClass()).getAllUsuarios())
                        .withRel("usuarios"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @usuariosService.isCurrentUser(#id)")
    public EntityModel<Usuario> getUsuarioById(@Validated @PathVariable Long id) {
        Optional<Usuario> usuario = usuariosService.getUsuarioById(id);

        if (usuario.isPresent()) {
            Usuario user = usuario.get();
            user.setPassword(null); // No devolver la contraseña
            return EntityModel.of(user,
                    WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(this.getClass()).getUsuarioById(id))
                            .withSelfRel(),
                    WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(this.getClass()).getAllUsuarios())
                            .withRel("all-usuarios"));
        } else {
            throw new usuariosNotFoundException("No se encontró el usuario con ID " + id);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            // Verificar si hay una sesión activa
            if (authService.isCurrentUserAuthenticated()) {
                Usuario currentUser = authService.getCurrentUser();
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, 
                        "No se puede registrar un nuevo usuario mientras haya una sesión activa. " +
                        "Usuario actual: " + currentUser.getUsername()));
            }

            // Verificar si el usuario ya existe
            if (usuariosService.findByUsername(registerRequest.getUsername()) != null) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "El nombre de usuario ya está en uso"));
            }

            // Crear nuevo usuario
            Usuario newUser = new Usuario();
            newUser.setUsername(registerRequest.getUsername());
            newUser.setPassword(registerRequest.getPassword());

            Usuario createdUser = usuariosService.createUsuario(newUser);
            // No devolver la contraseña en la respuesta
            createdUser.setPassword(null);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "Usuario registrado exitosamente", createdUser));
        } catch (Exception e) {
            log.error("Error registrando nuevo usuario: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error al registrar el usuario"));
        }
    }

    @PutMapping("/{id}")
    public EntityModel<Usuario> updateUsuario(@PathVariable Long id, @RequestBody Usuario usuario) {
        Usuario usuarioActualizado = usuariosService.updateUsuario(id, usuario);
        return EntityModel.of(usuarioActualizado,
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(this.getClass()).getUsuarioById(id)).withSelfRel(),
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(this.getClass()).getAllUsuarios())
                        .withRel("all-usuarios"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUsuario(@Validated @PathVariable Long id) {
        try {
            usuariosService.deleteUsuario(id);
            return ResponseEntity.ok(new ErrorResponse(true, "Usuario eliminado correctamente."));
        } catch (Exception e) {
            log.error("Error al eliminar el usuario con ID {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(false, "Ocurrió un error al eliminar el usuario."));
        }
    }

  

    @GetMapping("/{userId}/despachos")
    public ResponseEntity<?> getDespachosForUser(@PathVariable Long userId) {
        try {
            Usuario usuario = usuariosService.getUsuarioById(userId)
                    .orElseThrow(() -> new usuariosNotFoundException("User not found with ID " + userId));

            List<Despacho> despachos = usuario.getDespachos();
            if (despachos.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(despachos);
        } catch (usuariosNotFoundException e) {
            log.error("Error fetching despachos for user with ID {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(false, e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching despachos for user with ID {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(false, "Failed to retrieve despachos."));
        }
    }


    @PostMapping("/me/despachos")
    public ResponseEntity<?> addDespachoToCurrentUser(@Valid @RequestBody Despacho despacho) {
        try {
            Usuario currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Usuario no autenticado"));
            }
            
            // Crear y configurar el nuevo despacho
            Despacho nuevoDespacho = new Despacho();
            nuevoDespacho.setDireccion(despacho.getDireccion());
            nuevoDespacho.setActivo(despacho.isActivo());
            
            // Agregar el despacho al usuario actual
            Usuario updatedUser = usuariosService.addDespachoToUser(currentUser.getId(), nuevoDespacho);
            
            // Limpiar datos sensibles antes de devolver
            updatedUser.setPassword(null);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "Despacho agregado exitosamente", updatedUser));
                
        } catch (RuntimeException e) {
            log.error("Error al agregar despacho: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            log.error("Error inesperado al agregar despacho: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(false, "Error interno al agregar despacho"));
        }
    }

    @GetMapping("/me/despachos")
    public ResponseEntity<?> getCurrentUserDespachos() {
        try {
            Usuario currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Usuario no autenticado"));
            }

            List<Despacho> despachos = currentUser.getDespachos();
            if (despachos == null || despachos.isEmpty()) {
                return ResponseEntity.ok(new ApiResponse(true, "No hay despachos registrados", new ArrayList<>()));
            }

            return ResponseEntity.ok(new ApiResponse(true, "Despachos recuperados exitosamente", despachos));
        } catch (Exception e) {
            log.error("Error al obtener despachos del usuario actual: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(false, "Error al obtener los despachos"));
        }
    }

    @PostMapping("/{userId}/despachos")
    @PreAuthorize("hasRole('ADMIN') or @usuariosService.isCurrentUser(#userId)")
    public ResponseEntity<?> addDespachoToUser(@PathVariable Long userId, @Valid @RequestBody Despacho despacho) {
        log.info("Intentando agregar despacho para usuario ID: {}", userId);
        
        try {
            // Verificar autenticación actual
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            log.info("Usuario autenticado: {}, Roles: {}", 
                auth.getName(), 
                auth.getAuthorities().stream().map(Object::toString).collect(Collectors.joining(", ")));
            
            // Validar el usuario actual
            Usuario currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                log.error("No se pudo obtener el usuario actual");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Usuario no autenticado"));
            }
            
            log.info("Usuario actual: {}, ID: {}", currentUser.getUsername(), currentUser.getId());
            
            // Crear nuevo despacho
            Despacho nuevoDespacho = new Despacho();
            nuevoDespacho.setDireccion(despacho.getDireccion());
            nuevoDespacho.setActivo(true);
            
            Usuario updatedUser = usuariosService.addDespachoToUser(userId, nuevoDespacho);
            
            // Limpiar la contraseña antes de devolver la respuesta
            updatedUser.setPassword(null);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "Despacho agregado exitosamente", updatedUser));
        } catch (RuntimeException e) {
            log.error("Error al agregar despacho para usuario con ID {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            log.error("Error al agregar despacho para usuario con ID {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(false, "Error al agregar despacho"));
        }
    }

    @GetMapping("/{userId}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getRolesForUser(@PathVariable Long userId) {
        try {
            Usuario usuario = usuariosService.getUsuarioById(userId)
                    .orElseThrow(() -> new usuariosNotFoundException("Usuario no encontrado con ID " + userId));

            Set<Role> roles = usuario.getRoles();
            if (roles.isEmpty()) {
                return ResponseEntity.ok(new ApiResponse(true, "El usuario no tiene roles asignados", new HashSet<>()));
            }

            return ResponseEntity.ok(new ApiResponse(true, "Roles obtenidos exitosamente", roles));

        } catch (usuariosNotFoundException e) {
            log.error("Error obteniendo roles para el usuario con ID {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Usuario no encontrado: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error obteniendo roles para el usuario con ID {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error al obtener los roles"));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{userId}/roles")
    public ResponseEntity<?> addRoleToUser(@PathVariable Long userId, @RequestBody Role role) {
        try {
            Usuario updatedUser = usuariosService.addRoleToUser(userId, role);
            updatedUser.setPassword(null); // No devolver la contraseña
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            log.error("Failed to add role to user with ID {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(false, "Failed to add role"));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{userId}/roles/{role}")
    public ResponseEntity<?> removeRoleFromUser(@PathVariable Long userId, @PathVariable Role role) {
        try {
            usuariosService.removeRoleFromUser(userId, role);
            return ResponseEntity.ok(new ErrorResponse(true, "Role removed successfully."));
        } catch (Exception e) {
            log.error("Error removing role from user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(false, "Failed to remove role"));
        }
    }

    @DeleteMapping("/{userId}/despachos/{despachoId}")
    public ResponseEntity<?> removeDespachoFromUser(@PathVariable Long userId, @PathVariable Long despachoId) {
        try {
            usuariosService.removeDespachoFromUser(userId, despachoId);
            return ResponseEntity.ok(new ErrorResponse(true, "Despacho removed successfully."));
        } catch (Exception e) {
            log.error("Error removing despacho from user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(false, "Failed to remove despacho."));
        }
    }

    static class ErrorResponse {
        private final boolean respuesta;
        private final String message;

        public ErrorResponse(boolean respuesta, String message) {
            this.respuesta = respuesta;
            this.message = message;
        }

        public boolean isRespuesta() {
            return respuesta;
        }

        public String getMessage() {
            return message;
        }
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        ErrorResponse errorResponse = new ErrorResponse(false, "Sin datos ingresados.");
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgumentException(IllegalArgumentException ex) {
        return new ErrorResponse(false, "El ID no puede ser nulo o texto.");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }

    static class usuariosNotFoundException extends RuntimeException {
        public usuariosNotFoundException(String message) {
            super(message);
        }
    }
}
