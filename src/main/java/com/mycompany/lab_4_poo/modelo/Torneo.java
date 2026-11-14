package com.mycompany.lab_4_poo.modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

// Administra las inscripciones, los eventos y la clasificación de una copa
public final class Torneo {

    private final String codigo;
    private final String nombre;
    private final Disciplina disciplina;
    private EstadoTorneo estado;
    private Map<String, Participante> inscritos;
    private List<Evento> eventos;
    private Map<String, RegistroTabla> clasificacion;

    public Torneo(String codigo, String nombre, Disciplina disciplina) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El código del torneo no puede estar vacío.");
        }

        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El nombre del torneo no puede estar vacío.");
        }

        if (disciplina == null) {
            throw new IllegalArgumentException(
                    "El torneo debe tener una disciplina.");
        }

        this.codigo = codigo.trim().toUpperCase(Locale.ROOT);
        this.nombre = nombre.trim().replaceAll("\\s+", " ");
        this.disciplina = disciplina;
        this.estado = EstadoTorneo.EN_INSCRIPCION;
        this.inscritos = new LinkedHashMap<>();
        this.eventos = new ArrayList<>();
        this.clasificacion = new LinkedHashMap<>();
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public Disciplina getDisciplina() {
        return disciplina;
    }

    public EstadoTorneo getEstado() {
        return estado;
    }

    public List<Participante> getInscritos() {
        return Collections.unmodifiableList(
                new ArrayList<>(inscritos.values()));
    }

    public List<Evento> getEventos() {
        return Collections.unmodifiableList(new ArrayList<>(eventos));
    }

    /** Inscribe y devuelve la cuota que deberá registrar el gestor. */
    public BigDecimal inscribir(Participante participante) {
        validarInscripcion(participante);

        BigDecimal cuota = disciplina.getCuota();
        RegistroTabla registro = new RegistroTabla(participante);

        Map<String, Participante> nuevosInscritos =
                new LinkedHashMap<>(inscritos);

        Map<String, RegistroTabla> nuevaClasificacion =
                new LinkedHashMap<>(clasificacion);

        nuevosInscritos.put(participante.getCodigo(), participante);
        nuevaClasificacion.put(participante.getCodigo(), registro);

        this.inscritos = nuevosInscritos;
        this.clasificacion = nuevaClasificacion;

        return cuota;
    }

    /** Validación compartida por 
     * inscripción normal y precarga. */
    private void validarInscripcion(Participante participante) {
        if (estado != EstadoTorneo.EN_INSCRIPCION) {
            throw new IllegalStateException(
                    "Solo se pueden inscribir participantes durante la inscripción.");
        }

        if (participante == null) {
            throw new IllegalArgumentException(
                    "El participante no puede ser nulo.");
        }

        if (inscritos.containsKey(participante.getCodigo())) {
            throw new IllegalArgumentException(
                    "El participante ya está inscrito en este torneo.");
        }

        int cantidadEmpresa = 0;

        for (Participante actual : inscritos.values()) {
            if (actual.getEmpresa().equalsIgnoreCase(
                    participante.getEmpresa())) {
                cantidadEmpresa++;
            }
        }

        if (cantidadEmpresa >= 2) {
            throw new IllegalArgumentException(
                    "Una empresa no puede tener más de dos participantes en el torneo.");
        }

        disciplina.validarInscripcion(getInscritos(), participante);
    }

    public void cerrarInscripciones() {
        if (estado != EstadoTorneo.EN_INSCRIPCION) {
            throw new IllegalStateException(
                    "Las inscripciones de este torneo ya están cerradas.");
        }

        if (inscritos.size() < disciplina.getMinimo()) {
            throw new IllegalArgumentException(
                    "Se requieren al menos "
                    + disciplina.getMinimo()
                    + " participantes para cerrar.");
        }

        List<Evento> generados =
                disciplina.crearEventos(codigo, getInscritos());

        if (generados == null || generados.isEmpty()) {
            throw new IllegalStateException(
                    "La disciplina debe generar al menos un evento.");
        }

        List<Evento> nuevosEventos = new ArrayList<>();
        Set<String> codigosEventos = new HashSet<>();
        Set<String> participantesIncluidos = new HashSet<>();

        for (Evento evento : generados) {
            if (evento == null
                    || evento.getCodigo() == null
                    || evento.getCodigo().trim().isEmpty()
                    || !codigosEventos.add(evento.getCodigo())
                    || evento.getEstado() != EstadoEvento.SIN_PROGRAMAR) {

                throw new IllegalStateException(
                        "La disciplina generó eventos inválidos o repetidos.");
            }

            if (evento.getParticipantes() == null
                    || evento.getParticipantes().isEmpty()) {
                throw new IllegalStateException(
                        "Cada evento debe tener participantes inscritos.");
            }

            for (Participante participante : evento.getParticipantes()) {
                if (participante == null
                        || !inscritos.containsKey(participante.getCodigo())) {
                    throw new IllegalStateException(
                            "Un evento contiene un participante ajeno al torneo.");
                }

                participantesIncluidos.add(participante.getCodigo());
            }

            nuevosEventos.add(evento);
        }

        if (!participantesIncluidos.equals(inscritos.keySet())) {
            throw new IllegalStateException(
                    "Los eventos deben incluir a todos los inscritos.");
        }

        this.eventos = nuevosEventos;
        this.estado = EstadoTorneo.EN_JUEGO;
    }

    public Evento buscarEvento(String codigoEvento) {
        if (codigoEvento == null || codigoEvento.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El código del evento no puede estar vacío.");
        }

        String buscado = codigoEvento.trim().toUpperCase(Locale.ROOT);

        for (Evento evento : eventos) {
            if (evento.getCodigo().equals(buscado)) {
                return evento;
            }
        }

        throw new IllegalArgumentException(
                "El evento indicado no pertenece a este torneo.");
    }

    public Map<String, String> camposResultado(String codigoEvento) {
        return disciplina.camposResultado(buscarEvento(codigoEvento));
    }

    public void registrarResultado(
            String codigoEvento, Map<String, String> datos) {

        if (estado != EstadoTorneo.EN_JUEGO) {
            throw new IllegalStateException(
                    "Solo se registran resultados en torneos en juego.");
        }

        Evento evento = buscarEvento(codigoEvento);

        if (evento.getEstado() == EstadoEvento.FINALIZADO) {
            throw new IllegalStateException(
                    "El evento ya tiene un resultado registrado.");
        }

        if (evento.getEstado() != EstadoEvento.PROGRAMADO) {
            throw new IllegalStateException(
                    "El evento debe estar programado para registrar un resultado.");
        }

        Resultado resultado = disciplina.evaluarResultado(evento, datos);

        if (resultado == null || resultado.getAportes() == null) {
            throw new IllegalArgumentException(
                    "La disciplina no devolvió un resultado válido.");
        }

        Map<String, AporteResultado> aportes = resultado.getAportes();
        Set<String> participantesEvento = new HashSet<>();

        for (Participante participante : evento.getParticipantes()) {
            participantesEvento.add(participante.getCodigo());
        }

        if (!aportes.keySet().equals(participantesEvento)) {
            throw new IllegalArgumentException(
                    "Los aportes deben corresponder exactamente "
                    + "a los participantes del evento.");
        }

        Map<String, RegistroTabla> nuevaClasificacion =
                new LinkedHashMap<>(clasificacion);

        for (Map.Entry<String, AporteResultado> entrada : aportes.entrySet()) {
            RegistroTabla anterior = clasificacion.get(entrada.getKey());

            if (anterior == null || entrada.getValue() == null) {
                throw new IllegalArgumentException(
                        "El resultado contiene un aporte inválido.");
            }

            nuevaClasificacion.put(
                    entrada.getKey(),
                    anterior.conAporte(entrada.getValue())
            );
        }

        evento.registrarResultado(resultado);
        this.clasificacion = nuevaClasificacion;

        finalizarSiCorresponde();
    }

    public List<RegistroTabla> getTabla() {
        List<RegistroTabla> tabla =
                new ArrayList<>(clasificacion.values());

        Collections.sort(tabla);

        return Collections.unmodifiableList(tabla);
    }

    public Optional<Participante> getCampeon() {
        if (estado != EstadoTorneo.FINALIZADO
                || clasificacion.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(getTabla().get(0).getParticipante());
    }

    /** Calcula el premio, 
     * no registra su entrega ni modifica ingresos. */
    public BigDecimal calcularPremio() {
        return disciplina.getCuota()
                .multiply(BigDecimal.valueOf(inscritos.size()))
                .multiply(new BigDecimal("0.50"))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private void finalizarSiCorresponde() {
        if (estado != EstadoTorneo.EN_JUEGO || eventos.isEmpty()) {
            return;
        }

        for (Evento evento : eventos) {
            if (evento.getEstado() != EstadoEvento.FINALIZADO) {
                return;
            }
        }

        this.estado = EstadoTorneo.FINALIZADO;
    }

    /** Construye la precarga directamente, 
     * sin llamar a inscribir ni cobrar. */
    public static Torneo precargado(
            String codigo,
            String nombre,
            Disciplina disciplina,
            List<Participante> participantes) {

        if (participantes == null) {
            throw new IllegalArgumentException(
                    "La lista de precarga no puede ser nula.");
        }

        Torneo torneo = new Torneo(codigo, nombre, disciplina);

        for (Participante participante : participantes) {
            torneo.validarInscripcion(participante);

            torneo.inscritos.put(
                    participante.getCodigo(), participante);

            torneo.clasificacion.put(
                    participante.getCodigo(),
                    new RegistroTabla(participante)
            );
        }

        return torneo;
    }
}