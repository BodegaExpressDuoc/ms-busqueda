package cl.duoc.bodegaexpress.busqueda.dto;

import java.math.BigDecimal;

public record ArticuloResponse(Long id, String nombre, String descripcion,
                               BigDecimal precio, Integer stock) {
}
