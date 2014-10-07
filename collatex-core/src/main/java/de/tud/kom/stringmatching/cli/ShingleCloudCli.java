package de.tud.kom.stringmatching.cli;


import com.google.common.base.Functions;
import de.tud.kom.stringmatching.shinglecloud.ShingleCloud;
import de.tud.kom.stringmatching.shinglecloud.ShingleCloud.ShingleCloudMarker;
import de.tud.kom.stringmatching.shinglecloud.ShingleCloudMatch;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Collection;

/**
 * A command line interface for the shingle cloud algorithm.
 * 
 * Run ShingleCloudCli -h for usage information.
 * 
 * @author Arno Mittelbach
 *
 */
public class ShingleCloudCli {

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
		parser.accepts("ngram").withRequiredArg().ofType(Integer.class).defaultsTo(3);
		parser.accepts("minimum1").withRequiredArg().ofType(Integer.class).defaultsTo(3);
		parser.accepts("maximum0").withRequiredArg().ofType(Integer.class).defaultsTo(1);
		parser.accepts("preprocess").withRequiredArg().ofType(String.class).defaultsTo("simple");
		parser.accepts("tokenizer").withRequiredArg().ofType(String.class).defaultsTo("word");
		
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
		
		
		ShingleCloud shingleCloud = new ShingleCloud(haystack);
		
		configureShingleCloud(shingleCloud, options);
		
		
		shingleCloud.match(needle);
		
		
		presentResults(shingleCloud, options);
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


	private static void presentResults(ShingleCloud shingleCloud, OptionSet options) {
    for(ShingleCloudMarker marker : shingleCloud.getShingleCloud())
      if(ShingleCloudMarker.NoMatch.equals(marker))
        System.out.print("0, ");
      else if(ShingleCloudMarker.Match.equals(marker))
        System.out.print("1, ");
    System.out.println();
	}

	private static void configureShingleCloud(ShingleCloud shingleCloud,
			OptionSet options) {
		/* ngram */
		try{
			int ngram = (Integer)options.valueOf("ngram");
			shingleCloud.setNGramSize(ngram);

			int minimum1 = (Integer)options.valueOf("minimum1");
			shingleCloud.setMinimumNumberOfOnesInMatch(minimum1);

			int maximum0 =(Integer)options.valueOf("maximum0");
			shingleCloud.setMaximumNumberOfZerosBetweenMatches(maximum0);

      shingleCloud.setPreprocessingAlgorithm(Functions.<String>identity());

      shingleCloud.setTokenizer(ShingleCloud.WORD_TOKENIZER);
			
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
		System.out.println("--minimum1: the minimum number of ones needed to denote a match (default: 3)");
		System.out.println("--maximum0: the maximum number of consecutive zeroes alllowed in a match (default: 1)");
		System.out.println("--needle: path to a file containing the needle");
		System.out.println("--needledirect: specify needle directly");
		System.out.println("--ngram: the ngram size to use (default: 3)");
		System.out.println("--output: Output options as comma separated list. output options include: shinglecloud, containmentneedle, containmenthaystack, jaccardShingle, jaccardToken, matchedTokens, matchedShingles, tokensInNeedle, tokensInHaystack, shinglesInNeedle, shinglesInHaystack. If no output options are given a default output is generated.");
		System.out.println("--preprocess: The preprocessing algorithm to be used (none, case folding and whitespace removal, stop wording, stop wording + simple combined, the preprocessing used in the TEI Comparator): none|simple|stop|simple+stop|eebo (default: simple)");
		System.out.println("--tokenizer: Use either word or character tokenization (default: word)");
		System.exit(0);
	}

}
