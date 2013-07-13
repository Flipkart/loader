package perf.sample.resource;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import perf.sample.search.SearchService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

@Path("/memoryHogger")
public class MemoryHoggerResource {
    private SearchService searchService;
    private static List<String> searchedNames;
    static {
        searchedNames = new ArrayList<String>();
    }
    public MemoryHoggerResource(int searchPoolSize) {
        searchService = new SearchService(searchPoolSize);
    }
    private static Logger logger = LoggerFactory.getLogger(MemoryHoggerResource.class);
    /**
     * Get Agent Registration Information.
     * Mostly called by Loader-Server at its boot time to confirm the availability of agent
     * @return
     * @throws java.io.IOException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> welcome(@QueryParam("name") String namePattern) throws Exception {
        List<String> names = searchService.search(namePattern,0);
        searchedNames.addAll(names);
        return names;
    }

    @Path("/size")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public long namesSize() throws Exception {
        return searchedNames.size();
    }
}