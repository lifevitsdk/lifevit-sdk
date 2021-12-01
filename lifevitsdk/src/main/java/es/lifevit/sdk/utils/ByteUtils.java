package es.lifevit.sdk.utils;

public class ByteUtils {
    public static int toUnsignedInt(byte x) {
        return ((int) x) & 0xff;
    }

    /**
     * Converts the argument to a {@code long} by an unsigned
     * conversion.  In an unsigned conversion to a {@code long}, the
     * high-order 56 bits of the {@code long} are zero and the
     * low-order 8 bits are equal to the bits of the {@code byte} argument.
     * <p>
     * Consequently, zero and positive {@code byte} values are mapped
     * to a numerically equal {@code long} value and negative {@code
     * byte} values are mapped to a {@code long} value equal to the
     * input plus 2<sup>8</sup>.
     *
     * @param x the value to convert to an unsigned {@code long}
     * @return the argument converted to {@code long} by an unsigned
     * conversion
     * @since 1.8
     */
    public static long toUnsignedLong(byte x) {
        return ((long) x) & 0xffL;
    }

    public static byte[] intToBytesLittleIndian(int decimal) {
        byte[] bytes = new byte[4];

        bytes[0] = (byte) (decimal & 0xFF);
        bytes[1] = (byte) ((decimal >> 8) & 0xFF);
        bytes[2] = (byte) ((decimal >> 16) & 0xFF);
        bytes[3] = (byte) ((decimal >> 24) & 0xFF);

        return bytes;
    }


    public static int bytesToInt(byte[] bytes) {
        return (((bytes[0] & 0x000000ff) | ((bytes[1] << 8) & 0x0000ff00)) | ((bytes[2] << 16 & 0x00ff0000)) | ((bytes[3] << 24) & 0xff000000));
    }

    public static int bytesToIntReversed(byte[] bytes) {
        return (((bytes[3] & 0x000000ff) | ((bytes[2] << 8) & 0x0000ff00)) | ((bytes[1] << 16 & 0x00ff0000)) | ((bytes[0] << 24) & 0xff000000));
    }

    public static byte getWeekByte(boolean monday, boolean tuesday, boolean wednesday, boolean thursday, boolean friday, boolean saturday, boolean sunday) {
        byte value = 0x00;
        if (monday) {
            value += 0x01;
        }
        if (tuesday) {
            value += 0x02;
        }
        if (wednesday) {
            value += 0x04;
        }
        if (thursday) {
            value += 0x08;
        }
        if (friday) {
            value += 0x10;
        }
        if (saturday) {
            value += 0x20;
        }
        if (sunday) {
            value += 0x40;
        }

        return value;
    }


    public static double convertIEEE754BytesToFloat(byte[] bytes) {
        int bits = (bytes[0] << 24) | (bytes[1] << 16) | (bytes[2] << 8) | (bytes[3]);
        double sign = ((bytes[0] & 0b10000000) == 0) ? 1.0 : -1.0;
        int e = ((bits >>> 23) & 0xff);
        int m = (e == 0) ? (bits & 0x7fffff) << 1 : (bits & 0x7fffff) | 0x800000;
        return sign * m * Math.pow(2, e - 150);
    }

    public static byte[] convertIEEE754FloatToBytes(float f) {
        assert !Float.isNaN(f);
        // see also JavaDoc of Float.intBitsToFloat(int)

        int bits = Float.floatToIntBits(f);
        int s = (bits >> 31) == 0 ? 1 : -1;
        int e = (bits >> 23) & 0xFF;
        int m = (e == 0) ? (bits & 0x7FFFFF) << 1 : (bits & 0x7FFFFF) | 0x800000;

        int exp = (e - 150) / 4 + 6;
        int mant;
        int mantissaShift = (e - 150) % 4;  // compensate for base 16
        if (mantissaShift >= 0) mant = m >> mantissaShift;
        else mant = m >> (Math.abs(mantissaShift));
        if (mant > 0xFFFFFFF) {
            mant >>= 4;
            exp++;
        }  // loose of precision */
        byte a = (byte) ((1 - s) << 6 | (exp + 64));
        return new byte[]{a, (byte) (mant >> 16), (byte) (mant >> 8), (byte) mant};
    }


}
