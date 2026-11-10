package com.mycompany.lab_4_poo.modelo;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/** Evento entre dos participantes distintos. */
public final class Encuentro implements Evento {

    private final String codigo;
    private final Participante local;
    private final Participante visitante;
    private Programacion programacion;
    private Resultado resultado;

    public Encuentro(String codigo, Participante local, Participante visitante) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El código del encuentro no puede estar vacío.");
        }

        if (local == null || visitante == null) {
            throw new IllegalArgumentException(
                    "El encuentro requiere dos participantes.");
        }

        if (local.getCodigo().equals(visitante.getCodigo())) {
            throw new IllegalArgumentException(
                    "Un participante no puede enfrentarse a sí mismo.");
        }

        this.codigo = codigo.trim().toUpperCase(Locale.ROOT);
        this.local = local;
        this.visitante = visitante;
        this.programacion = null;
        this.resultado = null;
    }

    @Override
    public String getCodigo() {
        return codigo;
    }

    @Override
    public List<Participante> getParticipantes() {
        return Collections.unmodifiableList(Arrays.asList(local, visitante));
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
                    "El encuentro ya tiene un resultado registrado.");
        }

        if (getEstado() != EstadoEvento.PROGRAMADO) {
            throw new IllegalStateException(
                    "El encuentro debe estar programado para registrar un resultado.");
        }

        if (resultado == null) {
            throw new IllegalArgumentException(
                    "El resultado no puede ser nulo.");
        }

        Map<String, AporteResultado> aportes = resultado.getAportes();

        if (aportes == null || aportes.size() != 2
                || aportes.get(local.getCodigo()) == null
                || aportes.get(visitante.getCodigo()) == null) {
            throw new IllegalArgumentException(
                    "El resultado debe incluir un aporte válido para cada participante del encuentro.");
        }

        this.resultado = resultado;
    }
}