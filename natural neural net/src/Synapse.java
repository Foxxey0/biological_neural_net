import java.util.Random;

public class Synapse {
    double weight; // strength of connection
    double trace; // indicates how recently this synapse fired. useful for training. when something good happens, more recently fired synapses are rewarded more because they contributed more.
    boolean draw; // drawing all synapses on the window makes covers up the ones that get drawn first. so im only gonna draw SOME. theres a CHANCE that this will be drawn.

    Synapse() {
        weight = 0;
        trace = 0;
        draw = false;

        if (new Random().nextDouble() <= .03) {
            draw = true;
        }

    }

}
