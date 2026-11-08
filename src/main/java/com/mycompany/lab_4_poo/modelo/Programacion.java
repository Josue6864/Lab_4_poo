package com.mycompany.lab_4_poo.modelo;

import java.time.LocalDate;

/** Reúne la fecha, hora de inicio y sede de un evento. */
public final class Programacion {

    private final LocalDate fecha;
    private final int hora;
    private final String sede;

    public Programacion(LocalDate fecha, int hora, String sede) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha del evento no puede ser nula.");
        }
        if (hora < 0 || hora > 23) {
            throw new IllegalArgumentException("La hora debe estar entre 0 y 23.");
        }
        if (sede == null || sede.trim().isEmpty()) {
            throw new IllegalArgumentException("La sede no puede estar vacía.");
        }

        this.fecha = fecha;
        this.hora = hora;
        this.sede = sede.trim().replaceAll("\\s+", " ");
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public int getHora() {
        return hora;
    }

    public String getSede() {
        return sede;
    }
}