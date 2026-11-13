package edu.biblioteca.service;

public record Indicadores(int totalLibros,
                          int ejemplaresDisponibles,
                          int prestamosActivos,
                          int prestamosVencidos) {
}
