package chatbot.simple;

import java.io.Console;

/**
 * Class to handle code for training the chatbot
 *
 * @author Clark Gerrard
 * @version 14/05/26
 */

// Imports: 
// import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
// import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
// import java.util.stream.Stream;
import java.util.HashMap;
import java.util.Map;
import java.io.Console;

// SQL
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Array;


public class TrainerClass
{
    private NeuralNet neuralNet = new NeuralNet();
    public void trainChatbot() throws IOException
    {
        // Set up the file paths using relative paths
        final Path inputPath = Paths.get("resources/Dataset.txt");
       
        //Convert the input .txt file to a string and do some preprocessing
        final String inputString = Files.readString(inputPath).replaceAll("\r\n|\r|\n|\"|`|~|!|@|#|$|%|^|&|(|)|-|_|=||[|]|;|:|'|,|<|.|>|/", "")
        .replace("|", "").replace("*", "").replace("+", "").replace("{", "").replace("}", "").replace("?", "").toLowerCase();
        

        final short maxCorrelationDepth = 10;
        
        // Iterate through the dataset and find all unique tokens
        
        int predictLength = 1;

        Object[] uniqueTokens = neuralNet.tokenise(predictLength, inputString);
        
        trainModel(predictLength, uniqueTokens, 1000, maxCorrelationDepth);
        
    }
    
