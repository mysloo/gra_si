package pomocne;

import org.neuroph.core.Layer;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.Neuron;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.nnet.CompetitiveNetwork;
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.util.ConnectionFactory;

public class neuronNet {

    public static void main(String[] args) {
        Layer inputLayer = new Layer();
        inputLayer.addNeuron(new Neuron());
        inputLayer.addNeuron(new Neuron());

        Layer hiddenLayerOne = new Layer();
        hiddenLayerOne.addNeuron(new Neuron());
        hiddenLayerOne.addNeuron(new Neuron());
        hiddenLayerOne.addNeuron(new Neuron());
        hiddenLayerOne.addNeuron(new Neuron());

        Layer hiddenLayerTwo = new Layer();
        hiddenLayerTwo.addNeuron(new Neuron());
        hiddenLayerTwo.addNeuron(new Neuron());
        hiddenLayerTwo.addNeuron(new Neuron());
        hiddenLayerTwo.addNeuron(new Neuron());

        Layer outputLayer = new Layer();
        outputLayer.addNeuron(new Neuron());


        NeuralNetwork ann = new NeuralNetwork();
        ann.addLayer(0, inputLayer);
        ann.addLayer(1, hiddenLayerOne);
        ConnectionFactory.fullConnect(ann.getLayerAt(0), ann.getLayerAt(1));
        ann.addLayer(2, hiddenLayerTwo);
        ConnectionFactory.fullConnect(ann.getLayerAt(1), ann.getLayerAt(2));
        ann.addLayer(3, outputLayer);
        ConnectionFactory.fullConnect(ann.getLayerAt(2), ann.getLayerAt(3));
        ConnectionFactory.fullConnect(ann.getLayerAt(0),
                ann.getLayerAt(ann.getLayersCount()-1), false);
        ann.setInputNeurons(inputLayer.getNeurons());
        ann.setOutputNeurons(outputLayer.getNeurons());

        System.out.println(ann.getWeights());
    }

}
