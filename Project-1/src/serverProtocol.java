public class serverProtocol {
    private static int BAD_OP = 0;
    private static int TOO_FEW_INPUTS = 1;
    private static int TOO_MANY_INPUTS = 2;
    private static int INVALID_INPUTS = 3;
    private static int EXIT = 4;


    public String processInput(String input){
        boolean[] errorFlags = new boolean[5]; //defaults to all false

        String output = "server error";
        String[] tokens = input.split(" ");
        int[] values = new int[tokens.length - 1];

        for (int index = 1; index < tokens.length; index++){
            try {
                values[index - 1] = Integer.parseInt(tokens[index]);
            }
            catch (NumberFormatException ex) {
                errorFlags[INVALID_INPUTS] = true;
            }
        }

        if (values.length < 2){
            errorFlags[TOO_FEW_INPUTS] = true;
        }
        else if (values.length > 4){
            errorFlags[TOO_MANY_INPUTS] = true;
        }

        switch(tokens[0]){
            case "add":
                if (!errorFlags[INVALID_INPUTS]){
                    int sum = 0;
                    for (int val : values){
                        sum += val;
                    }
                    output = sum + "";
                }
                break;
            case "subtract":
                if (!errorFlags[INVALID_INPUTS]){
                    int sum = values[0];
                    for (int index = 1; index < values.length; index++){
                        sum -= values[index];
                    }
                    output = sum + "";
                }
                break;
            case "multiply":
                if (!errorFlags[INVALID_INPUTS]){
                    int product = 1;
                    for (int val : values){
                        product *= val;
                    }
                    output = product + "";
                }
                break;
            case "bye":
            case "terminate":
                errorFlags[EXIT] = true;
                return "-5";  //Override standard output procedure since -5 has the greatest ordinal value
            default:
                //error
                errorFlags[BAD_OP] = true; //Incorrect operations command
                break;
        }

        //Instead of setting output earlier, just set error flags then process them here later, since they have clear precedence, except for -5, "exit".
        for (int index = 0; index < errorFlags.length; index++){
            if (errorFlags[index]){
                output = "-" + (index + 1);
                break;
            }
        }

        return output;
    }
}
