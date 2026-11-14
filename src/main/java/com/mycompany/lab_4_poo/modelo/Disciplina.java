package com.mycompany.lab_4_poo.modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Configuración y comportamiento común de las disciplinas deportivas. */
public abstract class Disciplina {

    private final String codigo;
    private final String nombre;
    private final int minimo;
    private final int maximo;
    private final BigDecimal cuota;

    protected Disciplina(String codigo, String nombre, int minimo,
            int maximo, BigDecimal cuota) {

        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El código de la disciplina no puede estar vacío.");
        }

        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El nombre de la disciplina no puede estar vacío.");
        }

        if (minimo <= 0 || maximo < minimo) {
            throw new IllegalArgumentException(
                    "Los límites de participantes no son válidos.");
        }

        if (cuota == null || cuota.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "La cuota de inscripción debe ser mayor que cero.");
        }

        BigDecimal cuotaValidada;
        try {
            cuotaValidada = cuota.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException(
                    "La cuota debe expresarse en centavos exactos.", ex);
        }

        this.codigo = codigo.trim().toUpperCase(Locale.ROOT);
        this.nombre = nombre.trim().replaceAll("\\s+", " ");
        this.minimo = minimo;
        this.maximo = maximo;
        this.cuota = cuotaValidada;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public int getMinimo() {
        return minimo;
    }

    public int getMaximo() {
        return maximo;
    }

    public BigDecimal getCuota() {
        return cuota;
    }

    /** Calcula el presupuesto sin inscribir ni registrar ingresos. */
    public BigDecimal cotizar(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad cotizada debe ser mayor que cero.");
        }

        return cuota.multiply(BigDecimal.valueOf(cantidad));
    }

    /** Valida el cupo; las subclases pueden agregar condiciones específicas. */
    public void validarInscripcion(List<Participante> actuales,
            Participante nuevo) {

        if (actuales == null || nuevo == null) {
            throw new IllegalArgumentException(
                    "Se requieren los inscritos y el nuevo participante.");
        }

        if (actuales.size() >= maximo) {
            throw new IllegalArgumentException(
                    "El torneo alcanzó el máximo de participantes de la disciplina.");
        }
    }

    public abstract List<Evento> crearEventos(
            String codigoTorneo, List<Participante> inscritos);

    public abstract Map<String, String> camposResultado(Evento evento);

    public abstract Resultado evaluarResultado(
            Evento evento, Map<String, String> datos);

    /** Genera un encuentro por cada pareja, sin repetir el orden inverso. */
    protected List<Evento> generarEncuentros(
            String codigoTorneo, List<Participante> inscritos) {

        if (codigoTorneo == null || codigoTorneo.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El código del torneo no puede estar vacío.");
        }

        if (inscritos == null || inscritos.size() < minimo
                || inscritos.size() > maximo) {
            throw new IllegalArgumentException(
                    "La disciplina requiere entre " + minimo
                    + " y " + maximo + " participantes.");
        }

        Set<String> codigos = new HashSet<>();

        for (Participante participante : inscritos) {
            if (participante == null) {
                throw new IllegalArgumentException(
                        "Los inscritos no pueden contener participantes nulos.");
            }

            if (!codigos.add(participante.getCodigo())) {
                throw new IllegalArgumentException(
                        "Los inscritos no pueden contener códigos repetidos.");
            }
        }

        List<Evento> eventos = new ArrayList<>();
        String codigoBase = codigoTorneo.trim().toUpperCase(Locale.ROOT);
        int consecutivo = 1;

        for (int i = 0; i < inscritos.size(); i++) {
            for (int j = i + 1; j < inscritos.size(); j++) {
                String codigoEvento = String.format(
                        Locale.ROOT, "%s-E%03d", codigoBase, consecutivo++);

                eventos.add(new Encuentro(
                        codigoEvento, inscritos.get(i), inscritos.get(j)));
            }
        }

        return Collections.unmodifiableList(eventos);
    }
}
