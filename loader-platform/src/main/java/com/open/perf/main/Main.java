package com.open.perf.main;

import com.open.perf.domain.Loader;
import org.codehaus.jackson.map.ObjectMapper;
import java.io.File;

public class Main {
	public static void main(String[] args) throws Exception{
		if (args.length < 2){
			System.out.println("You need to give either json file or json on commandline");
			System.exit(1);
		}

        Loader loader = null;
		if (args[0].equalsIgnoreCase("-f")){
			loader = parseFileJson(args[1]);
			System.out.println("Loader Created : " + loader.toString() );
		} else {
			loader = parseCmdJson(args[1]);
			System.out.println("Loader Created : " + loader.toString() );
		}
        loader.start();
	}

    public static Loader parseCmdJson(String json) throws Exception{
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, Loader.class);
    }

    public static Loader parseFileJson(String filePath) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File(filePath), Loader.class);
    }

}
