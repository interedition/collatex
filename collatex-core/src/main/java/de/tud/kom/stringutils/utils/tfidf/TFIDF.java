package de.tud.kom.stringutils.utils.tfidf;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.Map.Entry;

import de.tud.kom.stringutils.preprocessing.Preprocess;
import de.tud.kom.stringutils.preprocessing.SimplePreprocessing;
import de.tud.kom.stringutils.tokenization.Tokenizer;
import de.tud.kom.stringutils.tokenization.WordTokenizer;

public class TFIDF {

	int nrOfDocuments = 0;
	
	private Preprocess preprocessing;
	private Tokenizer tokenizer;
	
	private Map<String, Integer> globalTermMap = new HashMap<String, Integer>();
	private Map<String, Integer> globalDocumentsContainingTerm = new HashMap<String, Integer>();
	private Map<UUID, Map<String, Integer>> documentTermMap = new HashMap<UUID, Map<String,Integer>>();
	private Map<UUID, Integer> documentTermCounter = new HashMap<UUID, Integer>();
	private Map<String, Double> weights = new HashMap<String, Double>();

	private boolean compiled = false;
	
	public TFIDF(){
		this.preprocessing = new SimplePreprocessing();
		this.tokenizer = new WordTokenizer();
	}
	
	public TFIDF(Preprocess preprocessing, Tokenizer tokenizer){
		this.preprocessing = preprocessing;
		this.tokenizer = tokenizer;
	}
	
	public Preprocess getPreprocessing() {
		return preprocessing;
	}

	public void setPreprocessing(Preprocess preprocessing) {
		this.preprocessing = preprocessing;
	}

	public Tokenizer getTokenizer() {
		return tokenizer;
	}

	public void setTokenizer(Tokenizer tokenizer) {
		this.tokenizer = tokenizer;
	}

	public void compile(){
		this.compiled  = true;
		
		for(String term : globalTermMap.keySet()){
			double weight = 0;
			for(UUID id : documentTermCounter.keySet()){
				double tfidf = getTFIDF(term, id);
				weight += tfidf;
			}
			weights.put(term, weight);
		}
	}
	
	public UUID getDocumentId(String doc){
	    MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		md5.update(doc.getBytes());

		return UUID.nameUUIDFromBytes(md5.digest());
	}
	
	public void addDocument(String doc){
		if(compiled)
			throw new IllegalStateException("Already compiled");
		
		nrOfDocuments++;

		String preprocessed = preprocessing.preprocessInput(doc);
		String[] tokenized = tokenizer.tokenize(preprocessed);
		UUID id = getDocumentId(doc);
		
		Map<String, Integer> termMap = new HashMap<String, Integer>();
		
		for(String token : tokenized){
			if(! termMap.containsKey(token))
				termMap.put(token, 1);
			else
				termMap.put(token, termMap.get(token) + 1);
		}
		
		/* store as document termmap */
		documentTermMap.put(id, termMap);
		documentTermCounter.put(id, tokenized.length);
		
		/* update global term map */
		for(String term : termMap.keySet()){
			if(! globalTermMap.containsKey(term))
				globalTermMap.put(term, termMap.get(term));
			else
				globalTermMap.put(term, globalTermMap.get(term) + termMap.get(term));
			
			if(! globalDocumentsContainingTerm.containsKey(term))
				globalDocumentsContainingTerm.put(term, 1);
			else
				globalDocumentsContainingTerm.put(term, globalDocumentsContainingTerm.get(term) + 1);
		}
	}
	
	public double getWeight(String term){
		return weights.get(term);
	}
	
	public double getTFIDF(String term, UUID id){
		return getTermFrequency(term, id) * getInverseDocumentFrequency(term);
	}
	
	public double getInverseDocumentFrequency(String term){
		double inverseDocFreq = Math.log10(getNumberOfDocuments() / (double) getNrOfDocumentsContainingTerm(term));
		return inverseDocFreq;
	}
	
	public double getTermFrequency(String term, UUID id){
		Map<String, Integer> termMap = getDocumentTermMap(id);
		if(! termMap.containsKey(term))
			return 0;
		
		double termFrequenzy = termMap.get(term) / (double) getNumberOfTermsInDocument(id);
		return termFrequenzy;
	}
	
	public Map<String, Integer> getDocumentTermMap(UUID id){
		if(! documentTermMap.containsKey(id))
			throw new IllegalArgumentException("unknown id");
		
		return documentTermMap.get(id);
	}
	
	public Integer getNumberOfTermsInDocument(UUID id){
		if(! documentTermCounter.containsKey(id))
			throw new IllegalArgumentException("unknown id");
		
		return documentTermCounter.get(id);
	}
	
	public int getNumberOfDocuments(){
		return nrOfDocuments;
	}
	
	public int getNrOfDocumentsContainingTerm(String term){
		if(! globalDocumentsContainingTerm.containsKey(term))
			return 0;
		return globalDocumentsContainingTerm.get(term);
	}
	
	public void printStatistics(){
		TreeMap<String, Double> terms = new TreeMap<String, Double>();
		for(String term : globalTermMap.keySet())
			terms.put(term, getWeight(term));
		
		for(Entry<String, Double> e : terms.entrySet())
			System.out.println(e.getKey() + "\t\t" + e.getValue());
	}
}
