package com.mycompany.lab_4_poo.modelo;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Fútbol sala con empates y puntuación de 3, 1 y 0 puntos de tabla. */
public class FutbolSala extends Disciplina {

    public FutbolSala() {
        this("FS", "Fútbol sala", new BigDecimal("600.00"));
    }

    protected FutbolSala(String codigo, String nombre, BigDecimal cuota) {
        super(codigo, nombre, 4, 8, cuota);
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
                    "El fútbol sala requiere dos participantes distintos.");
        }

        Map<String, String> campos = new LinkedHashMap<>();

        for (Participante participante : participantes) {
            campos.put(
                    participante.getCodigo() + ".marca",
                    "Goles de " + participante.getNombre()
            );
        }

        return Collections.unmodifiableMap(campos);
    }

    @Override
    public Resultado evaluarResultado(
            Evento evento, Map<String, String> datos) {

        Map<String, Integer> marcadores = leerMarcadores(evento, datos);
        List<Participante> participantes = evento.getParticipantes();

        int golesLocal = marcadores.get(
                participantes.get(0).getCodigo());

        int golesVisitante = marcadores.get(
                participantes.get(1).getCodigo());

        int puntosLocal;
        int puntosVisitante;

        if (golesLocal > golesVisitante) {
            puntosLocal = 3;
            puntosVisitante = 0;
        } else if (golesLocal < golesVisitante) {
            puntosLocal = 0;
            puntosVisitante = 3;
        } else {
            puntosLocal = 1;
            puntosVisitante = 1;
        }

        return crearResultado(
                evento,
                marcadores,
                Collections.emptyMap(),
                puntosLocal,
                puntosVisitante
        );
    }

    /** Lee los goles y las subclases validan sus campos adicionales. */
    protected Map<String, Integer> leerMarcadores(
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
                        "Faltan los goles de "
                        + participante.getNombre() + ".");
            }

            int goles;

            try {
                goles = Integer.parseInt(texto.trim());
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException(
                        "Los goles de " + participante.getNombre()
                        + " deben ser un entero válido.", ex);
            }

            if (goles < 0) {
                throw new IllegalArgumentException(
                        "Los goles no pueden ser negativos.");
            }

            marcadores.put(participante.getCodigo(), goles);
        }

        return Collections.unmodifiableMap(marcadores);
    }

    /** Construye aportes y diferencias sin modificar el evento ni la tabla. */
    protected Resultado crearResultado(
            Evento evento,
            Map<String, Integer> marcas,
            Map<String, Integer> penales,
            int puntosLocal,
            int puntosVisitante) {

        List<Participante> participantes = evento.getParticipantes();

        String local = participantes.get(0).getCodigo();
        String visitante = participantes.get(1).getCodigo();

        int diferencia = marcas.get(local) - marcas.get(visitante);

        Map<String, AporteResultado> aportes = new LinkedHashMap<>();

        aportes.put(local, new AporteResultado(
                puntosLocal,
                diferencia,
                puntosLocal > puntosVisitante ? 1 : 0
        ));

        aportes.put(visitante, new AporteResultado(
                puntosVisitante,
                -diferencia,
                puntosVisitante > puntosLocal ? 1 : 0
        ));

        return new ResultadoEncuentro(marcas, penales, aportes);
    }
}