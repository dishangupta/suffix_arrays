package empsyn.sa;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.lang3.StringUtils;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

public class Preprocessing {
	
	File inputDirectory;
	File outputFile ;
	
	
	public Preprocessing(){
		
	}
	
	
	public Preprocessing(String inputDir){
		long time = System.currentTimeMillis() ;
		File tempFile1 = new File(inputDir + "/Preproc_corp_removeSGML") ;
		File output_File = new File(inputDir + "/Preproc_corp_Tok") ;
		try{
			BufferedWriter writer = new BufferedWriter(new FileWriter(output_File));
			writer.write("");
			writer.close();
		}
		catch(IOException e){
			e.printStackTrace() ;
			System.out.println("File not Found !") ;
		}
		File dir = new File(inputDir) ;
		inputDirectory = dir ;
		outputFile = output_File ;
		File[] subdir = dir.listFiles() ;
		System.err.print("Removing SGML and Tokenizing") ;
		for(int i=0; i<subdir.length ; i++){
			if(subdir[i].isDirectory()){
			File[] files = subdir[i].listFiles() ;
			for(int j=0; j<files.length; j++){
					if((files[j].isDirectory()) || (!files[j].getName().contains("eng")))
						continue ;
					removeSGML(files[j], tempFile1) ;
					Tokenizer(tempFile1, output_File) ;
				}
			}	
		}	
		System.err.println("done") ;
		//tempFile1.delete() ;
		System.err.println("Took "+ (System.currentTimeMillis()-time)/(1000*60) + "mins to complete.") ;
	}
	
	public void removeSGML(File inputFile, File outputFile) {
	    FileReader file;
	    String line = "";
	    int i = 0 ;
	    try {
	        file = new FileReader(inputFile);
	        BufferedReader reader = new BufferedReader(file);
	        FileWriter output = new FileWriter(outputFile);
	        BufferedWriter writer = new BufferedWriter(output);
	        try {
	            while (reader.ready()) {
	            	line = reader.readLine() + "\n";
	            	if(line.contains("type=\"story\"")){
	            		while(!line.contains("<TEXT>")){
	            			line = reader.readLine() ;
	            		}
	            		while(!line.contains("</DOC")){
	            			line = reader.readLine() + "\n" ;
	            			if(line.contains("<"))
	            				continue;
	            			writer.write(line) ;
	            			i++ ;
	            			if(i%100000==0)
	            				System.err.print(".") ;
	            		}
	            	}
	            }
	          } finally {
	                        reader.close();
	                        writer.close();
	                    }
	    } catch (FileNotFoundException e) {
	        throw new RuntimeException("File not found");
	    } catch (IOException e) {
	        throw new RuntimeException("IO Error occured");
	    }
	    return;
	 }
	
	
	
	public void Tokenizer(File inputFile, File outputFile){
		String line = "" ;
		StringBuilder sb = new StringBuilder() ;		    
		try{
			BufferedReader in = new BufferedReader(new FileReader(inputFile)) ;
            int j=0 ;
			while(in.ready()){
            	line = in.readLine() + "\n";
            	sb.append(line) ;
            	j++ ;
    			if(j%100000==0)
    				System.err.print(".") ;
            }
		    in.close() ;
            String inputString = sb.toString();
		    InputStream modelIn_sent = getClass().getResourceAsStream("/en-sent.bin");//new FileInputStream("en-sent.bin") ;
		    	try {
		    		SentenceModel model_sent = new SentenceModel(modelIn_sent);
		    		SentenceDetectorME sentenceDetector = new SentenceDetectorME(model_sent);	
		    		String sentences[] = sentenceDetector.sentDetect(inputString);
		    		BufferedWriter out = new BufferedWriter(new FileWriter(outputFile,true)) ;
		    		InputStream modelIn_token = getClass().getResourceAsStream("/en-token.bin");//new FileInputStream("en-token.bin");
		    		TokenizerModel model_token = new TokenizerModel(modelIn_token);
	    			Tokenizer tokenizer = new TokenizerME(model_token) ;
	    			for(int i=0; i<sentences.length; i++){
		    			sentences[i] = sentences[i].replaceAll("\n"," ") ;
		    		    String[] tokens = tokenizer.tokenize(sentences[i]) ;
		    			sentences[i] = StringUtils.join(tokens, " ") ;
		    			out.write(sentences[i] + "\n") ;
		    			if(i%100000==0)
		    				System.err.print(".");
	    			}
		    		modelIn_token.close();
		    		out.close();
		    	}
	    		catch (IOException e) {
		    	  e.printStackTrace();
		    	}
		       	finally {
		    		if (modelIn_sent != null) {
		    			try {
		    				modelIn_sent.close();
		    			}
		    			catch (IOException e) {
		    			}
		    		}
		    	}
		}
		catch (IOException e){
			e.printStackTrace();
			System.out.println("File Not Found !") ;
		}
	}
	
	//shuffling was done through shuf command (Linux), because JVM ran out of memory.

	public void splitCorpus(String corpusFileName, int numParts){
		System.err.print("Splitting Corpus into " + numParts + " parts") ;
		int track ;
		String line = "" ;
		String append = "" ;
		try{
			File corpusFile = new File(corpusFileName) ;
			BufferedReader in = new BufferedReader(new FileReader(corpusFile)) ;
			int numLines = 0;
			while(in.ready()){
				line = in.readLine() ;
				numLines++ ;
				if(numLines%1000000 == 0)
					System.err.print(".") ;
			}
			in.close();
			in = new BufferedReader(new FileReader(corpusFile)) ;
			int j = 0 ;
			for(int i=1; i<=numParts; i++){
				if(i<10){
					append = ".partial-part" + "0" + i ;
				}
				else{
					append = ".partial-part" + i ;
				}
				BufferedWriter out = new BufferedWriter(new FileWriter(corpusFileName + append)) ;			
				double temp = i*1.0/numParts ;
				while(in.ready() && j!=(int) (temp*numLines)){
					line = in.readLine() ;
					out.write(line + "\n") ;
					j++ ;
					if(j%1000000==0)
						System.err.print(".") ;
				}
				out.close();
			}
			in.close();
		}
		catch(IOException e){
			e.printStackTrace() ;
			System.out.println("File Not Found!") ;
		}
	}
}


