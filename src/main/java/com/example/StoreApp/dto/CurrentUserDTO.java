package com.example.StoreApp.dto;

import java.util.Set;

import com.example.StoreApp.model.Role;
import com.example.StoreApp.model.Usuario;

public class CurrentUserDTO {
    private final Long id;
    private final String username;
    private final Set<Role> roles;
    private final boolean isAuthenticated;

    public CurrentUserDTO(Usuario usuario) {
        this.id = usuario.getId();
        this.username = usuario.getUsername();
        this.roles = usuario.getRoles();
        this.isAuthenticated = true;
    }

    // Constructor para usuario no autenticado
    public CurrentUserDTO() {
        this.id = null;
        this.username = null;
        this.roles = null;
        this.isAuthenticated = false;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }
}