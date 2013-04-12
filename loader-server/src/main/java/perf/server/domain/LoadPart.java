package perf.server.domain;
import com.open.perf.domain.Load;

import java.util.List;

/**
 * Represents a Performance LoadPart in a Performance Run
 */
public class LoadPart {
    private List<String> agents;
    private List<String> classes;
    private Load load;

    public List<String> getAgents() {
        return agents;
    }

    public LoadPart setAgents(List<String> agents) {
        this.agents = agents;
        return this;
    }

    public List<String> getClasses() {
        return classes;
    }

    public LoadPart setClasses(List<String> classes) {
        this.classes = classes;
        return this;
    }

    public Load getLoad() {
        return load;
    }

    public LoadPart setLoad(Load load) {
        this.load = load;
        return this;
    }
}
