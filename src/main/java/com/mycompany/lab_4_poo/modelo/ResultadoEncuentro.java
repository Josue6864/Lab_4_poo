package com.mycompany.lab_4_poo.modelo;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Conserva las marcas y los aportes de un encuentro de forma inmutable. */
public final class ResultadoEncuentro implements Resultado {

    private final Map<String, Integer> marcadores;
    private final Map<String, Integer> penales;
    private final Map<String, AporteResultado> aportes;

    /** La disciplina valida las reglas deportivas antes de construirlo. */
    ResultadoEncuentro(Map<String, Integer> marcadores,
            Map<String, Integer> penales,
            Map<String, AporteResultado> aportes) {

        if (marcadores == null || penales == null || aportes == null) {
            throw new IllegalArgumentException(
                    "Los mapas del resultado no pueden ser nulos.");
        }

        if (marcadores.size() != 2) {
            throw new IllegalArgumentException(
                    "Un encuentro requiere exactamente dos marcadores.");
        }

        for (Map.Entry<String, Integer> entrada : marcadores.entrySet()) {
            if (entrada.getKey() == null
                    || entrada.getKey().trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "Los códigos de los participantes no pueden estar vacíos.");
            }

            if (entrada.getValue() == null || entrada.getValue() < 0) {
                throw new IllegalArgumentException(
                        "Los marcadores deben ser enteros no negativos.");
            }
        }

        if (!aportes.keySet().equals(marcadores.keySet())) {
            throw new IllegalArgumentException(
                    "Los aportes deben corresponder a los dos participantes del encuentro.");
        }

        for (AporteResultado aporte : aportes.values()) {
            if (aporte == null) {
                throw new IllegalArgumentException(
                        "Cada participante debe tener un aporte válido.");
            }
        }

        if (!penales.isEmpty()) {
            if (!penales.keySet().equals(marcadores.keySet())) {
                throw new IllegalArgumentException(
                        "Los penales deben corresponder a los dos participantes del encuentro.");
            }

            for (Integer convertidos : penales.values()) {
                if (convertidos == null || convertidos < 0) {
                    throw new IllegalArgumentException(
                            "Los penales convertidos deben ser enteros no negativos.");
                }
            }
        }

        this.marcadores = Collections.unmodifiableMap(
                new LinkedHashMap<>(marcadores));

        this.penales = Collections.unmodifiableMap(
                new LinkedHashMap<>(penales));

        this.aportes = Collections.unmodifiableMap(
                new LinkedHashMap<>(aportes));
    }

    @Override
    public Map<String, AporteResultado> getAportes() {
        return aportes;
    }

    @Override
    public String getResumen() {
        StringBuilder resumen = new StringBuilder();

        for (Map.Entry<String, Integer> entrada : marcadores.entrySet()) {
            if (resumen.length() > 0) {
                resumen.append(" | ");
            }

            resumen.append(entrada.getKey())
                    .append(": ")
                    .append(entrada.getValue());

            if (!penales.isEmpty()) {
                resumen.append(" [penales: ")
                        .append(penales.get(entrada.getKey()))
                        .append("]");
            }
        }

        return resumen.toString();
    }
}