package de.tud.kom.stringmatching.cli;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Collection;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
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
		if(null == options.valuesOf("output") || (options.valuesOf("output") instanceof Collection && ((Collection)options.valuesOf("output")).isEmpty()))
			displayFullResults(shingleCloud);
		else {
			Collection<String> output = (Collection<String>) options.valuesOf("output");
			for(String outputOpt : output){
				outputOpt = outputOpt.toLowerCase();
				if("shinglecloud".equals(outputOpt))
					displayShingleCloud(shingleCloud);
				if("containmentneedle".equals(outputOpt))
					System.out.println(shingleCloud.getContainmentInNeedle());
				if("containmenthaystack".equals(outputOpt))
					System.out.println(shingleCloud.getContainmentInHaystack());
				if("matchedtokens".equals(outputOpt))
					System.out.println(shingleCloud.getNumberOfMatchingTokens());
				if("matchedshingles".equals(outputOpt))
					System.out.println(shingleCloud.getNumberOfMatchingShingles());
				if("jaccardshingle".equals(outputOpt))
					System.out.println(shingleCloud.getJaccardMeasureForShingles());
				if("jaccardtoken".equals(outputOpt))
					System.out.println(shingleCloud.getJaccardMeasureForTokens());
				if("tokensInNeedle".equals(outputOpt))
					System.out.println(shingleCloud.getNeedleShingles().getNumberOfTokens());
				if("shinglesInNeedle".equals(outputOpt))
					System.out.println(shingleCloud.getNeedleShingles().size());
				if("tokensInHaystack".equals(outputOpt))
					System.out.println(shingleCloud.getHaystackShingles().getNumberOfTokens());
				if("shinglesInHaystack".equals(outputOpt))
					System.out.println(shingleCloud.getHaystackShingles().size());
			}
		}
	}



	private static void displayShingleCloud(ShingleCloud shingleCloud) {
		for(ShingleCloudMarker marker : shingleCloud.getShingleCloud())
			if(ShingleCloudMarker.NoMatch.equals(marker))
				System.out.print("0, ");
			else if(ShingleCloudMarker.Match.equals(marker))
				System.out.print("1, ");
		System.out.println();
	}


	private static void displayFullResults(ShingleCloud shingleCloud) {
		System.out.println("Results ------------------------");
		System.out.println("Tokens in haystack: " + shingleCloud.getHaystackShingles().getNumberOfTokens());
		System.out.println("Tokens in needle: " + shingleCloud.getNeedleShingles().getNumberOfTokens());
		System.out.println("Number of matches: " + shingleCloud.getMatches().size());
		System.out.println("Number of matching shingles: " + shingleCloud.getNumberOfMatchingShingles());
		System.out.println("Number of matching tokens: " + shingleCloud.getNumberOfMatchingTokens());
		System.out.println("Containment in Needle: " + shingleCloud.getContainmentInNeedle());
		System.out.println("Containment in Haystack: " + shingleCloud.getContainmentInHaystack());
		
		System.out.println();
		System.out.println("Individual matchs -----------------------");
		int i = 1;
		for(ShingleCloudMatch match : shingleCloud.getMatches()){
			System.out.println("Match " + i++ + " --------------------");
			System.out.println("Matches rating: " + match.getRating());
			System.out.println("Containment in Needle: " + match.getContainmentInNeedle());
			System.out.println("Containment in Haystack: " + match.getContainmentInHaystack());
			System.out.println("Matched text:");
			System.out.println(match.getMatchedShingles());
			System.out.println();
			System.out.println();
		}
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

			String preprocess = (String) options.valueOf("preprocess");
			if("simple".equals(preprocess))
				shingleCloud.setPreprocessingAlgorithm(new SimplePreprocessing());
			else if("stop".equals(preprocess))
				shingleCloud.setPreprocessingAlgorithm(new StopWordRemoval());
			else if("simple+stop".equals(preprocess))
				shingleCloud.setPreprocessingAlgorithm(new StopWordRemovalAndSimplePreprocessing());
			else if("eebo".equals(preprocess))
				shingleCloud.setPreprocessingAlgorithm(new EEBOPreprocessing());
			else if("none".equals(preprocess))
				shingleCloud.setPreprocessingAlgorithm(new DummyPreprocess());
			else
				error("Unknown preprocessing algorithm");
			
			String tokenizer = (String) options.valueOf("tokenizer");
			if("word".equals(tokenizer))
				shingleCloud.setTokenizer(new WordTokenizer());
			else if("character".equals(tokenizer))
				shingleCloud.setTokenizer(new CharacterTokenizer());
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
