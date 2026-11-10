package com.mycompany.lab_4_poo.modelo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Evento que junta a los participantes de una carrera. */
public final class Carrera implements Evento {

    private final String codigo;
    private final List<Participante> participantes;
    private Programacion programacion;
    private Resultado resultado;

    public Carrera(String codigo, List<Participante> participantes) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El código de la carrera no puede estar vacío.");
        }

        if (participantes == null || participantes.isEmpty()) {
            throw new IllegalArgumentException(
                    "La carrera debe tener participantes.");
        }

        List<Participante> copia = new ArrayList<>();
        Set<String> codigos = new HashSet<>();

        for (Participante participante : participantes) {
            if (participante == null) {
                throw new IllegalArgumentException(
                        "La carrera no admite participantes nulos.");
            }

            if (!codigos.add(participante.getCodigo())) {
                throw new IllegalArgumentException(
                        "Un participante no puede repetirse en la carrera.");
            }

            copia.add(participante);
        }

        this.codigo = codigo.trim().toUpperCase(Locale.ROOT);
        this.participantes = Collections.unmodifiableList(copia);
        this.programacion = null;
        this.resultado = null;
    }

    @Override
    public String getCodigo() {
        return codigo;
    }

    @Override
    public List<Participante> getParticipantes() {
        return participantes;
    }

    @Override
    public Optional<Programacion> getProgramacion() {
        return Optional.ofNullable(programacion);
    }

    @Override
    public Optional<Resultado> getResultado() {
        return Optional.ofNullable(resultado);
    }

    @Override
    public void programar(Programacion propuesta, Collection<Evento> todos) {
        Calendario.validarProgramacion(this, propuesta, todos);
        this.programacion = propuesta;
    }

    @Override
    public void registrarResultado(Resultado resultado) {
        if (getEstado() == EstadoEvento.FINALIZADO) {
            throw new IllegalStateException(
                    "La carrera ya tiene un resultado registrado.");
        }

        if (getEstado() != EstadoEvento.PROGRAMADO) {
            throw new IllegalStateException(
                    "La carrera debe estar programada para registrar un resultado.");
        }

        if (resultado == null) {
            throw new IllegalArgumentException(
                    "El resultado no puede ser nulo.");
        }

        Map<String, AporteResultado> aportes = resultado.getAportes();

        if (aportes == null || aportes.size() != participantes.size()) {
            throw new IllegalArgumentException(
                    "El resultado debe incluir exactamente a los participantes de la carrera.");
        }

        for (Participante participante : participantes) {
            if (aportes.get(participante.getCodigo()) == null) {
                throw new IllegalArgumentException(
                        "Falta un aporte válido para el participante "
                        + participante.getCodigo() + ".");
            }
        }

        this.resultado = resultado;
    }
}