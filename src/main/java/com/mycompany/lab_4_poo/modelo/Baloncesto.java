package com.mycompany.lab_4_poo.modelo;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Baloncesto sin empates y con puntuación de 2 y 1 puntos de tabla. 
public final class Baloncesto extends Disciplina {

    public Baloncesto() {
        super("BAL", "Baloncesto", 4, 6, new BigDecimal("500.00"));
    }

    @Override
    public List<Evento> crearEventos(
            String codigoTorneo, List<Participante> inscritos) {

        return generarEncuentros(codigoTorneo, inscritos);
    }

    @Override
    public Map<String, String> camposResultado(Evento evento) {
        if (evento == null) {
            throw new IllegalArgumentException(
                    "El evento no puede ser nulo.");
        }

        List<Participante> participantes = evento.getParticipantes();

        if (participantes == null || participantes.size() != 2
                || participantes.get(0) == null
                || participantes.get(1) == null
                || participantes.get(0).getCodigo().equals(
                        participantes.get(1).getCodigo())) {

            throw new IllegalArgumentException(
                    "El baloncesto requiere dos participantes distintos.");
        }

        Map<String, String> campos = new LinkedHashMap<>();

        for (Participante participante : participantes) {
            campos.put(
                    participante.getCodigo() + ".marca",
                    "Puntos anotados por " + participante.getNombre()
            );
        }

        return Collections.unmodifiableMap(campos);
    }

    @Override
    public Resultado evaluarResultado(
            Evento evento, Map<String, String> datos) {

        if (datos == null) {
            throw new IllegalArgumentException(
                    "Los datos del resultado no pueden ser nulos.");
        }

        Map<String, String> campos = camposResultado(evento);

        for (String clave : datos.keySet()) {
            if (!campos.containsKey(clave)) {
                throw new IllegalArgumentException(
                        "Campo de resultado desconocido: " + clave);
            }
        }

        Map<String, Integer> marcadores = new LinkedHashMap<>();

        for (Participante participante : evento.getParticipantes()) {
            String texto = datos.get(
                    participante.getCodigo() + ".marca");

            if (texto == null || texto.trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "Faltan los puntos anotados por "
                        + participante.getNombre() + ".");
            }

            int puntos;

            try {
                puntos = Integer.parseInt(texto.trim());
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException(
                        "Los puntos anotados por "
                        + participante.getNombre()
                        + " deben ser un entero válido.", ex);
            }

            if (puntos < 0) {
                throw new IllegalArgumentException(
                        "Los puntos anotados no pueden ser negativos.");
            }

            marcadores.put(participante.getCodigo(), puntos);
        }

        List<Participante> participantes = evento.getParticipantes();

        String local = participantes.get(0).getCodigo();
        String visitante = participantes.get(1).getCodigo();

        int puntosLocal = marcadores.get(local);
        int puntosVisitante = marcadores.get(visitante);

        if (puntosLocal == puntosVisitante) {
            throw new IllegalArgumentException(
                    "El baloncesto no admite marcadores empatados.");
        }

        boolean ganaLocal = puntosLocal > puntosVisitante;
        int diferencia = puntosLocal - puntosVisitante;

        Map<String, AporteResultado> aportes = new LinkedHashMap<>();

        aportes.put(local, new AporteResultado(
                ganaLocal ? 2 : 1,
                diferencia,
                ganaLocal ? 1 : 0
        ));

        aportes.put(visitante, new AporteResultado(
                ganaLocal ? 1 : 2,
                -diferencia,
                ganaLocal ? 0 : 1
        ));

        return new ResultadoEncuentro(
                marcadores,
                Collections.emptyMap(),
                aportes
        );
    }
}
