package empsyn.sa;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Comparator;

public class SuffixArray {
	private static final int PROGRESS_GRANULARITY = 10000000;
	private static final String SPLITTER = "\\s+";

	public int[] corpus;
	public int[] suffix;
	public int length = -1;
	public Vocabulary dict;
	
	private IntegerWrapper[] tempSuffix;

	public SuffixArray(String filename, Vocabulary _dict) throws Exception {
		this(filename, _dict, false);
		return;
	}
	
	public SuffixArray(String filename, Vocabulary _dict, Boolean isRaw) throws Exception {
		dict = _dict;
		if (isRaw) 
			construct(filename, _dict);
		else {
			loadCorpusAndSuffix(filename); 	
			//dict.load(filename+".id_voc");
		}
		return;
	}

	public static Vocabulary constructVocab(String corpusFilename) throws Exception{
		System.err.print("Initializing vocaublary -- first scan");

		int length = 0;
		Vocabulary dict = new Vocabulary();
		String line;
		String[] tokens;
		
		BufferedReader in = new BufferedReader(new FileReader(corpusFilename));
		while(in.ready()) {
			line = in.readLine();
			tokens = line.split(SPLITTER);
			
			int id;
			for (int i = 0; i < tokens.length; i++) {
				if (tokens[i].length() == 0)
					continue;
				if (tokens[i].equalsIgnoreCase(Vocabulary.DOC_BOUNDARY))
					continue;
				id = dict.addWord(tokens[i].toLowerCase());
				length++;
				if (length % PROGRESS_GRANULARITY == 0)
					System.err.print(".");
				if (length % (PROGRESS_GRANULARITY * 50) == 0)
					Runtime.getRuntime().gc();
			}
			
			length++;   // Why this length++ ?
			if (length % PROGRESS_GRANULARITY == 0)
				System.err.print(".");
		}
		dict.finalizeVocabulary();
		dict.writeToFile(corpusFilename + ".Vocab") ; 
		in.close();
		
		System.err.println(" done "+ dict.getNumWords());
		return dict;
	}
	
	private void getLengthOfCorpus(String corpusFilename) throws Exception {
		length = 0;
		BufferedReader in = new BufferedReader(new FileReader(corpusFilename));
		String line;
		String[] tokens;
		while(in.ready()) {
			line = in.readLine();
			tokens = line.split(SPLITTER);
			
 			int id;
			for (int i = 0; i < tokens.length; i++) {
				if (tokens[i].length() == 0)
					continue;
				id = dict.addWord(tokens[i].toLowerCase());
				length++;
				if (length % PROGRESS_GRANULARITY == 0)
					System.err.print(".");
			}
			tokens = null; // Why null here and not in the previous method?
			line = null;
			
			length++;
			if (length % PROGRESS_GRANULARITY == 0)
				System.err.print(".");
		}
		in.close();
		return;
	}
	
	private void readAndConvertCorpus(String corpusFilename) throws Exception{
		System.err.print("Getting the length of corpus");
		getLengthOfCorpus(corpusFilename);
		System.err.println(" - done reading " + length);

		System.err.print("Converting raw corpus");
		
		Runtime.getRuntime().gc();
		tempSuffix = new IntegerWrapper[length];
		corpus = new int[length];
		int count = 0;
		BufferedReader in = new BufferedReader(new FileReader(corpusFilename));
		String line;
		String[] tokens;
		while(in.ready()) {
			line = in.readLine();
			tokens = line.split(SPLITTER);
			
			for (int i = 0; i < tokens.length; i++) {
				if (tokens[i].length() == 0)
					continue;
				corpus[count] = dict.getID(tokens[i]);
				count++;
				if (count % PROGRESS_GRANULARITY == 0)
					System.err.print(".");
			}
			
			line = null;
			tokens = null;
			
			corpus[count] = Vocabulary.SENTENCE_BOUNDARY_ID;
			count++;
			if (count % PROGRESS_GRANULARITY == 0)
				System.err.print(".");
		}
		in.close();
		
		System.err.println(" - done");
	}

