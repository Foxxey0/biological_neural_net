import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Random;

public class Main {

    //Notes:
    // synapsestrength is between 0 and .5. 0 is no connection. .5 is maximum physical strength of connection.
    // voltage is unitless here for simplicity.
    // when neuron voltage exceeds its threshold for sending a signal, it sends it's signals and immediately returns to resting voltage.
    // neurons are kept track of by their position in an array. weights[0] would be the strength of the synapse between the current neuron and neuron #0.
    // theres 10 numbers (0-9), and each number corresponds to 1 neuron. neurons 0-9. Ex. number 0 goes to neuron 0. number 1 goes to neuron 1.
    // 20 outputs (numbers 0-19). neurons 10-29.
    // the process for ticks is a little strange. at first i thought it was simple, but then i realized the original idea i had wouldnt work because you cant send the signals and do all the calculations before or after resetting neurons over the threshold. So instead, i split it up. First, for each neuron, ill calculate the numbers for all presynaptic neurons in order to queue up the recieving of the signals. Second, ill reset the neurons whose voltages are over the threshold (this works because i havent changed any voltages yet, i only have the queued signals). Third, ill apply the queued signals for new voltages (i have to do this last because if i do it at the same time as calculating, then i cant reset the neurons at the right time and it will mess up the calculations).
    // Recap. 3 stages for a tick. Fire (queue signals), reset (reset voltages that were/are over threshold), Recieve (add queued voltages to current voltages).
    // oh, and, input neurons dont get to recieve any signals from any other neurons. only when I choose to stimulate them.
    // a tick is a frame of activity in the net. each tick represents 1 "step" or frozen frame in time. The net processes at 120 ticks per second.

    // SETTINGS START
    public static int neurons_amount = 1000 +30; // 100 base. 10 for inputs and 20 for outputs.
    public static int inputNeurons_startID = 0;
    public static int inputNeurons_endID = 9;
    public static int outputNeurons_startID = 10;
    public static int outputNeurons_endID = 29;
    public static int hiddenNeurons_startID = 30;
//    public static int hiddenNeurons_endID = 1029;
    public static int hiddenNeurons_endID = neurons_amount -1; // the rest of the neurons are hidden neurons.
    public static int input_ticksPerSecond = 60; // 60;
    public static int net_ticksPerSecond = 120;

    public static double neuronRestingVoltage = 0; // unitless
    public static double neuronVoltageDecayPerTick = .99;
    public static double sendSignal_voltageThreshold = 1; // unitless. minimum activation required to send a signal
    public static double sendSignal_voltageToSend = 1; // unitless
    public static double sendInputSignal_voltageToSend = 2; // unitless. i did more than 1 just in case the input neuron decays before it needs to fire. so it should fire whenever it is activated.

    public static double minSynapseStrength = 0;
    public static double maxSynapseStrength = .15;

    public static double reward = .01;
    public static double punishment = -.01;
    public static double synapseTraceIncrease = 1; // when a synapse fires, how much should the trace variable increase?
    public static double synapseTraceMax = 1;
    public static double synapseTraceDecayPerTick = .99;

    public static int render_neuronSize = 10;
    // SETTINGS END

    public static int input_millisPerTick = 1000 /input_ticksPerSecond;
    public static int net_millisPerTick = 1000 /net_ticksPerSecond;

    public static long input_previousTickTime = System.currentTimeMillis();
    public static long net_previousTickTime = System.currentTimeMillis();

    public static int input1 = 0;
    public static int input2 = 0;
    public static int output1goal;
    public static long intermission = 0;

    public static boolean tune = false;
    public static boolean inputting = false;

    public static Neuron[] net;

