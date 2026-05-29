package chatbot.simple;

/**
 * Class to handle all the direct interactions with the user
 *
 * @author Clark Gerrard
 * @version 14/05/26
 */
// Imports
import java.util.Scanner;

public class UserInterface
{
    // Setup variables
    static Scanner scanner = new Scanner(System.in);
    private static TrainerClass Train = new TrainerClass();
    private static TesterClass Test = new TesterClass();
    
    static boolean exitValue = false;
    static int roundType = 2;
    static String prompt;
    static int predictLength;
    

    public static void main(String[] args)
    {
        // Remnant of the training process. Left in so you can see how I trained the chatbot.
        // I don't want the user to train the chatbot so I commented it all out
        
        // Inform the user on whether to train or test the AI
        System.out.println("If this is a training round, type 1");
        System.out.println("If this is a testing round, type 2");
        System.out.print("Enter 1 or 2: ");
        // Query the user on whether to train or test the AI
         while(exitValue == false) {
             roundType = Integer.parseInt(queryUser(0));
             switch (roundType){ // Ensure the user inputs a correct value
                case 1:
                    exitValue = true;
                case 2:
                    exitValue = true;
            }
            if (exitValue == false) {
                System.out.println("Try again!");
            }
        }
        System.out.println("Success! round type is "+roundType);
        
        
        // Next, we either test or train the chatbot, depending on the users choice earlier.
        if (roundType == 1){
            try
            {
                Train.trainChatbot();
            }
            catch (java.io.IOException ioe)
            {
                ioe.printStackTrace();
                System.out.println("Congrats, you somehow screwed up my code :(");
                // You won't be able to screw up my code >:(
            }
        }
        else if (roundType == 2){
            // Tell the user what we are querying
            System.out.println("Please input a prompt for the chatbot: ");
            
            // Get a prompt form the user
            prompt = queryUser(1);
            
            // Test the chatbot using the predictValue and the prompt
            try
           {
               Test.testChatbot(predictLength, prompt);
           }
           catch (java.io.IOException ioe)
           {
               ioe.printStackTrace();
               System.out.println("Congrats, you somehow screwed up my code :(");
               // You won't be able to screw up my code >:)
           }
        }
        
    }
    
    public static String queryUser(int dataType) {
        // Setup some variables
        String queriedValue;
        boolean exitValue = false;
        short count = 0;
        
        while (exitValue == false){
            
            // Print "Try again!" if this loop has been iterated through more than once
            if(count > 0){
                System.out.println("Try again!");
            }
            
            queriedValue = scanner.nextLine(); // Ask user for an input
            
            try {
                // Use a switch to ensure the user inputted the correct data type
                switch(dataType){
                    case 0: // attempt to converet the queried value into an integer
                        int outputInt = Integer.parseInt(queriedValue);
                        //System.out.println(outputInt);
                        exitValue = true;
                        return queriedValue;
                    case 1: // This is for strings, so we can just return the value
                        exitValue = true;
                        return queriedValue;
                }
            } catch(Exception e){
                System.out.println(e);
            }
            count++;
        }
        return "ERROR 1"; // Error if it breaks out of the while loop
    }
}