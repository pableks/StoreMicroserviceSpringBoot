package com.example.StoreApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.StoreApp.model.Producto;
import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByCategoria(String categoria);
    Producto findBySku(String sku);
}