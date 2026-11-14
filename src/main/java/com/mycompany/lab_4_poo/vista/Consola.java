package com.mycompany.lab_4_poo.vista;

import com.mycompany.lab_4_poo.controlador.GestorArenaGT;
import com.mycompany.lab_4_poo.modelo.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.StringJoiner;

// Lee y presenta información; las operaciones se delegan al gestor. 
public final class Consola {

    private final GestorArenaGT gestor;
    private final Scanner entrada;

    public Consola(GestorArenaGT gestor) {
        if (gestor == null) {
            throw new IllegalArgumentException(
                    "La consola necesita un gestor.");
        }

        this.gestor = gestor;
        this.entrada = new Scanner(System.in);
    }

    public void ejecutar() {
        while (true) {
            mostrarMenu();

            try {
                int opcion = leerEntero("Selecciona una opción: ");

                if (opcion == 0) {
                    System.out.println("Hasta pronto.");
                    return;
                }

                ejecutarOpcion(opcion);

            } catch (IllegalArgumentException | IllegalStateException ex) {
                System.out.println(
                        "Operación rechazada: " + ex.getMessage());

            } catch (NoSuchElementException ex) {
                System.out.println(
                        "\nEntrada finalizada. Programa cerrado.");
                return;
            }
        }
    }

    private void mostrarMenu() {
        System.out.println("\n========== ArenaGT ==========");
        System.out.println("1. Registrar participante");
        System.out.println("2. Crear torneo");
        System.out.println("3. Cotizar inscripción");
        System.out.println("4. Inscribir participante");
        System.out.println("5. Cerrar inscripciones");
        System.out.println("6. Programar o reprogramar evento");
        System.out.println("7. Registrar resultado");
        System.out.println("8. Listar participantes");
        System.out.println("9. Listar torneos");
        System.out.println("10. Consultar detalle de un torneo");
        System.out.println("11. Calendario general");
        System.out.println("12. Calendario de un torneo");
        System.out.println("13. Calendario de un participante");
        System.out.println("14. Tabla de posiciones y campeón");
        System.out.println("15. Ingresos y premios entregados");
        System.out.println("16. Ranking de empresas");
        System.out.println("0. Salir");
    }

    private void ejecutarOpcion(int opcion) {
        switch (opcion) {
            case 1: {
                String codigo = leerTexto("Código del participante: ");
                String nombre = leerTexto("Nombre: ");
                String empresa = leerTexto("Empresa: ");

                gestor.registrarParticipante(codigo, nombre, empresa);

                System.out.println("Participante registrado.");
                break;
            }

            case 2: {
                mostrarDisciplinas();

                String codigo = leerTexto("Código del nuevo torneo: ");
                String nombre = leerTexto("Nombre del torneo: ");
                String disciplina = leerTexto("Código de la disciplina: ");

                gestor.crearTorneo(codigo, nombre, disciplina);

                System.out.println(
                        "Torneo creado. Sus inscripciones están abiertas.");
                break;
            }

            case 3: {
                mostrarDisciplinas();

                String disciplina = leerTexto("Código de la disciplina: ");
                int cantidad = leerEntero("Cantidad de participantes: ");

                System.out.println(
                        "Cotización: "
                        + dinero(gestor.cotizar(disciplina, cantidad)));
                break;
            }

            case 4: {
                String torneo = leerTexto("Código del torneo: ");
                String participante = leerTexto("Código del participante: ");

                gestor.inscribir(torneo, participante);

                System.out.println(
                        "Inscripción realizada. Cuota: "
                        + dinero(gestor.buscarTorneo(torneo)
                                .getDisciplina().getCuota()));
                break;
            }

            case 5: {
                String torneo = leerTexto("Código del torneo: ");

                gestor.cerrarInscripciones(torneo);

                System.out.println(
                        "Inscripciones cerradas y eventos generados.");

                mostrarDetalleTorneo(torneo);
                break;
            }

            case 6: {
                String torneo = leerTexto("Código del torneo: ");

                mostrarDetalleTorneo(torneo);

                String evento = leerTexto("Código del evento: ");

                gestor.buscarTorneo(torneo).buscarEvento(evento);

                LocalDate fecha = leerFecha("Fecha (AAAA-MM-DD): ");
                int hora = leerEntero("Hora completa (0 a 23): ");
                String sede = leerTexto("Sede: ");

                gestor.programar(torneo, evento, fecha, hora, sede);

                System.out.println("Programación guardada.");
                break;
            }

            case 7: {
                String torneo = leerTexto("Código del torneo: ");

                mostrarDetalleTorneo(torneo);

                String evento = leerTexto("Código del evento: ");

                Map<String, String> datos =
                        capturarResultado(torneo, evento);

                gestor.registrarResultado(torneo, evento, datos);

                System.out.println("Resultado registrado.");

                mostrarTabla(torneo);
                break;
            }

            case 8:
                mostrarParticipantes(gestor.getParticipantes());
                break;

            case 9:
                mostrarTorneos();
                break;

            case 10:
                mostrarDetalleTorneo(
                        leerTexto("Código del torneo: "));
                break;

            case 11:
                mostrarCalendario(gestor.calendarioGeneral());
                break;

            case 12:
                mostrarCalendario(
                        gestor.calendarioTorneo(
                                leerTexto("Código del torneo: ")));
                break;

            case 13:
                mostrarCalendario(
                        gestor.calendarioParticipante(
                                leerTexto("Código del participante: ")));
                break;

            case 14:
            case 15:
            case 16:
                mostrarReporte(opcion);
                break;

            default:
                throw new IllegalArgumentException(
                        "Selecciona una opción entre 0 y 16.");
        }
    }

