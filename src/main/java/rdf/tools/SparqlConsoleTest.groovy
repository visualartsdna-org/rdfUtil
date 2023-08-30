 package rdf.tools

import static org.junit.jupiter.api.Assertions.*

import org.apache.jena.rdf.model.Model
import org.junit.jupiter.api.Test
import rdf.JenaUtils
import org.apache.jena.rdf.model.*

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
	
	@Test
	void testOneFile() {
		def d = "G:/My Drive/CWVA/artists/rspates/archive/lsys/image"
		def t = new File("/temp/lsysAll.ttl")
		t.text=""
		new File(d).eachFileRecurse(groovy.io.FileType.FILES){
			t.text << it.text
			print "."
		}
		
	}

	@Test
	void testDir() {
		long ctms = System.currentTimeMillis()
		def m = ju.loadDirModel("C:/temp/git/cwvaContent/ttl")
		println "${System.currentTimeMillis() - ctms} ms"
		new SparqlConsole().show(m)
	}

	@Test
	void testRecurseDirRDFS() {
		long ctms = System.currentTimeMillis()
		def data = ju.loadDirRecurseModel("C:/temp/git/cwvaContent/ttl/data")
		println "data ${data.size()} in ${System.currentTimeMillis() - ctms} ms"
		ctms = System.currentTimeMillis()
		def schema = ju.loadDirRecurseModel("C:/temp/git/cwvaContent/ttl/model")
		println "schema ${schema.size()} in ${System.currentTimeMillis() - ctms} ms"
		
		def rdfs = ModelFactory.createRDFSModel(schema, data);
		new SparqlConsole().show(rdfs)
	}

	// open a query window on a model variable 
	// defined in a runtime code context
	@Test
	void test0() {
		//def m = ju.loadFileModelFilespec("c:/temp/lsysAll.ttl")
		//def m = ju.loadFileModelFilespec("G:/My Drive/CWVA/artists/rspates/archive/lsys/data/lsys.ttl")
		def m = ju.loadFileModelFilespec("C:/test/webCrawler/baijiDolphin1.ttl")
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
		def infile = "C:/test/linkeddata/schemaorg/schemaorg-current-http.ttl"
		new SparqlConsole().show(infile)
	}

	@Test
	void test4() {
		def l = [
			"C:/stage/plannedDecember/node/ttl/art.ttl",
			"C:/stage/plannedDecember/node/ttl/test.ttl",
			]
		new SparqlConsole().show(l)
	}

	@Test
	void test() {
		new SparqlConsole().show("/temp/junk/tags.ttl")
	}
	
	@Test
	void testTdb() {
	def tdb = "C:/devtools/apache-jena-fuseki-3.13.1/run/configuration/fxdata.ttl"
	new SparqlConsole().showTdb(tdb)
	}


}
