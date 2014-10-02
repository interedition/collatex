package de.tud.kom.stringmatching.cli;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Collection;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import de.tud.kom.stringmatching.gst.GST;
import de.tud.kom.stringmatching.gst.GSTTile;
import de.tud.kom.stringmatching.gst.utils.GSTHighlighter;
import de.tud.kom.stringmatching.gst.utils.XMLHighlighter;
import de.tud.kom.stringmatching.shinglecloud.ShingleCloud;
import de.tud.kom.stringmatching.shinglecloud.ShingleCloudMatch;
import de.tud.kom.stringmatching.shinglecloud.ShingleCloud.ShingleCloudMarker;
import de.tud.kom.stringutils.preprocessing.DummyPreprocess;
import de.tud.kom.stringutils.preprocessing.EEBOPreprocessing;
import de.tud.kom.stringutils.preprocessing.SimplePreprocessing;
import de.tud.kom.stringutils.preprocessing.StopWordRemoval;
import de.tud.kom.stringutils.preprocessing.StopWordRemovalAndSimplePreprocessing;
import de.tud.kom.stringutils.tokenization.CharacterTokenizer;
import de.tud.kom.stringutils.tokenization.WordTokenizer;

/**
 * A command line interface for the gst algorithm.
 * 
 * Run GstCli -h for usage information.
 * 
 * @author Arno Mittelbach
 *
 */
public class GstCli {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/* parse options */
		OptionParser parser = new OptionParser();
		
		parser.accepts("h");
		parser.accepts("help");
		
		parser.accepts("needle").withRequiredArg();
		parser.accepts("haystack").withRequiredArg();
		parser.accepts("needledirect").withRequiredArg().ofType(String.class);
		parser.accepts("haystackdirect").withRequiredArg().ofType(String.class);

		
		// sc options
		parser.accepts("tilelength").withRequiredArg().ofType(Integer.class).defaultsTo(3);
		parser.accepts("preprocess").withRequiredArg().ofType(String.class).defaultsTo("simple");
		parser.accepts("tokenizer").withRequiredArg().ofType(String.class).defaultsTo("word");
		parser.accepts("xml");
		
		// output options
		parser.accepts("output").withRequiredArg().ofType(String.class).withValuesSeparatedBy(',');
		
		
		OptionSet options = null;
		
		try{
			options = parser.parse(args);
		} catch(Exception e){
			error(e + ": " + e.getMessage());
		}
		
		if(options.has("h") || options.has("help"))
			displayHelp();
		
