package rdf.util

import static org.junit.jupiter.api.Assertions.*

import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import org.junit.jupiter.api.Test

class JsonRdfUtilTest {
	

	@Test
	void test0() {
		def fn = ""
		def m = new JsonSlurper().parse(new File(fn))
		def sb = new StringBuilder()
		def prefix = "tko:"
		new JsonRdfUtil().jsonToTtl(m, sb, prefix)
		println ""+sb
	}

	@Test
	void test() {
		def fn = "C:/temp/Takeout/rspates.art/takeout-20230928T154928Z-001.zip"

		def zipFile = new java.util.zip.ZipFile(new File(fn))

		zipFile.entries().findAll { !it.directory }
		.findAll {
			(""+it).endsWith(".json")
			//.findAll { (""+it).contains("Extinction") && (""+it).endsWith(".json")
		}
		.each {
			//println it
			def m = new JsonSlurper().parseText( zipFile.getInputStream(it).text )
			//println JsonOutput.prettyPrint(zipFile.getInputStream(it).text)
			m.findAll{
				filter (it, "publish") && !filter (it, "tsh")
			}.each{
				//println m.textContent
				def m2= writeTko2Ttl(m.title, m.textContent)
//				m2.each{k,v->
//					println "$k\n\t$v"
//				}
				def s = makeTtl(m2)
				println "$s\n\n"
			}
		}
	}
	
	def makeTtl(m) {
		def s = """
@prefix tko: <http://visualartsdna.org/takeout#> .
@prefix xs: <http://www.w3.org/2001/XMLSchema#> .
@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .
"""
		m.each{k,v->
			s += """
		tko:${camelCase(k)} a skos:Concept ;
			skos:prefLabel "$k";
			skos:definition "$v" ;
			.
"""
		}
		s
	}

	def writeTko2Ttl(title,text) {
		def topConcept = false
		def newConcept = false
		def fndText = false
		def concept=null
		def txt = ""
		def blankcnt = 0
		def m=[:]
		text.eachLine{
			//println "===$it"
			if (it.trim() == "") {
				blankcnt++
				if (blankcnt>=2) {
					newConcept = true
				}
				else if (fndText && !topConcept) {
					topConcept = true
					m[title]=txt
					fndText = false
					txt = ""
					//blankcnt = 0
				}
				else if (fndText && newConcept) {
					newConcept = false
					concept=txt
					fndText = false
					txt = ""
					blankcnt = 0
				}
				else if (concept && blankcnt==1) {
					m[concept]=txt
					fndText = false
					txt = ""
				}
			}
			else {
				txt += it
				fndText = true
				blankcnt=0
			}
		}
		m
	}
	

	static def camelCase(s) {
		s.replaceAll( /( )([A-Za-z0-9])/, {
			it[2].toUpperCase()
		} )
	}



	def filter(entry,criteria) {
		if (entry.getKey() == "labels")// && it.getValue().name=="tsh"
			return entry.getValue().find{m2->
				m2.name==criteria
			}
		false
	}
}
