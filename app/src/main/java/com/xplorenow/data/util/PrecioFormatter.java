package com.xplorenow.data.util;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public final class PrecioFormatter {

    private static final NumberFormat NF = NumberFormat
            .getNumberInstance(new Locale("es", "AR"));

    static {
        NF.setMaximumFractionDigits(0);
    }

    private PrecioFormatter() {} // util class, no se instancia

    public static String format(BigDecimal precio) {
        if (precio == null || precio.signum() == 0) {
            return "Gratis";
        }
        return "$ " + NF.format(precio);
    }
}