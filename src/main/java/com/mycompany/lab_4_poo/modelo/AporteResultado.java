package com.mycompany.lab_4_poo.modelo;

/** Contiene el efecto de un resultado para un participante en un evento. */
public final class AporteResultado {

    private final int puntos;
    private final int diferencia;
    private final int victorias;

    public AporteResultado(int puntos, int diferencia, int victorias) {
        if (puntos < 0) {
            throw new IllegalArgumentException("Los puntos de tabla no pueden ser negativos.");
        }
        if (victorias < 0 || victorias > 1) {
            throw new IllegalArgumentException("El aporte de victorias debe ser 0 o 1.");
        }

        this.puntos = puntos;
        this.diferencia = diferencia;
        this.victorias = victorias;
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
