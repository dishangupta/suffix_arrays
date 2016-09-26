package empsyn.sa;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Vocabulary {
	private static final int PROGRESS_GRANULARITY = 300000;
	public static final String SENTENCE_BOUNDARY = "_SENT_END_";
	public static final int SENTENCE_BOUNDARY_ID = 0;
	public static final String DOC_BOUNDARY = "_END_OF_TEXT_";
	public static final int DOC_BOUNDARY_ID = 1;

	private static final int MAX_PRESET_ID = DOC_BOUNDARY_ID;
	
	public static final String OUT_OF_VOCABULARY = "_OOV_";


	private Map<String, Integer> wordToId;
	private String[] idToWord;

	//TODO: this is a temporary solution. Should be the max of the hardcoded ids above.
	private int numWords = MAX_PRESET_ID;

	public Vocabulary() {
		numWords = MAX_PRESET_ID;
		wordToId = new HashMap<String, Integer>();
		idToWord = new String[numWords];
	}

	public void setNumWords(int _numWords) {
		numWords = _numWords;
		idToWord = new String[numWords];
	}
	public int getNumWords() {
		return numWords;
	}

	private void countNumWords(String filename) throws Exception {
		BufferedReader in = new BufferedReader(new FileReader(filename));
		int count = 0;
		while (in.ready()) {
			String line = in.readLine();
			count++;
		}
		in.close();
		setNumWords(count);
	}

	public void load(String filename) {
		try {
			System.err.print("Loading vocabularies");
			countNumWords(filename);
			BufferedReader in = new BufferedReader(new FileReader(filename));
			int count = 0;
			while (in.ready() && count++ <= numWords) {
				String line = in.readLine();
				String[] split = line.split("\\s+");

				int id = Integer.parseInt(split[1]);
				// TODO: Special case for _SENT_END_ -- better fix it later
				split[0] = split[0].toLowerCase();
				wordToId.put(split[0], Integer.valueOf(id));
				idToWord[id] = split[0];
				if (count % PROGRESS_GRANULARITY == 0)
					System.err.print(".");
				//System.out.println("ID " + id + ": " + split[0]);
			}
			System.err.println(" done reading " + numWords + " types");
			in.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void writeToFile(String filename) throws Exception {
		//OutputStream out = new BufferedOutputStream(new FileOutputStream(filename));
		Writer out = new BufferedWriter(new FileWriter(filename));

		System.err.println("\tTotal " + idToWord.length + " types in the dictionary");
		for (int i = 0; i < idToWord.length; i++)
			out.write(idToWord[i] + "\t" + i + "\n");

		out.close();
	}

	public String getWord(int id) {
		if (id >= 0 && id < idToWord.length)
			return idToWord[id];
		else
			return OUT_OF_VOCABULARY;
	}
	public int getID(String word) {
		if (wordToId.containsKey(word.toLowerCase()))
			return wordToId.get(word.toLowerCase());
		else {
			if (word.equalsIgnoreCase(DOC_BOUNDARY))
				return DOC_BOUNDARY_ID;
			if (word.equalsIgnoreCase(SENTENCE_BOUNDARY))
				return SENTENCE_BOUNDARY_ID;
			return numWords;  // for OUT_OF_VOCABULARY
		}
	}

	public String[] getWords(int[] ids, int offset, int length) {
		String[] retValue = new String[length];
		for (int i = offset; i < offset + length; i++)
			retValue[i-offset] = getWord(ids[i]);
		return retValue;
	}
	public int[] getIDs(String[] words, int offset, int length) {
		int[] retValue = new int[length];
		for (int i = offset; i < offset + length; i++)
			retValue[i] = getID(words[i]);
		return retValue;
	}

	public String[] getWords(int[] ids) {
		return getWords(ids, 0, ids.length);
	}
	public int[] getIDs(String[] words) {
		return getIDs(words, 0, words.length);
	}

	public int addWord(String word) {
		if (!wordToId.containsKey(word)) {
			numWords++;
			wordToId.put(word, numWords);
		}
		return wordToId.get(word);
	}

	// This should be done once all the words are added
	public void finalizeVocabulary() {
		numWords++;
		idToWord = new String[numWords];
		idToWord[SENTENCE_BOUNDARY_ID] = "";
		idToWord[DOC_BOUNDARY_ID] = "";

		Iterator<String> wordsItr = wordToId.keySet().iterator();

		while(wordsItr.hasNext()) {
			String token = wordsItr.next();
			int id = wordToId.get(token);
			idToWord[id] = token;
		}

		Arrays.sort(idToWord, new TokenComparator());
		wordToId.clear();

		for (int i = MAX_PRESET_ID; i < idToWord.length; i++)
			wordToId.put(idToWord[i], i);

		idToWord[SENTENCE_BOUNDARY_ID] = SENTENCE_BOUNDARY;
		wordToId.put(SENTENCE_BOUNDARY, SENTENCE_BOUNDARY_ID);
		idToWord[DOC_BOUNDARY_ID] = DOC_BOUNDARY;
		wordToId.put(DOC_BOUNDARY, DOC_BOUNDARY_ID);

		return;
	}

	private static class TokenComparator implements Comparator<String> {
		@Override
		public int compare(String o1, String o2) {
			return o1.compareTo(o2);
		}

	}
}
