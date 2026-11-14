package com.mycompany.lab_4_poo.controlador;

import com.mycompany.lab_4_poo.modelo.*;
import java.math.BigDecimal;
import java.text.Collator;
import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Coordina las operaciones de la aplicación y 
 * sus registros economicos. */
public final class GestorArenaGT {

    private final Map<String, Participante> participantes;
    private final Map<String, Torneo> torneos;
    private final Map<String, Disciplina> disciplinas;
    private final Map<String, BigDecimal> ingresosPorDisciplina;
    private final Map<String, BigDecimal> premiosPorTorneo;

    public GestorArenaGT() {
        participantes = new LinkedHashMap<>();
        torneos = new LinkedHashMap<>();
        disciplinas = new LinkedHashMap<>();
        ingresosPorDisciplina = new LinkedHashMap<>();
        premiosPorTorneo = new LinkedHashMap<>();

        List<Disciplina> catalogo = Arrays.asList(
                new FutbolSala(), new FutbolSalaPenales(),
                new Baloncesto(), new Voleibol(), new Relevos());

        for (Disciplina disciplina : catalogo) {
            disciplinas.put(disciplina.getCodigo(), disciplina);

            ingresosPorDisciplina.put(
                    disciplina.getCodigo(), new BigDecimal("0.00"));
        }
    }

    public void registrarParticipante(
            String codigo, String nombre, String empresa) {

        Participante participante =
                new Participante(codigo, nombre, empresa);

        if (participantes.containsKey(participante.getCodigo())) {
            throw new IllegalArgumentException(
                    "El código del participante ya existe.");
        }

        participantes.put(participante.getCodigo(), participante);
    }

    public void crearTorneo(
            String codigo, String nombre, String codigoDisciplina) {

        Disciplina disciplina = buscarDisciplina(codigoDisciplina);
        Torneo torneo = new Torneo(codigo, nombre, disciplina);

        if (torneos.containsKey(torneo.getCodigo())) {
            throw new IllegalArgumentException(
                    "El código del torneo ya existe.");
        }

        torneos.put(torneo.getCodigo(), torneo);
    }

    public Participante buscarParticipante(String codigo) {
        Participante participante =
                participantes.get(normalizarCodigo(codigo));

        if (participante == null) {
            throw new IllegalArgumentException(
                    "El participante indicado no existe.");
        }

        return participante;
    }

    public Torneo buscarTorneo(String codigo) {
        Torneo torneo = torneos.get(normalizarCodigo(codigo));

        if (torneo == null) {
            throw new IllegalArgumentException(
                    "El torneo indicado no existe.");
        }

        return torneo;
    }

    private Disciplina buscarDisciplina(String codigo) {
        Disciplina disciplina =
                disciplinas.get(normalizarCodigo(codigo));

        if (disciplina == null) {
            throw new IllegalArgumentException(
                    "La disciplina indicada no existe.");
        }

        return disciplina;
    }

