package market;

import market.MarketHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Market {

    public static void main(String[] args) {

        try {
            MarketHandler marketHandler = new MarketHandler();
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String line = null;
            while ((line = br.readLine()) != null) {
                System.out.println("Type 'exit' to quit.");
                if (line.equals("exit"))
                    break;
            }
            marketHandler.exit();
            System.out.println("Client connection closed!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}