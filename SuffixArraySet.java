package empsyn.sa;

import empsyn.util.Utility;

public class SuffixArraySet {
	public SuffixArray[] sas;
	public Vocabulary dict;
	public int length = -1;
	public Configuration conf ;

	public SuffixArraySet(String[] saFilenames, String vocabFilename, Configuration conf) {
		try {
			this.conf = conf ;
			dict = new Vocabulary();
			dict.load(vocabFilename);

			sas = new SuffixArray[saFilenames.length];
			length = saFilenames.length;
			for (int i = 0; i < sas.length; i++)
				sas[i] = new SuffixArray(saFilenames[i], dict);
		} catch(Exception e) {
			System.err.println("Error while loading suffix arrays...");
			e.printStackTrace();
		}
	}
	
	public int[] getIDs (String[] words) {
		return dict.getIDs(words);
	}
	
	public SuffixArrayRange[] findPhrase (int[] ids) {
		SuffixArrayRange[] retRange = new SuffixArrayRange[conf.DATA_SIZE];
		for (int i = 0; i < conf.DATA_SIZE; i++) 
			retRange[i] = sas[i].searchPhrase(ids);
		return retRange;
	}
	
	public int getOccurrences (int[] ids) {
		int count = 0;
		SuffixArrayRange r;
		for (int i = 0; i < conf.DATA_SIZE; i++) {
			r = sas[i].searchPhrase(ids);
			count += r.getFrequency();
		}
		return count;
	}
	
	public String getSentence(int saIdx, int corpusPos) {
		int startPos, endPos;
		
		for (startPos = corpusPos; startPos > 0; startPos--) 
			if (sas[saIdx].corpus[startPos] == Vocabulary.SENTENCE_BOUNDARY_ID)
				break;
		for (endPos = corpusPos; endPos < sas[saIdx].length; endPos++) 
			if (sas[saIdx].corpus[endPos] == Vocabulary.SENTENCE_BOUNDARY_ID)
				break;
		startPos++;
		endPos--;
		
		return Utility.join(dict.getWords(sas[saIdx].corpus, startPos, endPos - startPos + 1), " ");
	}
	
	public String[] getSentenceSamples (int[] phraseIDs, int numSamples) {
		String[] retSentences = new String[numSamples];
		SuffixArrayRange[] phrasePos = findPhrase(phraseIDs);
		int frequency = 0;
		for (int i = 0; i < phrasePos.length; i++)
			frequency += phrasePos[i].getFrequency();
		
		if (numSamples > frequency)
			numSamples = frequency;
		int[] perm = Utility.getPerm(frequency, numSamples);
		int cumul = 0, res;
		for (int i = 0; i < perm.length; i++) {
			cumul = 0;
			for (int saIdx = 0; saIdx < sas.length; saIdx++) {
				if (perm[i] < cumul + phrasePos[saIdx].getFrequency()) {
					res = perm[i] - cumul;
					retSentences[i] = getSentence(saIdx, sas[saIdx].suffix[phrasePos[saIdx].getStart() + res]);
					break;
				}
				else
				//if (cumul + phrasePos[saIdx].getFrequency() < perm[i]) 
					cumul += phrasePos[saIdx].getFrequency();
			}
		}
		
		return retSentences;
	}
	
	public static void buildVocab (String mergedFile) {
		try {
			Vocabulary dict = SuffixArray.constructVocab(mergedFile);
			dict.writeToFile(mergedFile+".vocab");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		if (args.length < 1) {
			System.err.println("Usage: [program] [entire corpus]");
			return;
		}
		String entireCorpus = args[0];
		buildVocab(entireCorpus);
	}
}
