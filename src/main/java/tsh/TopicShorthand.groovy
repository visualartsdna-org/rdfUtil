package tsh
import groovy.json.JsonSlurper;
import java.io.File;
import rdf.JenaUtils;

class TopicShorthand {

	def ju = new JenaUtils()

	def Prefixes = """
@prefix schema: <https://schema.org/> .
@prefix tko:   <http://visualartsdna.org/takeout#> .
@prefix vad:  <http://visualartsdna.org/2021/07/16/model#> .
@prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .

"""

	TopicShorthand() {
	}

	def printTopics( m) {
		def sb = new StringBuilder()
		def map = [:]
		def l = getGraph(m)
		def top = [:]
		l.each {
			if (it["@type"] == "skos:ConceptScheme") {
				top = it
			} else {
				map[it["@id"]] = it
			}
		}
		def l2 = top.contains
		def ls = ""
		int i=0
		l2.each{
			ls += "${i++>0?",":""}$it"
		}
//		sb.append """conceptScheme; ${top["@id"]} ; $top.hasTopConcept ; $ls ; the scheme label\n"""
		def m3 = map.find{k,v->
			v.head == top.hasTopConcept
		}
		printTopic(m3.getValue(),map,sb,1)
		""+sb
	}
	
	def printTopic(m, map, sb, n) {
		def l2 = []
		if (m.memberList) l2 = m.memberList["@list"]
		def ls = ""
		int i=0
		l2.each{
			ls += "${i++>0?",":""}$it"
		}
		
		def m3 = map[m.head]
		if (!m3) return
		def title = m3.prefLabel
		def defn = m3.definition

		sb.append """
<h$n>
$title
<h$n/>
<br/>
$defn
"""
		l2.each{
			printTopic(map[it], map, sb, n+1)
			
		}
	}

	def getGraph( model) {

		def s = ju.saveModelString(model,"json-ld")
		def map = new JsonSlurper().parseText(s)
		map["@graph"]
	}

	def writeTtl(File fn) {
		writeTtl(fn.text)
	}

	def writeTtl( tshLines) {
		def sb = new StringBuilder()
		sb.append Prefixes
		tshLines.eachLine{
			def l = it.split(";")
			if (l[0] == "conceptScheme") {
				//def l2 = l[2].split(",")
				
				sb.append """tko:topScheme  a            skos:ConceptScheme ;
        rdfs:label          "${l[4]}" ;
        tko:contains        ${l[3]} ;
        skos:hasTopConcept  ${l[2].trim()} .\n"""
			}
			else {
				
				sb.append """tko:topScheme  a		tko:Topic ;
        tko:head		${l[2]} ;
        skos:memberList		${l[3]} .\n"""
			}
		}
		""+sb
	}

	def writeTsh( m) {
		def sb = new StringBuilder()
		def map = [:]
		def l = getGraph(m)
		def top = [:]
		l.each {
			if (it["@type"] == "skos:ConceptScheme") {
				top = it
			} else {
				map[it["@id"]] = it
			}
		}
		def l2 = top.contains
		def ls = ""
		int i=0
		l2.each{
			ls += "${i++>0?",":""}$it"
		}
		sb.append """conceptScheme; ${top["@id"]} ; $top.hasTopConcept ; $ls ; the scheme label\n"""
		def m3 = map.find{k,v->
			v.head == top.hasTopConcept
		}
		writeTopic(m3.getValue(),map,sb)
		""+sb
	}
	
	def writeTopic(m, map, sb) {
		def l2 = []
		if (m.memberList) l2 = m.memberList["@list"]
		def ls = ""
		int i=0
		l2.each{
			ls += "${i++>0?",":""}$it"
		}

		sb.append """topic ; ${m["@id"]} ; ${m.head} ; (${ls})\n"""
		l2.each{
			writeTopic(map[it], map, sb)
			
		}
	}


	def getQName( m,  s) {
		return ju.getPrefix(m,s)
	}

}
