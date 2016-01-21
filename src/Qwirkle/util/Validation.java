package qwirkle.util;

public class Validation {
    /**
     * Checks a string to be a valid ip address
     *
     * @param host ip address String
     * @return
     */
    public static boolean checkIP(String host) {

        // Localhost is valid
        if (host.equals("localhost")) {
            return true;

        } else if (host.matches("([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\.([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\.([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\.([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])") == true) {

            // Valid ipv4 address
            return true;

        } else if (host.matches("([0-9a-f]+)\\:([0-9a-f]+)\\:([0-9a-f]+)\\:([0-9a-f]+)\\:([0-9a-f]+)\\:([0-9a-f]+)\\:([0-9a-f]+)\\:([0-9a-f]+)") == true) {

            // Valid ipv6, but not supported
            return false;
        }

        // Default false
        return false;
    }

    /**
     * Checks a string to be a valid port
     *
     * @param port
     * @return
     */
    public static boolean checkPort(String port) {
        try {

            // Valid port number
            int parsedPort = Integer.parseInt(port, 10);

            // Port can not be 0
            if (parsedPort != 0) {
                return true;
            } else {
                return false;
            }

        } catch (NumberFormatException e) {

            // Could not parse int from string
            return false;
        }
    }
    
    
}
