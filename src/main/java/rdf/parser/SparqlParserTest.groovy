package rdf.parser

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

class SparqlParserTest {

	@Test
	void test() {
		def query = """
select * {
 ?s a	schema:Organization 
}
"""
		def update = """
DELETE { the:LCornelissenSon skos:altLabel 'LCS' }
INSERT { the:LCornelissenSon skos:altLabel 'William' }
WHERE
  { the:LCornelissenSon skos:altLabel 'LCS'
  } 
"""
			def baos  = new ByteArrayInputStream(query.getBytes())
			SparqlParser parser = new SparqlParser(baos);
			try {
				parser.Top();
				println parser.mode
				println parser.type
			} catch (TokenMgrError pe) {
				println pe
			} catch (ParseException pe) {
				println pe
			}
	}

	
	@Test
	void test2() {
		
		def s0 = """rdf.parser.TokenMgrError: Lexical error at line 16, column 28.  Encountered: '13' (13), after prefix "c\""""
		def s = """rdf.parser.TokenMgrEror: Lexical error at line 16, column 28.  Encountered: '32' (32), after prefix "c\""""
		def s1 = """rdf.parser.TokenMgrError: Lexical error at line 16, column 8.  Encountered: '9' (9), after prefix "m\""""
		errFix(s0)
	}
	
	def errFix(s) {
		def r = (s =~ /.*'([0-9]+)' \(([0-9]+)\).*/)[0]
		println "${r[1]}"
		println "${r[2]}"
		s
	}
	

}
