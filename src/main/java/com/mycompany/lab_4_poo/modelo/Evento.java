package com.mycompany.lab_4_poo.modelo;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Comportamiento común de los encuentros, las carreras y futuros eventos. */
public interface Evento extends Comparable<Evento> {

    String getCodigo();

    /** Devuelve los competidores en una lista no modificable. */
    List<Participante> getParticipantes();

    Optional<Programacion> getProgramacion();

    Optional<Resultado> getResultado();

    /** Obtiene el estado sin almacenar un atributo adicional. */
    default EstadoEvento getEstado() {
        if (getResultado().isPresent()) {
            return EstadoEvento.FINALIZADO;
        }
        if (getProgramacion().isPresent()) {
            return EstadoEvento.PROGRAMADO;
        }
        return EstadoEvento.SIN_PROGRAMAR;
    }

    /** Valida estado y conflictos antes de asignar o sustituir la programación. */
    void programar(Programacion propuesta, Collection<Evento> todos);

    /** Admite una única carga válida para exactamente sus participantes. */
    void registrarResultado(Resultado resultado);

    /** Usa el mismo criterio de orden que todos los calendarios. */
    @Override
    default int compareTo(Evento otro) {
        return Calendario.comparar(this, otro);
    }
}
