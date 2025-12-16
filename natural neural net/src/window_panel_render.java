import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class window_panel_render extends JPanel {

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (Main.net == null) {
            return;
        }

        // draw synapses
        for (int i = 0; i <= Main.neurons_amount -1; i++) { // i is current neuron
            if (Main.net[i] == null) {
                continue;
            }

            int[] neuronPos = findNeuronPos(i);
            int neuronX = neuronPos[0];
            int neuronY = neuronPos[1];

            for (int k = 0; k <= Main.neurons_amount -1; k++) { // k is possible presynaptic neuron
                if (Main.net[i].synapses[k] == null) {
                    // this synapse doesnt exist.
                } else {
                    ((Graphics2D)g).setStroke(new BasicStroke(1));
//                    g.setColor(new Color((int)(Main.net[i].synapses[k].trace /Main.synapseTraceMax *255), (int)(Main.net[i].synapses[k].weight /Main.maxSynapseStrength *255), (int)(Main.net[i].synapses[k].trace /Main.synapseTraceMax *255)));
//
                    // green. white when excited.
                    int red = (int)(Main.net[i].synapses[k].weight /Main.maxSynapseStrength *Main.net[i].synapses[k].trace /Main.synapseTraceMax *255);
                    int green = (int)(Main.net[i].synapses[k].weight /Main.maxSynapseStrength *255);
                    int blue = (int)(Main.net[i].synapses[k].weight /Main.maxSynapseStrength *Main.net[i].synapses[k].trace /Main.synapseTraceMax *255);

                    // white. green when excited.
//                    int red = (int)(Main.net[i].synapses[k].weight /Main.maxSynapseStrength *(Main.synapseTraceMax -(Main.net[i].synapses[k].trace /Main.synapseTraceMax)) *255);
//                    int green = (int)(Main.net[i].synapses[k].weight /Main.maxSynapseStrength *255);
//                    int blue = (int)(Main.net[i].synapses[k].weight /Main.maxSynapseStrength *(Main.synapseTraceMax -(Main.net[i].synapses[k].trace /Main.synapseTraceMax)) *255);
                    g.setColor(new Color(red, green, blue));

//                    if (Main.net[k].inhibitory) { // inhibitory
//                        g.setColor(new Color(255, (int)(Main.net[i].synapses[k].trace /Main.synapseTraceMax *255), (int)(Main.net[i].synapses[k].trace /Main.synapseTraceMax *255)));
//                    } else { // excitatory
//                        g.setColor(new Color((int)(Main.net[i].synapses[k].trace /Main.synapseTraceMax *255), (int)(Main.net[i].synapses[k].trace /Main.synapseTraceMax *255), 255));
//                    }

                    // draw synapse
                    int[] presynapticNeuronPos = findNeuronPos(k);

//                    if (!Main.net[i].synapses[k].draw) {
//                        continue;
//                    }

                    g.drawLine(neuronX +(Main.render_neuronSize /2), neuronY +(Main.render_neuronSize /2), presynapticNeuronPos[0] +(Main.render_neuronSize /2), presynapticNeuronPos[1] +(Main.render_neuronSize /2));
                }
            }
        }

        // draw neurons
        for (int i = 0; i <= Main.neurons_amount -1; i++) { // i is current neuron
            if (Main.net[i] == null) {
                continue;
            }

            int[] neuronPos = findNeuronPos(i);
            int neuronX = neuronPos[0];
            int neuronY = neuronPos[1];

            double min = 0;
            double max = 0;

            // clamp differently. normal ones are normal. output ones have way more range.
            if (i >= Main.outputNeurons_startID && i <= Main.outputNeurons_endID) { // output neuron
                min = -20; // hordcoded
                max = 20; // hordcoded
            } else {
                min = -Main.sendSignal_voltageThreshold;
                max = Main.sendSignal_voltageThreshold;
            }

            double voltage_clamped = Math.max(min, Math.min(max, Main.net[i].voltage));

            int red = 0;
            int green = 0;
            int blue = 0;

            if (Main.net[i].inhibitory) { // inhibitory
                if (Main.net[i].voltage < 0) { // negative
                    red = (int)(255 -((voltage_clamped /min) *255));
                    green = 0;
                    blue = 0;
                } else if (Main.net[i].voltage > 0) { // positive
                    red = 255;
                    green = (int)((voltage_clamped /max) *255);
                    blue = (int)((voltage_clamped /max) *255);
                } else {
                    red = 255;
                    green = 0;
                    blue = 0;
                }
            } else { // excitatory
                if (Main.net[i].voltage < 0) { // negative
                    red = 0;
                    green = 0;
                    blue = (int)(255 -((voltage_clamped /min) *255));
                } else if (Main.net[i].voltage > 0) { // positive
                    red = (int)((voltage_clamped /max) *255);
                    green = (int)((voltage_clamped /max) *255);
                    blue = 255;
                } else {
                    red = 0;
                    green = 0;
                    blue = 255;
                }
            }

            g.setColor(new Color(red, green, blue));

            // draw neuron
            g.fillRoundRect(neuronX, neuronY, Main.render_neuronSize, Main.render_neuronSize, Main.render_neuronSize, Main.render_neuronSize);
        }

    }

    // gets the position to draw a neuron at, using the neuron's ID
    int[] findNeuronPos(int ID) {
        int[] pos = new int[2];
        pos[0] = 20 +(Main.net[ID].rank *55); // x
        pos[1] = 20 +(Main.net[ID].positionInLayer *12); // y
        return pos;
    }

}
