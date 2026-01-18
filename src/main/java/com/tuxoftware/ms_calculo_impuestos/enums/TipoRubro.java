package com.tuxoftware.ms_calculo_impuestos.enums;

public enum TipoRubro {
    CARGO,      // Suma al total (Impuesto Base, Adicional, Recargos)
    DESCUENTO,  // Resta al total (Pronto Pago, INAPAM)
    INFORMATIVO // No afecta la suma (ej. "Base Gravable: $500,000")
}