    private static String normalizarCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El código no puede estar vacío.");
        }

        return codigo.trim().toUpperCase(Locale.ROOT);
    }

    public List<Participante> getParticipantes() {
        return Collections.unmodifiableList(
                new ArrayList<>(participantes.values()));
    }

    public List<Torneo> getTorneos() {
        return Collections.unmodifiableList(
                new ArrayList<>(torneos.values()));
    }

    public List<Disciplina> getDisciplinas() {
        return Collections.unmodifiableList(
                new ArrayList<>(disciplinas.values()));
    }

    public BigDecimal cotizar(String codigoDisciplina, int cantidad) {
        return buscarDisciplina(codigoDisciplina).cotizar(cantidad);
    }

    public void inscribir(
            String codigoTorneo, String codigoParticipante) {

        Torneo torneo = buscarTorneo(codigoTorneo);
        Participante participante =
                buscarParticipante(codigoParticipante);

        String disciplina = torneo.getDisciplina().getCodigo();

        BigDecimal cuota = torneo.inscribir(participante);

        ingresosPorDisciplina.put(
                disciplina,
                ingresosPorDisciplina.get(disciplina).add(cuota)
        );
    }

    public void cerrarInscripciones(String codigoTorneo) {
        buscarTorneo(codigoTorneo).cerrarInscripciones();
    }

    public void programar(
            String codigoTorneo,
            String codigoEvento,
            LocalDate fecha,
            int hora,
            String sede) {

        Evento evento =
                buscarTorneo(codigoTorneo).buscarEvento(codigoEvento);

        Programacion propuesta = new Programacion(fecha, hora, sede);

        evento.programar(propuesta, todosLosEventos());
    }

    public Map<String, String> camposResultado(
            String codigoTorneo, String codigoEvento) {

        return buscarTorneo(codigoTorneo)
                .camposResultado(codigoEvento);
    }

    public void registrarResultado(
            String codigoTorneo,
            String codigoEvento,
            Map<String, String> datos) {

        Torneo torneo = buscarTorneo(codigoTorneo);
        EstadoTorneo anterior = torneo.getEstado();

        torneo.registrarResultado(codigoEvento, datos);

        if (anterior == EstadoTorneo.EN_JUEGO
                && torneo.getEstado() == EstadoTorneo.FINALIZADO) {

            premiosPorTorneo.putIfAbsent(
                    torneo.getCodigo(), torneo.calcularPremio());
        }
    }

    private List<Evento> todosLosEventos() {
        List<Evento> eventos = new ArrayList<>();

        for (Torneo torneo : torneos.values()) {
            eventos.addAll(torneo.getEventos());
        }

        return eventos;
    }

    public Calendario calendarioGeneral() {
        return new Calendario(todosLosEventos());
    }

    public Calendario calendarioTorneo(String codigoTorneo) {
        return new Calendario(
                buscarTorneo(codigoTorneo).getEventos());
    }

    public Calendario calendarioParticipante(
            String codigoParticipante) {

        Participante participante =
                buscarParticipante(codigoParticipante);

        List<Evento> seleccionados = new ArrayList<>();

        for (Evento evento : todosLosEventos()) {
            for (Participante competidor : evento.getParticipantes()) {
                if (competidor.getCodigo().equals(
                        participante.getCodigo())) {

                    seleccionados.add(evento);
                    break;
                }
            }
        }

        return new Calendario(seleccionados);
    }

    public List<RegistroTabla> tabla(String codigoTorneo) {
        return buscarTorneo(codigoTorneo).getTabla();
    }

    public BigDecimal getIngresosTotales() {
        BigDecimal total = new BigDecimal("0.00");

        for (BigDecimal ingreso : ingresosPorDisciplina.values()) {
            total = total.add(ingreso);
        }

        return total;
    }

    public Map<String, BigDecimal> getIngresosPorDisciplina() {
        return Collections.unmodifiableMap(
                new LinkedHashMap<>(ingresosPorDisciplina));
    }

    public Map<String, BigDecimal> getPremiosEntregados() {
        return Collections.unmodifiableMap(
                new LinkedHashMap<>(premiosPorTorneo));
    }

    public List<Map.Entry<String, Integer>> rankingEmpresas() {
        Map<String, String> nombres = new LinkedHashMap<>();
        Map<String, Integer> puntos = new LinkedHashMap<>();

        for (Participante participante : participantes.values()) {
            String empresa = participante.getEmpresa();
            String clave = empresa.toUpperCase(Locale.ROOT);

            nombres.putIfAbsent(clave, empresa);
            puntos.putIfAbsent(clave, 0);
        }

        for (Torneo torneo : torneos.values()) {
            for (RegistroTabla registro : torneo.getTabla()) {
                String clave = registro.getParticipante()
                        .getEmpresa()
                        .toUpperCase(Locale.ROOT);

                try {
                    puntos.put(
                            clave,
                            Math.addExact(
                                    puntos.get(clave),
                                    registro.getPuntos())
                    );
                } catch (ArithmeticException ex) {
                    throw new IllegalStateException(
                            "El total de puntos de una empresa "
                            + "excede el rango permitido.", ex);
                }
            }
        }

        List<Map.Entry<String, Integer>> ranking =
                new ArrayList<>();

        for (Map.Entry<String, Integer> entrada : puntos.entrySet()) {
            ranking.add(
                    new AbstractMap.SimpleImmutableEntry<>(
                            nombres.get(entrada.getKey()),
                            entrada.getValue())
            );
        }

        Collator orden = Collator.getInstance(
                Locale.forLanguageTag("es-GT"));

        orden.setStrength(Collator.SECONDARY);

        ranking.sort((a, b) -> {
            int comparacion = Integer.compare(
                    b.getValue(), a.getValue());

            return comparacion != 0
                    ? comparacion
                    : orden.compare(a.getKey(), b.getKey());
        });

        return Collections.unmodifiableList(ranking);
    }

    /** Prepara los ejemplos y los incorpora sin cobrar ni premiar. */
    public void cargarDatosIniciales() {
        if (!participantes.isEmpty() || !torneos.isEmpty()) {
            throw new IllegalStateException(
                    "La precarga solo se permite al iniciar, "
                    + "con los registros vacíos.");
        }

        String[][] datos = {
            {"P01", "Alfa", "TecnoGT"},
            {"P02", "Beta", "TecnoGT"},
            {"P03", "Gamma", "Logística Maya"},
            {"P04", "Delta", "Logística Maya"},
            {"P05", "Epsilon", "Servicios Quetzal"},
            {"P06", "Zeta", "Servicios Quetzal"}
        };

        Map<String, Participante> nuevosParticipantes =
                new LinkedHashMap<>();

        for (String[] dato : datos) {
            Participante participante =
                    new Participante(dato[0], dato[1], dato[2]);

            nuevosParticipantes.put(
                    participante.getCodigo(), participante);
        }

        List<Participante> inscritos =
                new ArrayList<>(nuevosParticipantes.values());

        Map<String, Torneo> nuevosTorneos = new LinkedHashMap<>();
        List<Evento> agenda = new ArrayList<>();

        for (Disciplina disciplina : disciplinas.values()) {
            String codigo = "T-" + disciplina.getCodigo();

            Torneo torneo = Torneo.precargado(
                    codigo,
                    "Copa " + disciplina.getNombre(),
                    disciplina,
                    inscritos
            );

            torneo.cerrarInscripciones();

            nuevosTorneos.put(codigo, torneo);
            agenda.addAll(torneo.getEventos());
        }

        LocalDate inicio = LocalDate.of(2026, 11, 9);

        for (int turno = 0; turno < agenda.size(); turno++) {
            Programacion propuesta = new Programacion(
                    inicio.plusDays(turno / 12),
                    8 + turno % 12,
                    "Arena Central"
            );

            agenda.get(turno).programar(propuesta, agenda);
        }

        // Torneo, número de evento, marcas y penales opcionales.
        String[][] marcadores = {
            {"T-FS", "001", "3", "0"},
            {"T-FS", "010", "2", "0"},
            {"T-FS", "015", "1", "1"},

            {"T-FSP", "001", "1", "1", "4", "3"},
            {"T-FSP", "010", "2", "0"},

            {"T-BAL", "002", "12", "8"},
            {"T-BAL", "003", "7", "8"},
            {"T-BAL", "004", "7", "8"},
            {"T-BAL", "006", "9", "8"},
            {"T-BAL", "007", "9", "8"},

            {"T-VOL", "001", "3", "0"},
            {"T-VOL", "010", "3", "1"},
            {"T-VOL", "015", "3", "2"}
        };

        for (String[] marcador : marcadores) {
            Torneo torneo = nuevosTorneos.get(marcador[0]);

            Evento evento = torneo.buscarEvento(
                    marcador[0] + "-E" + marcador[1]);

            String local =
                    evento.getParticipantes().get(0).getCodigo();

            String visitante =
                    evento.getParticipantes().get(1).getCodigo();

            Map<String, String> resultado = new LinkedHashMap<>();

            resultado.put(local + ".marca", marcador[2]);
            resultado.put(visitante + ".marca", marcador[3]);

            if (marcador.length == 6) {
                resultado.put(local + ".penales", marcador[4]);
                resultado.put(visitante + ".penales", marcador[5]);
            }

            torneo.registrarResultado(
                    evento.getCodigo(), resultado);
        }

        Torneo relevos = nuevosTorneos.get("T-REL");
        Map<String, String> tiempos = new LinkedHashMap<>();

        String[] segundos = {
            "50.1", "51.9", "52.4", "55.0", "58.0"
        };

        for (int i = 0; i < inscritos.size(); i++) {
            String codigo = inscritos.get(i).getCodigo();

            tiempos.put(
                    codigo + ".descalificado",
                    i == 5 ? "S" : "N"
            );

            if (i < segundos.length) {
                tiempos.put(codigo + ".tiempo", segundos[i]);
            }
        }

        relevos.registrarResultado("T-REL-E001", tiempos);

        participantes.putAll(nuevosParticipantes);
        torneos.putAll(nuevosTorneos);
    }
}