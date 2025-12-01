 package rdf.tools

import static org.junit.jupiter.api.Assertions.*

import org.apache.jena.rdf.model.Model
import org.junit.jupiter.api.Test
import rdf.JenaUtilities
import rdf.JenaUtils

class SparqlConsoleTest {

	def ju = new JenaUtils()
	// test of querying a model variable
	// in a debugging context
	@Test
	void test1() {
		Model m = ju.loadFileModelFilespec("sample.ttl")
		// set break on println, then add expression 
		// to open a query window on this model:
		// new rdf.tools.SparqlConsole().show(m)
		//
		// NOTE: the var "m" must be specifically typed
		// as a jena Model for this to work!!
		println m.size()
	}

	// open a query window on a model variable 
	// defined in a runtime code context
	@Test
	void test0() {
		def m = ju.loadFileModelFilespec("sample.ttl")
		new SparqlConsole().show(m)
	}

	// open a query window on a TTL model file directly
	@Test
	void test2() {
		new SparqlConsole().show("sample.ttl")
	}

	@Test
	void test3() {
//		def infile = "C:/test/cwva/ttl/art.ttl"
//		def infile = "C:/temp/fibo.ttl"
//		def infile = "C:/stage/february2022/node/ttl/art.ttl"
		//def infile = "C:/test/linkeddata/instance-types_lang=en_specific.ttl"
//		def infile = "C:/test/linkeddata/instance-types_lang=en_specific.ttl"
		def infile = "C:/temp/git/cwvaServer/stats.ttl"
		new SparqlConsole().show(infile)
	}

	@Test
	void test4() {
		def l = [
			"C:/temp/git/cwvaServer/artPal.ttl",
			"C:/temp/git/cwvaServer/stats.ttl",
			]
		new SparqlConsole().show(l)
	}

	@Test
	void test() {
		new SparqlConsole().show("data/skosData.ttl")
	}
	
	@Test
	void testTdb() {
	def tdb = "C:/devtools/apache-jena-fuseki-3.13.1/run/configuration/fxdata.ttl"
	new SparqlConsole().showTdb(tdb)
	}
	
	@Test
	void testMetrics() {
		def m = new JenaUtilities().loadFiles("/work/stats/ttl/out_202505011357.ttl")
		new SparqlConsole().show(m)
	}
	

	@Test
	void testLoadSize() {
		Model m = ju.loadFileModelFilespec(
			"C:/temp/test/ttl/vocab/palette.ttl")
		// set break on println, then add expression
		// to open a query window on this model:
		// new rdf.tools.SparqlConsole().show(m)
		//
		// NOTE: the var "m" must be specifically typed
		// as a jena Model for this to work!!
		println m.size()
	}


}
