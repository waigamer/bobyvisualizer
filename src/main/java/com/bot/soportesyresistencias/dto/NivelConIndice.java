package com.bot.soportesyresistencias.dto;

public class NivelConIndice {
    private final double nivel;
    private final int indice;

    public NivelConIndice(double nivel, int indice) {
        this.nivel = nivel;
        this.indice = indice;
    }

    public double getNivel() {
        return nivel;
    }

    public int getIndice() {
        return indice;
    }
}

