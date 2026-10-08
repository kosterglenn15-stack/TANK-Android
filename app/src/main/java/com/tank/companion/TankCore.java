package com.tank.companion;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class TankCore {
    private static final int[] SHARE1 = new int[]{27, 242, 94, 214, 246, 57, 67, 142, 212, 156, 27, 122, 206, 154, 139, 45, 167, 226, 239, 133, 171, 228, 224, 49, 105, 90, 197, 203, 235, 47, 169, 134};
    private static final int[] SHARE2 = new int[]{57, 150, 214, 26, 153, 224, 14, 26, 176, 153, 70, 182, 235, 41, 241, 44, 54, 130, 80, 253, 1, 10, 66, 46, 110, 60, 86, 155, 155, 239, 138, 170};
    private static final int[] SHARE3 = new int[]{109, 224, 15, 173, 147, 23, 99, 108, 38, 43, 64, 32, 130, 206, 76, 49, 228, 13, 2, 237, 16, 216, 22, 27, 195, 252, 247, 48, 95, 131, 164, 115};
    private static final int[] SHARE4 = new int[]{56, 53, 32, 166, 143, 30, 5, 32, 68, 169, 119, 83, 6, 253, 19, 90, 45, 4, 139, 189, 194, 16, 243, 203, 84, 207, 194, 158, 20, 61, 218, 181};

    private static final BigInteger P127 =
        new BigInteger("170141183460469231731687303715884105727");

    private static final String B36 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private TankCore() {}

    public static final class Result {
        public String wordle;
        public String date;
        public String coordinate;
        public String kalebration;
        public String cicada;
        public String distorted;
        public String waypoint;
        public String password;
    }

    private static byte[] rootSecret() {
        byte[] out = new byte[32];
        for (int i = 0; i < 32; i++) {
            out[i] = (byte)(SHARE1[i] ^ SHARE2[i] ^ SHARE3[i] ^ SHARE4[i]);
        }
        return out;
    }

    private static byte[] sha256(byte[] data) throws Exception {
        return MessageDigest.getInstance("SHA-256").digest(data);
    }

    private static byte[] sha512(byte[] data) throws Exception {
        return MessageDigest.getInstance("SHA-512").digest(data);
    }

    private static byte[] hmac256(byte[] key, byte[] data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(data);
    }

    private static byte[] concat(byte[]... arrays) {
        int total = 0;
        for (byte[] a : arrays) total += a.length;
        byte[] out = new byte[total];
        int pos = 0;
        for (byte[] a : arrays) {
            System.arraycopy(a, 0, out, pos, a.length);
            pos += a.length;
        }
        return out;
    }

    private static BigInteger unsignedBigInt(byte[] data, int start, int count) {
        byte[] slice = new byte[count];
        System.arraycopy(data, start, slice, 0, count);
        return new BigInteger(1, slice);
    }

    private static String dailyCoordinate(String date) throws Exception {
        byte[] digest = hmac256(
            rootSecret(),
            ("COORD|" + date).getBytes(StandardCharsets.US_ASCII)
        );

        BigInteger a = unsignedBigInt(digest, 0, 8);
        BigInteger b = unsignedBigInt(digest, 8, 8);
        double denom = new BigInteger("18446744073709551615").doubleValue();

        double lat = (a.doubleValue() / denom) * 180.0 - 90.0;
        double lon = (b.doubleValue() / denom) * 360.0 - 180.0;

        return String.format(Locale.US, "%.6f,%.6f", lat, lon);
    }

    private static boolean isPrime(int n) {
        if (n < 2) return false;
        if (n == 2 || n == 3) return true;
        if ((n % 2) == 0) return false;

        for (int d = 3; d * d <= n; d += 2) {
            if ((n % d) == 0) return false;
        }
        return true;
    }

    private static String toBase36(BigInteger number) {
        if (number.signum() == 0) return "0";
        BigInteger base = BigInteger.valueOf(36);
        StringBuilder sb = new StringBuilder();

        while (number.signum() > 0) {
            BigInteger[] qr = number.divideAndRemainder(base);
            sb.append(B36.charAt(qr[1].intValue()));
            number = qr[0];
        }

        return sb.reverse().toString();
    }

    private static byte[] kalebration(String wordle, String date, String coord) throws Exception {
        byte[] seedBytes = sha256(
            (wordle + "|" + date + "|" + coord + "|KALEBRATION")
                .getBytes(StandardCharsets.UTF_8)
        );

        BigInteger seed = unsignedBigInt(seedBytes, 0, 16);
        BigInteger a = unsignedBigInt(seedBytes, 0, 4).add(BigInteger.valueOf(3301));
        BigInteger b = unsignedBigInt(seedBytes, 4, 4).add(BigInteger.valueOf(7331));
        BigInteger c = unsignedBigInt(seedBytes, 8, 4).add(BigInteger.valueOf(13007));
        BigInteger d = unsignedBigInt(seedBytes, 12, 4).add(BigInteger.valueOf(17011));

        BigInteger n = seed.mod(P127.subtract(BigInteger.ONE)).add(BigInteger.ONE);

        StringBuilder marks = new StringBuilder();

        for (int i = 1; i <= 3301; i++) {
            int r = n.mod(BigInteger.valueOf(4)).intValue();

            if (r == 0) {
                n = n.divide(BigInteger.valueOf(2)).add(a).mod(P127);
            } else if (r == 1) {
                n = n.multiply(BigInteger.valueOf(3)).add(b).mod(P127);
            } else if (r == 2) {
                n = n.multiply(n).add(c).mod(P127);
            } else {
                n = n.multiply(BigInteger.valueOf(5)).add(d).mod(P127);
            }

            if (isPrime(i)) {
                if (marks.length() > 0) marks.append("|");
                marks.append(n.toString(16));
            }
        }

        return sha256(marks.toString().getBytes(StandardCharsets.US_ASCII));
    }

    private static byte[] cicada(
        String wordle,
        String date,
        String coord,
        byte[] kale
    ) throws Exception {
        byte[] raw = concat(
            (wordle + "|" + date + "|" + coord + "|")
                .getBytes(StandardCharsets.US_ASCII),
            kale
        );

        byte[] h = sha512(raw);
        BigInteger num = unsignedBigInt(h, 0, 24);
        String text = toBase36(num);

        StringBuilder mixed = new StringBuilder();
        for (int i = 0; i < text.length(); i += 5) {
            int end = Math.min(i + 5, text.length());
            mixed.append(new StringBuilder(text.substring(i, end)).reverse());
        }

        if (mixed.length() == 0) mixed.append("TANK");

        int shift = 0;
        for (int i = 0; i < wordle.length(); i++) {
            shift += wordle.charAt(i) - 64;
        }
        shift = shift % mixed.length();

        String m = mixed.toString();
        String rotated = m.substring(shift) + m.substring(0, shift);

        StringBuilder extracted = new StringBuilder();
        for (int i = 1; i <= rotated.length(); i++) {
            if (isPrime(i)) extracted.append(rotated.charAt(i - 1));
        }

        if (extracted.length() == 0) extracted.append(rotated);

        return sha256(extracted.toString().getBytes(StandardCharsets.US_ASCII));
    }

    private static String distortedCoordinate(
        String coord,
        String wordle,
        byte[] kale
    ) {
        String[] parts = coord.split(",");
        double lat = Double.parseDouble(parts[0]);
        double lon = Double.parseDouble(parts[1]);

        int wordSum = 0;
        for (int i = 0; i < wordle.length(); i++) {
            wordSum += wordle.charAt(i) - 64;
        }

        long k1 = unsignedBigInt(kale, 0, 4).longValue();
        long k2 = unsignedBigInt(kale, 4, 4).longValue();

        double off1 = ((k1 % 200001L) - 100000L) / 1000000.0;
        double off2 = ((k2 % 200001L) - 100000L) / 1000000.0;

        double lat2 = lat + off1;
        double lon2 = lon + off2;

        if ((wordSum % 2) != 0) {
            double temp = lat2;
            lat2 = lon2;
            lon2 = temp;
        }

        return reverseDecimal(lat2) + "," + reverseDecimal(lon2);
    }

    private static String reverseDecimal(double value) {
        String sign = value < 0 ? "-" : "";
        double a = Math.abs(value);
        long whole = (long)a;
        long frac = Math.round((a - whole) * 1000000.0);
        String fracText = String.format(Locale.US, "%06d", frac);
        return sign + whole + "." + new StringBuilder(fracText).reverse();
    }

    private static String waypoint(
        String distorted,
        byte[] cicada,
        String wordle,
        String date
    ) throws Exception {
        byte[] seed = sha256(
            concat(
                (distorted + "|").getBytes(StandardCharsets.US_ASCII),
                cicada,
                ("|" + wordle + "|" + date).getBytes(StandardCharsets.US_ASCII)
            )
        );

        StringBuilder out = new StringBuilder("WP-");
        for (int i = 0; i < 10; i++) {
            out.append(String.format(Locale.US, "%02X", seed[i] & 0xFF));
        }
        return out.toString();
    }

    private static String hex(byte[] data) {
        StringBuilder sb = new StringBuilder(data.length * 2);
        for (byte b : data) {
            sb.append(String.format(Locale.US, "%02X", b & 0xFF));
        }
        return sb.toString();
    }

    private static byte[] finalComposite(
        String wordle,
        String date,
        String coord,
        byte[] kale,
        byte[] cicada,
        String distorted,
        String waypoint
    ) throws Exception {
        final List<String[]> pieces = new ArrayList<String[]>();
        pieces.add(new String[]{"W", wordle});
        pieces.add(new String[]{"D", date});
        pieces.add(new String[]{"C", coord});
        pieces.add(new String[]{"K", hex(kale)});
        pieces.add(new String[]{"X", hex(cicada)});
        pieces.add(new String[]{"G", distorted});
        pieces.add(new String[]{"P", waypoint});

        byte[] selector = sha256(
            concat(wordle.getBytes(StandardCharsets.US_ASCII), kale)
        );

        final List<Object[]> decorated = new ArrayList<Object[]>();

        for (int i = 0; i < pieces.size(); i++) {
            int rank = (selector[i] & 0xFF) ^
                (selector[selector.length - 1 - i] & 0xFF);
            decorated.add(new Object[]{rank, pieces.get(i)[0], pieces.get(i)[1]});
        }

        Collections.sort(
            decorated,
            new Comparator<Object[]>() {
                @Override
                public int compare(Object[] a, Object[] b) {
                    int ra = (Integer)a[0];
                    int rb = (Integer)b[0];
                    if (ra != rb) return ra < rb ? -1 : 1;
                    return ((String)a[1]).compareTo((String)b[1]);
                }
            }
        );

        StringBuilder joined = new StringBuilder();

        for (int i = 0; i < decorated.size(); i++) {
            if (i > 0) joined.append("||");
            joined.append((String)decorated.get(i)[1])
                  .append(":")
                  .append((String)decorated.get(i)[2]);
        }

        return sha512(joined.toString().getBytes(StandardCharsets.UTF_8));
    }

    public static Result calculate(String wordle, String date) throws Exception {
        wordle = wordle.trim().toUpperCase(Locale.US);
        date = date.trim();

        if (!wordle.matches("[A-Z]{5}")) {
            throw new IllegalArgumentException("Wordle must be exactly 5 letters.");
        }

        Result r = new Result();
        r.wordle = wordle;
        r.date = date;
        r.coordinate = dailyCoordinate(date);

        byte[] kale = kalebration(wordle, date, r.coordinate);
        r.kalebration = hex(kale);

        byte[] cic = cicada(wordle, date, r.coordinate, kale);
        r.cicada = hex(cic);

        r.distorted = distortedCoordinate(r.coordinate, wordle, kale);
        r.waypoint = waypoint(r.distorted, cic, wordle, date);
        r.password = hex(
            finalComposite(
                wordle,
                date,
                r.coordinate,
                kale,
                cic,
                r.distorted,
                r.waypoint
            )
        );

        return r;
    }
}