    public void trainModel(int predictLength, Object[] tokens, int epochs, int maxCorrelationDepth) throws IOException
    {
        // Setup the variables
        final Path inputPath = Paths.get("resources/Dataset2.txt");   
        final String inputString = Files.readString(inputPath).replaceAll("\\r\\n|\\r|\\n|\"|`|~|!|@|#|$|%|^|&|(|)|-|_|=||[|]|;|:|'|,|<|.|>|/", "")
        .replace("|", "").replace("*", "").replace("+", "").replace("{", "").replace("}", "").replace("?", "").toLowerCase();
        String[] currentStrings = new String[maxCorrelationDepth];
        
        // Initialise the model
        Map<Integer, float[][]> modelWeights = new HashMap<>();
        Map<Integer, float[]> modelBiases = new HashMap<>();
        final short hiddenLayers = 5;
        final int hiddenLayerWidth = maxCorrelationDepth * 3;
        Float[][] oneHot = new Float[maxCorrelationDepth][];
        ArrayList<Float> output = new ArrayList<Float>();
        
        // Used to calculate the likelihood of a key appearing:
        int tokenSize = tokens.length;
        
        // Used to improve the model
        int backpropSampleSize = 1000;
        ArrayList<Float>[][] backpropOutputs = new ArrayList[backpropSampleSize][hiddenLayers + 2];



        // Initiate the model
        Object[] initiatedModel = initModel(modelWeights, modelBiases, maxCorrelationDepth, hiddenLayers, tokenSize, hiddenLayerWidth);
        ((Map<Integer, float[][]>)initiatedModel[0]).forEach((key, value) -> {
            modelWeights.put(key, value);
        });

        ((Map<Integer, float[]>)initiatedModel[1]).forEach((key, value) -> {
            modelBiases.put(key, value);
        });


        float[][] targets = new float[backpropSampleSize][modelBiases.get(hiddenLayers + 1).length];
        
        // while(backpropSampleSize > 0)
        // {
        //     System.out.println(modelBiases.keySet());
        // }
        // Train the ai for many generations
        for(int epoch = 0; epoch < epochs; epoch++)
        {
            // System.out.println(epoch);
            for (int i = 1; i < inputString.length() / (maxCorrelationDepth * predictLength) - 2; i++) // inputString.length() / (maxCorrelationDepth * predictLength) - 1
            {
                // Clear the previous output data
                output.clear();

                // Partition the relevant tokens
                
                // float[] input = new float[maxCorrelationDepth];
                
                for (int currentString = 0; currentString < maxCorrelationDepth; currentString++)
                {
                    currentStrings[currentString] = inputString.substring(i + predictLength * currentString, i + (currentString + 1) * predictLength);
                    // System.out.println("currentStrings[" + currentString + "]: " + currentStrings[currentString]);
                    
                    // input[currentString] += Arrays.asList(tokens).indexOf((Object)currentStrings[currentString]);
                }
                // Convert the input array into a form usable by the calculateModel method
                for (int inputValue = 0; inputValue < maxCorrelationDepth; inputValue++)
                {
                    // output.add(input[inputValue]);

                    oneHot[inputValue] = NeuralNet.oneHot(currentStrings[inputValue], NeuralNet.ObjectToString(tokens));
                }

                int oneHotLength = oneHot.length;
                for (int tokenOneHot = 0; tokenOneHot < oneHotLength; tokenOneHot++)
                {
                    for (int index = 0; index < tokenSize; index++)
                    {
                        output.add(oneHot[tokenOneHot][index]);
                    }
                }

                // Calculate the output for the model
                backpropOutputs[(i-1)%backpropSampleSize] = neuralNet.calculateModel(output, modelWeights, modelBiases);
                // System.out.println("backPropOutputs: " + Arrays.toString(backpropOutputs[(i-1)%backpropSampleSize]));

                // Calculate the preferred output for the model
                String target = inputString.substring(i + predictLength * 10, i + 11 * predictLength);
                
                // System.out.println(modelBiases.keySet());
                // System.out.print("prevTokens: ");
                for (int currTarget = 0; currTarget < modelBiases.get(hiddenLayers + 1).length; currTarget++)
                {
                    // System.out.print(targets[(i-1)%backpropSampleSize][currTarget]);
                    if (currTarget == Arrays.asList(tokens).indexOf((Object)target))
                    {
                        targets[(i-1)%backpropSampleSize][currTarget] = 1;
                    } else
                    {
                        targets[(i-1)%backpropSampleSize][currTarget] = 0;
                    }
                }
                // System.out.println();
                // System.out.println("i: " + i);
                if ((i-1)%backpropSampleSize == 0 && i != 1)
                {

                    // System.out.println("backpropOutputs.length: " + backpropOutputs.length);

                    // for (int sample = 0; sample < backpropOutputs.length; sample++)
                    // {
                    //     System.out.println("sample: " + sample);
                    //     System.out.println("backpropOutputs[sample].length: " + backpropOutputs[sample].length);
                    //     System.out.println("backpropOutputs[sample]: " + backpropOutputs[sample]);
                    //     for (int activation = 0; activation < backpropOutputs[sample].length; activation++)
                    //     {
                    //         System.out.println("activation " + activation + ": " + backpropOutputs[sample][activation].toString());
                    //     }
                    // }

                    // modelWeights.forEach((key, value) ->{
                    //     System.out.println("modelWeights value length" + key + ": " + value.length);
                    // });

                    Object[] adjustedModel = NeuralNet.tweakModel(modelBiases, modelWeights, backpropOutputs, targets, 0.01f);
                        
                    ((HashMap<Integer, float[][]>) adjustedModel[0]).forEach((key, value) ->{
                        if (!modelWeights.get(key).equals(value))
                        {
                            System.out.println("modelWeights " + key + ": " + Arrays.toString(modelWeights.get(key)));
                            System.out.println("value " + key + ": " + Arrays.toString(value));
                            System.out.println("Model Adjustment Error!");
                        }

                        modelWeights.put(key, value);
                    });

                    ((HashMap<Integer, float[]>) adjustedModel[1]).forEach((key, value) ->{
                        if (!modelBiases.get(key).equals(value))
                        {
                            System.out.println("modelBiases " + key + ": " + Arrays.toString(modelBiases.get(key)));
                            System.out.println("value " + key + ": " + Arrays.toString(value));
                            System.out.println("Model Adjustment Error!");
                        }

                        modelBiases.put(key, value);
                    });

                    System.out.println("MSE: " + adjustedModel[2]);
                    System.out.println("epoch: " + epoch);
                }

            }    
            
        }
        
        uploadToDB(modelWeights, modelBiases, tokens);

    }
    