    private String leerTexto(String mensaje) {
        System.out.print(mensaje);

        if (!entrada.hasNextLine()) {
            throw new NoSuchElementException(
                    "No hay más datos de entrada.");
        }

        return entrada.nextLine().trim();
    }

    private int leerEntero(String mensaje) {
        while (true) {
            try {
                return Integer.parseInt(leerTexto(mensaje));

            } catch (NumberFormatException ex) {
                System.out.println(
                        "Escribe un número entero válido.");
            }
        }
    }

    private LocalDate leerFecha(String mensaje) {
        while (true) {
            try {
                return LocalDate.parse(leerTexto(mensaje));

            } catch (DateTimeParseException ex) {
                System.out.println(
                        "Escribe una fecha válida con formato AAAA-MM-DD.");
            }
        }
    }

    private Map<String, String> capturarResultado(
            String codigoTorneo, String codigoEvento) {

        Map<String, String> campos =
                gestor.camposResultado(codigoTorneo, codigoEvento);

        Map<String, String> datos = new LinkedHashMap<>();

        System.out.println(
                "Deja vacíos los campos que no correspondan al resultado.");

        for (Map.Entry<String, String> campo : campos.entrySet()) {
            datos.put(
                    campo.getKey(),
                    leerTexto(campo.getValue() + ": ")
            );
        }

        return datos;
    }

    private void mostrarDisciplinas() {
        System.out.println("\nDisciplinas disponibles:");

        for (Disciplina disciplina : gestor.getDisciplinas()) {
            System.out.printf(
                    "%s | %s | %d a %d participantes | Cuota: %s%n",
                    disciplina.getCodigo(),
                    disciplina.getNombre(),
                    disciplina.getMinimo(),
                    disciplina.getMaximo(),
                    dinero(disciplina.getCuota())
            );
        }
    }

    private void mostrarParticipantes(
            List<Participante> participantes) {

        if (participantes.isEmpty()) {
            System.out.println("No hay participantes para mostrar.");
            return;
        }

        for (Participante participante : participantes) {
            System.out.printf(
                    "%s | %s | Empresa: %s%n",
                    participante.getCodigo(),
                    participante.getNombre(),
                    participante.getEmpresa()
            );
        }
    }

    private void mostrarTorneos() {
        List<Torneo> torneos = gestor.getTorneos();

        if (torneos.isEmpty()) {
            System.out.println("No hay torneos registrados.");
            return;
        }

        for (Torneo torneo : torneos) {
            System.out.printf(
                    "%s | %s | %s | %s | Inscritos: %d%n",
                    torneo.getCodigo(),
                    torneo.getNombre(),
                    torneo.getDisciplina().getNombre(),
                    torneo.getEstado().name().replace('_', ' '),
                    torneo.getInscritos().size()
            );
        }
    }

    private void mostrarDetalleTorneo(String codigoTorneo) {
        Torneo torneo = gestor.buscarTorneo(codigoTorneo);

        System.out.println(
                "\n" + torneo.getCodigo() + " | " + torneo.getNombre());

        System.out.println(
                "Disciplina: " + torneo.getDisciplina().getNombre());

        System.out.println(
                "Estado: " + torneo.getEstado().name().replace('_', ' '));

        System.out.println("Participantes inscritos:");
        mostrarParticipantes(torneo.getInscritos());

        System.out.println(
                "Eventos programados, en orden cronológico:");

        mostrarCalendario(gestor.calendarioTorneo(codigoTorneo));

        System.out.println("Eventos sin programar:");

        boolean hayPendientes = false;

        for (Evento evento : torneo.getEventos()) {
            if (!evento.getProgramacion().isPresent()) {
                hayPendientes = true;

                System.out.println(
                        evento.getCodigo()
                        + " | " + nombresParticipantes(evento));
            }
        }

        if (!hayPendientes) {
            System.out.println("No hay eventos sin programar.");
        }
    }

