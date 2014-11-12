package perf.server.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.perf.domain.Group;
import com.flipkart.perf.domain.GroupFunction;
import com.flipkart.perf.domain.Load;
import com.flipkart.perf.server.domain.LoadPart;
import com.flipkart.perf.server.domain.PerformanceRun;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Useful to create run json by providing very little details about your performance run.
 * It generates a template which you can fill and save as performance Run
 */
public class PerformanceShortInput {
    public static class GroupShortInput {
        private String groupName;
        private List<String> functions;

        public String getGroupName() {
            return groupName;
        }

        public GroupShortInput setGroupName(String groupName) {
            this.groupName = groupName;
            return this;
        }

        public List<String> getFunctions() {
            return functions;
        }

        public GroupShortInput setFunctions(List<String> functions) {
            this.functions = functions;
            return this;
        }

        public Group buildGroup() {
            Group group = new Group().setName(groupName);
            for(String function : functions) {
                GroupFunction groupFunction = new GroupFunction().setFunctionClass(function).setFunctionalityName("GIVE_Functionality_Name");

            }
            return group;
        }
    }
    private String runName;
    private List<GroupShortInput> groups;

    public String getRunName() {
        return runName;
    }

    public PerformanceShortInput setRunName(String runName) {
        this.runName = runName;
        return this;
    }

    public List<GroupShortInput> getGroups() {
        return groups;
    }

    public PerformanceShortInput setGroups(List<GroupShortInput> groups) {
        this.groups = groups;
        return this;
    }

    public PerformanceRun buildPerformanceRun() {
        PerformanceRun performanceRun = new PerformanceRun();

        List<Group> groups = new ArrayList<Group>();
        for(GroupShortInput groupShortInput : this.getGroups()) {
            Group group = new Group().setName(groupShortInput.groupName);
        }
        Load load = new Load();

        LoadPart loadPart = new LoadPart();
        loadPart.setAgents(1);

        performanceRun.setRunName(this.runName);

        return performanceRun;
    }

    public static void main(String[] args) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        GroupShortInput g1 = new GroupShortInput();
        g1.setGroupName("G1").setFunctions(Arrays.asList(new String[]{"F1", "F2"}));

        GroupShortInput g2 = new GroupShortInput();
        g2.setGroupName("G2").setFunctions(Arrays.asList(new String[]{"F3", "F4"}));

        PerformanceShortInput input = new PerformanceShortInput();
        input.setRunName("SampleTemplate").setGroups(Arrays.asList(new GroupShortInput[]{g1,g2}));

        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(input));
    }
}
