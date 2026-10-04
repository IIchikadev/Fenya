package aethereal.util;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/** Only labelled total prices are accepted; seller names and unit-price hints are ignored. */
public final class AuctionPrice {
    private static final Pattern PRICE = Pattern.compile("(?iu)(?:цена|стоимость|price)\\s*[:：]\\s*[$₽]?\\s*([0-9][0-9 .,\\u00a0\\u202f]*)([kкmм]?)");
    private AuctionPrice() {}
    public static long parse(String line) {
        String clean = line.replaceAll("§.", "").toLowerCase(java.util.Locale.ROOT);
        if (clean.contains("за 1") || clean.contains("за шт") || clean.contains("per item")) return -1;
        var match = PRICE.matcher(clean);
        if (!match.find()) return -1;
        String value = match.group(1).replaceAll("[\\s\\u00a0\\u202f]", "");
        String suffix = match.group(2);
        try {
            if (suffix.isEmpty()) {
                if (!value.matches("[0-9]+|[0-9]{1,3}(?:[,.][0-9]{3})+")) return -1;
                value = value.replace(",", "").replace(".", "");
            } else value = value.replace(',', '.');
            long result = new BigDecimal(value).multiply(BigDecimal.valueOf(suffix.isEmpty() ? 1 : suffix.equals("k") || suffix.equals("к") ? 1000 : 1000000)).longValueExact();
            return result > 0 ? result : -1;
        } catch (ArithmeticException | NumberFormatException ignored) { return -1; }
    }
}
