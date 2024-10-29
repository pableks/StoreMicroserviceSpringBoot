package com.example.StoreApp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.StoreApp.model.Usuario;
import com.example.StoreApp.repository.UsuariosRepository;

@Service
public class AuthService {
    
    @Autowired
    private UsuariosRepository usuariosRepository;

    public Usuario getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (!isCurrentUserAuthenticated()) {
                return null;
            }
            return usuariosRepository.findByUsername(authentication.getName());
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isCurrentUserAuthenticated() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            return authentication != null && 
                   authentication.isAuthenticated() && 
                   !authentication.getPrincipal().equals("anonymousUser") &&
                   authentication.getName() != null &&
                   usuariosRepository.findByUsername(authentication.getName()) != null;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isCurrentUser(Long userId) {
        Usuario currentUser = getCurrentUser();
        return currentUser != null && currentUser.getId().equals(userId);
    }
}