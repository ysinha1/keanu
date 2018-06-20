package io.improbable.keanu.ABM.crowd;

import io.improbable.keanu.randomfactory.RandomFactory;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;

public class BlackBoxCrowdModelTest {



    @Test
    public void run() {

    }

    private DoubleTensor[] blackBoxModel(DoubleTensor[] inputs, RandomFactory<Double> random) {
        DoubleTensor[] output = new DoubleTensor[1];
        ArrayList<Double> inputArray = new ArrayList<>(inputs.length);
        for (DoubleTensor input : inputs) {
            inputArray.add(input.scalar());
        }
        Iterator<Double> it = inputArray.iterator();
        Double m = it.next();
        Double c = it.next();
        ArrayList<Double> xPoints = new ArrayList<>();
        ArrayList<Double> yPoints = new ArrayList<>();
        while (it.hasNext()) {
            double x = it.next();
            double y = it.next();
            xPoints.add(x);
            yPoints.add(y);
        }

        Double SumOfSquaredError = 0.0;

        for (int i = 0; i < yPoints.size(); i++) {
            Double yActual = m * xPoints.get(i) + c;
            Double yExpected = yPoints.get(i);
            Double error = yActual - yExpected;
            SumOfSquaredError += error * error;
        }

        Double MSE = SumOfSquaredError / yPoints.size();
        double[] RMSE = new double[1];
        RMSE[0] = Math.sqrt(MSE);

        output[0] = DoubleTensor.create(RMSE);

        return output;
    }
}
