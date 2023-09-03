package tsh

import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.impl.RDFListImpl
import rdf.JenaUtils
import groovy.json.JsonSlurper
import org.apache.jena.rdf.model.Model

/**
 * A the:Topic is defined in thesaurus.ttl
 * 
 * Handles key names: Topic, Concept and ConceptScheme one line definitions
 * If key name is absent "Topic" is understood
 * Semicolon or tab are delimiters
 * grammar is:
 * [topic] ; uri ; head concept uri ; (rdfList of topic uris)
 * concept ; label {camelcased to uri} ; definition
 * conceptScheme ; uri ; top concept uri ; {comma-delim list of inScheme concept uris} ; label
 *
# TSH example
#topic; the:abc; the:ExtinctionStatement; (the:def the:ghi the:jkl the:mno)
the:abc; the:ExtinctionStatement; (the:def the:ghi the:jkl the:mno)
topic; the:def; the:Holocene; ()

concept; Extinction Statement;  Memorials to extinct species

conceptScheme; the:topScheme; the:ExtinctionStatement; the:Holocene,the:Anthropocene,the:EndangeredSpecies; the scheme label
# end of example
 *
 * @author ricks
 *
 */
class TopicShorthand {

	def ju = new JenaUtils()

	/**
	 * Given a simplified collection
	 * from a model graph (a la json-ld)
	 * print the text members of the
	 * collection
	 * @param c simplified collection
	 * @param id the string uri of the top topic
	 * @return
	 */
	def printTopics(c, id) {
		printTopics(c, id, "")
	}
	
	static def TAB = "  "
	// nested titles are further 
	// indented with defined tab
	def printTopics(c, id, tab) {
		def m = c[id]
		def cptId = m.head
		def cpt = c[cptId]
		if (cpt) {
			println "$tab${cpt.label}\n"
			println "${cpt.definition}\n\n"
		}
		if (m.memberList)
			m.memberList.each{
				printTopics(c,it,tab+TAB)
			}
	}

	/**
	 * return the graph of a model
	 * as a simplified collection
	 * @param model
	 */
	def getGraph(model) {
		def s = ju.saveModelString(model,"json-ld")
		//println s
		def c = new JsonSlurper().parseText(s)
		def map=[:]
		def key
		c["@graph"].each{m->
			def m2=[:]
			map[m["@id"]]=m2
			m.each{k,v->
				//def k = removeNs(k0)
				if (v instanceof Map
						&& v.containsKey("@list")) {
					def l2=[]
					v["@list"].each{
						l2+= it
					}
					m2[k]=l2
				} else if (v instanceof List) {
					def l2=[]
					v.each{
						l2+= it
					}
					m2[k]=l2
				} else if (k=="@id") {
					m2.id = v
				} else if (k=="@type") {
					m2.type = v
				} else {
					m2[k] = v
				}
			}
		}
		map
	}

	/** 
	 * parse TSH into TTL
	 * @param file name
	 */
	def parseTsh2Ttl(fn) {
		def sb = new StringBuilder()
		sb.append """
@prefix cwva: <http://visualartsdna.org/2021/07/16/model#> .
@prefix dct: <http://purl.org/dc/terms/> .
@prefix foaf: <http://xmlns.com/foaf/0.1/> .
@prefix owl: <http://www.w3.org/2002/07/owl#> .
@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
@prefix schema: <https://schema.org/> .
@prefix skos: <http://www.w3.org/2004/02/skos/core#> .
@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
@prefix the:   <http://visualartsdna.org/thesaurus/> .

"""

		new File(fn).text.eachLine{
			//println it
			if (it.trim() == "") return
				if (it.trim().startsWith("#")) return // comment
				def lf = it.trim().split(/\t|;[ ]*/)
			switch (lf[0].toLowerCase()) {
				case "topic":
					sb.append """
${lf[1]}
	a the:Topic ;
	the:head ${lf[2]} ;
	skos:memberList ${lf[3]} ;
	.
"""
					break

				case "concept":
					sb.append """
the:${util.Text.camelCase(lf[1])}
	a skos:Concept ;
	rdfs:label         "${lf[1]}" ;
	skos:definition    \"\"\"${lf[2]}\"\"\" ;
	.
"""

					break

				case "conceptscheme":
					if (lf.size() == 5) { // has a label
						sb.append """
${lf[1]}
	a skos:ConceptScheme ;
	skos:hasTopConcept ${lf[2]} ;
	rdfs:label "${lf[4]}" ;"""
					} else {
						sb.append """
${lf[1]}
	a skos:ConceptScheme ;
	skos:hasTopConcept ${lf[2]} ;"""
					}
					sb.append """
	the:contains ${lf[3]} ;
	.
"""
					break

				default:
					if (lf.size()==3) { // assume Topic
						sb.append """
${lf[0]}
	a the:Topic ;
	the:head ${lf[1]} ;
	skos:memberList ${lf[2]} ;
	.
"""
						break

					} else {
						println "ERROR--can't parse:\n$it"
					}
			}

		}
		sb

	}

	/**
	 * get an RDFList from a model as a java list
	 * model open model
	 * resource uri string
	 * property uri string
	 * returns a list of member uri Strings
	 */
	def getRdfList(model,resource, property) {
		Resource res = model.getResource(resource)
		Property prop = model.createProperty(property)
		Resource node = (Resource) res.getProperty(prop).getObject();
		def l = new RDFListImpl(node.asNode(), model).asJavaList()
		def ls = []
		l.each{ ls += ""+it}
		ls
	}


	def getGraph0(model) {
		def s = ju.saveModelString(model,"json-ld")
		//println s
		def c = new JsonSlurper().parseText(s)

		c["@graph"]
	}
}