    public void uploadToDB(Map<Integer, float[][]>modelWeights, Map<Integer, float[]> modelBiases, Object[] uniqueTokens)
    {
        

        Console console = System.console();

        if (console == null)
        {
            System.out.println("Failed. Please provide a valid console");
        }

        char[] password = console.readPassword("enter password for user postgres: ");

        // SQL insertion
        String url = "jdbc:postgresql://localhost:5432/Model";

        System.out.println("after url");

        String sql1 = "TRUNCATE TABLE weights";
        
        System.out.println("sql: " + sql1);
        try (Connection conn = DriverManager.getConnection(url, "postgres", new String(password));
        PreparedStatement pstmt = conn.prepareStatement(sql1)) {
            
            System.out.println("in try");

            pstmt.executeUpdate();
        } catch (SQLException e)
        {
            System.err.println(e.getMessage());
            System.out.println("in catch");
        }
        
        modelWeights.forEach((key, value) ->{

            String sql2 = "INSERT INTO weights(key,value) VALUES(?,?)" + 
            "ON CONFLICT (key) DO UPDATE SET value = EXCLUDED.value";
            
            System.out.println("in .forEach()");
            System.out.println("sql: " + sql2);

            try (Connection conn = DriverManager.getConnection(url, "postgres", new String(password));
            PreparedStatement pstmt = conn.prepareStatement(sql2)) {

                System.out.println("in try");
                
                Array sqlArray = conn.createArrayOf("float4", value);

                System.out.println("after array");

                pstmt.setInt(1, key);
                pstmt.setArray(2, sqlArray);

                System.out.println("after pstmt.set");

                int rowsInserted = pstmt.executeUpdate();
                System.out.println(rowsInserted + " rows inserted.");

            } catch (SQLException e)
            {
                System.err.println(e.getMessage());
                System.out.println("in catch");
            }

        });

        String sql3 = "TRUNCATE TABLE biases";
        
        System.out.println("sql: " + sql3);
        try (Connection conn = DriverManager.getConnection(url, "postgres", new String(password));
        PreparedStatement pstmt = conn.prepareStatement(sql3)) {
            
            System.out.println("in try");

            pstmt.executeUpdate();
        } catch (SQLException e)
        {
            System.err.println(e.getMessage());
            System.out.println("in catch");
        }
        
        modelBiases.forEach((key, value) ->{

            String sql4 = "INSERT INTO biases(key,value) VALUES(?,?)" + 
            "ON CONFLICT (key) DO UPDATE SET value = EXCLUDED.value";
            
            System.out.println("in .forEach()");
            System.out.println("sql: " + sql4);

            try (Connection conn = DriverManager.getConnection(url, "postgres", new String(password));
            PreparedStatement pstmt = conn.prepareStatement(sql4)) {

                System.out.println("in try");

                Float[] newValue = new Float[value.length];
                
                for (int i = 0; i < value.length; i++)
                {
                    newValue[i] = value[i];
                }
                
                Array sqlArray = conn.createArrayOf("FLOAT", newValue);

                System.out.println("after array");

                pstmt.setInt(1, key);
                pstmt.setArray(2, sqlArray);

                System.out.println("after pstmt.set");

                int rowsInserted = pstmt.executeUpdate();
                System.out.println(rowsInserted + " rows inserted.");

            } catch (SQLException e)
            {
                System.err.println(e.getMessage());
                System.out.println("in catch");
            }

        });


        
        String sql5 = "TRUNCATE TABLE tokens";
        
        System.out.println("sql: " + sql5);
        try (Connection conn = DriverManager.getConnection(url, "postgres", new String(password));
        PreparedStatement pstmt = conn.prepareStatement(sql5)) {
            
            System.out.println("in try");

            pstmt.executeUpdate();
        } catch (SQLException e)
        {
            System.err.println(e.getMessage());
            System.out.println("in catch");
        }


        String sql6 = "INSERT INTO tokens(token) VALUES(?)";

        System.out.println("sql: " + sql6);
        try (Connection conn = DriverManager.getConnection(url, "postgres", new String(password));
        PreparedStatement pstmt = conn.prepareStatement(sql6)) {
            
            System.out.println("in try");

            pstmt.setArray(1, conn.createArrayOf("TEXT", uniqueTokens));

            System.out.println("after set");

            int rowsInserted = pstmt.executeUpdate();
            System.out.println(rowsInserted + " rows inserted.");
        } catch (SQLException e)
        {
            System.err.println(e.getMessage());
            System.out.println("in catch");
        }

    }

