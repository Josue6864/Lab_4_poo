package com.mycompany.lab_4_poo.modelo;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Voleibol al mejor de cinco sets, con ganador de tres sets. 
public final class Voleibol extends Disciplina {

    public Voleibol() {
        super("VOL", "Voleibol", 4, 6, new BigDecimal("450.00"));
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
                    "El voleibol requiere dos participantes distintos.");
        }

        Map<String, String> campos = new LinkedHashMap<>();

        for (Participante participante : participantes) {
            campos.put(
                    participante.getCodigo() + ".marca",
                    "Sets ganados por " + participante.getNombre()
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
                        "Faltan los sets ganados por "
                        + participante.getNombre() + ".");
            }

            int sets;

            try {
                sets = Integer.parseInt(texto.trim());
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException(
                        "Los sets de " + participante.getNombre()
                        + " deben ser un entero válido.", ex);
            }

            if (sets < 0 || sets > 3) {
                throw new IllegalArgumentException(
                        "Los sets ganados deben estar entre 0 y 3.");
            }

            marcadores.put(participante.getCodigo(), sets);
        }

        List<Participante> participantes = evento.getParticipantes();

        String local = participantes.get(0).getCodigo();
        String visitante = participantes.get(1).getCodigo();

        int setsLocal = marcadores.get(local);
        int setsVisitante = marcadores.get(visitante);

        int setsGanador = Math.max(setsLocal, setsVisitante);
        int setsPerdedor = Math.min(setsLocal, setsVisitante);

        if (setsGanador != 3 || setsPerdedor > 2) {
            throw new IllegalArgumentException(
                    "El ganador debe tener 3 sets "
                    + "y el perdedor debe tener 0, 1 o 2.");
        }

        boolean ganaLocal = setsLocal > setsVisitante;

        int puntosGanador = setsPerdedor == 2 ? 2 : 3;
        int puntosPerdedor = setsPerdedor == 2 ? 1 : 0;
        int diferencia = setsLocal - setsVisitante;

        Map<String, AporteResultado> aportes = new LinkedHashMap<>();

        aportes.put(local, new AporteResultado(
                ganaLocal ? puntosGanador : puntosPerdedor,
                diferencia,
                ganaLocal ? 1 : 0
        ));

        aportes.put(visitante, new AporteResultado(
                ganaLocal ? puntosPerdedor : puntosGanador,
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