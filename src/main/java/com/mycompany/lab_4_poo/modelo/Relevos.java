package com.mycompany.lab_4_poo.modelo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

// Relevos con una carrera unica, tiempos distintos y posibles descalificaciones.
public final class Relevos extends Disciplina {

    public Relevos() {
        super("REL", "Relevos", 3, 8, new BigDecimal("300.00"));
    }

    @Override
    public List<Evento> crearEventos(
            String codigoTorneo, List<Participante> inscritos) {

        if (codigoTorneo == null || codigoTorneo.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El código del torneo no puede estar vacío.");
        }

        if (inscritos == null || inscritos.size() < getMinimo()
                || inscritos.size() > getMaximo()) {
            throw new IllegalArgumentException(
                    "Relevos requiere entre 3 y 8 participantes.");
        }

        String codigoEvento =
                codigoTorneo.trim().toUpperCase(Locale.ROOT) + "-E001";

        return Collections.singletonList(
                new Carrera(codigoEvento, inscritos));
    }

    @Override
    public Map<String, String> camposResultado(Evento evento) {
        if (evento == null) {
            throw new IllegalArgumentException(
                    "El evento no puede ser nulo.");
        }

        List<Participante> participantes = evento.getParticipantes();

        if (participantes == null || participantes.size() < getMinimo()
                || participantes.size() > getMaximo()) {
            throw new IllegalArgumentException(
                    "La carrera debe tener entre 3 y 8 participantes.");
        }

        Map<String, String> campos = new LinkedHashMap<>();
        Set<String> codigos = new HashSet<>();

        for (Participante participante : participantes) {
            if (participante == null
                    || !codigos.add(participante.getCodigo())) {
                throw new IllegalArgumentException(
                        "Los participantes deben existir y tener códigos distintos.");
            }

            campos.put(
                    participante.getCodigo() + ".descalificado",
                    "¿Fue descalificado "
                    + participante.getNombre() + "? (S/N)"
            );

            campos.put(
                    participante.getCodigo() + ".tiempo",
                    "Tiempo de " + participante.getNombre()
                    + " en segundos (usa punto decimal; "
                    + "dejar vacío si fue descalificado)"
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

        Map<String, BigDecimal> tiempos = new LinkedHashMap<>();
        Set<String> descalificados = new LinkedHashSet<>();
        Set<BigDecimal> tiemposRegistrados = new TreeSet<>();

        for (Participante participante : evento.getParticipantes()) {
            String codigo = participante.getCodigo();
            String estado = datos.get(codigo + ".descalificado");

            if (estado == null || estado.trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "Debe indicar S o N para la descalificación de "
                        + participante.getNombre() + ".");
            }

            estado = estado.trim().toUpperCase(Locale.ROOT);

            if (!estado.equals("S") && !estado.equals("N")) {
                throw new IllegalArgumentException(
                        "La descalificación debe indicarse con S o N.");
            }

            String textoTiempo = datos.get(codigo + ".tiempo");

            if (estado.equals("S")) {
                if (textoTiempo != null && !textoTiempo.trim().isEmpty()) {
                    throw new IllegalArgumentException(
                            "Un participante descalificado "
                            + "no puede tener un tiempo registrado.");
                }

                descalificados.add(codigo);
                continue;
            }

            if (textoTiempo == null || textoTiempo.trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "Falta el tiempo de "
                        + participante.getNombre() + ".");
            }

            BigDecimal tiempo;

            try {
                tiempo = new BigDecimal(textoTiempo.trim());
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException(
                        "El tiempo de " + participante.getNombre()
                        + " debe ser un número válido con punto decimal.",
                        ex);
            }

            if (tiempo.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(
                        "Los tiempos deben ser mayores que cero.");
            }

            if (!tiemposRegistrados.add(tiempo)) {
                throw new IllegalArgumentException(
                        "Los tiempos de los participantes no pueden repetirse.");
            }

            tiempos.put(codigo, tiempo);
        }

        List<Map.Entry<String, BigDecimal>> ordenados =
                new ArrayList<>(tiempos.entrySet());

        ordenados.sort(
                (a, b) -> a.getValue().compareTo(b.getValue()));

        int[] puntosPorLugar = {10, 8, 6, 5, 4, 3, 2, 1};

        Map<String, AporteResultado> aportes = new LinkedHashMap<>();

        for (int i = 0; i < ordenados.size(); i++) {
            aportes.put(
                    ordenados.get(i).getKey(),
                    new AporteResultado(
                            puntosPorLugar[i],
                            0,
                            i == 0 ? 1 : 0
                    )
            );
        }

        for (String codigo : descalificados) {
            aportes.put(codigo, new AporteResultado(0, 0, 0));
        }

        return new ResultadoCarrera(
                tiempos,
                descalificados,
                aportes
        );
    }
}