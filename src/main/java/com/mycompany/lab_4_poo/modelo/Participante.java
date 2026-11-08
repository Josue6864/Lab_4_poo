package com.mycompany.lab_4_poo.modelo;

import java.util.Locale;

/** Representa un equipo o cuarteto que puede inscribirse en varios torneos. */
public final class Participante {

    private final String codigo;
    private final String nombre;
    private final String empresa;

    public Participante(String codigo, String nombre, String empresa) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("El código del participante no puede estar vacío.");
        }
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del participante no puede estar vacío.");
        }
        if (empresa == null || empresa.trim().isEmpty()) {
            throw new IllegalArgumentException("La empresa no puede estar vacía.");
        }

        this.codigo = codigo.trim().toUpperCase(Locale.ROOT);
        this.nombre = nombre.trim().replaceAll("\\s+", " ");
        this.empresa = empresa.trim().replaceAll("\\s+", " ");
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmpresa() {
        return empresa;
    }
}