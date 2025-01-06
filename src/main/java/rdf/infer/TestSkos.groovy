package rdf.infer

import static org.junit.jupiter.api.Assertions.*

import org.apache.jena.rdf.model.*
import org.apache.jena.reasoner.*
import org.junit.jupiter.api.Test
import org.apache.jena.util.*
import org.apache.jena.vocabulary.ReasonerVocabulary
import org.apache.jena.reasoner.rulesys.GenericRuleReasonerFactory
import org.apache.jena.riot.RDFDataMgr
import rdf.JenaUtils

class TestSkos {

	@Test
	void test() {
		//skosInfer("data/skosData.ttl","data/skos.rules")
		skosInfer("C:/temp/git/cwvaContent/ttl","data/skos.rules")
	}
	
	def skosInfer(df,rules) {
		def ju = new JenaUtils()
		
		String demoURI = "http://www.w3.org/2004/02/skos/core#";
		PrintUtil.registerPrefix("skos", demoURI);
		
		def data = ju.loadFiles(df)
		println "${data.size()}"
		Model m = ju.newModel()
		
		Resource configuration =  m.createResource();
		configuration.addProperty(ReasonerVocabulary.PROPruleMode, "hybrid");
		configuration.addProperty(ReasonerVocabulary.PROPruleSet,  rules);
		
		// Create an instance of such a reasoner
		Reasoner reasoner = GenericRuleReasonerFactory.theInstance().create(configuration);
		
		// Load test data
		InfModel infmodel = ModelFactory.createInfModel(reasoner, data);
		
		def ms = ju.saveModelString(infmodel)
//		println ms
		ms.eachLine {
			if (it.contains("skos:broader"))
			println it
		}
	}

	def run() {
		// Register a namespace for use in the demo
		String demoURI = "http://jena.hpl.hp.com/demo#";
		PrintUtil.registerPrefix("demo", demoURI);
		
		// Create an (RDF) specification of a hybrid reasoner which
		// loads its data from an external file.
		Model m = ModelFactory.createDefaultModel();
		Resource configuration =  m.createResource();
		configuration.addProperty(ReasonerVocabulary.PROPruleMode, "hybrid");
		configuration.addProperty(ReasonerVocabulary.PROPruleSet,  "data/demo.rules");
		
		// Create an instance of such a reasoner
		Reasoner reasoner = GenericRuleReasonerFactory.theInstance().create(configuration);
		
		// Load test data
		Model data = RDFDataMgr.loadModel("file:data/demoData.rdf");
		InfModel infmodel = ModelFactory.createInfModel(reasoner, data);
		
		// Query for all things related to "a" by "p"
		Property p = data.getProperty(demoURI, "p");
		Resource a = data.getResource(demoURI + "a");
		StmtIterator i = infmodel.listStatements(a, p, (RDFNode)null);
		while (i.hasNext()) {
			System.out.println(" - " + PrintUtil.print(i.nextStatement()));
		}
	}
}
