package com.flipkart.perf.server.health.metric;

import com.flipkart.perf.common.constant.MathConstant;
import com.flipkart.perf.common.util.Clock;
import com.flipkart.perf.server.util.ObjectMapperUtil;
import org.apache.commons.lang.RandomStringUtils;
import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import org.rrd4j.core.*;
import org.rrd4j.graph.RrdGraph;
import org.rrd4j.graph.RrdGraphDef;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;

/**
 * Follow https://code.google.com/p/rrd4j/wiki/Tutorial when in doubt
 */
public class RRD4JArchivingEngine extends MetricArchivingEngine{
    private String rrdBasePath;
    private Map<String, String> metricPathMap;
    private Map<String, String> metricDataSourceMap;
    private Map<String, RrdDb> metricRddDBMap;
    private static final String RRD_BASE_PATH = "basePath";

    public RRD4JArchivingEngine(Map<String, Object> config) throws InterruptedException, IOException {
        super(config);
        metricPathMap = new LinkedHashMap<String, String>();
        metricRddDBMap = new LinkedHashMap<String, RrdDb>();
        metricDataSourceMap = new HashMap<String, String>();

        File rrdBasePath = new File(this.rrdBasePath = config.get(RRD_BASE_PATH).toString());
        if(!rrdBasePath.exists())
            rrdBasePath.mkdirs();

        for(File metricSourceFile : rrdBasePath.listFiles()) {
            if(metricSourceFile.getName().endsWith(".rrd")) {
                String metricName = metricSourceFile.getName().replace(".rrd","");
                metricPathMap.put(metricName, metricSourceFile.getAbsolutePath());
                RrdDb rrdDb = new RrdDb(metricSourceFile.getAbsolutePath());
                metricRddDBMap.put(metricName, rrdDb);
                metricDataSourceMap.put(metricName,rrdDb.getRrdDef().getDsDefs()[0].getDsName());
            }
        }
    }

    /**
     * Will be used in case user wants the persistence to be done in bulk to avoid lot of frequent writes
     * @param resourceMetrics
     * @throws IOException
     */
    public void archive(List<ResourceMetric> resourceMetrics) throws IOException{
        logger.info(ObjectMapperUtil.instance().defaultPrettyPrintingWriter().writeValueAsString(resourceMetrics));
        for(ResourceMetric resourceMetric : resourceMetrics) {
            for(Metric metric : resourceMetric.getMetrics()) {
                if(!metricRddDBMap.containsKey(metric.getName())) {

                    String newDataSourceNameForMetric = getUniqueRandomDataSourceName();
                    String metricRRDFilePath = this.rrdBasePath + File.separator + metric.getName() + ".rrd";

                    DsDef dsDef = new DsDef(newDataSourceNameForMetric, DsType.valueOf(metric.getMetricType().toString()), 600, Double.NaN, Double.NaN);
                    RrdDef rrdDef = new RrdDef(metricRRDFilePath);
                    rrdDef.addDatasource(dsDef);
                    rrdDef.setStartTime((int)((resourceMetric.getAt() - 1000) / MathConstant.THOUSAND));

                    /*
                    Following Archives should be based on Metric Data Type. Keeping it as is for the time being
                     */
                    // Assuming every point comes in 30 seconds
                    rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, 1, 2 * 60 * 24 * 1); // archiving it for 1 day
                    rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, 10, 12 * 24 * 7); // Archiving for 7 days
                    rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, 120, 24 * 30); // archiving for 30 days
                    rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, 3600, 30 * 12 * 2); // archiving roughly for 2 years

                    rrdDef.addArchive(ConsolFun.TOTAL, 0.5, 1, 2 * 60 * 24 * 1); // archiving it for 1 day
                    rrdDef.addArchive(ConsolFun.TOTAL, 0.5, 10, 12 * 24 * 7); // Archiving for 7 days
                    rrdDef.addArchive(ConsolFun.TOTAL, 0.5, 120, 24 * 30); // archiving for 30 days
                    rrdDef.addArchive(ConsolFun.TOTAL, 0.5, 3600, 30 * 12 * 2); // archiving roughly for 2 years
                    RrdDb rrdDb = new RrdDb(rrdDef);

