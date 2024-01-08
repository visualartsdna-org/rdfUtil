package rdf.util

import static org.junit.jupiter.api.Assertions.*

import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import org.junit.jupiter.api.Test
import rdf.JenaUtils
import tsh.TopicShorthand
import groovy.io.FileType

class JsonRdfUtilTest {
	def ju = new JenaUtils()
	
	def prefixMap = [
		note:"skos",
		changeNote:"skos",
		definition:"skos",
		editorialNote:"skos",
		example:"skos",
		scopeNote:"skos",
		historyNote:"skos",
	]

	//	ability to include concept in text resulting in
	//  inclusion of that concept's definition 
	//  it's a way to expand text, could be just for paragraphs
	
	@Test
	void testDriver() {
		driver()
	}

	//	driver for complete pass to html
	def driver() {
		def path = "C:/temp/Takeout/rspates.art"
		
		// get latest zip
		def zipFile = getLatestZip(path)
		
		// extract tsh to ttl
		def model = extractTsh2Ttl(zipFile)
		
		// extract concepts to ttl
		def m2 = extractConcepts2Ttl(zipFile)
		model.add m2
//		ju.saveModelFile(model,"test.ttl","TTL")
		
		// render model as html
		def s = new TopicShorthand().printTopics(model)
		println s
	}
	
	def extractConcepts2Ttl(zipFile) {
		def s = ""
		zipFile.entries().findAll { !it.directory }
		.findAll {
			(""+it).endsWith(".json")
		}
		.each {
			def m = new JsonSlurper().parseText( zipFile.getInputStream(it).text )
			m.findAll{
				filter (it, "publish") && !filter (it, "tsh")
			}.each{
				//println m.textContent
				def m2= writeTko2Map(m.title, m.textContent)
				s += makeTtl(m2)
				//println "$s\n\n"
			}
		}
//		def n=1
//		s.eachLine{
//			println "${n++}  $it"
//		}
		ju.saveStringModel(s, "TTL")
	}
	
	def extractTsh2Ttl(zipFile) {
		def s=""
		zipFile.entries().findAll { !it.directory }
		.findAll {
			(""+it).endsWith(".json")
		}
		.each {
			def m = new JsonSlurper().parseText( zipFile.getInputStream(it).text )
			m.findAll{
				filter (it, "publish") && filter (it, "tsh")
			}.each{
				println m.textContent
				s = new tsh.TopicShorthand().writeTtl( m.textContent)
			}
		}
		println s
		ju.saveStringModel(s, "TTL")
	}
	
	def getLatestZip(path) {
		
				long dt=0
				File f
				
				
				new File(path).traverse(type: groovy.io.FileType.FILES, nameFilter: ~/.*\.zip$/) { it ->
					if (it.lastModified() > dt) {
						dt = it.lastModified()
						f = it
					}
				}
				//	println "$f, ${new Date(f.lastModified()).format('EEE MMM dd hh:mm:ss a yyyy')}"
				new java.util.zip.ZipFile(f)
	}
	
	@Test
	void testExtractTshFromZip() {
		def fn = "C:/temp/Takeout/rspates.art/takeout-20230928T154928Z-001.zip"
		def s = extractTshFromZip2Ttl(fn)
		println s
	}

	def extractTshFromZip2Ttl(fn) {
		def s
				def zipFile = new java.util.zip.ZipFile(new File(fn))
		
				zipFile.entries().findAll { !it.directory }
				.findAll {
					(""+it).endsWith(".json")
				}
				.each {
					def m = new JsonSlurper().parseText( zipFile.getInputStream(it).text )
					m.findAll{
						filter (it, "publish") && filter (it, "tsh")
					}.each{
						//println m.textContent
						s = new tsh.TopicShorthand().writeTtl( m.textContent)
					}
				}
//						println s
				s
	}

	// https://stackoverflow.com/questions/3662144/recursive-listing-of-all-files-matching-a-certain-filetype-in-groovy
	@Test
	void testRecentZip() {
		def path = "C:/temp/Takeout/rspates.art"
		def dir = new File(path)
		long dt=0
		File f
		
		
		new File(path).traverse(type: groovy.io.FileType.FILES, nameFilter: ~/.*\.zip$/) { it ->
			if (it.lastModified() > dt) {
				dt = it.lastModified()
				f = it
			}
		}
			println "$f, ${new Date(f.lastModified()).format('EEE MMM dd hh:mm:ss a yyyy')}"
	}
	
	// loads latest keep takeout
	// filters on labels
	// captures takeout concepts
	// creates ttl
	@Test
	void test() {
		def path = "C:/temp/Takeout/rspates.art"

		long dt=0
		File f
		
		
		new File(path).traverse(type: groovy.io.FileType.FILES, nameFilter: ~/.*\.zip$/) { it ->
			if (it.lastModified() > dt) {
				dt = it.lastModified()
				f = it
			}
		}
			println "$f, ${new Date(f.lastModified()).format('EEE MMM dd hh:mm:ss a yyyy')}"
		def zipFile = new java.util.zip.ZipFile(f)

		zipFile.entries().findAll { !it.directory }
		.findAll {
			(""+it).endsWith(".json")
		}
		.each {
			def m = new JsonSlurper().parseText( zipFile.getInputStream(it).text )
			m.findAll{
				filter (it, "publish") && !filter (it, "tsh")
			}.each{
				//println m.textContent
				def m2= writeTko2Map(m.title, m.textContent)
				def s = makeTtl(m2)
				println "$s\n\n"
			}
		}
	}


