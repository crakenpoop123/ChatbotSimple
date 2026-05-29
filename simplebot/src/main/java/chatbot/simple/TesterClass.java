package chatbot.simple;

/**
 * Class to test the Chatbot
 *
 * @author Clark Gerrard
 * @version 14/05/26
 */

// Imports
import java.io.IOException;
import java.io.Console;

// SQL
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.sql.Array;

public class TesterClass
{
    
    static String currChars;
    static String predictedChars;
    static ArrayList<Float>[] modelOutputs;
    static String[] tokenisedPrompt;
    static short maxCorrelationDepth = 10;
    static String[] currentStrings = new String[maxCorrelationDepth];

    private NeuralNet neuralNet = new NeuralNet();
    

    public static void testChatbot(int predictLength, String prompt) throws IOException
    {
        

        
        // Set the initial currChars
        if (prompt.length() >= predictLength)
        {
            currChars = prompt.substring(prompt.length()-10, prompt.length());
        } else
        {
            System.out.println("Type a longer prompt");
        }
        
        Object[] data = readDatabase();

        System.out.println("data obtained");

        Map<Integer, float[][]> modelWeights = new HashMap<>();
        ((Map<Integer, Float[][]>)data[0]).forEach((key, value) -> {
            modelWeights.put(key, Float2Tofloat2(value));
        });
        Map<Integer, float[]> modelBiases = new HashMap<>();
        ((Map<Integer, Float[]>)data[1]).forEach((key, value) -> {
            modelBiases.put(key, FloatTofloat(value));
        });
        String[] tokens = ObjectToString((Object[]) data[2]);
        ArrayList<Float> output = new ArrayList<>();

        System.out.println("data seperated");
        System.out.println("tokens length: " + tokens.length);
        System.out.println("tokens: " + Arrays.toString(tokens));
        System.out.println("modelBiases.get(3): " + Arrays.toString(modelBiases.get(3)));
        System.out.println("modelBiases.size(): " + modelBiases.size());

        
        for (int predictions = 0; predictions < 100; predictions++)
        {
            output.clear();
            int promptLength = prompt.length();
            float[] tokenisedFloats = new float[maxCorrelationDepth];
            for (int currentString = 0; currentString < maxCorrelationDepth; currentString++)
            {
                currentStrings[currentString] = prompt.substring(promptLength - maxCorrelationDepth, promptLength);
                // System.out.println("currentStrings[" + currentString + "]: " + currentStrings[currentString]);
                
                tokenisedFloats[currentString] += Arrays.asList(tokens).indexOf((Object)currentStrings[currentString]);
            }
            // tokenisedPrompt = ObjectToFloat(NeuralNet.tokenise(tokens[0].length(), prompt.substring(prompt.length() - modelWeights.get(0).length, prompt.length())));

            for (int inputValue = 0; inputValue < maxCorrelationDepth; inputValue++)
            {
                output.add(tokenisedFloats[inputValue]);
            }

            modelOutputs = NeuralNet.calculateModel(output, modelWeights, modelBiases);
            String token = tokens[modelOutputs[modelOutputs.length - 1].indexOf(Collections.max(modelOutputs[modelOutputs.length - 1]))];
            prompt += token;
            System.out.print(token);
        }

    }
    
    public static Object[] readDatabase()
    {

        Map<Integer, Float[][]> weights = new HashMap<>();
        Map<Integer, Float[]> biases = new HashMap<>();
        ArrayList<String> tokens = new ArrayList<>();

        Console console = System.console();

        if (console == null)
        {
            System.out.println("Failed. Please provide a valid console");
        }
        
        char[] password = console.readPassword("enter password for user postgres: ");

        // SQL reading
        String url = "jdbc:postgresql://localhost:5432/Model";

        System.out.println("after url");

        String sql = "SELECT key, value FROM weights";

        System.out.println("sql: " + sql);

        try (Connection conn = DriverManager.getConnection(url, "postgres", new String(password));
        PreparedStatement pstmt = conn.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery()) {
            
            while(rs.next())
            {
                weights.put(rs.getInt("key"), sqlArrayToObjectArray(rs.getArray("value")));
            }

        } catch (SQLException e)
        {
            System.err.println(e.getMessage());
            System.out.println("in catch");
        }

        sql = "SELECT key, value FROM biases";

        System.out.println("sql: " + sql);

        try (Connection conn = DriverManager.getConnection(url, "postgres", new String(password));
        PreparedStatement pstmt = conn.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery()) {
            
            while(rs.next())
            {
                biases.put(rs.getInt("key"), (Float[]) rs.getArray("value").getArray());
            }

        } catch (SQLException e)
        {
            System.err.println(e.getMessage());
            System.out.println("in catch");
        }
        


        sql = "SELECT token FROM tokens";

        System.out.println("sql: " + sql);

        try (Connection conn = DriverManager.getConnection(url, "postgres", new String(password));
        PreparedStatement pstmt = conn.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery()) {
            
            while(rs.next())
            {
                String[] tokenArray = ((String[]) rs.getArray("token").getArray());

                System.out.println("tokenArray: " + tokenArray);

                for (int i = 0; i < tokenArray.length; i++)
                {
                    tokens.add(tokenArray[i]);
                }
            }

        } catch (SQLException e)
        {
            System.err.println(e.getMessage());
            System.out.println("in catch");
        }

        System.out.println("tokens in readDB: " + tokens.toString());

        Object[] output = new Object[3];
        output[0] = weights;
        output[1] = biases;
        output[2] = tokens.toArray();
        return output;

    }

    public static Float[][] sqlArrayToObjectArray(java.sql.Array Array) throws SQLException
    {
        Float[][] objectArray = (Float[][]) Array.getArray();

        int arraySize = objectArray.length;

        Float[][] outputArray = new Float[arraySize][];

        for (int i = 0; i < arraySize; i++)
        {
            outputArray[i] = objectArray[i];
        }

        return outputArray;
    }

    public static float[] FloatTofloat(Float[] input)
    {
        int inputLength = input.length;
        float[] output = new float[inputLength];

        for (int i = 0; i < inputLength; i++)
        {
            output[i] = input[i];
        }

        return output;
    }

    public static float[][] Float2Tofloat2(Float[][] input)
    {
        int inputLength = input.length;
        float[][] output = new float[inputLength][];

        for (int i = 0; i < inputLength; i++)
        {
            output[i] = FloatTofloat(input[i]);
        }

        return output;
    }

    public static String[] ObjectToString(Object[] input)
    {
        int inputSize = input.length;
        String[] output = new String[inputSize];

        for (int i = 0; i < inputSize; i++)
        {
            output[i] = (String) input[i];
        }

        return output;
    }

    public static Float[] ObjectToFloat(Object[] input)
    {
        int inputSize = input.length;
        Float[] output = new Float[inputSize];

        for (int i = 0; i < inputSize; i++)
        {
            output[i] = (Float) input[i];
        }

        return output;
    }
}