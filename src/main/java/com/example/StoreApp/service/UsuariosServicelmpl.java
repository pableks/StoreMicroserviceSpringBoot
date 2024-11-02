package com.example.StoreApp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.StoreApp.model.Despacho;
import com.example.StoreApp.model.Role;
import com.example.StoreApp.model.Usuario;
import com.example.StoreApp.repository.DespachoRepository;
import com.example.StoreApp.repository.UsuariosRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.ArrayList;

@Service
public class UsuariosServicelmpl implements UsuariosService {

    private static final Logger log = LoggerFactory.getLogger(UsuariosServicelmpl.class);


    @Autowired
    private UsuariosRepository usuariosRepository;

    @Autowired
    private DespachoRepository despachoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthService authService;

    @Override
    public List<Usuario> getAllUsuarios() {
        return usuariosRepository.findAll();
    }

    @Override
    public Optional<Usuario> getUsuarioById(Long id) {
        return usuariosRepository.findById(id);
    }

    

    @Override
    public Usuario createUsuario(Usuario usuario) {
        // Encriptar la contraseña
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        
        // Asignar rol USER por defecto si no tiene roles
        if (usuario.getRoles() == null || usuario.getRoles().isEmpty()) {
            Set<Role> roles = new HashSet<>();
            roles.add(Role.USER);
            usuario.setRoles(roles);
        }
        
        return usuariosRepository.save(usuario);
    }

    @Override
    public Usuario updateUsuario(Long id, Usuario usuario) {
        return usuariosRepository.findById(id)
            .map(existingUser -> {
                existingUser.setUsername(usuario.getUsername());
                if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
                    existingUser.setPassword(passwordEncoder.encode(usuario.getPassword()));
                }
                if (usuario.getRoles() != null && !usuario.getRoles().isEmpty()) {
                    existingUser.setRoles(usuario.getRoles());
                }
                return usuariosRepository.save(existingUser);
            })
            .orElse(null);
    }

    @Override
    public void deleteUsuario(Long id) {
        usuariosRepository.deleteById(id);
    }

    @Override
    public Usuario findByUsername(String username) {
        return usuariosRepository.findByUsername(username);
    }

    @Override
    public void removeRoleFromUser(Long userId, Role role) {
        usuariosRepository.findById(userId).ifPresent(usuario -> {
            usuario.getRoles().remove(role);
            // Asegurar que siempre tenga al menos el rol USER
            if (usuario.getRoles().isEmpty()) {
                usuario.getRoles().add(Role.USER);
            }
            usuariosRepository.save(usuario);
        });
    }

    @Override
    public Usuario addRoleToUser(Long userId, Role role) {
        return usuariosRepository.findById(userId)
            .map(usuario -> {
                usuario.getRoles().add(role);
                return usuariosRepository.save(usuario);
            })
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    @Override
    public Usuario addDespachoToUser(Long userId, Despacho despacho) {
        log.info("Servicio: Agregando despacho para usuario ID: {}", userId);
        
        Usuario usuario = usuariosRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        log.info("Usuario encontrado: {}", usuario.getUsername());
        
        // Validar la dirección
        if (despacho.getDireccion() == null || despacho.getDireccion().trim().isEmpty()) {
            throw new IllegalArgumentException("La dirección es requerida");
        }

        // Inicializar la lista de despachos si es null
        if (usuario.getDespachos() == null) {
            usuario.setDespachos(new ArrayList<>());
        }
        
        // Guardar el despacho
        despacho.setUsuario(usuario);
        Despacho savedDespacho = despachoRepository.save(despacho);
        log.info("Despacho guardado con ID: {}", savedDespacho.getId());
        
        // Actualizar la lista de despachos del usuario
        usuario.getDespachos().add(savedDespacho);
        
        // Guardar y retornar el usuario actualizado
        return usuariosRepository.save(usuario);
    }



    @Override
    public void removeDespachoFromUser(Long userId, Long despachoId) {
        usuariosRepository.findById(userId).ifPresent(usuario -> {
            usuario.getDespachos().removeIf(despacho -> despacho.getId().equals(despachoId));
            usuariosRepository.save(usuario);
        });
    }

    // Método helper para Spring Security
    @Override
    public boolean isCurrentUser(Long userId) {
        return authService.isCurrentUser(userId);
    }
}