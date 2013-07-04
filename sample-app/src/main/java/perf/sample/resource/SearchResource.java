package perf.sample.resource;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import perf.sample.search.SearchService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;

@Path("/search")
public class SearchResource {
    private SearchService searchService;
    public SearchResource(int searchPoolSize) {
        searchService = new SearchService(searchPoolSize);
    }
    private static Logger logger = LoggerFactory.getLogger(SearchResource.class);
    /**
     * Get Agent Registration Information.
     * Mostly called by Loader-Server at its boot time to confirm the availability of agent
     * @return
     * @throws java.io.IOException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> welcome(@QueryParam("name") String namePattern) throws Exception {
        return searchService.search(namePattern);
    }

}