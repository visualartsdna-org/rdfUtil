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

}
