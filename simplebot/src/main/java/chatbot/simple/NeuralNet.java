package chatbot.simple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Code for calculating Neural Network
 *
 * @author Clark Gerrad
 * @version 21/05/26
 */
public class NeuralNet
{
    
    public static float sigmoid(float z)
    {
        return (float)(1 / (1 + Math.exp(-z)));
    }
    
    public static ArrayList<Float>[] calculateModel(ArrayList<Float> output, Map<Integer, float[][]> weights, Map<Integer, float[]> biases)
    {
        int layers = biases.size();
        ArrayList<Float>[] activationList = new ArrayList[layers];
        activationList[0] = output;
        
        for (int layer = 0; layer < layers - 1; layer++)
        {
            output = new ArrayList<>(calculateLayer(output, weights.get(layer), biases.get(layer + 1)));
            activationList[layer + 1] = output;
        }
        return activationList;
    }

    public static Object[] tokenise(int predictLength, String inputString)
    {
        String currChar = "";
        ArrayList<String> uniqueTokens = new ArrayList<>();

        for (int i = 0; i < inputString.length(); i++)
        {
            currChar = String.valueOf(inputString.charAt(i));
            if (!uniqueTokens.contains(currChar))
            {
                uniqueTokens.add(currChar);
            }
        }

        return uniqueTokens.toArray();
    }

    public static ArrayList<Float> calculateLayer(ArrayList<Float> inputs, float[][] weights, float[] biases)
    {
        // System.out.println("input size: " + inputs.size());
        // System.out.println("biases length: " + biases.length);
        // System.out.println("weights length: " + weights.length);

        ArrayList<Float> outputs = new ArrayList<>();
        for (int bias = 0; bias < biases.length; bias++)
        {
            float biasTotal = 0;
            // System.out.println("bias: " + bias);
            for (int input = 0; input < inputs.size(); input++)
            {
                try {
                    biasTotal += inputs.get(input) * weights[input][bias];    
                } catch (Exception e) {
                    System.out.println("weights[input] length: " + weights[input - 1].length);
                    System.out.println("biases length: " + biases.length);
                    System.out.println("weights length: " + weights.length);
                    System.out.println("input size: " + inputs.size());
                }
            }
            outputs.add(sigmoid(biasTotal + biases[bias]));
            if (sigmoid(biasTotal + biases[bias]) > 1)
            {
                while (true) 
                {
                    System.out.println(sigmoid(biasTotal + biases[bias]));
                }
            }
        }
        return outputs;
    }

    public static Object[] tweakModel(Map<Integer, float[]> biases, Map<Integer, float[][]> weights, ArrayList<Float>[][] activations, float[][] targets, float stepSize)
    {
        Object[] changes = backPropIterations(biases, weights, activations, targets);

        // System.out.println(Arrays.toString(weights.get(0)[0]));

        HashMap<Integer, float[][]> weightGrad = new HashMap<>();
        HashMap<Integer, float[]> biasGrad = new HashMap<>();
        HashMap<Integer, float[][]> outputWeights = new HashMap<>();
        HashMap<Integer, float[]> outputBiases = new HashMap<>();

        ((HashMap<Integer, float[][]>) changes[0]).forEach((key, value) ->{
            weightGrad.put(key, value);
        });

        ((HashMap<Integer, float[]>) changes[1]).forEach((key, value) ->{
            biasGrad.put(key, value);
            // System.out.println("biasGrad " + key + ": " + Arrays.toString(value));
        });

        // Adjust weights
        for (Map.Entry<Integer, float[][]> entry : weightGrad.entrySet())
        {
            int layer = entry.getKey();
            float[][] currentWeightGrad = entry.getValue();
            float[][] weightValue = weights.get(layer);
            for (int i = 0; i < weightValue.length; i++)
            {
                for (int j = 0; j < weightValue[i].length; j++)
                {
                    weightValue[i][j] -= currentWeightGrad[i][j] * stepSize;
                }
            }
            outputWeights.put(layer, weightValue);
        }

        // Adjust biases
        for (Map.Entry<Integer, float[]> entry : biasGrad.entrySet())
        {
            int layer = entry.getKey();
            float[] currentBiasGrad = entry.getValue();
            float[] biasValue = biases.get(layer);
            for (int i = 0; i < biasValue.length; i++)
            {
                biasValue[i] -= currentBiasGrad[i] * stepSize;
            }
            outputBiases.put(layer, biasValue);
        }

        Object[] output = new Object[3];

        // Convert the activations into a form that findCost can understand
        int numSamples = activations.length;
        int outputLayerIndex = activations[0].length - 1;
        float[][] arrayActivations = new float[numSamples][];
        for (int s = 0; s < numSamples; s++)
        {
            arrayActivations[s] = NeuralNet.listToFloat(activations[s][outputLayerIndex]);
        }

        // for (int neuronLayer = 0; neuronLayer < biases.size(); neuronLayer++)
        // {
        //     int neuronCount = biases.get(neuronLayer).length;
        //     float[] randomBiases = new float[neuronCount];
            
        //     for (int neuron = 0; neuron < neuronCount; neuron++)
        //     {
        //         randomBiases[neuron] = (float)Math.random();
        //     }
            
        //     outputBiases.put(neuronLayer, randomBiases);
        //     // System.out.println(neuronLayer + ": " + Arrays.toString(randomBiases));    
        // }

        output[0] = outputWeights;
        output[1] = outputBiases;
        output[2] = findCost(arrayActivations, targets);

        return output;

    }

