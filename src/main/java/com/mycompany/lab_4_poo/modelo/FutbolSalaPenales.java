package com.mycompany.lab_4_poo.modelo;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Variante de fútbol sala que exige definir los empates mediante penales. */
public final class FutbolSalaPenales extends FutbolSala {

    public FutbolSalaPenales() {
        super(
                "FSP",
                "Fútbol sala con penales",
                new BigDecimal("750.00")
        );
    }

    @Override
    public Map<String, String> camposResultado(Evento evento) {
        Map<String, String> campos =
                new LinkedHashMap<>(super.camposResultado(evento));

        for (Participante participante : evento.getParticipantes()) {
            campos.put(
                    participante.getCodigo() + ".penales",
                    "Penales de " + participante.getNombre()
                    + " (solo si empatan en goles; en otro caso, dejar vacío)"
            );
        }

        return Collections.unmodifiableMap(campos);
    }

    @Override
    public Resultado evaluarResultado(
            Evento evento, Map<String, String> datos) {

        Map<String, Integer> marcadores = leerMarcadores(evento, datos);
        List<Participante> participantes = evento.getParticipantes();

        String local = participantes.get(0).getCodigo();
        String visitante = participantes.get(1).getCodigo();

        int golesLocal = marcadores.get(local);
        int golesVisitante = marcadores.get(visitante);

        if (golesLocal != golesVisitante) {
            for (Participante participante : participantes) {
                String texto = datos.get(
                        participante.getCodigo() + ".penales");

                if (texto != null && !texto.trim().isEmpty()) {
                    throw new IllegalArgumentException(
                            "No deben registrarse penales cuando los goles son distintos.");
                }
            }

            return super.evaluarResultado(evento, datos);
        }

        Map<String, Integer> penales = new LinkedHashMap<>();

        for (Participante participante : participantes) {
            String texto = datos.get(
                    participante.getCodigo() + ".penales");

            if (texto == null || texto.trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "Un empate en goles requiere los penales de ambos participantes.");
            }

            int convertidos;

            try {
                convertidos = Integer.parseInt(texto.trim());
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException(
                        "Los penales de " + participante.getNombre()
                        + " deben ser un entero válido.", ex);
            }

            if (convertidos < 0) {
                throw new IllegalArgumentException(
                        "Los penales convertidos no pueden ser negativos.");
            }

            penales.put(participante.getCodigo(), convertidos);
        }

        int penalesLocal = penales.get(local);
        int penalesVisitante = penales.get(visitante);

        if (penalesLocal == penalesVisitante) {
            throw new IllegalArgumentException(
                    "Los penales no pueden terminar empatados.");
        }

        int puntosLocal = penalesLocal > penalesVisitante ? 2 : 1;
        int puntosVisitante = penalesVisitante > penalesLocal ? 2 : 1;

        return crearResultado(
                evento,
                marcadores,
                penales,
                puntosLocal,
                puntosVisitante
        );
    }
}