package com.example.StoreApp.service;

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

@Service
public class UsuariosServicelmpl implements UsuariosService {

    @Autowired
    private UsuariosRepository usuariosRepository;

    @Autowired
    private DespachoRepository despachoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
        Usuario usuario = usuariosRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        despacho.setUsuario(usuario);
        usuario.getDespachos().add(despacho);
        despachoRepository.save(despacho);
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
        // Implementar la lógica para verificar si el usuario actual es el mismo que userId
        // Esto dependerá de cómo manejes la autenticación
        return true; // Por ahora retorna true, deberás implementar la lógica real
    }
}