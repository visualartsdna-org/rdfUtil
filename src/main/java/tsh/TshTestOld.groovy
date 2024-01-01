package tsh

import static org.junit.jupiter.api.Assertions.*

import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import org.apache.jena.rdf.model.Model
import org.junit.jupiter.api.Test
import com.petebevin.markdown.MarkdownProcessor
import rdf.JenaUtils
import static groovy.io.FileType.FILES

class TshTestOld {

	def ju = new JenaUtils()
	def tsh = new TopicShorthand()
	def base = "C:/temp/git/cwvaContent/ttl/topics"

	@Test
	void testMarkdownJustTtl() {
		//def fn = "C:/temp/git/cwvaContent/ttl/topics/topics.tsh"
		
		// generate ttl from the tsh
		//def sb = tsh.parseTsh2Ttl(new File(fn))
		//println ""+sb
		Model m = ju.loadFileModelFilespec("C:/temp/Takeout/results/rspates.art.ttl")
		
		// query for tsh concept definition
		def dc = ju.queryListMap(m,"","""
prefix tko:   <http://visualartsdna.org/takeout#>
prefix skos:  <http://www.w3.org/2004/02/skos/core#>
select ?def {
	?s skos:prefLabel  "tsh" .
	?s skos:member ?ol .
	?ol skos:definition ?def .
}
""")
		// TODO: handle the case where multiple 
		// members of tsh concept are found
		def sb = tsh.writeTtl(dc.def[0])
		m.add ju.saveStringModel(""+sb,"ttl")
		
		
		// extract the model graph from json-ld returning collection
		def c = tsh.getGraph(m)
		//c.each{println it}
		def md = tsh.printTopics(c, "tko:abc")
		println md
		MarkdownProcessor markup = new MarkdownProcessor()
		def s = markup.markdown(md)

		new File("C:/temp/git/cwvaContent/ttl/topics/topic3.html").text = s

	}
	
	@Test
	void testMarkdownTshFile() {
		def fn = "C:/temp/git/cwvaContent/ttl/topics/topics.tsh"
		
		// generate ttl from the tsh
		def sb = tsh.writeTtl(new File(fn))
		//println ""+sb
		Model m = ju.saveStringModel(""+sb,"ttl")
		//println m.size()
		m.add ju.loadFileModelFilespec("C:/temp/Takeout/results/rspates.art.ttl")
		
		// extract the model graph from json-ld returning collection
		def c = tsh.getGraph(m)
		//c.each{println it}
		def md = tsh.printTopics(c, "tko:abc")
		println md
		MarkdownProcessor markup = new MarkdownProcessor()
		def s = markup.markdown(md)

		new File("C:/temp/git/cwvaContent/ttl/topics/topic2.html").text = s

	}
	
	// TTL -> TSH
	@Test
	void Ttl2Tsh() {
		println "Ttl2Tsh"
		new File("$base").eachFileRecurse(FILES) { ttlFile->
			if(ttlFile.name.endsWith('.ttl')) {
				def root= getFileRoot(ttlFile)
				println "root: $root"
				
				def tshFile = new File("$base/${root}.tsh")
				if (tshFile.exists() && fileIsNewer(tshFile,ttlFile)) {
					println "ERROR: TSH more recent than TTL"
					println "$tshFile > $ttlFile"
				} else {
					def m = ju.loadFileModelFilespec(""+ttlFile)
					def s = tsh.writeTsh(m)
					tshFile.text = s
					println "Wrote new TSH, $tshFile"
				}
			}
		}
	}

	// TSH -> TTL
	@Test
	void Tsh2Ttl() {
		println "Tsh2Ttl"
		new File("$base").eachFileRecurse(FILES) { tshFile->
			if(tshFile.name.endsWith('.tsh')) {
				def root= getFileRoot(tshFile)
				println "root: $root"

				def ttlFile = new File("$base/${root}.ttl")
				if (ttlFile.exists()) {
					if (fileIsNewer(ttlFile,tshFile)) {
						println "ERROR: TTL more recent than TSH"
						println "$ttlFile > $tshFile"
						return
					} else {
						def ofdt = getFileDtExt(ttlFile) // back ttl up
						ttlFile.renameTo("$base/${root}.ttl.$ofdt")
						println "backed up TTL"
					}
				}
				def sb = tsh.parseTsh2Ttl(tshFile)
				//println ""+sb
				def m = ju.saveStringModel(""+sb,"ttl")
				ju.saveModelFile(m,"$base/${root}.ttl","ttl")
				println "Wrote new TTL, $ttlFile"
			}
		}
	}
	// is f1 newer than f2
	def fileIsNewer(File f1,File f2) {
		def f1Dt = getModDt(f1)
		def f2Dt = getModDt(f2)
		f1Dt > f2Dt
	}

	def getFileDtExt(File f) {
		def dt0 = getModDt(f)
		// 2023-09-05T12:32:33.5586785Z
		def dt = (dt0 =~ /(.*)\..*/)[0][1]
		dt.replaceAll(/[-T:]/,"")
	}

	def getModDt(File f) {
		Path file = Paths.get(""+f);
		BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);
		attr.lastModifiedTime()
	}

	def getFileRoot(f) {
		(f.name =~ /(.*)\..*/)[0][1]
	}

}
