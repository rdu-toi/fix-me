import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Instruments {

    private HashMap<String, String[]> instruments = new HashMap<String, String[]>();

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

        Random rand = new Random();
        int randNum = rand.nextInt(lines.size());
        String rawInstruments = lines.get(randNum);

        String[] instrumentsArray = rawInstruments.split("\\|");
        for (String element: instrumentsArray) {
            String[] elementArray = element.split(",");
            String[] valueArray = {elementArray[1], elementArray[2]};
            instruments.put(elementArray[0], valueArray);
        }
    }

    public HashMap<String, String[]> getInstruments() {
        return instruments;
    }

}