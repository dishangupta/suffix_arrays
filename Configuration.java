package empsyn.sa;

public class Configuration {
	
	
	//Shared Context Scoring 
	public static final int LINEAR_SUM = 0 ;
	public static final int HARMONIC_MEAN = 1 ;
	public static final int PRODUCT_LOG = 2;
	
	//Shared Context Scoring Method Used
	public int SHARED_CONTEXT_SCORE_METHOD = LINEAR_SUM ;
	
	public boolean MUTUAL_INFORMATION = true ;
	public boolean KL_DIVERGENCE = true;
	
	/* Data Usage */
	
	public int MAX_PHRASE_OCCURRENCES = 5000 ;
	public int DATA_SIZE = 1 ;
	
	/* Context length */
	public int MIN_ONE_SIDED_CONTEXT_LENGTH = 3;
	public int MAX_ONE_SIDED_CONTEXT_LENGTH = 4; 
	public int MIN_DBL_SIDED_CONTEXT_LENGTH = 2;
	public int MAX_DBL_SIDED_CONTEXT_LENGTH = 3; 
	public int CONTEXT_FREQ_THRESH = 5 ;

	/* Scoring Coefficients */
	public double LEFT_MATCHING_COEFF = 1;
	public double RIGHT_MATCHING_COEFF = 1;
	
	// The following two coefficients are 'additional' to the left match and the right match.
	// For instance, 0 for MIX_MATCHING_COEFF means there is no 'additional' score for mix-matches and just same as other matches. 
	public double MIX_MATCHING_COEFF = 1; // 1 for each side -- 2 overall
	public double PAIR_MATCHING_COEFF = 10;
	
	//Candidate Related	
	public int CONTEXT_MATCH_MINNUM = 10 ;
	public int CANDIDATE_FREQ_THRESH = 100;
	public int SENTENCE_SAMPLES = 10;
	public int MIN_CAND_COEFF = 0;
	public int MIN_CAND_CONST = 1;
	public int MAX_CAND_COEFF = 2;
	public int MAX_CAND_CONST = 2;
	
	//Scoring Equation Parameters
	public double LCTXT_WITH_QUERY_POW = 1.0 ;
	public double LCTXT_WITH_CAND_POW = 1.0 ;
	public double LCTXT_POW = 1.0 ;
	public double LCAND_POW = 0.5 ;
	public double LCTXT_STOPWORDS_COEFF = 2.0;
	public double LCTXT_LENGTH_COEFF = 0.5 ;
	public double LCTXT_CONST_COEFF = 1.0 ;
	
	public double RCTXT_WITH_QUERY_POW = 1.0 ;
	public double RCTXT_WITH_CAND_POW = 1.0 ;
	public double RCTXT_POW = 1.0 ;
	public double RCAND_POW = 0.5 ;
	public double RCTXT_STOPWORDS_COEFF = 2.0;
	public double RCTXT_LENGTH_COEFF = 0.5 ;
	public double RCTXT_CONST_COEFF = 1.0 ;
	
	public double LRCTXT_WITH_QUERY_POW = 1.0 ;
	public double LRCTXT_WITH_CAND_POW = 1.0 ;
	public double LRCTXT_POW = 1.0 ;
	public double LRCAND_POW = 0.5 ;
	public double LRCTXT_STOPWORDS_COEFF = 2.0;
	public double LRCTXT_LENGTH_COEFF = 0.5 ;
	public double LRCTXT_CONST_COEFF = 1.0 ;
	
	//Post Candidate Extraction
	public int DUMP_LENGTH = 100;
	
	private static Configuration defaultConf = new Configuration();
	
	public static Configuration getDefault() {
		return defaultConf;
	}
}
