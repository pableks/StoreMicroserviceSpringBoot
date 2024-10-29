package com.example.StoreApp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.StoreApp.model.Producto;
import com.example.StoreApp.repository.ProductoRepository;
import java.util.List;
import java.util.Optional;

@Service
public class ProductoServiceImpl implements ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Override
    public List<Producto> getAllProductos() {
        return productoRepository.findAll();
    }

    @Override
    public Optional<Producto> getProductoById(Long id) {
        return productoRepository.findById(id);
    }

    @Override
    public Producto createProducto(Producto producto) {
        return productoRepository.save(producto);
    }

    @Override
    public Producto updateProducto(Long id, Producto producto) {
        return productoRepository.findById(id)
            .map(existingProducto -> {
                existingProducto.setNombre(producto.getNombre());
                existingProducto.setPrecio(producto.getPrecio());
                existingProducto.setStock(producto.getStock());
                existingProducto.setDescripcion(producto.getDescripcion());
                existingProducto.setCategoria(producto.getCategoria());
                existingProducto.setSku(producto.getSku());
                return productoRepository.save(existingProducto);
            })
            .orElse(null);
    }

    @Override
    public void deleteProducto(Long id) {
        productoRepository.deleteById(id);
    }

    @Override
    public List<Producto> getProductosByCategoria(String categoria) {
        return productoRepository.findByCategoria(categoria);
    }

    @Override
    public Producto getProductoBySku(String sku) {
        return productoRepository.findBySku(sku);
    }
}
