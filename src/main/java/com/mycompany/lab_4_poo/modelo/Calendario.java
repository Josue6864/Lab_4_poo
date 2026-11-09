package com.mycompany.lab_4_poo.modelo;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Vista de eventos ordenada al construirse.
 * Después de reprogramar se solicita un calendario nuevo.
 */
public final class Calendario implements Iterable<Evento> {

    private final List<Evento> eventos;

    private static final Comparator<Evento> ORDEN = (primero, segundo) -> {
        Programacion p1 = primero.getProgramacion().orElseThrow(
                () -> new IllegalStateException(
                        "No se puede ordenar un evento sin programar."));

        Programacion p2 = segundo.getProgramacion().orElseThrow(
                () -> new IllegalStateException(
                        "No se puede ordenar un evento sin programar."));

        int comparacion = p1.getFecha().compareTo(p2.getFecha());
        if (comparacion != 0) {
            return comparacion;
        }

        comparacion = Integer.compare(p1.getHora(), p2.getHora());
        if (comparacion != 0) {
            return comparacion;
        }

        Collator ordenAlfabetico = Collator.getInstance(
                Locale.forLanguageTag("es-GT"));

        ordenAlfabetico.setStrength(Collator.SECONDARY);

        return ordenAlfabetico.compare(p1.getSede(), p2.getSede());
    };

    public Calendario(Collection<Evento> origen) {
        if (origen == null) {
            throw new IllegalArgumentException(
                    "La colección de eventos no puede ser nula.");
        }

        List<Evento> copia = new ArrayList<>();

        for (Evento evento : origen) {
            if (evento == null) {
                throw new IllegalArgumentException(
                        "El calendario no admite eventos nulos.");
            }

            if (evento.getProgramacion().isPresent()) {
                copia.add(evento);
            }
        }

        copia.sort(ORDEN);
        this.eventos = Collections.unmodifiableList(copia);
    }

    @Override
    public Iterator<Evento> iterator() {
        return eventos.iterator();
    }

    public static int comparar(Evento a, Evento b) {
        Objects.requireNonNull(a, "El primer evento no puede ser nulo.");
        Objects.requireNonNull(b, "El segundo evento no puede ser nulo.");

        return ORDEN.compare(a, b);
    }

    /** Comprueba los conflictos sin modificar la programación de ningún evento. */
    public static void validarProgramacion(Evento objetivo,
            Programacion propuesta, Collection<Evento> todos) {

        if (objetivo == null || propuesta == null || todos == null) {
            throw new IllegalArgumentException(
                    "Se requieren el evento, la propuesta y la agenda global.");
        }

        if (objetivo.getEstado() == EstadoEvento.FINALIZADO) {
            throw new IllegalStateException(
                    "Un evento finalizado no puede reprogramarse.");
        }

        Set<String> codigosObjetivo = new HashSet<>();

        for (Participante participante : objetivo.getParticipantes()) {
            codigosObjetivo.add(participante.getCodigo());
        }

        for (Evento otro : todos) {
            if (otro == null) {
                throw new IllegalArgumentException(
                        "La agenda global no admite eventos nulos.");
            }

            if (objetivo.getCodigo().equals(otro.getCodigo())) {
                continue;
            }

            Optional<Programacion> programacion = otro.getProgramacion();

            if (!programacion.isPresent()) {
                continue;
            }

            Programacion existente = programacion.get();

            boolean mismaFecha = propuesta.getFecha().equals(
                    existente.getFecha());

            boolean mismaHora = propuesta.getHora() == existente.getHora();

            if (!mismaFecha || !mismaHora) {
                continue;
            }

            if (propuesta.getSede().equalsIgnoreCase(existente.getSede())) {
                throw new IllegalArgumentException(
                        "La sede ya tiene otro evento en esa fecha y hora.");
            }

            for (Participante participante : otro.getParticipantes()) {
                if (codigosObjetivo.contains(participante.getCodigo())) {
                    throw new IllegalArgumentException(
                            "El participante " + participante.getCodigo()
                            + " ya tiene otro evento en esa fecha y hora.");
                }
            }
        }
    }
}