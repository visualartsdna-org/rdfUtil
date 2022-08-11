package rdf.tools

import static org.junit.jupiter.api.Assertions.*

import org.apache.jena.rdf.model.*
import org.apache.jena.reasoner.*
import org.junit.jupiter.api.Test
import rdf.JenaUtils

class Loader {
	

		static def loadInf(dataFile,schemaFile) {
			
			def ju = new JenaUtils()
			Model data = ju.loadFiles(dataFile);
			Model schema = ju.loadFiles(schemaFile);
			def mdl = ModelFactory.createRDFSModel(schema, data);
			new SparqlConsole().show(mdl)
		}
		static def loadOwl(dataFile,schemaFile) {
			
			def ju = new JenaUtils()
			Model data = ju.loadFiles(dataFile);
			Model schema = ju.loadOntImports(schemaFile)
			//ju.loadFiles(schemaFile);
		def reasoner = ReasonerRegistry.getOWLReasoner();
		reasoner = reasoner.bindSchema(schema);
		InfModel infmodel = ModelFactory.createInfModel(reasoner, data);
			new SparqlConsole().show(infmodel)
		}
	
		
	
	static def load(path) {
		
		def m = new JenaUtils().loadFiles(path)
		new SparqlConsole().show(m)
	}

}
