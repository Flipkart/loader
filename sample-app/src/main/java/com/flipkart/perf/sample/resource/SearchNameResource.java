package com.flipkart.perf.sample.resource;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.yammer.dropwizard.jersey.params.IntParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.flipkart.perf.sample.search.SearchService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/search")
public class SearchNameResource {
    private SearchService searchService;
    public SearchNameResource(int searchPoolSize) {
        searchService = new SearchService(searchPoolSize);
    }

    static class Result {
        private List<String> names;
        private int namesSearched;

        public List<String> getNames() {
            return names;
        }

        public Result setNames(List<String> names) {
            this.names = names;
            return this;
        }

        public int getNamesSearched() {
            return namesSearched;
        }

        public Result setNamesSearched(int namesSearched) {
            this.namesSearched = namesSearched;
            return this;
        }
    }

    private static Logger logger = LoggerFactory.getLogger(SearchNameResource.class);
    /**
     * Get Agent Registration Information.
     * Mostly called by Loader-Server at its boot time to confirm the availability of agent
     * @return
     * @throws java.io.IOException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String search(@QueryParam("name") String namePattern, @QueryParam("delay") @DefaultValue("0") IntParam delay) throws Exception {
        List<String> names = searchService.search(namePattern, delay.get());
        Result result = new Result().setNames(names).setNamesSearched(names.size());
        ObjectMapper mapper = buildObjectMapper();
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
    }

    private ObjectMapper buildObjectMapper() {
        return new ObjectMapper();
    }

}