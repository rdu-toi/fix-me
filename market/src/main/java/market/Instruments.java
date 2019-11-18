package market;

import market.Message;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Instruments {

    private HashMap<String, Integer[]> instruments = new HashMap<String, Integer[]>();

    public Instruments() {
        this.getInstrumentsFromFile();
    }

    private void getInstrumentsFromFile() {
        List<String> lines = Collections.emptyList(); 
        try
        {
          lines = Files.readAllLines(Paths.get(System.getProperty("user.dir") + "/market/src/main/java/", "instruments.txt"), StandardCharsets.UTF_8); 
        } 

        catch (IOException e) 
        { 
          e.printStackTrace(); 
        }

        int choice = Integer.parseInt(lines.get(0));

        String rawInstruments = lines.get(choice);

        String[] instrumentsArray = rawInstruments.split("\\|");
        for (String element: instrumentsArray) {
            String[] elementArray = element.split(",");
            Integer[] valueArray = {Integer.parseInt(elementArray[1]), Integer.parseInt(elementArray[2])};
            instruments.put(elementArray[0], valueArray);
        }
    }

    public boolean changeQuanity(Message message) {
        String instrument = message.getSecurityID();
        int quantity = message.getNumShares();
        int price = message.getPrice();
        int side = message.getSide();

        if (!instruments.containsKey(instrument)) {
            return false;
        }

        Integer[] currentInstrumentValue = instruments.get(instrument);
        Integer[] newInstrumentValue = new Integer[2];
        if (side == 1 && quantity <= currentInstrumentValue[0] && price == currentInstrumentValue[1]) {
            newInstrumentValue[0] = currentInstrumentValue[0] - quantity;
            newInstrumentValue[1] = currentInstrumentValue[1];
            instruments.replace(instrument, newInstrumentValue);
        }
        else if (side == 2 && price == currentInstrumentValue[1]) {
            newInstrumentValue[0] = currentInstrumentValue[0] + quantity;
            newInstrumentValue[1] = currentInstrumentValue[1];
            instruments.replace(instrument, newInstrumentValue);
        }
        else
            return false;
        return true;
    }

    public void printInstruments() {
        System.out.println("MARKET INVENTORY:");
        for (String i : instruments.keySet()) {
            System.out.println("    Instrument: " + i + ", Quantity: " + instruments.get(i)[0] + ", Price: " + instruments.get(i)[1]);
        }
    }

    public HashMap<String, Integer[]> getInstruments() {
        return instruments;
    }

}