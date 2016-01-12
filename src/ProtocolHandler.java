import java.util.ArrayList;
import java.util.Scanner;

public class ProtocolHandler {

    private Protocol.Server protocol;

    public ProtocolHandler(String type) {

    }

//    public void announce(String name, String features) {
//        //HALLO_playername_features
//
//    }

    public static String createPackage(String command, ArrayList<String> parameters) {

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

        return sb.toString();
    }

    public static ArrayList<Object> readPackage(String packet) {
        ArrayList<Object> result = new ArrayList<Object>();

        // Scanner to read a message
        Scanner in1 = new Scanner(packet);
        in1.useDelimiter(Protocol.Server.Settings.COMMAND_END);
        String outerElement = in1.next();

        // Scanner to read parts of a message
        Scanner in2 = new Scanner(outerElement);
        in2.useDelimiter(Character.toString(Protocol.Server.Settings.DELIMITER));

        // Keep track of parameters positions
        int counter = 0;

        // Loop over inner parameters
        while (in2.hasNext())
        {
            String value = in2.next();

            if(counter == 0) {
                result.add(value);
            } else {

                // Scanner to read parts of a parameter
                Scanner in3 = new Scanner(value);
                in3.useDelimiter(Protocol.Server.Settings.DELIMITER2);

                ArrayList<Object> subResult = new ArrayList<Object>();

                // Loop over inner parameters
                while (in3.hasNext()) {
                    String value2 = in3.next();
                    subResult.add(value2);
                }

                if(subResult.size() <= 1){
                    result.add(value);
                } else {
                    result.add(subResult);
                }
            }

            // Increment counter to keep
            counter++;
        }
        return result;
    }
}
