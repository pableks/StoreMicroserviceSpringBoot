package com.example.StoreApp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.example.StoreApp.model.Producto;
import com.example.StoreApp.service.ProductoService;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    // Public endpoints
    @GetMapping
    public ResponseEntity<List<Producto>> getAllProductos() {
        List<Producto> productos = productoService.getAllProductos();
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> getProductoById(@PathVariable Long id) {
        return productoService.getProductoById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<Producto>> getProductosByCategoria(@PathVariable String categoria) {
        List<Producto> productos = productoService.getProductosByCategoria(categoria);
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<Producto> getProductoBySku(@PathVariable String sku) {
        Producto producto = productoService.getProductoBySku(sku);
        return producto != null ? ResponseEntity.ok(producto) : ResponseEntity.notFound().build();
    }

    // Admin-only endpoints
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Producto> createProducto(@Valid @RequestBody Producto producto) {
        Producto createdProducto = productoService.createProducto(producto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProducto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Producto> updateProducto(@PathVariable Long id, @Valid @RequestBody Producto producto) {
        Producto updatedProducto = productoService.updateProducto(id, producto);
        return updatedProducto != null ? 
            ResponseEntity.ok(updatedProducto) : 
            ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProducto(@PathVariable Long id) {
        productoService.deleteProducto(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Error: " + e.getMessage());
    }
}