    public static void main(String[] args) {

        // init neurons START
        net = new Neuron[neurons_amount];

        // init neurons
        for (int i = 0; i <= neurons_amount -1; i++) {
            net[i] = new Neuron(i);
        }
        // init connections of neurons
        for (int i = 0; i <= neurons_amount -1; i++) {
            net[i].initConnections();
        }
        // init neurons END

        JFrame window = new JFrame();
        JPanel window_panel = new JPanel();
        window_panel_render window_panel_render = new window_panel_render();
        JTextField window_title_inputs = new JTextField();
        JTextField window_title_input1 = new JTextField();
        JTextField window_textfield_input_input1 = new JTextField();
        JTextField window_textfield_true_input1 = new JTextField();
        JTextField window_title_input2 = new JTextField();
        JTextField window_textfield_input_input2 = new JTextField();
        JTextField window_textfield_true_input2 = new JTextField();
        JTextField window_title_output1goal = new JTextField();
        JTextField window_textfield_input_output1goal = new JTextField();
        JTextField window_textfield_true_output1goal = new JTextField();
        JButton window_button_newTrial = new JButton();
        JTextField window_title_outputs = new JTextField();
        JTextField window_title_output1 = new JTextField();
        JTextField window_textfield_output1 = new JTextField();
        JTextField window_title_tickDuration = new JTextField();
        JTextField window_textfield_tickDuration = new JTextField();
        JTextField window_title_intermisson = new JTextField();
        JTextField window_textfield_input_intermisson = new JTextField();
        JTextField window_textfield_true_intermisson = new JTextField();
        JButton window_button_set_intermisson = new JButton();
        JTextField window_title_reward = new JTextField();
        JTextField window_textfield_input_reward = new JTextField();
        JTextField window_textfield_true_reward = new JTextField();
        JButton window_button_set_reward = new JButton();
        JTextField window_title_punishment = new JTextField();
        JTextField window_textfield_input_punishment = new JTextField();
        JTextField window_textfield_true_punishment = new JTextField();
        JButton window_button_set_punishment = new JButton();
        JTextField window_title_tune = new JTextField();
        JCheckBox window_checkbox_tune = new JCheckBox();
        JTextField window_title_inputting = new JTextField();
        JCheckBox window_checkbox_inputting = new JCheckBox();

        window.add(window_panel);
        window.setSize(1900, 1000);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        window_panel.setLayout(null);
        window_panel.setFont(window_panel.getFont().deriveFont(16f));

        window_panel.add(window_panel_render);
        window_panel.add(window_title_inputs);
        window_panel.add(window_title_input1);
        window_panel.add(window_textfield_input_input1);
        window_panel.add(window_textfield_true_input1);
        window_panel.add(window_title_input2);
        window_panel.add(window_textfield_input_input2);
        window_panel.add(window_textfield_true_input2);
        window_panel.add(window_title_output1goal);
        window_panel.add(window_textfield_input_output1goal);
        window_panel.add(window_textfield_true_output1goal);
        window_panel.add(window_button_newTrial);
        window_panel.add(window_title_outputs);
        window_panel.add(window_title_output1);
        window_panel.add(window_textfield_output1);
        window_panel.add(window_title_tickDuration);
        window_panel.add(window_textfield_tickDuration);
        window_panel.add(window_title_intermisson);
        window_panel.add(window_textfield_input_intermisson);
        window_panel.add(window_textfield_true_intermisson);
        window_panel.add(window_button_set_intermisson);
        window_panel.add(window_title_reward);
        window_panel.add(window_textfield_input_reward);
        window_panel.add(window_textfield_true_reward);
        window_panel.add(window_button_set_reward);
        window_panel.add(window_title_punishment);
        window_panel.add(window_textfield_input_punishment);
        window_panel.add(window_textfield_true_punishment);
        window_panel.add(window_button_set_punishment);
        window_panel.add(window_title_tune);
        window_panel.add(window_checkbox_tune);
        window_panel.add(window_title_inputting);
        window_panel.add(window_checkbox_inputting);

        // set font for all components of window_panel
        for (Component component : window_panel.getComponents()) {
            component.setFont(window_panel.getFont());
        }

        window_panel_render.setBounds(650, 10, 1200, 900);
        window_panel_render.setBackground(Color.BLACK);

        int x_start = 10;
        int y_start = 10;
        int x = x_start;
        int y = y_start;

        window_title_inputs.setBounds(x, y, 100, window_title_inputs.getFontMetrics(window_title_inputs.getFont()).getHeight() +2);
        window_title_inputs.setText("inputs:");
        window_title_inputs.setEditable(false);

        x += 20;
        y += window_title_inputs.getHeight();
        window_title_input1.setBounds(x, y, 100, window_title_input1.getFontMetrics(window_title_input1.getFont()).getHeight() +2);
        window_title_input1.setText("input 1:");
        window_title_input1.setEditable(false);

        x += window_title_input1.getWidth();
        window_textfield_input_input1.setBounds(x, y, 100, window_textfield_input_input1.getFontMetrics(window_textfield_input_input1.getFont()).getHeight() +2);
        window_textfield_input_input1.setText(String.valueOf(input1));
        window_textfield_input_input1.setEditable(true);

        x += window_textfield_input_input1.getWidth();
        window_textfield_true_input1.setBounds(x, y, 100, window_textfield_true_input1.getFontMetrics(window_textfield_true_input1.getFont()).getHeight() +2);
        window_textfield_true_input1.setText(String.valueOf(input1));
        window_textfield_true_input1.setEditable(false);

        x = x_start +20;
        y += window_title_input1.getHeight();
        window_title_input2.setBounds(x, y, 100, window_title_input2.getFontMetrics(window_title_input2.getFont()).getHeight() +2);
        window_title_input2.setText("input 2:");
        window_title_input2.setEditable(false);

        x += window_title_input2.getWidth();
        window_textfield_input_input2.setBounds(x, y, 100, window_textfield_input_input2.getFontMetrics(window_textfield_input_input2.getFont()).getHeight() +2);
        window_textfield_input_input2.setText(String.valueOf(input2));
        window_textfield_input_input2.setEditable(true);

        x += window_textfield_input_input2.getWidth();
        window_textfield_true_input2.setBounds(x, y, 100, window_textfield_true_input2.getFontMetrics(window_textfield_true_input2.getFont()).getHeight() +2);
        window_textfield_true_input2.setText(String.valueOf(input2));
        window_textfield_true_input2.setEditable(false);

        x = x_start;
        y += window_title_input2.getHeight() +20;
        window_title_output1goal.setBounds(x, y, 100, window_title_output1goal.getFontMetrics(window_title_output1goal.getFont()).getHeight() +2);
        window_title_output1goal.setText("output 1 goal:");
        window_title_output1goal.setEditable(false);

        x += window_title_output1goal.getWidth();
        window_textfield_input_output1goal.setBounds(x, y, 100, window_textfield_input_output1goal.getFontMetrics(window_textfield_input_output1goal.getFont()).getHeight() +2);
        window_textfield_input_output1goal.setText(String.valueOf(output1goal));
        window_textfield_input_output1goal.setEditable(true);

        x += window_textfield_input_output1goal.getWidth();
        window_textfield_true_output1goal.setBounds(x, y, 100, window_textfield_true_output1goal.getFontMetrics(window_textfield_true_output1goal.getFont()).getHeight() +2);
        window_textfield_true_output1goal.setText("0");
        window_textfield_true_output1goal.setEditable(false);

        x = x_start;
        y += window_title_output1goal.getHeight() +20;
        window_button_newTrial.setBounds(x, y, 100, window_button_newTrial.getFontMetrics(window_button_newTrial.getFont()).getHeight() +2);
        window_button_newTrial.setText("new trial");

        y += window_button_newTrial.getHeight() +20;
        window_title_outputs.setBounds(x, y, 100, window_title_outputs.getFontMetrics(window_title_outputs.getFont()).getHeight() +2);
        window_title_outputs.setText("outputs:");
        window_title_outputs.setEditable(false);

        x += 20;
        y += window_title_outputs.getHeight();
        window_title_output1.setBounds(x, y, 100, window_title_output1.getFontMetrics(window_title_output1.getFont()).getHeight() +2);
        window_title_output1.setText("output 1:");
        window_title_output1.setEditable(false);

        x += window_title_output1.getWidth();
        window_textfield_output1.setBounds(x, y, 100, window_textfield_output1.getFontMetrics(window_textfield_output1.getFont()).getHeight() +2);
        window_textfield_output1.setText("N/A");
        window_textfield_output1.setEditable(false);

        x = x_start;
        y += window_title_output1.getHeight() +20;
        window_title_tickDuration.setBounds(x, y, 200, window_title_tickDuration.getFontMetrics(window_title_tickDuration.getFont()).getHeight() +2);
        window_title_tickDuration.setText("Tick Duration [milliseconds]:");
        window_title_tickDuration.setEditable(false);

        x += window_title_tickDuration.getWidth();
        window_textfield_tickDuration.setBounds(x, y, 100, window_textfield_tickDuration.getFontMetrics(window_textfield_tickDuration.getFont()).getHeight() +2);
        window_textfield_tickDuration.setText("N/A");
        window_textfield_tickDuration.setEditable(false);

        x = x_start;
        y += window_title_tickDuration.getHeight() +20;
        window_title_intermisson.setBounds(x, y, 200, window_title_intermisson.getFontMetrics(window_title_intermisson.getFont()).getHeight() +2);
        window_title_intermisson.setText("Intermission [milliseconds]:");
        window_title_intermisson.setEditable(false);

        x += window_title_intermisson.getWidth();
        window_textfield_input_intermisson.setBounds(x, y, 100, window_textfield_input_intermisson.getFontMetrics(window_textfield_input_intermisson.getFont()).getHeight() +2);
        window_textfield_input_intermisson.setText(String.valueOf(intermission));
        window_textfield_input_intermisson.setEditable(true);

        x += window_textfield_input_intermisson.getWidth();
        window_textfield_true_intermisson.setBounds(x, y, 100, window_textfield_true_intermisson.getFontMetrics(window_textfield_true_intermisson.getFont()).getHeight() +2);
        window_textfield_true_intermisson.setText(String.valueOf(intermission));
        window_textfield_true_intermisson.setEditable(false);

        x += window_textfield_true_intermisson.getWidth();
        window_button_set_intermisson.setBounds(x, y, 200, window_button_set_intermisson.getFontMetrics(window_button_set_intermisson.getFont()).getHeight() +2);
        window_button_set_intermisson.setText("set intermission");

        x = x_start;
        y += window_title_intermisson.getHeight() +20;
        window_title_reward.setBounds(x, y, 100, window_title_reward.getFontMetrics(window_title_reward.getFont()).getHeight() +2);
        window_title_reward.setText("reward:");
        window_title_reward.setEditable(false);

        x += window_title_reward.getWidth();
        window_textfield_input_reward.setBounds(x, y, 100, window_textfield_input_reward.getFontMetrics(window_textfield_input_reward.getFont()).getHeight() +2);
        window_textfield_input_reward.setText(String.valueOf(reward));
        window_textfield_input_reward.setEditable(true);

        x += window_textfield_input_reward.getWidth();
        window_textfield_true_reward.setBounds(x, y, 100, window_textfield_true_reward.getFontMetrics(window_textfield_true_reward.getFont()).getHeight() +2);
        window_textfield_true_reward.setText(String.valueOf(reward));
        window_textfield_true_reward.setEditable(false);

        x += window_textfield_true_reward.getWidth();
        window_button_set_reward.setBounds(x, y, 200, window_button_set_reward.getFontMetrics(window_button_set_reward.getFont()).getHeight() +2);
        window_button_set_reward.setText("set reward");

        x = x_start;
        y += window_title_reward.getHeight() +20;
        window_title_punishment.setBounds(x, y, 100, window_title_punishment.getFontMetrics(window_title_punishment.getFont()).getHeight() +2);
        window_title_punishment.setText("punishment:");
        window_title_punishment.setEditable(false);

        x += window_title_punishment.getWidth();
        window_textfield_input_punishment.setBounds(x, y, 100, window_textfield_input_punishment.getFontMetrics(window_textfield_input_punishment.getFont()).getHeight() +2);
        window_textfield_input_punishment.setText(String.valueOf(punishment));
        window_textfield_input_punishment.setEditable(true);

        x += window_textfield_input_punishment.getWidth();
        window_textfield_true_punishment.setBounds(x, y, 100, window_textfield_true_punishment.getFontMetrics(window_textfield_true_punishment.getFont()).getHeight() +2);
        window_textfield_true_punishment.setText(String.valueOf(punishment));
        window_textfield_true_punishment.setEditable(false);

        x += window_textfield_true_punishment.getWidth();
        window_button_set_punishment.setBounds(x, y, 200, window_button_set_punishment.getFontMetrics(window_button_set_punishment.getFont()).getHeight() +2);
        window_button_set_punishment.setText("set punishment");

        x = x_start;
        y += window_title_punishment.getHeight() +20;
        window_title_tune.setBounds(x, y, 100, window_title_tune.getFontMetrics(window_title_tune.getFont()).getHeight() +2);
        window_title_tune.setText("tune: ");
        window_title_tune.setEditable(false);

        x += window_title_tune.getWidth();
        window_checkbox_tune.setBounds(x, y, 100, window_checkbox_tune.getFontMetrics(window_checkbox_tune.getFont()).getHeight() +2);
        window_checkbox_tune.setSelected(tune);
        window_checkbox_tune.setBackground(Color.white);

        x = x_start;
        y += window_title_tune.getHeight() +20;
        window_title_inputting.setBounds(x, y, 100, window_title_inputting.getFontMetrics(window_title_inputting.getFont()).getHeight() +2);
        window_title_inputting.setText("inputting: ");
        window_title_inputting.setEditable(false);

        x += window_title_inputting.getWidth();
        window_checkbox_inputting.setBounds(x, y, 100, window_checkbox_inputting.getFontMetrics(window_checkbox_inputting.getFont()).getHeight() +2);
        window_checkbox_inputting.setSelected(inputting);
        window_checkbox_inputting.setBackground(Color.white);

        window_button_newTrial.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("change trial");

                int parsed1 = Integer.parseInt(window_textfield_input_input1.getText());
                window_textfield_true_input1.setText(String.valueOf(parsed1));
                input1 = parsed1;

                int parsed2 = Integer.parseInt(window_textfield_input_input2.getText());
                window_textfield_true_input2.setText(String.valueOf(parsed2));
                input2 = parsed2;

                int parsed3 = Integer.parseInt(window_textfield_input_output1goal.getText());
                window_textfield_true_output1goal.setText(String.valueOf(parsed3));
                output1goal = parsed3;

            }
        });

        window_button_set_intermisson.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long parsed = Long.parseLong(window_textfield_input_intermisson.getText());
                window_textfield_true_intermisson.setText(String.valueOf(parsed));
                intermission = parsed;
            }
        });

        window_button_set_reward.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                double parsed = Double.parseDouble(window_textfield_input_reward.getText());
                window_textfield_true_reward.setText(String.valueOf(parsed));
                reward = parsed;
            }
        });

        window_button_set_punishment.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                double parsed = Double.parseDouble(window_textfield_input_punishment.getText());
                window_textfield_true_punishment.setText(String.valueOf(parsed));
                punishment = parsed;
            }
        });

        window_checkbox_tune.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tune = window_checkbox_tune.isSelected();
            }
        });

        window_checkbox_inputting.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inputting = window_checkbox_inputting.isSelected();
            }
        });

        window.setVisible(true);

        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {

                long tickStartTime;

                while (true) { // each iteration is a tick
                    try {
//                        Thread.sleep(1); // safety so program doesnt crash if its too fast for some reason. I dont know if i need this.

                        window_panel_render.repaint();

                        Thread.sleep(intermission);

                        // do signal sending / Progress 1 still-frame in time.
                        if (System.currentTimeMillis() -net_previousTickTime > net_millisPerTick) {

                            // update the timing system for net
                            net_previousTickTime = System.currentTimeMillis();

                            // for finding out what the duration of this tick will be
                            tickStartTime = System.currentTimeMillis();

                            // feed in the inputs
                            if (inputting) {
                                if (System.currentTimeMillis() -input_previousTickTime > input_millisPerTick) {
                                    // update the timing system for input
                                    input_previousTickTime = System.currentTimeMillis();

                                    net[input1].voltage = sendInputSignal_voltageToSend;
                                    net[input2].voltage = sendInputSignal_voltageToSend;
                                }
                            }

                            // decay neuron voltage
                            for (int i = 0; i <= neurons_amount -1; i++) { // i is current neuron
                                net[i].voltage = net[i].voltage *neuronVoltageDecayPerTick;
                            }

                            // decay synapse trace
                            for (int i = 0; i <= neurons_amount -1; i++) { // i is current neuron
                                for (int k = 0; k <= neurons_amount -1; k++) { // k is possible presynaptic neuron
                                    if (net[i].synapses[k] == null) {
                                        // this synapse doesnt exist.
                                    } else {
                                        net[i].synapses[k].trace = net[i].synapses[k].trace * synapseTraceDecayPerTick;
                                    }
                                }
                            }

                            // stage: fire (calculate fired signals of presynaptic neurons for each neuron)
                            for (int i = 0; i <= neurons_amount -1; i++) { // i is current neuron
                                for (Integer k : net[i].presynapticNeurons) { // k is presynaptic neuron
                                    if (net[k].voltage >= sendSignal_voltageThreshold) { // check if the presynaptic neuron voltage is past the threshold. if so, fire.
                                        net[i].queuedVoltage += sendSignal_voltageToSend *net[i].synapses[k].weight *(net[k].inhibitory? -1 : 1);
                                        net[i].synapses[k].trace = Math.min(synapseTraceMax, net[i].synapses[k].trace +synapseTraceIncrease);
                                    }
                                }
                            }

                            // reset
                            for (int i = 0; i <= neurons_amount -1; i++) { // i is current neuron
                                // output neurons dont feed into anything, so they dont need to reset. let them keep their voltage.
                                if (i >= outputNeurons_startID && i <= outputNeurons_endID) {
                                    continue;
                                }

                                if (net[i].voltage >= sendSignal_voltageThreshold) {
                                    net[i].voltage = neuronRestingVoltage;
                                }
                            }

                            // recieve
                            for (int i = 0; i <= neurons_amount -1; i++) { // i is current neuron
                                net[i].voltage += net[i].queuedVoltage;
                                net[i].queuedVoltage = 0;
                            }

                            // give me output (strongest output neuron)
                            int biggestVoltage_neuronID = 0;
                            double biggestVoltage = 0;
                            boolean first = true;
                            for (int i = outputNeurons_startID; i <= outputNeurons_endID; i++) { // i is current neuron
                                if (first) {
                                    first = false;
                                    biggestVoltage_neuronID = i;
                                    biggestVoltage = net[i].voltage;
                                } else {
                                    if (net[i].voltage > biggestVoltage) {
                                        biggestVoltage_neuronID = i;
                                        biggestVoltage = net[i].voltage;
                                    }
                                }
                            }
                            int output = biggestVoltage_neuronID -outputNeurons_startID; // remember, im representing numbers through specific neurons. neurons 10-29 are for numbers 0-19. so i can just subtract 10 from neuron ID to get the number it represents. To make it scalable, i can just subtract it by the id of the first output neuron since the first output neuron represents number 0 and subtracting that number from itself gets 0, and everything after that is in order.
                            window_textfield_output1.setText(String.valueOf(output));

                            System.out.print(output +",, ");
                            for (int i = outputNeurons_startID; i <= outputNeurons_endID; i++) {
                                System.out.print(net[i].voltage +", ");
                            }
                            System.out.println();
//                            System.out.println(net[16].presynapticNeuronWeights[net[16].presynapticNeurons.get(1)]);
//                            System.out.println(net[net[16].presynapticNeurons.get(1)].voltage);

                            // tune. automatic (technically manual since i choose the numbers) reward/punish system
                            if (tune) {
//                                for (Synapse firedSynapse : firedSynapses) {
//                                    // only change weights of input to hidden and hidden to output. not hidden to hidden. cancel hidden to hidden.
//                                    if (firedSynapse.targetNeuronID >= hiddenNeurons_startID && firedSynapse.targetNeuronID <= hiddenNeurons_endID && firedSynapse.presynapticNeuronID >= hiddenNeurons_startID && firedSynapse.presynapticNeuronID <= hiddenNeurons_endID) { // both are hidden
//                                        continue;
//                                    }
//                                    if (output == output1goal) { // correct
//                                        // strengthen synapse
//                                        net[firedSynapse.targetNeuronID].presynapticNeuronWeights[firedSynapse.presynapticNeuronID] = Math.max(Math.min(maxSynapseStrength, net[firedSynapse.targetNeuronID].presynapticNeuronWeights[firedSynapse.presynapticNeuronID] +rewardNum), minSynapseStrength);
//                                    } else { // incorrect
//                                        // weaken synapse
//                                        net[firedSynapse.targetNeuronID].presynapticNeuronWeights[firedSynapse.presynapticNeuronID] = Math.max(Math.min(maxSynapseStrength, net[firedSynapse.targetNeuronID].presynapticNeuronWeights[firedSynapse.presynapticNeuronID] +punishmentNum), minSynapseStrength);
//                                    }
//                                }

//                                for (Synapse firedSynapse : firedSynapses) {
//                                    // dont train inhibitory weights
//                                    if (net[firedSynapse.presynapticNeuronID].inhibitory) {
//                                        continue;
//                                    }
//
////                                    if (firedSynapse.targetNeuronID >= hiddenNeurons_startID && firedSynapse.targetNeuronID <= hiddenNeurons_endID && firedSynapse.presynapticNeuronID >= hiddenNeurons_startID && firedSynapse.presynapticNeuronID <= hiddenNeurons_endID) { // both are hidden
////                                        continue;
////                                    }
////                                    if (firedSynapse.presynapticNeuronID >= inputNeurons_startID && firedSynapse.presynapticNeuronID <= inputNeurons_endID) {
////                                        continue;
////                                    }
//                                    // only change weights of input to hidden and hidden to output. not hidden to hidden. cancel hidden to hidden.
////                                if (firedSynapse.targetNeuronID != output1goal +outputNeurons_startID) {
////                                    // weaken synapse
////                                    net[firedSynapse.targetNeuronID].presynapticNeuronWeights[firedSynapse.presynapticNeuronID] = Math.max(Math.min(maxSynapseStrength, net[firedSynapse.targetNeuronID].presynapticNeuronWeights[firedSynapse.presynapticNeuronID] +punishmentNum), minSynapseStrength);
////                                } else {
////                                    // strengthen synapse
////                                    net[firedSynapse.targetNeuronID].presynapticNeuronWeights[firedSynapse.presynapticNeuronID] = Math.max(Math.min(maxSynapseStrength, net[firedSynapse.targetNeuronID].presynapticNeuronWeights[firedSynapse.presynapticNeuronID] +rewardNum), minSynapseStrength);
////
////                                }
//
//
////                                    if (firedSynapse.targetNeuronID -outputNeurons_startID == output) {
////                                        if (output != output1goal) {
////                                            net[firedSynapse.targetNeuronID].presynapticNeuronWeights[firedSynapse.presynapticNeuronID] = Math.max(Math.min(maxSynapseStrength, net[firedSynapse.targetNeuronID].presynapticNeuronWeights[firedSynapse.presynapticNeuronID] +punishment), minSynapseStrength);
////                                        } else {
////                                            net[firedSynapse.targetNeuronID].presynapticNeuronWeights[firedSynapse.presynapticNeuronID] = Math.max(Math.min(maxSynapseStrength, net[firedSynapse.targetNeuronID].presynapticNeuronWeights[firedSynapse.presynapticNeuronID] +reward), minSynapseStrength);
////                                        }
////                                    }
//
//                                    // try to go through the entire chain and give dopamine to all of the ones backwards starting from the correct output
////                                    if (firedSynapse.targetNeuronID -outputNeurons_startID == output) {
////                                        if (output != output1goal) {
////                                            net[firedSynapse.targetNeuronID].presynapticNeuronWeights[firedSynapse.presynapticNeuronID] = Math.max(Math.min(maxSynapseStrength, net[firedSynapse.targetNeuronID].presynapticNeuronWeights[firedSynapse.presynapticNeuronID] +punishment), minSynapseStrength);
////                                        } else {
////                                            net[firedSynapse.targetNeuronID].presynapticNeuronWeights[firedSynapse.presynapticNeuronID] = Math.max(Math.min(maxSynapseStrength, net[firedSynapse.targetNeuronID].presynapticNeuronWeights[firedSynapse.presynapticNeuronID] +reward), minSynapseStrength);
////                                        }
////                                    }
//
//                                }

//                                for (int i = 0; i <= neurons_amount -1; i++) { // i is current neuron
//                                    for (int k = 0; k <= neurons_amount -1; k++) { // k is possible presynaptic neuron
//                                        if (net[i].synapses[k] == null) {
//                                            // this synapse doesnt exist.
//                                        } else {
////                                            if (net[i].voltage >= sendSignal_voltageThreshold *.95) { // checks if the post neuron (i) fired.
//                                                net[i].synapses[k].weight = Math.max(minSynapseStrength, Math.min(maxSynapseStrength, net[i].synapses[k].weight +(net[i].synapses[k].trace *(output == output1goal? reward : punishment))));;
////                                            }
//                                        }
//                                    }
//                                }

//                                // the strongest one
//                                for (int i = 0; i <= neurons_amount -1; i++) { // i is current neuron
//
//                                    if (i >= inputNeurons_startID && i <= inputNeurons_endID) {
//                                        continue;
//                                    }
//                                    if (i >= hiddenNeurons_startID && i <= hiddenNeurons_endID) {
//                                        continue;
//                                    }
//                                    if (i != output +outputNeurons_startID) {
//                                        continue;
//                                    }
//
//                                    if (output == output1goal) {
//                                        double strongestWeightStrength = 0;
//                                        int strongestWeightID = 0;
//                                        boolean first2 = true;
//                                        for (int k = 0; k <= neurons_amount - 1; k++) { // k is possible presynaptic neuron
//                                            if (net[i].synapses[k] == null) {
//                                                // this synapse doesnt exist.
//                                            } else {
//                                                if (first2) {
//                                                    first2 = false;
//                                                    strongestWeightStrength = net[i].synapses[k].weight;
//                                                    strongestWeightID = k;
//                                                } else {
//                                                    if (net[i].synapses[k].weight < strongestWeightStrength) {
//                                                        strongestWeightStrength = net[i].synapses[k].weight;
//                                                        strongestWeightID = k;
//                                                    }
//                                                }
//                                            }
//                                        }
//                                        net[i].synapses[strongestWeightID].weight = Math.max(minSynapseStrength, Math.min(maxSynapseStrength, net[i].synapses[strongestWeightID].weight +reward));;
//                                    } else {
//                                        double strongestWeightStrength = 0;
//                                        int strongestWeightID = 0;
//                                        boolean first2 = true;
//                                        for (int k = 0; k <= neurons_amount - 1; k++) { // k is possible presynaptic neuron
//                                            if (net[i].synapses[k] == null) {
//                                                // this synapse doesnt exist.
//                                            } else {
//                                                if (first2) {
//                                                    first2 = false;
//                                                    strongestWeightStrength = net[i].synapses[k].weight;
//                                                    strongestWeightID = k;
//                                                } else {
//                                                    if (net[i].synapses[k].weight > strongestWeightStrength) {
//                                                        strongestWeightStrength = net[i].synapses[k].weight;
//                                                        strongestWeightID = k;
//                                                    }
//                                                }
//                                            }
//                                        }
//                                        net[i].synapses[strongestWeightID].weight = Math.max(minSynapseStrength, Math.min(maxSynapseStrength, net[i].synapses[strongestWeightID].weight +punishment));;
//                                    }
//
//                                }

                                // goes back from the output neuron. deals with the strongest path from start to finish.
                                for (int i = outputNeurons_startID; i <= outputNeurons_endID; i++) {
                                    int layerToTune = new Random().nextInt(21) +1; // 1-21
                                    if (i -outputNeurons_startID == output1goal) {
                                        int layer = 21;
                                        int currentNeuronID = i;
                                        while (true) {

                                            if (layer == 0) {
                                                break;
                                            }

                                            double strongestSynapseStrength = 0;
                                            int strongestSynapseID = 0; // ID of presynaptic neuron that synapse connects to
                                            boolean first2 = true;
                                            for (int k = 0; k <= neurons_amount - 1; k++) { // k is possible presynaptic neuron

                                                if (net[currentNeuronID].synapses[k] == null) {
                                                    continue;
                                                }

                                                if (net[currentNeuronID].synapses[k].trace < .90 *Math.pow(synapseTraceDecayPerTick, (21 -layer) *2)) {
                                                    continue;
                                                }

                                                if (net[k].inhibitory) {
                                                    continue;
                                                }

                                                if (first2) {
                                                    first2 = false;
                                                    strongestSynapseStrength = net[currentNeuronID].synapses[k].weight;
                                                    strongestSynapseID = k;
                                                } else {
                                                    if (net[currentNeuronID].synapses[k].weight > strongestSynapseStrength) {
                                                        strongestSynapseStrength = net[currentNeuronID].synapses[k].weight;
                                                        strongestSynapseID = k;
                                                    }
                                                }
                                            }

                                            if (net[currentNeuronID].synapses[strongestSynapseID] == null) {
                                                int neuronToSaveID = new Random().nextInt(neurons_amount -10) +10;
                                                int presynapticNeuronToSaveID = net[neuronToSaveID].presynapticNeurons.get(new Random().nextInt(net[neuronToSaveID].presynapticNeurons.size()));
                                                if (net[presynapticNeuronToSaveID].inhibitory & !net[neuronToSaveID].inhibitory) {
                                                    continue;
                                                }
                                                if (!net[presynapticNeuronToSaveID].inhibitory & net[neuronToSaveID].inhibitory) {
                                                    continue;
                                                }
                                                Synapse synapseToSave = net[neuronToSaveID].synapses[presynapticNeuronToSaveID];
                                                synapseToSave.weight = Math.max(minSynapseStrength, Math.min(maxSynapseStrength, synapseToSave.weight +reward));
                                                break;
                                            }

                                            // strengthen it
//                                             if (layer == layerToTune) {
                                                net[currentNeuronID].synapses[strongestSynapseID].weight = Math.max(minSynapseStrength, Math.min(maxSynapseStrength, net[currentNeuronID].synapses[strongestSynapseID].weight +reward));
//                                                break;
//                                            }

                                            layer--;
                                            currentNeuronID = strongestSynapseID;
                                        }
                                    } else if (net[i].voltage > 1) {
                                        int layer = 21;
                                        int currentNeuronID = i;
                                        while (true) {

                                            if (layer == 0) {
                                                break;
                                            }

                                            double strongestSynapseStrength = 0;
                                            int strongestSynapseID = 0; // ID of presynaptic neuron that synapse connects to
                                            boolean first2 = true;
                                            for (int k = 0; k <= neurons_amount - 1; k++) { // k is possible presynaptic neuron

                                                if (net[currentNeuronID].synapses[k] == null) {
                                                    continue;
                                                }

                                                if (net[currentNeuronID].synapses[k].trace < .90 *Math.pow(synapseTraceDecayPerTick, (21 -layer) *2)) {
                                                    continue;
                                                }

                                                if (net[k].inhibitory) {
                                                    continue;
                                                }

                                                if (first2) {
                                                    first2 = false;
                                                    strongestSynapseStrength = net[currentNeuronID].synapses[k].weight;
                                                    strongestSynapseID = k;
                                                } else {
                                                    if (net[currentNeuronID].synapses[k].weight > strongestSynapseStrength) {
                                                        strongestSynapseStrength = net[currentNeuronID].synapses[k].weight;
                                                        strongestSynapseID = k;
                                                    }
                                                }
                                            }

                                            if (net[currentNeuronID].synapses[strongestSynapseID] == null) {

                                                break;
                                            }

                                            // strengthen it
                                            if (layer == layerToTune) {
                                                net[currentNeuronID].synapses[strongestSynapseID].weight = Math.max(minSynapseStrength, Math.min(maxSynapseStrength, net[currentNeuronID].synapses[strongestSynapseID].weight +punishment));
                                                break;
                                            }

                                            layer--;
                                            currentNeuronID = strongestSynapseID;
                                        }
                                    }
                                }
                            }

                            // tick duration stuff
                            long tickDuration = System.currentTimeMillis() -tickStartTime;
                            window_textfield_tickDuration.setText(String.valueOf(tickDuration));

                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread1.start();

    }

}