    private String nombresParticipantes(Evento evento) {
        StringJoiner nombres = new StringJoiner(" / ");

        for (Participante participante : evento.getParticipantes()) {
            nombres.add(
                    participante.getCodigo()
                    + " " + participante.getNombre());
        }

        return nombres.toString();
    }

    private void mostrarCalendario(Calendario calendario) {
        boolean hayEventos = false;

        for (Evento evento : calendario) {
            hayEventos = true;

            Programacion programacion =
                    evento.getProgramacion().get();

            System.out.printf(
                    Locale.ROOT,
                    "%s %02d:00 | %s | %s | %s | %s%n",
                    programacion.getFecha(),
                    programacion.getHora(),
                    programacion.getSede(),
                    evento.getCodigo(),
                    nombresParticipantes(evento),
                    evento.getEstado().name().replace('_', ' ')
            );

            evento.getResultado().ifPresent(resultado ->
                    System.out.println(
                            "  Resultado: " + resultado.getResumen()));
        }

        if (!hayEventos) {
            System.out.println("No hay eventos programados.");
        }
    }

    private void mostrarReporte(int opcion) {
        switch (opcion) {
            case 14:
                mostrarTabla(leerTexto("Código del torneo: "));
                break;

            case 15:
                mostrarFinanzas();
                break;

            case 16:
                mostrarRanking();
                break;

            default:
                throw new IllegalArgumentException(
                        "Reporte desconocido.");
        }
    }

    private void mostrarTabla(String codigoTorneo) {
        Torneo torneo = gestor.buscarTorneo(codigoTorneo);
        List<RegistroTabla> tabla = gestor.tabla(codigoTorneo);

        System.out.println(
                "\nTabla de posiciones: " + torneo.getNombre());

        if (tabla.isEmpty()) {
            System.out.println(
                    "Todavía no hay participantes inscritos.");
            return;
        }

        System.out.printf(
                "%-8s %-22s %8s %8s %9s%n",
                "Código", "Participante", "Puntos", "Dif.", "Victorias");

        for (RegistroTabla fila : tabla) {
            System.out.printf(
                    "%-8s %-22s %8d %8d %9d%n",
                    fila.getParticipante().getCodigo(),
                    fila.getParticipante().getNombre(),
                    fila.getPuntos(),
                    fila.getDiferencia(),
                    fila.getVictorias()
            );
        }

        if (torneo.getCampeon().isPresent()) {
            System.out.println(
                    "Campeón: " + torneo.getCampeon().get().getNombre());

        } else {
            System.out.println(
                    "El torneo todavía no tiene campeón.");
        }
    }

    private void mostrarFinanzas() {
        Map<String, BigDecimal> ingresos =
                gestor.getIngresosPorDisciplina();

        System.out.println("\nIngresos por inscripciones:");

        for (Disciplina disciplina : gestor.getDisciplinas()) {
            System.out.println(
                    disciplina.getNombre()
                    + ": "
                    + dinero(ingresos.get(disciplina.getCodigo())));
        }

        System.out.println(
                "Ingresos totales: "
                + dinero(gestor.getIngresosTotales()));

        Map<String, BigDecimal> premios =
                gestor.getPremiosEntregados();

        System.out.println("Premios entregados:");

        if (premios.isEmpty()) {
            System.out.println("No hay premios registrados.");
        }

        for (Map.Entry<String, BigDecimal> premio : premios.entrySet()) {
            Torneo torneo = gestor.buscarTorneo(premio.getKey());

            System.out.println(
                    torneo.getCodigo()
                    + " | " + torneo.getNombre()
                    + " | Campeón: "
                    + torneo.getCampeon().get().getNombre()
                    + " | " + dinero(premio.getValue()));
        }
    }

    private void mostrarRanking() {
        List<Map.Entry<String, Integer>> ranking =
                gestor.rankingEmpresas();

        System.out.println("\nRanking de empresas:");

        if (ranking.isEmpty()) {
            System.out.println("No hay empresas registradas.");
            return;
        }

        int posicion = 1;

        for (Map.Entry<String, Integer> empresa : ranking) {
            System.out.printf(
                    "%d. %s | %d puntos%n",
                    posicion++,
                    empresa.getKey(),
                    empresa.getValue()
            );
        }
    }

    private String dinero(BigDecimal monto) {
        return "Q" + monto.setScale(
                2, RoundingMode.HALF_UP).toPlainString();
    }
}