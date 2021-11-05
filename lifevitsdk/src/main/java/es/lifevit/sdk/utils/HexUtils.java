package es.lifevit.sdk.utils;


public class HexUtils {

    public static byte hexToByte(String hex){
        return (byte) (short) Short.decode(hex);
    }

    public static byte[] hexToBytes(String hexString) {
        return  hexToBytes(hexString, ", ");
    }
        public static byte[] hexToBytes(String hexString, String separator) {
        String[] split = hexString.split(separator);
        byte[] arr = new byte[split.length];
        for (int i = 0; i < split.length; i++) {
            arr[i] = (byte) (short) Short.decode(split[i]);
        }

        return arr;
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
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