	private void sortSuffix(String filename) throws Exception {
		System.err.println("Initializing for sort..");
		System.err.println("Before gc - " + Runtime.getRuntime().freeMemory());
		try {
			Thread.sleep(0);                   //What does this do?
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Runtime.getRuntime().gc();
		System.err.println("After gc - " + Runtime.getRuntime().freeMemory());
		
		for (int i = 0; i < length; i++) {
			tempSuffix[i] = new IntegerWrapper();
			tempSuffix[i].value = i;
		}
		Runtime.getRuntime().gc();
		System.err.println("After gc - " + Runtime.getRuntime().freeMemory());

		System.err.println("Sorting suffix..");
		Arrays.sort(tempSuffix, new SuffixComparator(this));
		writeToFile(filename, tempSuffix);
		
	}
	
	public void writeToFile(String baseFilename, IntegerWrapper[] tempSuffix) throws Exception {
		//System.err.println("Writing vocabularies..");		
		//dict.writeToFile(baseFilename + ".id_voc");
		
		System.err.println("Writing corpus & suffix..");
		OutputStream corpOut = new BufferedOutputStream(new FileOutputStream(baseFilename+".sa_corpus"));
		OutputStream suffOut = new BufferedOutputStream(new FileOutputStream(baseFilename+".sa_suffix"));
		
		corpOut.write(ByteConverter.convertToBytes(length));
		suffOut.write(ByteConverter.convertToBytes(dict.getNumWords()));
		System.err.println("\tTotal " + length + " tokens in the corpus");
		for (int i = 0; i < length; i++) {
			corpOut.write(ByteConverter.convertToBytes(corpus[i]));
			suffOut.write(ByteConverter.convertToBytes(tempSuffix[i].value));
		}
		
		corpOut.close();
		suffOut.close();
	}
	
	public void construct(String filename, Vocabulary _dict) throws Exception {
		dict = _dict;
		//constructVocab(filename);
		readAndConvertCorpus(filename);
		sortSuffix(filename);
		Runtime.getRuntime().gc();
		//writeToFile(filename);
	}

	
	public int [] loadBinaryIntArray(InputStream in) throws Exception {
		byte[] temp = new byte[4];
		int[] target = new int[length];
		int converted;

		for (int i = 0; i < length; i++) {
			in.read(temp);

			converted = ByteConverter.convert(temp);
			target[i] = converted;

			if (i % PROGRESS_GRANULARITY == 0)
				System.err.print(".");
		}		
		System.err.println(" done");

		return target;
	}

	public void loadCorpusAndSuffix(String filenameBase) throws Exception {
		byte[] temp = new byte[4];

		FileInputStream fin = new FileInputStream(filenameBase+".sa_corpus");
		BufferedInputStream in = new BufferedInputStream(fin);

		in.read(temp);
		length = ByteConverter.convert(temp);  //Why are you loading the length in the byte array?

		System.err.println("Corpus size -- " + length + "words.");
		if (length < 0)
			throw new Exception("Corpus length too long -- over 2^31-1");

		System.err.print("Loading corpus");
		corpus = loadBinaryIntArray(in);
		System.err.println("\tTotal " + ByteConverter.convert(temp) + " tokens.");

		in.close();
		fin.close();

		fin = new FileInputStream(filenameBase+".sa_suffix");
		in = new BufferedInputStream(fin);

		in.read(temp);
		//dict.setNumWords( ByteConverter.convert(temp) );
		System.err.print("Loading suffix");
		suffix = loadBinaryIntArray(in);
		System.err.println("\tTotal " + ByteConverter.convert(temp) + " types.");
		
		in.close();
		fin.close();

		return;
	}

	public String[] getSnippet(int offset, int length) {
		return dict.getWords(corpus, offset, length);
	}

	public Boolean isOkay(int idx) {
//		if (corpus[idx] >= 2 && corpus[idx] <= 100)
//			return false;
		return true;
	}

	
	public int compareSubStringAgainstCorpus(int str, int suffixIdx, int offset) {
		while (suffixIdx < length && suffix[suffixIdx] + offset < length && !isOkay(corpus[suffix[suffixIdx]+offset]))
			suffixIdx++;
		
		if (suffixIdx == length || suffix[suffixIdx] + offset >= length)
			return -1;

		int compResult = str - corpus[suffix[suffixIdx]+offset] ;
		if (compResult != 0)
			return compResult;

		return 0;		
	}

	public int searchPhraseLeftBound(int words, int s, int e, int offset) {
		int m;

		while(e-s > 1) {
			m = (e + s) / 2;
			int compResult = compareSubStringAgainstCorpus(words, m, offset);

			if (compResult <= 0)
				e = m;
			else
				s = m;

		}

		if (compareSubStringAgainstCorpus(words, s, offset) == 0)
			return s;
		else
			return e;
	}
	public int searchPhraseRightBound(int words, int s, int e, int offset) {
		int m;

		while(e-s > 1) {
			m = (e + s) / 2;
			int compResult = compareSubStringAgainstCorpus(words, m, offset);

			if (compResult >= 0)
				s = m;
			else
				e = m;

		}

		if (compareSubStringAgainstCorpus(words, e, offset) == 0)
			return e;
		else
			return s;
	}

	public SuffixArrayRange searchPhrase(int[] words, int s, int e) {
		for (int i = 0; i < words.length; i++)
		{
			s = searchPhraseLeftBound(words[i], s, e, i);
			e = searchPhraseRightBound(words[i], s, e, i);
		}

		if (e == s) {
			if (compareSubStringAgainstCorpus(words[words.length-1], s, words.length - 1) != 0)
				e = s - 1;
		}
		SuffixArrayRange ret = new SuffixArrayRange(s,e);
		
		return ret;
	}
	public SuffixArrayRange searchPhrase(int[] words, SuffixArrayRange range) { //What is this method for?
		return searchPhrase(words, range.getStart(), range.getEnd());	
	}
	public SuffixArrayRange searchPhrase(int[] words) {
		return searchPhrase(words, 0, length - 1);
	}
	
	public int[] getCorpusSnippetInID(int corpusPos, int offset, int snippetLength) {
		int[] retIDs = new int[snippetLength];
		for (int i = 0; i < snippetLength; i++) {
			int pos = corpusPos + offset + i;
			if (pos < 0 || pos >= length)
				retIDs[i] = dict.getNumWords();
			else
				retIDs[i] = corpus[pos];
		}
		return retIDs;
	}
	public int[] getSuffixSnippetInID(int suffixPos, int offset, int snippetLength) {//What's this for?
		return getCorpusSnippetInID(suffix[suffixPos], offset, snippetLength);
	}
	
	public static void buildSuffixArrays (String partial, String vocab) {
		try {
			Vocabulary dict = new Vocabulary();
			dict.load(vocab);
			SuffixArray sa = new SuffixArray(partial, dict, true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		long time = System.currentTimeMillis() ;
		if (args.length < 2) {
			System.err.println("Usage: [program] [partial corpus] [vocab]");
			return;
		}
		String partial_Dir = args[0];
		String vocab = args[1];
		File dir = new File(partial_Dir) ; 
		File[] files = dir.listFiles() ;
		String partial = "" ;
		for(int i=0; i<files.length ; i++){
			partial = files[i].getPath() ;
			buildSuffixArrays(partial, vocab);
		}
		System.err.println("Took " + (System.currentTimeMillis()-time)/(1000*60) + "min to complete");
	}
	
	private static class ByteConverter {
		public static int convert_forward(byte[] target) {
			int retValue = 0;
			for (int i = 0; i < target.length; i++)
				retValue = (retValue << 8) + (int)target[i];
			return retValue;
		}
		public static int convert_backward(byte[] target) {
			int retValue = 0;
			for (int i = target.length - 1; i >= 0; i--)
				retValue = (retValue << 8) + (int)(target[i] & 0xFF);
			return retValue;
		}
		public static int convert(byte[] target) throws Exception {
			if (target.length > 4)
				throw new Exception ("Byte array too long to convert to int");

			return convert_backward(target);
		}
		public static byte[] convertToBytes(int target) {
			byte[] temp = new byte[4];
			for (int i = 0; i < 4; i++) {
				temp[i] = (byte) (target & 0xFF);
				target = target >> 8;
			}
			return temp;
		}
	}

		
	private static class IntegerWrapper {
		public int value = 0;
		public int hashCode() {
			return value;
		}
		public boolean equals(Object o) {
			if (o instanceof IntegerWrapper)
				return (((IntegerWrapper) o).value == value);
			else 
				return false;
		}
	}

	private static class SuffixComparator implements Comparator<IntegerWrapper> {
		private SuffixArray sa;
		
		public SuffixComparator(SuffixArray _sa) {
			sa = _sa;
		}
		
		@Override
		public int compare(IntegerWrapper obj1, IntegerWrapper obj2) {
			int o1 = obj1.value;
			int o2 = obj2.value;
			while(o1 < sa.length && o2 < sa.length) {
				int compResult = sa.corpus[o1] - sa.corpus[o2];
				if (compResult != 0)
					return compResult;
				o1++;
				o2++;
			}
			
			if (o1 == sa.length && o2 < sa.length)
				return -1;
			else if (o2 == sa.length && o1 < sa.length)
				return 1;
			
			return 0;
		}
	}
}
