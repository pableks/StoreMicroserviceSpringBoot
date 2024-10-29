package com.example.StoreApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.StoreApp.model.Despacho;

public interface DespachoRepository extends JpaRepository<Despacho, Long> {
}