    public static Object[] backPropIterations(Map<Integer, float[]> biases, Map<Integer, float[][]> weights, ArrayList<Float>[][] activations, float[][] targets)
    {
        int iterations = targets.length;
        Map<Integer, float[]> biasGrad = new HashMap<>();
        Map<Integer, float[][]> weightGrad = new HashMap<>();

        for (int iteration = 0; iteration < iterations; iteration++)
        {
            Object[] gradient = backProp(biases, weights, activations[iteration], targets[iteration]);

            Map<Integer, float[][]> gradW = (HashMap<Integer, float[][]>) gradient[0];
            Map<Integer, float[]> gradB = (HashMap<Integer, float[]>) gradient[1];

            // Add the weight grads
            for (Map.Entry<Integer, float[][]> entries : gradW.entrySet())
            {
                int layer = entries.getKey();
                float[][] value = entries.getValue();
                float[][] total = weightGrad.get(layer);
                if (total == null)
                {
                    float[][] copy = new float[value.length][];
                    for (int i = 0; i < value.length; i++)
                    {
                        copy[i] = Arrays.copyOf(value[i], value[i].length);
                    }
                    weightGrad.put(layer, copy);
                } else
                {
                    for (int i = 0; i < total.length; i++)
                    {
                        for (int j = 0; j < total[i].length; j++)
                        {
                            total[i][j] += value[i][j];
                        }
                    }
                    weightGrad.put(layer, total);
                }
            }

            // Add bias grads
            for (Map.Entry<Integer, float[]> entries : gradB.entrySet())
            {
                int layer = entries.getKey();
                float[] value = entries.getValue();
                float[] total = biasGrad.get(layer);
                if (total == null)
                {
                    biasGrad.put(layer, Arrays.copyOf(value, value.length));
                } else
                {
                    for (int i = 0; i < total.length; i++)
                    {
                        total[i] += value[i];
                    }
                    biasGrad.put(layer, total);
                }
            }
        }

        // Average weightGrad maps
        for (Map.Entry<Integer, float[][]> e : weightGrad.entrySet())
        {
            int layer = e.getKey();
            float[][] currentGrad = e.getValue();
            for (int i = 0; i < currentGrad.length; i++)
            {
                for (int j = 0; j < currentGrad[i].length; j++)
                {
                    currentGrad[i][j] /= iterations;
                }
            }
            weightGrad.put(layer, currentGrad);
        }

        // Average biasGrad maps
        for (Map.Entry<Integer, float[]> e : biasGrad.entrySet())
        {
            int layer = e.getKey();
            float[] currentGrad = e.getValue();
            for (int i = 0; i < currentGrad.length; i++)
            {
                currentGrad[i] /= iterations;
            }

            biasGrad.put(layer, currentGrad);
            // System.out.println("backPropIt biasGrad " + layer + ": " + Arrays.toString(currentGrad));
        }

        // Package the Grads together
        Object[] output = new Object[2];

        output[0] = weightGrad;
        output[1] = biasGrad;

        return output;
    }

