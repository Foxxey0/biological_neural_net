import java.util.ArrayList;
import java.util.Random;

public class Neuron {

    static int[] layerNextPositions = new int[22];

    double queuedVoltage; // unitless. voltage thats the sum of the signals, that is queued from all the presynaptic neurons. Upon the reciving stage, queued voltage will be added to current voltage, which has already been handles correctly to reset if it was over the threshold.
    double voltage; // unitless. current voltage.
    ArrayList<Integer> presynapticNeurons; // list of neuron IDs that feed into current neuron
    Synapse[] synapses; // list of weights, 1 weight for each other neuron in the neural net. maps to neuron IDs in the normal way. the downside is that theres a lot of extra weights (95%) that wont be used since theres no connection, so i might change that later.
    boolean inhibitory;
    int rank;
    int positionInLayer; // just for the render. this isnt needed for actually processing stuff. its as simple as a neuron connects to every neuron in the previous rank/layer.
    int ID;

    Neuron(int ID) { // recently added this ID parameter. now the neurons can know their own ID. useful.
        queuedVoltage = 0;
        voltage = Main.neuronRestingVoltage;
        presynapticNeurons = new ArrayList<Integer>();
        synapses = new Synapse[Main.neurons_amount]; // to keep things consistent, i wont subtract one. even though i dont want this neuron to have a connection to itself, I want to keep each neuron ID'ed by it's position in an array. So instead, i made it so each neuron skips its own ID during it's own summing, every tick.
        inhibitory = false;
        rank = 0;
        positionInLayer = 0;
        this.ID = ID;

        // set random rank so this neuron is in a "layer" from 0 - 21 (22 layers)
        if (ID >= Main.inputNeurons_startID && ID <= Main.inputNeurons_endID) { // input neuron is layer 0
            rank = 0;
        } else if (ID >= Main.outputNeurons_startID && ID <= Main.outputNeurons_endID) { // output neuron is layer 21
            rank = 21;
        } else { // hidden neuron can be any layer between
            int die = new Random().nextInt(20) +1; // 1-20
            rank = die;
        }

        // set position in layer
        positionInLayer = layerNextPositions[rank];
        layerNextPositions[rank] ++;

        // if this is a hidden neuron, 20% chance this neuron is inhibitory, not excitatory.
        if (ID >= Main.hiddenNeurons_startID && ID <= Main.hiddenNeurons_endID) {
            double die2 = new Random().nextDouble();
            if (die2 <= .20) {
                inhibitory = true;
            }
        }
    }

    public void initConnections() {
//        // init the connections (synapses). 5% hidden to hidden. all other connection rules too.
//        if (ID >= Main.inputNeurons_startID && ID <= Main.inputNeurons_endID) { // dont let input neurons get signals from other neurons. only from me.
//            // dont have any presynaptic neurons
//        } else {
//            for (int i = 0; i <= Main.neurons_amount -1; i++) { // i is other neuron (this code block will decide if it will be a presynaptic neuron)
//                if (i == ID) { // skip current neuron. we dont want it to be a presynaptic neuron to itself!
//                    continue;
//                }
//                if (i >= Main.outputNeurons_startID && i <= Main.outputNeurons_endID) { // skip output neurons. we do NOT want to use output neurons as presynaptic neurons at ALL.
//                    continue;
//                }
//                if (ID >= Main.outputNeurons_startID && ID <= Main.outputNeurons_endID) { // if current neuron is an output neuron, we do NOT want to use any input neurons as presynaptic neurons (because its too direct).
//                    if (i >= Main.inputNeurons_startID && i <= Main.inputNeurons_endID) {
//                        continue;
//                    }
//                }
//                // 5% chance that a connection will be made between hidden to hidden. otherwise, 100%. we want ALL input to hidden and ALL hidden to output.
//                if (ID >= Main.hiddenNeurons_startID && ID <= Main.hiddenNeurons_endID && i >= Main.hiddenNeurons_startID && i <= Main.hiddenNeurons_endID) { // both are hidden
//                    double die2 = new Random().nextDouble();
//                    if (die2 <= .05) {
//                        presynapticNeurons.add(i);
//                    }
//                } else {
//                    presynapticNeurons.add(i);
//                }
//            }
//        }

        // layers exist now, so we can just connect to layers behind
        if (ID >= Main.inputNeurons_startID && ID <= Main.inputNeurons_endID) { // first layer has no connections backwards.
            // dont have any presynaptic neurons
        } else {
            for (int i = 0; i <= Main.neurons_amount -1; i++) { // i is other neuron (this code block will decide if it will be a presynaptic neuron)
                if (Main.net[i].rank == rank -1) {
                    presynapticNeurons.add(i);
                }
            }
        }

        // randomize weights
//        for (int i = 0; i <= Main.neurons_amount -1; i++) {
////            presynapticNeuronWeights[i] = new Random().nextDouble() *(Main.maxSynapseStrength -Main.minSynapseStrength) +Main.minSynapseStrength;
//            presynapticNeuronWeights[i] = new Random().nextDouble() *(.05 -0) +0; // start with weaker weights
//        }
        for (Integer presynapticNeuronID : presynapticNeurons) { // only set the weights of the connections that exist. theres no reason to do non-existing connections.
//            presynapticNeuronWeights[presynapticNeuronID] = new Random().nextDouble() *(Main.maxSynapseStrength -Main.minSynapseStrength) +Main.minSynapseStrength;
            double max = (Main.net[presynapticNeuronID].inhibitory? .15 : .15);
//            if (new Random().nextDouble() <= .92) {
//                max = 0;
//            }
            synapses[presynapticNeuronID] = new Synapse();
            synapses[presynapticNeuronID].weight = new Random().nextDouble() *(max -0) +0; // start with weaker weights
        }
    }

}
