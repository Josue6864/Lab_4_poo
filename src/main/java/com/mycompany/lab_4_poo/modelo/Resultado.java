package com.mycompany.lab_4_poo.modelo;

import java.util.Map;

/** Contrato para resultados completos y validados por una disciplina. */
public interface Resultado {

    /** Devuelve aportes por código de participante en un mapa no modificable. */
    Map<String, AporteResultado> getAportes();

    /** Describe las marcas, penales, tiempos o descalificaciones registrados. */
    String getResumen();
}