package com.example.StoreApp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonBackReference;
import org.springframework.hateoas.RepresentationModel;

@Entity
@Table(name = "despachos")
public class Despacho extends RepresentationModel<Despacho> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Cambiamos a IDENTITY
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "La dirección no puede estar vacía")
    @Column(name = "direccion", nullable = false, length = 255)
    private String direccion;

    @Column(name = "activo")
    private boolean activo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    @JsonBackReference
    private Usuario usuario;

    // Constructor por defecto
    public Despacho() {
        this.activo = true;
    }

    // Getters and Setters remain the same
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}