    public static Object[] backProp(Map<Integer, float[]> biases, Map<Integer, float[][]> weights, ArrayList<Float>[] activations, float[] target)
    {
        Map<Integer, float[][]> weightsGrad = new HashMap<>();
        Map<Integer, float[]> biasesGrad = new HashMap<>();

        int layers = activations.length;

        // compute z values for each layer 1 through layers - 1
        // Used to reduce redundant calculations
        float[][] zArray = new float[layers][];
        for (int layer = 1; layer < layers; layer++)
        {
            int size = activations[layer].size();
            zArray[layer] = new float[size];
            float[] prevActivation = listToFloat(activations[layer - 1]); // prev layer's activations
            float[][] currWeights = weights.get(layer - 1); // current weights
            float[] currBiases = biases.get(layer); // current biases
            float[][] inputWeights = switchFloat(currWeights); // neuron j is the terminal neuron for inputWeights[j]
            for (int j = 0; j < size; j++)
            {
                zArray[layer][j] = calculateWeightedSum(prevActivation, inputWeights[j], currBiases[j]);
            }
        }

        int L = layers - 1;

        // delta for output layer
        float[] delta = new float[activations[L].size()];
        for (int j = 0; j < delta.length; j++)
        {
            float out = activations[L].get(j); // output
            // System.out.println("IQRange: " + (IQRange(ObjectTofloat(activations[L].toArray()))*0.7 + 0.3));
            float delCostDelA = (float) (2 * (out - target[j]) * ((IQRange(ObjectTofloat(activations[L].toArray())))*0.7 + 0.3)); // Partial derivative of cost with respect to activation
            float sigmaPrime = sigmoid(zArray[L][j]) * (1 - sigmoid(zArray[L][j])); // derivative of sigmoid
            delta[j] = delCostDelA * sigmaPrime;
        }

        // Comput weight gradient
        float[][] weightGrad = new float[activations[L - 1].size()][activations[L].size()];
        for (int i = 0; i < activations[L - 1].size(); i++)
        {
            for (int j = 0; j < activations[L].size(); j++)
            {
                weightGrad[i][j] = delta[j] * activations[L - 1].get(i);
            }
        }
        weightsGrad.put(L - 1, weightGrad);
        biasesGrad.put(L, Arrays.copyOf(delta, delta.length)); // Fixes a weird bug with inconsistent sizing

        // backpropagate the other layers
        for (int layer = L - 1; layer > 0; layer--)
        {
            // compute delta for this layer
            float[] deltaPrev = new float[activations[layer].size()];
            float[][] backPropWeights = weights.get(layer); // Weights between layer and layer + 1
            for (int i = 0; i < activations[layer].size(); i++)
            {
                float sum = 0f;
                for (int j = 0; j < delta.length; j++)
                {
                    sum += backPropWeights[i][j] * delta[j];
                }
                float sigmaPrime = sigmoid(zArray[layer][i]) * (1 - sigmoid(zArray[layer][i]));
                deltaPrev[i] = sum * sigmaPrime;
            }

            // Weight gradients
            float[][] prevWeightGrads = new float[activations[layer - 1].size()][activations[layer].size()];
            for (int i = 0; i < activations[layer - 1].size(); i++)
            {
                for (int j = 0; j < activations[layer].size(); j++)
                {
                    prevWeightGrads[i][j] = deltaPrev[j] * activations[layer - 1].get(i);
                }
            }
            weightsGrad.put(layer - 1, prevWeightGrads);
            biasesGrad.put(layer, Arrays.copyOf(deltaPrev, deltaPrev.length)); // Again, fixes weird bug with array sizes

            // Move focus to previous layer
            delta = deltaPrev;
        }

        Object[] output = new Object[2];
        output[0] = weightsGrad;
        output[1] = biasesGrad;

        return output;
    }

    public static float findGradient(float z, float input, float output, float weight, float target, short gradType)
    { // No longer used
        // Del Cost over del input
        float costInputGrad = 2 * (output - target);

        // Del input over del z
        float inputZGrad = sigmoid(z) * (1 - sigmoid(z));

        float zGrad = 0;
        if (gradType == 0)
        {
            // Del z over del weight
            zGrad = input;
        } else if (gradType == 1)
        {
            // Del z over del bias
            zGrad = 1;
        } else 
        {
            // Del z over del input
            // Outputs the target for the previous neuron
            zGrad = weight;
        }
        // Del cost over del weight
        float costGrad = costInputGrad * inputZGrad * zGrad;

        // System.out.println("costGrad: " + costGrad);

        return costGrad;
    }

    public static float calculateWeightedSum(float[] inputs, float[] weights, float bias)
    {
        float sum = 0;
        for (int input = 0; input < inputs.length; input++)
        {
            sum += inputs[input] * weights[input];
        }
        sum += bias;

        return sum;
    }

    public static double findCost(float[][] output, float[][] target)
    {
        double cost = 0;
        int entries = 0;

        // System.out.println("cost output length: " + output.length);

        for (int prediction = 0; prediction < output.length; prediction++)
        {
            entries += output[prediction].length;
            for (int node = 0; node < output[prediction].length; node++)
            {
                cost += Math.pow(output[prediction][node] - target[prediction][node],  2);
            }
        }
        return cost / entries;
    }

    public static float[] listToFloat(ArrayList<Float> input)
    {
        float[] output = new float[input.size()];
        for (int item = 0; item < input.size(); item++)
        {
            output[item] = input.get(item);
        }
        return output;
    }

    public static float[][] switchFloat(float[][] input)
    {
        float[][] output = new float[input[0].length][input.length];
        for (int i = 0; i < input.length; i++)
        {
            for (int j = 0; j < input[i].length; j++)
            {
                output[j][i] = input[i][j];
            }
        }

        return output;
    }

    public static float IQRange(float[] input)
    {
        float[] sortedInput = input;
        Arrays.sort(sortedInput);

        int LQIndex = (int) Math.floor(sortedInput.length/4);
        int UQIndex = (int) Math.floor(3 * sortedInput.length/4);

        float IQRange = sortedInput[UQIndex] - sortedInput[LQIndex];

        return IQRange;

    }

    public static float[] ObjectTofloat(Object[] input)
    {
        int inputLength = input.length;
        float[] output = new float[inputLength];

        for (int i = 0; i < inputLength; i++)
        {
            output[i] = (float) input[i];
        }

        return output;
    }

}
