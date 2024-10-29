package com.example.StoreApp.service;

import com.example.StoreApp.model.Producto;
import java.util.List;
import java.util.Optional;

public interface ProductoService {
    List<Producto> getAllProductos();
    Optional<Producto> getProductoById(Long id);
    Producto createProducto(Producto producto);
    Producto updateProducto(Long id, Producto producto);
    void deleteProducto(Long id);
    List<Producto> getProductosByCategoria(String categoria);
    Producto getProductoBySku(String sku);
}