		runComparison(options);
	}


	private static void runComparison(OptionSet options) {
		String needle = getNeedle(options);
		String haystack = getHaystack(options);
		
		GST gst = new GST(haystack);
		
		configureGst(gst, options);
		
		
		gst.match(needle);
		
		
		presentResults(gst, options);
	}


	private static String getHaystack(OptionSet options) {
		if(options.has("haystackdirect"))
			return (String) options.valueOf("haystackdirect");
		else
			return loadFile(options.valueOf("haystack"));
	}


	private static String getNeedle(OptionSet options) {
		if(options.has("needledirect"))
			return (String) options.valueOf("needledirect");
		else
			return loadFile(options.valueOf("needle"));
	}


	private static void presentResults(GST gst, OptionSet options) {
		if(null == options.valuesOf("output") || (options.valuesOf("output") instanceof Collection && ((Collection)options.valuesOf("output")).isEmpty()))
			displayFullResults(gst);
		else {
			Collection<String> output = (Collection<String>) options.valuesOf("output");
			for(String outputOpt : output){
				if("highlight".equals(outputOpt))
					displayHighlight(gst, options);
				if("containmentneedle".equals(outputOpt))
					System.out.println(gst.getContainmentInNeedle());
				if("containmenthaystack".equals(outputOpt))
					System.out.println(gst.getContainmentInHaystack());
			}
		}
	}



	private static void displayHighlight(GST gst, OptionSet options) {
		String needle = getNeedle(options);
		String haystack = getHaystack(options);
		
		GSTHighlighter highlighter;
		if(options.has("xml")){
			highlighter = new XMLHighlighter();
		} else {
			highlighter = new GSTHighlighter();
		}

		highlighter.setOpeningDelimiter("<matchedTile>");
		highlighter.setClosingDelimiter("</matchedTile>");
		highlighter.setMinimumTileLength(gst.getMinimumTileLength());
		highlighter.setPreprocessor(gst.getPreprocessingAlgorithm());
		highlighter.setTokenizer(gst.getTokenizer());
		System.out.println(highlighter.produceHighlightedText(haystack, needle));
	}


	private static void displayFullResults(GST gst) {
		System.out.println("Results ------------------------");
		System.out.println("Number of tiles: " + gst.getTiles().size());
		System.out.println("Containment in Needle: " + gst.getContainmentInNeedle());
		System.out.println("Containment in Haystack: " + gst.getContainmentInHaystack());
		
		System.out.println();
		System.out.println("Individual tiles -----------------------");
		int i = 1;
		for(GSTTile tile : gst.getTiles()){
			System.out.println("Tile " + i++ + " --------------------");
			System.out.println("Tile length: " + tile.getLength());
			System.out.println("Matched text:");
			System.out.println(tile.getText());
			System.out.println();
			System.out.println();
		}
	}


	private static void configureGst(GST gst,
			OptionSet options) {
		/* ngram */
		try{
			int tileLength = (Integer)options.valueOf("tilelength");
			gst.setMinimumTileLength(tileLength);
			
			if(options.has("xml"))
				gst.useXMLMode();
			
			String preprocess = (String) options.valueOf("preprocess");
			if("simple".equals(preprocess))
				gst.setPreprocessingAlgorithm(new SimplePreprocessing());
			else if("stop".equals(preprocess))
				gst.setPreprocessingAlgorithm(new StopWordRemoval());
			else if("simple+stop".equals(preprocess))
				gst.setPreprocessingAlgorithm(new StopWordRemovalAndSimplePreprocessing());
			else if("eebo".equals(preprocess))
				gst.setPreprocessingAlgorithm(new EEBOPreprocessing());
			else if("none".equals(preprocess))
				gst.setPreprocessingAlgorithm(new DummyPreprocess());
			else
				error("Unknown preprocessing algorithm");
			
			String tokenizer = (String) options.valueOf("tokenizer");
			if("word".equals(tokenizer))
				gst.setTokenizer(new WordTokenizer());
			else if("character".equals(tokenizer))
				gst.setTokenizer(new CharacterTokenizer());
			else
				error("Unknown tokenizer");
			
		}catch(Exception e){
			error(e + ": " + e.getMessage());
		}
	}


	private static String loadFile(Object valueOf) {
		if(! (valueOf instanceof String))
			error("Could not read input files");
		
		String filename = (String) valueOf;
		File file = new File(filename);
		if(! file.exists())
			error(filename + " does not exist.");
		
		StringBuilder input = new StringBuilder();
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file));
			String s;
			while((s = br.readLine()) != null) {
				input.append(s).append("\n");
			}
			br.close();
		} catch (Exception e) {
			error(e.getMessage());
		}

		return input.toString();
	}


	private static void error(String string) {
		System.out.println("An error occured: " + string);
		displayHelp();
	}


	private static void displayHelp() {
		System.out.println("Options:");
		System.out.println("--haystack: path to a file containing the haystack");
		System.out.println("--haystackdirect: specify haystack directly");
		System.out.println("-h: displays this help message");
		System.out.println("--help: displays this help message");
		System.out.println("--needle: path to a file containing the needle");
		System.out.println("--needledirect: specify needle directly");
		System.out.println("--output: Output options as comma separated list. output options include: highlight, containmentneedle, containmenthaystack. If no output options are given a default output is generated.");
		System.out.println("--preprocess: The preprocessing algorithm to be used (none, case folding and whitespace removal, stop wording, stop wording + simple combined, the preprocessing used in the TEI Comparator): none|simple|stop|simple+stop|eebo (default: simple)");
		System.out.println("--tokenizer: Use either word or character tokenization (default: word)");
		System.out.println("--tilelength: The minimal tile length to use (default: 3)");
		System.out.println("--xml");
		System.exit(0);
	}

}
