package com.mycompany.lab_4_poo.modelo;

import java.text.Collator;
import java.util.Locale;
import java.util.Objects;

/** Conserva los acumulados de un participante dentro de un solo torneo. */
public final class RegistroTabla implements Comparable<RegistroTabla> {

    private final Participante participante;
    private final int puntos;
    private final int diferencia;
    private final int victorias;

    public RegistroTabla(Participante participante) {
        this(participante, 0, 0, 0);
    }

    private RegistroTabla(Participante participante, int puntos,
            int diferencia, int victorias) {
        if (participante == null) {
            throw new IllegalArgumentException("La fila debe tener un participante.");
        }
        if (puntos < 0 || victorias < 0) {
            throw new IllegalArgumentException("Los puntos y las victorias no pueden ser negativos.");
        }

        this.participante = participante;
        this.puntos = puntos;
        this.diferencia = diferencia;
        this.victorias = victorias;
    }

    /** Crea una fila actualizada sin modificar el registro anterior. */
    public RegistroTabla conAporte(AporteResultado aporte) {
        if (aporte == null) {
            throw new IllegalArgumentException("El aporte del resultado no puede ser nulo.");
        }

        try {
            return new RegistroTabla(
                    participante,
                    Math.addExact(puntos, aporte.getPuntos()),
                    Math.addExact(diferencia, aporte.getDiferencia()),
                    Math.addExact(victorias, aporte.getVictorias())
            );
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("Los acumulados exceden el rango permitido.", ex);
        }
    }

    @Override
    public int compareTo(RegistroTabla otro) {
        Objects.requireNonNull(otro, "No se puede comparar con un registro nulo.");

        int comparacion = Integer.compare(otro.puntos, this.puntos);
        if (comparacion != 0) {
            return comparacion;
        }

        comparacion = Integer.compare(otro.diferencia, this.diferencia);
        if (comparacion != 0) {
            return comparacion;
        }

        comparacion = Integer.compare(otro.victorias, this.victorias);
        if (comparacion != 0) {
            return comparacion;
        }

        Collator ordenAlfabetico = Collator.getInstance(Locale.forLanguageTag("es-GT"));
        ordenAlfabetico.setStrength(Collator.SECONDARY);
        return ordenAlfabetico.compare(
                this.participante.getNombre(),
                otro.participante.getNombre()
        );
    }

    public Participante getParticipante() {
        return participante;
    }

    public int getPuntos() {
        return puntos;
    }

    public int getDiferencia() {
        return diferencia;
    }

    public int getVictorias() {
        return victorias;
    }
}