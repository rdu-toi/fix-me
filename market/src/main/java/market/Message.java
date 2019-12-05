package market;

public class Message {

    private boolean isValid = true;
    private int clientId = 0;
    private int ordType = 0;
    private int side = 0;
    private String securityID = null;
    private int numShares = 0;
    private int exDestination = 0;
    private int price = 0;
    private String checkSum = null;

    public Message(String data) {
        try {
            String[] messageArray = data.split("\\|");
            for (String message : messageArray) {
                if (message.contains("109=")) {                                                                  // ClientId: 109=Int
                    int value = Integer.parseInt(message.substring(message.indexOf("109=") + 4));
                    this.setClientId(value);
                } else if (message.contains("40=")) {                                                                   // OrdType: 40=Int [1=Market]
                    int value = Integer.parseInt(message.substring(message.indexOf("40=") + 3));
                    if (value != 1)
                        this.setValid(false);
                    this.setOrdType(value);
            } else if (message.contains("54=")) {                                                                       // Side(Buy or Sell): 54=Int [Buy=1, Sell=2]
                    int value = Integer.parseInt(message.substring(message.indexOf("54=") + 3));
                    this.setSide(value);
                } else if (message.contains("48=")) {                                                                   // SecurityID(Instrument): 48=String
                    String value = message.substring(message.indexOf("48=") + 3);
                    this.setSecurityID(value);
                } else if (message.contains("53=")) {                                                                   // NumShares: 53=Int
                    int value = Integer.parseInt(message.substring(message.indexOf("53=") + 3));
                    if (value < 0)
                        this.setValid(false);
                    this.setNumShares(value);
                } else if (message.contains("100=")) {                                                                  // ExDestination: 100=Int
                    int value = Integer.parseInt(message.substring(message.indexOf("100=") + 4));
                    this.setExDestination(value);
                } else if (message.contains("44=")) {                                                                   // Price: 44=Int
                    int value = Integer.parseInt(message.substring(message.indexOf("44=") + 3));
                    this.setPrice(value);
                } else if (message.contains("10=")) {                                                                   // CheckSum: 10=Int
                    String value = message.substring(message.indexOf("10=") + 3);
                    this.setSecurityID(value);
                }
            }
        } catch (NumberFormatException | NullPointerException nfe) {
            System.out.println(nfe);
            this.setValid(false);
            return;
        }
    }

    public boolean checkValidity() {
        if (this.isValid() == false)
            return false;
        else if (this.securityID == null)
            return false;
        else if (this.clientId <= 0)
            return false;
        else if (this.ordType != 1)
            return false;
        else if (this.side <= 0 || this.side >= 3)
            return false;
        else if (this.numShares <= 0)
            return false;
        else if (this.exDestination <= 0)
            return false;
        else if (this.price <= 0)
            return false;
        return true;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean isValid) {
        this.isValid = isValid;
    }

    public String getSecurityID() {
        return securityID;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        if (this.price == 0)
            this.price = price;
    }

    public int getExDestination() {
        return exDestination;
    }

    public void setExDestination(int exDestination) {
        if (this.exDestination == 0)
            this.exDestination = exDestination;
    }

    public int getNumShares() {
        return numShares;
    }

    public void setNumShares(int numShares) {
        if (this.numShares == 0)
            this.numShares = numShares;
    }

    public int getSide() {
        return side;
    }

    public void setSide(int side) {
        if (this.side == 0)
            this.side = side;
    }

    public int getOrdType() {
        return ordType;
    }

    public void setOrdType(int ordType) {
        if (this.ordType == 0)
            this.ordType = ordType;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        if (this.clientId == 0)
            this.clientId = clientId;
    }

    public void setSecurityID(String securityID) {
        if (this.securityID == null)
		    this.securityID = securityID;
    }

    public String getChecksum() {
        return checkSum;
    }

    public void setChecksum(String checkSum) {
        if (this.checkSum == null)
            this.checkSum = checkSum;
    }

}