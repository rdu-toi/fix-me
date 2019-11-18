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
                if (line.equals("exit"))
                    break;
            }
            brokerHandler.exit();
            System.out.println("Client connection closed!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}