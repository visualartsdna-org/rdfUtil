package rdf.parse

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

class KeepParserTest {

	@Test
	void test() {
		def s = new File("C:/temp/git/cwvaServer/tkoTest.txt")
		parseTkoInput(s)
	}

	def parseTkoInput(File file){
		def baos  = new FileInputStream(file)
		KeepParser parser = new KeepParser(baos);
		try {
			parser.parse();
		} catch (TokenMgrError pe) {
			println pe
			throw new RuntimeException(pe)
		} catch (ParseException pe) {
			println pe
			throw new RuntimeException(pe)
		}

		println parser.sectionList
	}

	def parseTkoInput(String input){
		def baos  = new ByteArrayInputStream( input.getBytes())
		KeepParser parser = new KeepParser(baos);
		try {
			parser.parse();
		} catch (TokenMgrError pe) {
			println pe
			throw new RuntimeException(pe)
		} catch (ParseException pe) {
			println pe
			throw new RuntimeException(pe)
		}

		println parser.sectionList
	}

}
