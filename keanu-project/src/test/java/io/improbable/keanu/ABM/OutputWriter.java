package io.improbable.keanu.ABM;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;

public class OutputWriter {

    FileWriter file;

    public void initialiseJSON(int XSIZE, int YSIZE, Double preyReproductionGradient, Double preyReproductionConstant,
                               Double predReproductionGradient) {

        JSONObject output = new JSONObject();

        output.put("XSIZE", XSIZE);
        output.put("YSIZE", YSIZE);
        output.put("preyReproductionGradient", preyReproductionGradient);
        output.put("preyReproductionConstant", preyReproductionConstant);
        output.put("predReproductionGradient", predReproductionGradient);

        try {file = new FileWriter("output.json");
            file.write(output.toJSONString());
            file.append("\n");
        } catch (IOException e) {e.printStackTrace();}
    }

    public void dumpToJSON(Double time, Integer numberOfPrey, Integer numberOfPredators, Agent[][] grid) {

        JSONObject output = new JSONObject();

        output.put("time", time.toString());
        output.put("numberOfPrey", numberOfPrey.toString());
        output.put("numberOfPredators", numberOfPredators.toString());

        JSONArray agents = new JSONArray();
        for (int i=0; i<grid.length; i++) {
            for (int j=0; j<grid[0].length; j++) {
                Agent location = grid[i][j];
                if (location instanceof Prey) {
                    agents.add("Prey:" + i + ":" + j);
                } else if (location instanceof Predator) {
                    agents.add("Predator:" + i + ":" + j);
                }
            }
        }
        output.put("agents", agents);

        try {file.append(output.toJSONString());
            file.append("\n");
        } catch (IOException e) {e.printStackTrace();}
    }
}
