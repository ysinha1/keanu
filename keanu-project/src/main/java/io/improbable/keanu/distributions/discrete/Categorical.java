package io.improbable.keanu.distributions.discrete;

import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.tensor.intgr.IntegerTensor;
import io.improbable.keanu.tensor.intgr.Nd4jIntegerTensor;
import io.improbable.keanu.vertices.dbl.KeanuRandom;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static java.util.Comparator.reverseOrder;

public class Categorical {

    public static IntegerTensor sample(IntegerTensor k, DoubleTensor p, KeanuRandom random) {
        List<Integer> categories = k.asFlatList();
        List<Double> probabilities = p.asFlatList();
        List<CategoricalProbability> catProbabilities = new ArrayList<>();
        double totalProbability = 0;

        for (int i = 0; i < probabilities.size(); i++) {
            double probability = probabilities.get(i);
            catProbabilities.add(new CategoricalProbability(categories.get(i), probability));
            totalProbability += probability;
        }

        if (totalProbability != 1.0) {
            for (CategoricalProbability catProb : catProbabilities) {
                catProb.setProbability(catProb.getProbability() / totalProbability);
            }
        }

        catProbabilities.sort(reverseOrder());
        double cumalativeSum = 0;
        for (CategoricalProbability catProb : catProbabilities) {
            double probability = catProb.getProbability();
            catProb.setProbability(cumalativeSum);
            cumalativeSum += probability;
        }

        double rand = random.nextDouble();
        CategoricalProbability chosenCategory = catProbabilities.get(0);

        for (CategoricalProbability catProb : catProbabilities) {
            if (catProb.getProbability() <= rand) {
                chosenCategory = catProb;
            }
        }

        return IntegerTensor.scalar(chosenCategory.getRank());
    }

    public static class CategoricalProbability implements Comparable {

        private int rank;
        private double probability;

        public CategoricalProbability(int rank, double probability) {
            this.rank = rank;
            this.probability = probability;
        }

        public double getProbability() {
            return probability;
        }

        public int getRank() {
            return rank;
        }

        public void setProbability(double probability) {
            this.probability = probability;
        }

        @Override
        public int compareTo(@NotNull Object o) {
            CategoricalProbability cat = (CategoricalProbability) o;
            double comparitorsProb = cat.getProbability();
            return Double.compare(getProbability(), comparitorsProb);
        }
    }


}
