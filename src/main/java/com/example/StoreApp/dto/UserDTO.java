package com.example.StoreApp.dto;

import java.util.Set;

import com.example.StoreApp.model.Role;
import com.example.StoreApp.model.Usuario;

public class UserDTO {
    private Long id;
    private String username;
    private Set<Role> roles;

    // Constructores
    public UserDTO() {
    }

    public UserDTO(Usuario usuario) {
        this.id = usuario.getId();
        this.username = usuario.getUsername();
        this.roles = usuario.getRoles();
    }

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
}