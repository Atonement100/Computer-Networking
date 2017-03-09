import java.util.concurrent.ThreadLocalRandom;

public enum networkAction {
    PASS,
    CORRUPT,
    DROP;

    static public networkAction generateNetworkAction(){
        int randomNum = ThreadLocalRandom.current().nextInt(0, 4); //exclusive of upper
        switch (randomNum){
            case 0:
            case 1: return PASS; //50%
            case 2: return CORRUPT; //25%
            case 3: return DROP; //25%
            default: return PASS; // Should never default
        }
    }
}
