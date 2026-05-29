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
import java.util.HashMap;
import java.util.Map;
import java.sql.Array;

public class TesterClass
{
    
    String currChars;
    String predictedChars;
    ArrayList<Float>[] modelOutputs;

    private NeuralNet neuralNet = new NeuralNet();

    public void testChatbot(int predictLength, String prompt) throws IOException
    {
        

        
        // Set the initial currChars
        if (prompt.length() >= predictLength)
        {
            currChars = prompt.substring(prompt.length()-10, prompt.length());
        } else
        {
            System.out.println("Type a longer prompt");
        }
        
        // Print the orginal prompt. Makes the output look cleaner
        System.out.print(prompt);
        
        Object[] data = readDatabase();

        System.out.println("data obtained");

        Map<Integer, Float[][]> modelWeights = (Map<Integer, Float[][]>) data[0];
        Map<Integer, Float[]> modelBiases = (Map<Integer, Float[]>) data[1];
        float[] tokens = FloatTofloat((Float[]) data[2]);

        System.out.println("data seperated");
        System.out.println(Arrays.toString(tokens));



        // for (int predictions = 0; predictions < 100; predictions++)
        // {
        //     modelOutputs = NeuralNet.calculateModel();
        // }

    }
    
    public Object[] readDatabase()
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
        ResultSet rs = pstmt.executeQuery(sql)) {
            
            while(rs.next())
            {
                weights.put(rs.getInt("key"), (Float[][]) sqlArrayToObjectArray(rs.getArray("value")));
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
        ResultSet rs = pstmt.executeQuery(sql)) {
            
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
        ResultSet rs = pstmt.executeQuery(sql)) {
            
            while(rs.next())
            {
                String[] tokenArray = ((String[]) rs.getArray("token").getArray());

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

        Object[] output = new Object[3];
        output[0] = weights;
        output[1] = biases;
        output[2] = (Float[]) tokens.toArray();
        return output;

    }

    public Object[][] sqlArrayToObjectArray(java.sql.Array Array) throws SQLException
    {
        Object[] objectArray = (Object[]) Array.getArray();

        int arraySize = objectArray.length;

        Object[][] outputArray = new Object[arraySize][];

        for (int i = 0; i < arraySize; i++)
        {
            outputArray[i] = ((Object[]) ((java.sql.Array) objectArray[i]).getArray());
        }

        return outputArray;
    }

    public float[] FloatTofloat(Float[] input)
    {
        int inputSize = input.length;
        float[] output = new float[inputSize];

        for (int i = 0; i < inputSize; i++)
        {
            output[i] = input[i];
        }

        return output;
    }
}