	@Test
	void testRecentZip0() {
		def path = "C:/temp/Takeout/rspates.art"
		def dir = new File(path)
		new File(path).traverse(type: groovy.io.FileType.FILES, nameFilter: ~/.*\.zip$/) { it ->
			println "$it, ${new Date(it.lastModified()).format('EEE MMM dd hh:mm:ss a yyyy')}"
		}
	}

	@Test
	void test0() {
		def fn = "C:/temp/Takeout/rspates.art/takeout-20230928T154928Z-001.zip"

		def zipFile = new java.util.zip.ZipFile(new File(fn))

		zipFile.entries().findAll { !it.directory }
		.findAll {
			(""+it).endsWith(".json")
		}
		.each {
			def m = new JsonSlurper().parseText( zipFile.getInputStream(it).text )
			m.findAll{
				filter (it, "publish") && !filter (it, "tsh")
			}.each{
				//println m.textContent
				def m2= writeTko2Map(m.title, m.textContent)
				def s = makeTtl(m2)
				println "$s\n\n"
			}
		}
	}

	@Test
	void testJson() {
		def fn = "C:/temp/Takeout/rspates.art/Extinction Statement.json"

		def m = new JsonSlurper().parseText( new File(fn).text )
		def m2= writeTko2Map(m.title, m.textContent)
		def s = makeTtl(m2)
		println "$s\n\n"
	}
	
	@Test
	void testInlineConcept() {
		def fn = "C:/temp/Takeout/rspates.art/Extinction Statement.json"
		def tshFn = "C:/temp/git/cwvaContent/ttl/topics0/topics.tsh"
		def m = new JsonSlurper().parseText( new File(fn).text )
		def m2= writeTko2Map(m.title, m.textContent)
		def s = makeTtl(m2)
		//println "$s\n\n"
		def model = ju.saveStringModel(s,"TTL")
		def tsh = new tsh.TopicShorthand().writeTtl(new File(tshFn).text)
		model.add ju.saveStringModel(tsh,"TTL")
		
		def s2 = new TopicShorthand().printTopics(model)
		println s2
	}
	
	def fixUri(s) {
		s = s.replaceAll(",","_")
		s.replaceAll("\\.","")
	}

	def makeTtl(m) {
		def s = """
@prefix tko: <http://visualartsdna.org/takeout#> .
@prefix xs: <http://www.w3.org/2001/XMLSchema#> .
@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .
@prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
"""
		m.each{k,v->
			def uri = camelCase(fixUri(k))
			s += """
		tko:${uri} a skos:Concept ;
			skos:prefLabel "$k";
			skos:definition "${v.definition}" ;
"""
			v.findAll{k1,v1->
				k1 != "definition"
			}.each{k1,v1->
				s += """
			${prefixMap[k1]}:$k1 "${v1.trim()}" ;
				"""
			}
			s +="\t\t."
		}
		s
	}

	def writeTko2Map(title,text) {
		def topConcept = false
		def newConcept = false
		def fndText = false
		def concept=null
		def txt = ""
		def blankcnt = 0
		def m=[:]
		m[title]=[:]
		text.eachLine{
			if (it=="Endangered Designation") {
				println "here"
			}
			if (it.trim() == "") {
				blankcnt++
				if (blankcnt>=2) {
					newConcept = true
				}
				else if (fndText && !topConcept) {
					topConcept = true
					m[title]["definition"]=txt
					fndText = false
					txt = ""
				}
				else if (fndText && newConcept) {
					newConcept = false
					concept=txt
					m[concept]=[:]
					fndText = false
					txt = ""
					blankcnt = 0
				}
				else if (concept && blankcnt==1) {
					m[concept]["definition"]=txt
					fndText = false
					txt = ""
				}
			}
			else {
				if (it.startsWith("[")) {
					def key= (it=~/\[([A-Za-z]+)\:([A-Za-z0-9\.\-_ ]+)\]/)[0][1]
					def value= (it=~/\[([A-Za-z]+)\:([A-Za-z0-9\.\-_ ]+)\]/)[0][2]
					
					// inline  concept
					if (key == "inline") {
						txt += it
					} else {
						m[concept][key] = value
					}
				} else {
					txt += it
				}
				fndText = true
				blankcnt=0
			}
		}
		m[concept]["definition"]=txt
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

	@Test
	void test00() {
		def fn = ""
		def m = new JsonSlurper().parse(new File(fn))
		def sb = new StringBuilder()
		def prefix = "tko:"
		new JsonRdfUtil().jsonToTtl(m, sb, prefix)
		println ""+sb
	}
	
	@Test
	void testIC() {
		def m=[EndangeredDesignation1:"stuff1", EndangeredDesignation2:"stuff2"]
		def s = """
Species are classified \n[inline:EndangeredDesignation1]\nThere is an emphasis on\n[inline:EndangeredDesignation2]\nthe acceptability
"""
		def cg0 = (s =~ /(\[inline\:.*\])/).collect{
			it[1]
		}.each{
			def cpt = (it =~ /\[inline\:(.*)\]/)[0][1]
			println cpt
			s = s.replaceAll("\\$it",m[cpt])
		}
		
		println s

	}
	

}
