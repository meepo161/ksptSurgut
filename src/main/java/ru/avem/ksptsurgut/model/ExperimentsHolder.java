package ru.avem.ksptsurgut.model;

import ru.avem.ksptsurgut.Constants;

import java.util.ArrayList;
import java.util.List;

public class ExperimentsHolder {

    private static final List<ru.avem.ksptsurgut.model.Experiment> experiments = new ArrayList<>();

    static {
        experiments.add(new ru.avem.ksptsurgut.model.Experiment("layouts/phase3/experiment1ViewPhase3.fxml",
                Constants.Experiments.EXPERIMENT1_NAME));

        experiments.add(new ru.avem.ksptsurgut.model.Experiment("layouts/phase3/experiment2ViewPhase3.fxml",
                Constants.Experiments.EXPERIMENT2_NAME));

        experiments.add(new ru.avem.ksptsurgut.model.Experiment("layouts/phase3/experiment2ViewPhase3.fxml",
                Constants.Experiments.EXPERIMENT2_NAME));

        experiments.add(new ru.avem.ksptsurgut.model.Experiment("layouts/phase3/experiment3ViewPhase3.fxml",
                Constants.Experiments.EXPERIMENT3_NAME));

        experiments.add(new ru.avem.ksptsurgut.model.Experiment("layouts/phase3/experiment4ViewPhase3.fxml",
                Constants.Experiments.EXPERIMENT4_NAME));

        experiments.add(new ru.avem.ksptsurgut.model.Experiment("layouts/phase3/experiment5ViewPhase3.fxml",
                Constants.Experiments.EXPERIMENT5_NAME));

        experiments.add(new ru.avem.ksptsurgut.model.Experiment("layouts/phase3/experiment6ViewPhase3.fxml",
                Constants.Experiments.EXPERIMENT6_NAME));
    }

    public static ru.avem.ksptsurgut.model.Experiment getExperimentByName(String name) {
        for (ru.avem.ksptsurgut.model.Experiment experiment : experiments) {
            if (experiment.getTitle().equals(name)) {
                return experiment;
            }
        }
        throw new NullPointerException("Проверьте правильность названия испытания");
    }

    public static List<String> getNamesOfExperiments() {
        List<String> names = new ArrayList<>();

        for (ru.avem.ksptsurgut.model.Experiment experiment : experiments) {
            names.add(experiment.getTitle());
        }

        return names;
    }
}
