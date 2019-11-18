package market;

import java.nio.charset.StandardCharsets;

public class Checksum {

    public Checksum() {
    }

    public String convert(String message) {
        message = message.replace("|", "\u0001");
        byte[] messageBytes = message.getBytes(StandardCharsets.US_ASCII);
        int total = 0;

        for (int i = 0; i < message.length(); i++)
            total += messageBytes[i];

        int checkSum = total % 256;
        String checkSumString = String.valueOf(checkSum);

        return checkSumString;
    }

    public boolean compare(String message) {
        String givenCheckSum = message.substring(message.indexOf("|10=") + 4).replace("|", "");
        String messageWithoutCheckSum = message.replace(message.substring(message.indexOf("|10=") + 1), "");

        if (convert(messageWithoutCheckSum).equals(givenCheckSum))
            return true;
        else
            return false;
    }

}