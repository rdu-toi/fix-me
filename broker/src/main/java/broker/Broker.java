package broker;

import broker.BrokerHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Broker {
    public static void main(String[] args) {
        try {
            BrokerHandler brokerHandler = new BrokerHandler();
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String line = null;
            while ((line = br.readLine()) != null) {
                System.out.println("Type 'exit' to quit.");
                if (line.equals("exit"))
                    break;
            }
            brokerHandler.exit();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}