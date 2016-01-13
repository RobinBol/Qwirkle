package ServerClient;

import java.util.ArrayList;
import java.util.Scanner;

public class ProtocolHandler {


    /**
     * Method that creates a package according to protocol. It takes a command
     * and an array list of parameters to be send.
     *
     * @param command    Protocol command (e.g. "REQUESTGAME" or "QUIT")
     * @param parameters Individual parameters in an array list
     * @return Package formatted as a string
     */
    public static String createPackage(String command, ArrayList<Object> parameters) {

        // Build the package using a string builder
        StringBuilder sb = new StringBuilder();

        // First element of package is the command identifier
        sb.append(command);

        // Loop over all additional parameters
        for (int i = 0; i < parameters.size(); i++) {

            // Append default delimeter
            sb.append(Protocol.Server.Settings.DELIMITER);

            // Append the parameter
            sb.append(parameters.get(i));

            // If final parameter
            if (i == parameters.size() - 1) {

                // Append command end
                sb.append(Protocol.Server.Settings.COMMAND_END);
            }
        }

        // Return package as string
        return sb.toString();
    }

    /**
     * Parses a message and returns a more usable output format.
     *
     * @param packet The incoming message
     * @return ArrayList holding all elements of the package nicely formatted
     */
    public static ArrayList<Object> readPackage(String packet) {
        ArrayList<Object> result = new ArrayList<>();

        // Scanner to read a message
        Scanner packetScanner = new Scanner(packet);
        packetScanner.useDelimiter(Protocol.Server.Settings.COMMAND_END);

        // Check if there is something to read
        if (packetScanner.hasNext()) {

            // Get whole message
            String message = packetScanner.next();

            // Scanner to read parts of a message
            Scanner parameterScanner = new Scanner(message);
            parameterScanner.useDelimiter(Character.toString(Protocol.Server.Settings.DELIMITER));

            // Keep track of parameters positions
            int counter = 0;

            // Loop over inner parameters
            while (parameterScanner.hasNext()) {
                String value = parameterScanner.next();

                // Always add command first
                if (counter == 0) {
                    result.add(value);
                } else {

                    // Scanner to read parts of a parameter
                    Scanner subparameterScanner = new Scanner(value);
                    subparameterScanner.useDelimiter(Protocol.Server.Settings.DELIMITER2);

                    ArrayList<Object> subResult = new ArrayList<>();

                    // Loop over inner parameters
                    while (subparameterScanner.hasNext()) {
                        String value2 = subparameterScanner.next();
                        subResult.add(value2);
                    }

                    // If no real subparameters found, add as regular parameter
                    if (subResult.size() <= 1) {
                        result.add(value);
                    } else {
                        result.add(subResult);
                    }
                }

                // Increment counter to keep
                counter++;
            }
        }

        // Return formatted package as ArrayList
        return result;
    }
}
