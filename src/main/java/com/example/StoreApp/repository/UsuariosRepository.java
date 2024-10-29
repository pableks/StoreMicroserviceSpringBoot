package com.example.StoreApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.StoreApp.model.Usuario;

public interface UsuariosRepository extends JpaRepository<Usuario, Long>{
    Usuario findByUsername(String username);
}
