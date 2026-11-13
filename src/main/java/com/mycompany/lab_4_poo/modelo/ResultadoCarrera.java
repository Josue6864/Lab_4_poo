package com.mycompany.lab_4_poo.modelo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Conserva los tiempos, descalificaciones y aportes de una carrera. */
public final class ResultadoCarrera implements Resultado {

    private final Map<String, BigDecimal> tiempos;
    private final Set<String> descalificados;
    private final Map<String, AporteResultado> aportes;

    /** La disciplina valida tiempos repetidos, posiciones y puntos antes de construirlo. */
    ResultadoCarrera(Map<String, BigDecimal> tiempos,
            Set<String> descalificados,
            Map<String, AporteResultado> aportes) {

        if (tiempos == null || descalificados == null || aportes == null) {
            throw new IllegalArgumentException(
                    "Las colecciones del resultado no pueden ser nulas.");
        }

        if (aportes.isEmpty()) {
            throw new IllegalArgumentException(
                    "El resultado debe incluir participantes.");
        }

        for (Map.Entry<String, AporteResultado> entrada : aportes.entrySet()) {
            if (entrada.getKey() == null
                    || entrada.getKey().trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "Los códigos de los participantes no pueden estar vacíos.");
            }

            if (entrada.getValue() == null) {
                throw new IllegalArgumentException(
                        "Cada participante debe tener un aporte válido.");
            }
        }

        Set<String> participantes = new HashSet<>(tiempos.keySet());

        for (String codigo : descalificados) {
            if (!participantes.add(codigo)) {
                throw new IllegalArgumentException(
                        "Un participante no puede tener tiempo y estar descalificado a la vez.");
            }
        }

        if (!participantes.equals(aportes.keySet())) {
            throw new IllegalArgumentException(
                    "Cada participante debe tener un tiempo o una descalificación y su aporte.");
        }

        for (BigDecimal tiempo : tiempos.values()) {
            if (tiempo == null || tiempo.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(
                        "Los tiempos deben ser mayores que cero.");
            }
        }

        this.tiempos = Collections.unmodifiableMap(
                new LinkedHashMap<>(tiempos));

        this.descalificados = Collections.unmodifiableSet(
                new LinkedHashSet<>(descalificados));

        this.aportes = Collections.unmodifiableMap(
                new LinkedHashMap<>(aportes));
    }

    @Override
    public Map<String, AporteResultado> getAportes() {
        return aportes;
    }

    @Override
    public String getResumen() {
        List<Map.Entry<String, BigDecimal>> ordenados =
                new ArrayList<>(tiempos.entrySet());

        ordenados.sort((a, b) -> a.getValue().compareTo(b.getValue()));

        StringBuilder resumen = new StringBuilder();
        int lugar = 1;

        for (Map.Entry<String, BigDecimal> entrada : ordenados) {
            if (resumen.length() > 0) {
                resumen.append(" | ");
            }

            resumen.append(lugar)
                    .append(". ")
                    .append(entrada.getKey())
                    .append(": ")
                    .append(entrada.getValue()
                            .stripTrailingZeros()
                            .toPlainString())
                    .append(" s");

            lugar++;
        }

        for (String codigo : descalificados) {
            if (resumen.length() > 0) {
                resumen.append(" | ");
            }

            resumen.append(codigo).append(": DESCALIFICADO");
        }

        return resumen.toString();
    }
}
