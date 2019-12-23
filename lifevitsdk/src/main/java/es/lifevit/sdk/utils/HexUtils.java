package es.lifevit.sdk.utils;


public class HexUtils {
    public static byte[] hexToBytes(String hexString) {

        String[] split = hexString.split(", ");
        byte[] arr = new byte[split.length];
        for (int i = 0; i < split.length; i++) {
            arr[i] = (byte) (short) Short.decode(split[i]);
        }

        return arr;
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String getStringToPrint(byte[] bytes) {
             char[] hexChars = new char[bytes.length * 3 - 1];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 3] = hexArray[v >>> 4];
            hexChars[j * 3 + 1] = hexArray[v & 0x0F];
            if (j != bytes.length - 1) {
                hexChars[j * 3 + 2] = ':';
            }
        }
        return new String(hexChars);
    }

}