                    metricRddDBMap.put(metric.getName(), rrdDb);
                    metricPathMap.put(metric.getName(), metricRRDFilePath);
                    metricDataSourceMap.put(metric.getName(), newDataSourceNameForMetric);
                }

                RrdDb rrdDb = new RrdDb(metricPathMap.get(metric.getName()));
                Sample sample = rrdDb.createSample();
                sample.setAndUpdate((int)(resourceMetric.getAt()/ MathConstant.THOUSAND) + ":" +metric.getValue());
                rrdDb.close();
            }
        }
    }

    private String getUniqueRandomDataSourceName() {
        String uniqueString = RandomStringUtils.randomAlphanumeric(20);
        while(metricDataSourceMap.values().contains(uniqueString))
            uniqueString = RandomStringUtils.randomAlphanumeric(20);
        return uniqueString;
    }

    @Override
    public List<String> metrics() throws IOException {
        return new ArrayList<String>(metricPathMap.keySet());
    }

    /**
     *
     * @param consolFuc AVERAGE MIN MAX FIRST LAST TOTAL
     * @param metricName its a data source in RRD4j
     * @param startTimeSec in seconds
     * @param endTimeSec in seconds
     * @return
     * @throws IOException
     */
    @Override
    public String fetchMetrics(String metricName, String consolFuc, long startTimeSec, long endTimeSec) throws IOException {
        RrdDb rrdDb = metricRddDBMap.get(metricName);
        int dataSourceIndex = rrdDb.getDatasource(metricDataSourceMap.get(metricName)).getDsIndex();
        FetchRequest fetchRequest = rrdDb.createFetchRequest(ConsolFun.valueOf(consolFuc), startTimeSec, endTimeSec);
        FetchData fetchData = fetchRequest.fetchData();
        StringBuffer buffer = new StringBuffer();
        long[] timestamps = fetchData.getTimestamps();
        double[][] values = fetchData.getValues();
        for (int row = 0; row < fetchData.getRowCount(); row++) {
            Map<Long, Double> instance = new HashMap<Long, Double>();
            instance.put(timestamps[row], values[dataSourceIndex][row]);
            buffer.append(ObjectMapperUtil.instance().writeValueAsString(instance) + "\n");
        }
        return buffer.toString();
    }

    @Override
    public InputStream fetchMetricsImage(String metricName, String consolFuc, long startTimeSec, long endTimeSec) throws IOException {
        RrdGraphDef graphDef = new RrdGraphDef();
        graphDef.setTimeSpan(startTimeSec, endTimeSec);
        graphDef.datasource(metricName, metricPathMap.get(metricName), metricDataSourceMap.get(metricName), ConsolFun.valueOf(consolFuc));
        graphDef.line(metricName, new Color(0xFF, 0, 0), null, 2);
        graphDef.setTitle(metricName);
        String imageFilePath = "/tmp/"+metricName+".ig";
        graphDef.setFilename(imageFilePath);
        RrdGraph graph = new RrdGraph(graphDef);
        BufferedImage bi = new BufferedImage(100,100,BufferedImage.TYPE_INT_RGB);
        graph.render(bi.getGraphics());
        return new FileInputStream(imageFilePath);
    }


    public static void main(String[] args ) throws IOException, InterruptedException {
        Map<String, Object> info = new HashMap<String, Object>();
        info.put(RRD_BASE_PATH, "/tmp/rrdPath");
        RRD4JArchivingEngine engine = new RRD4JArchivingEngine(info);
        long startTime = Clock.milliTick()- 200000;
        System.out.println("Start Time : "+Clock.milliTick() );
        for(int i=1;i<=10;i++) {
            engine.archive(new ResourceMetric().addMetrics("speed",new Random().nextInt(100000)));
            engine.archive(new ResourceMetric().addMetrics("speed2",new Random().nextInt(100000)));
            engine.archive(new ResourceMetric().addMetrics("speed3",new Random().nextInt(100000)));
            Clock.sleep(1000);
        }
        System.out.println(engine.fetchMetrics("speed", "TOTAL", (int) (startTime / 1000), (int) (Clock.milliTick() / 1000)));
        System.out.println(engine.fetchMetrics("speed2", "TOTAL", (int) (startTime / 1000), (int) (Clock.milliTick() / 1000)));
        System.out.println(engine.fetchMetrics("speed3", "TOTAL", (int) (startTime / 1000), (int) (Clock.milliTick() / 1000)));

/*
        // Creating RRd Database
        RrdDef rrdDef = new RrdDef("./test.rrd");
        rrdDef.setStartTime(920804400L);
        rrdDef.addDatasource("speed", DsType.COUNTER, 600, Double.NaN, Double.NaN);
        rrdDef.addArchive(ConsolFun.TOTAL, 0.5, 1, 24);
        rrdDef.addArchive(ConsolFun.TOTAL, 0.5, 6, 10);

        RrdDb rrdDb = new RrdDb(rrdDef);
        for(DsDef dsDef : rrdDef.getDsDefs()){
            System.out.println(ObjectMapperUtil.instance().defaultPrettyPrintingWriter().writeValueAsString(dsDef));
        }
        rrdDb.close();



        // Pushing Data
        String file = "./test10.rrd";
        RrdDef rrdDef = new RrdDef(file);
        int startTime = 920804700;
        rrdDef.setStartTime(920804700 - 300);
        rrdDef.addDatasource("speed", DsType.GAUGE, 600, Double.NaN, Double.NaN);
        // Assuming every point comes in 30 seconds
        rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, 1, 2 * 60 * 24 * 1); // archiving it for 1 day
        rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, 10, 12 * 24 * 7); // Archiving for 7 days
        rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, 120, 24 * 30); // archiving for 30 days
        rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, 3600, 30 * 12 * 2); // archiving roughly for 2 years

        rrdDef.addArchive(ConsolFun.TOTAL, 0.5, 1, 2 * 60 * 24 * 1); // archiving it for 1 day
        rrdDef.addArchive(ConsolFun.TOTAL, 0.5, 10, 12 * 24 * 7); // Archiving for 7 days
        rrdDef.addArchive(ConsolFun.TOTAL, 0.5, 120, 24 * 30); // archiving for 30 days
        rrdDef.addArchive(ConsolFun.TOTAL, 0.5, 3600, 30 * 12 * 2); // archiving roughly for 2 years

        RrdDb rrdDb = new RrdDb(rrdDef);
        for(DsDef dsDef : rrdDef.getDsDefs()){
            System.out.println(ObjectMapperUtil.instance().defaultPrettyPrintingWriter().writeValueAsString(dsDef));
        }
        rrdDb.close();

        rrdDb = new RrdDb(file);
        Sample sample = rrdDb.createSample();

        int repeats = 10000;
        for(int i=0,j=startTime;i<repeats;i++, j+=300) {
            String line = j+":"+(10000 + i);
            logger.info(line);
            sample.set(line).update();
        }
        rrdDb.close();

        rrdDb = new RrdDb(file);
        int dataSourceIndex = rrdDb.getDatasource("speed").getDsIndex();
        FetchRequest fetchRequest = rrdDb.createFetchRequest(ConsolFun.AVERAGE, startTime - 300, startTime + (repeats * 300));
        FetchData fetchData = fetchRequest.fetchData();
        System.out.println("Avg "+fetchData.dump());




        fetchRequest = rrdDb.createFetchRequest(ConsolFun.TOTAL, startTime - 300, startTime + (repeats * 300), 300 * 10);
        fetchData = fetchRequest.fetchData();
        System.out.println("Tot "+fetchData.dump());
        rrdDb.close();
*/


/*
        RrdGraphDef graphDef = new RrdGraphDef();
        graphDef.setTimeSpan(920804400L, 920808000L);
        graphDef.datasource("myspeed", "./test.rrd", "speed", ConsolFun.TOTAL);
        graphDef.line("myspeed", new Color(0xFF, 0, 0), null, 2);
        graphDef.setFilename("./speed.ig");
        RrdGraph graph = new RrdGraph(graphDef);
        BufferedImage bi = new BufferedImage(100,100,BufferedImage.TYPE_INT_RGB);
        graph.render(bi.getGraphics());
*/
    }
}
