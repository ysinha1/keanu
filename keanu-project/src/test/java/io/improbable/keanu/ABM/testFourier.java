package io.improbable.keanu.ABM;

import java.util.function.BiConsumer;

import static java.lang.Math.*;

// Experiments to characterise the spatial distribution of agents
public class testFourier {

    Double[][] spatialDomain;
    Double[][] waveDomainRealValueComponent;
    Double[][] waveDomainImaginaryComponent;
    Double[][] waveDomainAmplitudeComponent;
    int ySize;
    int xSize;

    public testFourier (Double[][] spatialDomain, Double[][] waveDomainRealValueComponent,
                        Double[][] waveDomainImaginaryComponent, Double[][] waveDomainAmplitudeComponent) {
        this.spatialDomain = spatialDomain;
        this.waveDomainRealValueComponent = waveDomainRealValueComponent;
        this.waveDomainImaginaryComponent = waveDomainImaginaryComponent;
        this.waveDomainAmplitudeComponent = waveDomainAmplitudeComponent;
        ySize = spatialDomain.length;
        xSize = spatialDomain[0].length;
    }

    public testFourier (Double[][] spatialDomain) {
        this(spatialDomain,
             new Double[spatialDomain.length][spatialDomain[0].length],
             new Double[spatialDomain.length][spatialDomain[0].length],
             new Double[spatialDomain.length][spatialDomain[0].length]);
    }

    public testFourier (Double[][] waveDomainRealValueComponent, Double[][] waveDomainImaginaryComponent) {
        this(new Double[waveDomainRealValueComponent.length][waveDomainRealValueComponent[0].length],
            waveDomainRealValueComponent,
            waveDomainImaginaryComponent,
            new Double[waveDomainRealValueComponent.length][waveDomainRealValueComponent[0].length]);
    }

    public void forward2DFourierTransformFromSpaceToWavenumber() {
        iterateOverOutput(this::iterateOverSpatialDomain);
    }

    public void reverse2DFourierTransformFromWavenumberToSpace() {
        iterateOverOutput(this::iterateOverSpectralDomain);

    }

    public void iterateOverOutput(BiConsumer<Integer, Integer> sourceDomainIterator) {
        for (int yWave=0; yWave<ySize; yWave++) {
            for (int xWave=0; xWave<xSize; xWave++) {
                sourceDomainIterator.accept(yWave, xWave);
            }
        }
    }

    private void iterateOverSpatialDomain (Integer yWave, Integer xWave) {
        for (int ySpace=0; ySpace<ySize; ySpace++) {
            for (int xSpace=0; xSpace<xSize; xSpace++) {
                waveDomainRealValueComponent[yWave][xWave] += (spatialDomain[ySpace][xSpace]
                    * cos(2 * PI * ((1.0 * xWave * xSpace / xSize) + (1.0 * yWave * ySpace / ySize))))
                    / sqrt(xSize * ySize);
                waveDomainImaginaryComponent[yWave][xWave] -= (spatialDomain[ySpace][xSpace]
                    * sin(2 * PI * ((1.0 * xWave * xSpace / xSize) + (1.0 * yWave * ySpace / ySize))))
                    / sqrt(xSize * ySize);
                waveDomainAmplitudeComponent[yWave][xWave] = sqrt(
                    waveDomainRealValueComponent[yWave][xWave] * waveDomainRealValueComponent[yWave][xWave]
                        + waveDomainImaginaryComponent[yWave][xWave] * waveDomainImaginaryComponent[yWave][xWave]);
            }
        }
    }

    private void iterateOverSpectralDomain (Integer ySpace, Integer xSpace) {
        for (int yWave=0; yWave<ySize; yWave++) {
            for (int xWave=0; xWave<xSize; xWave++) {
                spatialDomain[ySpace][xSpace] += (
                    waveDomainRealValueComponent[yWave][xWave]
                        * cos(2 * PI * ((1.0 * xSpace * xWave / xSize) + (1.0 * ySpace * yWave / ySize)))
                  - waveDomainImaginaryComponent[yWave][xWave]
                        * sin(2 * PI * ((1.0 * xSpace * xWave / xSize) + (1.0 * ySpace * yWave / ySize))))
                  / sqrt(xSize * ySize);
            }
        }
    }
}