    public Object[] initModel(Map<Integer, float[][]> modelWeights, Map<Integer, float[]> modelBiases, int maxCorrelationDepth, int hiddenLayers, int tokenSize, int hiddenLayerWidth)
    {
        // Setup the neural biases
        // Input/output layers:
        modelBiases.put(0, new float[maxCorrelationDepth * tokenSize]);
        modelBiases.put(hiddenLayers + 1, new float[tokenSize]);
        // Hidden layer:
        for (int layer = 0; layer < hiddenLayers; layer++)
        {
            modelBiases.put(layer + 1, new float[hiddenLayerWidth]);
        }
        // Randomise the neural biases:
        for (int neuronLayer = 0; neuronLayer < hiddenLayers + 2; neuronLayer++)
        {
            int neuronCount = modelBiases.get(neuronLayer).length;
            float[] randomBiases = new float[neuronCount];
            
            for (int neuron = 0; neuron < neuronCount; neuron++)
            {
                randomBiases[neuron] = (float)Math.random();
            }
            
            modelBiases.put(neuronLayer, randomBiases);
            // System.out.println(neuronLayer + ": " + Arrays.toString(randomBiases));    
        }
        


        // Setup the neural weights:
        for (int neuronLayer = 0; neuronLayer < hiddenLayers + 1; neuronLayer++) // hiddenLayers + 1 becuase the output shouldn't have weights coming out
        {
            modelWeights.put(neuronLayer, new float[modelBiases.get(neuronLayer).length][modelBiases.get(neuronLayer + 1).length]);
            // System.out.println("nextBiases: " + Arrays.toString(modelBiases.get(neuronLayer + 1)));
            // System.out.println("currLayer length: " + modelBiases.get(neuronLayer).length);
            // System.out.println("nextLayer length: " + modelBiases.get(neuronLayer + 1).length);
        }
        // modelWeights.forEach((key, value) ->{
        //     System.out.println("modelWeights value length" + key + ": " + value.length);
        // });
        // Randomise the neural weights:
        for (int neuronLayer = 0; neuronLayer < modelWeights.size(); neuronLayer++)
        {
            float[][] layerWeights = new float[modelBiases.get(neuronLayer).length][modelBiases.get(neuronLayer + 1).length];
            // System.out.print(neuronLayer + ": ");
            for (int neuron = 0; neuron < modelWeights.get(neuronLayer).length; neuron++)
            {
                float[] randomWeights = new float[modelWeights.get(neuronLayer)[neuron].length];
                
                for (int weight = 0; weight < randomWeights.length; weight++)
                {
                    randomWeights[weight] = (float)Math.random();
                    if (neuronLayer != 0)
                    {
                        randomWeights[weight] /= randomWeights.length;
                    }else
                    {
                        randomWeights[weight] /= (tokenSize / 2);
                    }
                }
                layerWeights[neuron] = randomWeights;
                // System.out.print(Arrays.toString(randomWeights));
            }
            modelWeights.put(neuronLayer, layerWeights);
            // System.out.println("\n");
        }

        // Return the modelWeights and modelBiases
        Object[] output = new Object[2];
        output[0] = modelWeights;
        output[1] = modelBiases;

        return output;
    }

}