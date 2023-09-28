package tsh

import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.impl.RDFListImpl
import rdf.JenaUtils
import groovy.json.JsonSlurper
import org.apache.jena.rdf.model.Model

/**
 * A tko:Topic is defined in thesaurus.ttl
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
#topic; tko:abc; tko:ExtinctionStatement; (tko:def tko:ghi tko:jkl tko:mno)
tko:abc; tko:ExtinctionStatement; (tko:def tko:ghi tko:jkl tko:mno)
topic; tko:def; tko:Holocene; ()

concept; Extinction Statement;  Memorials to extinct species

conceptScheme; tko:topScheme; tko:ExtinctionStatement; tko:Holocene,tko:Anthropocene,tko:EndangeredSpecies; the scheme label
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
		printTopics(c, id, "#")
	}
	
	static def TAB = "#"
	// nested titles are further 
	// indented with defined tab
	def printTopics(c, id, tab) {
		def sb = new StringBuilder()
		def m = c[id]
		def cptId = m.head
		def cpt = c[cptId]
		if (cpt) {
			sb.append "$tab${cpt.prefLabel}\n"
			sb.append "${cpt.definition}\n\n"
		}
		if (m.memberList)
			m.memberList.each{
				sb += printTopics(c,it,tab+TAB)
			}
		""+sb
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
	def parseTsh2Ttl(File fn) {
		parseTsh2Ttl(fn.text)
	}
	
	/** 
	 * parse TSH into TTL
	 * @param String name
	 */
	def parseTsh2Ttl(String tshLines) {
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
@prefix tko:   <http://visualartsdna.org/takeout#> .

"""

		def topicOrder = []
		tshLines.eachLine{
			//println it
			if (it.trim() == "") return
				if (it.trim().startsWith("#")) return // comment
				def lf = it.trim().split(/\t|;[ ]*/)
			switch (lf[0].toLowerCase()) {
				case "topic":
					topicOrder += lf[1]
					sb.append """
${lf[1]}
	a tko:Topic ;
	tko:head ${lf[2]} ;
	skos:memberList ${lf[3]} ;
	.
"""
					break

				case "concept":
					sb.append """
tko:${util.Text.camelCase(lf[1])}
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
	tko:contains ${lf[3]} ;
	.
"""
					break

				default:
					topicOrder += lf[0]
					if (lf.size()==3) { // assume Topic
						sb.append """
${lf[0]}
	a tko:Topic ;
	tko:head ${lf[1]} ;
	skos:memberList ${lf[2]} ;
	.
"""
						break

					} else {
						println "ERROR--can't parse:\n$it"
					}
			}

		}
		def tos = ""
		topicOrder.each{
			tos += "$it "
		}
		sb.append """
		tko:aTopicOrder
			a tko:TopicOrder ;
			skos:memberList ($tos) ;
		.
"""
		sb
	}
	
	/**
	 * Given a model extract the Topics
	 * and return them in TSH syntax
	 * @param m
	 * @return String
	 */
	def writeTsh(m) {
		
		def s = writeTshConceptScheme(m)
		s += writeTshTopic(m)
		s
	}
	
	/**
	 * Given a model extract the Topics
	 * and return them in TSH syntax
	 * @param m
	 * @return String
	 */
	def writeTshConceptScheme(m) {
		def sb = new StringBuilder()
		def lm = ju.queryListMap1(m,qPrefixes, """
select ?s ?tc ?l ?ml {
		?s a skos:ConceptScheme ;
			skos:hasTopConcept  ?tc ;
			tko:contains ?ml .
	optional {?s rdfs:label ?l}
}
""")

		
		def pm = [:]
		lm.each{
			pm.s = it.s
			pm.tc = it.tc
			pm.l = it.l ?: ""
			if (!pm.containsKey("ml"))
				pm.ml = []
			pm.ml += it.ml
			
		}
		if (pm.isEmpty()) return ""
		
		sb.append """conceptScheme; ${getQName(m,pm.s)} ; ${getQName(m,pm.tc)} ; """
		int i=0
		pm.ml.each{fqn->
			sb.append "${i++==0?"":","}${getQName(m,fqn)}"
		}
		sb.append "; ${pm.l}\n"
		
		""+sb
	}
	
	/**
	 * Given a model extract the Topics
	 * and return them in TSH syntax
	 * @param m
	 * @return String
	 */
	def writeTshTopic(m) {
		def sb = new StringBuilder()
		def lm = ju.queryListMap1(m,qPrefixes, """
select ?s ?h ?rl {
		?s a tko:Topic ;
			tko:head ?h .
			#skos:memberList ?rl .
}
""")

		def topicOrder = []
		
		try {
			topicOrder =ju.getRdfListQName(m,
			"http://visualartsdna.org/thesaurus/aTopicOrder",
			"http://www.w3.org/2004/02/skos/core#memberList")
		} catch (Exception e) {
			println "# ERROR-no topic order provided!"
		}

		def pm = [:]
		lm.each{
			def key = getQName(m,it.s)
			if (!pm.containsKey(key))
				pm[key]=[:]
			pm[key].s =it.s
			pm[key].h = getQName(m,it.h)
		}

		topicOrder.each{t->
			def map=pm[t]
			sb.append """topic; ${t} ; ${map.h} ; ("""
			def l2 = ju.getRdfListQName(m,map.s,"http://www.w3.org/2004/02/skos/core#memberList")
			int i=0
			l2.each{qn->
				sb.append "${i++==0?"":" "}${qn}"
			}
			sb.append ")\n"
		}
		""+sb
	}
	
	def getQName(m,s) {
		def q = ju.getPrefix(m,s)
		"${q[0]}${q[1]}"
	}


	def getGraph0(model) {
		def s = ju.saveModelString(model,"json-ld")
		//println s
		def c = new JsonSlurper().parseText(s)

		c["@graph"]
	}
	
	def qPrefixes = """
prefix owl: <http://www.w3.org/2002/07/owl#> 
prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> 
prefix skos: <http://www.w3.org/2004/02/skos/core#> 
prefix xsd: <http://www.w3.org/2001/XMLSchema#> 
pprefix tko:   <http://visualartsdna.org/takeout#>
"""
}
