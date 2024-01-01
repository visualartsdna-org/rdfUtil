package tsh

import static org.junit.jupiter.api.Assertions.*
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import org.apache.jena.rdf.model.Model
import org.junit.jupiter.api.Test
import rdf.JenaUtils
import static groovy.io.FileType.FILES


class TshTest {

	def ju = new JenaUtils()
	def tsh = new TopicShorthand()
	def base = "C:/temp/git/cwvaContent/ttl/topics"

	@Test
	void testPrintTopics() {
		def ttlFile = "$base/topics.ttl"
		def m = ju.loadFileModelFilespec(""+ttlFile)
		def m2 = ju.loadFileModelFilespec("C:/temp/Takeout/results/rspates.art.ttl")
		m.add m2
		
		def s = tsh.printTopics(m)
		println s

	}
	
	@Test
	void testTsh2Ttl() {
		def s = tsh.writeTtl(new File("$base/topics.tsh"))
		println s
//		new File("C:/temp/git/cwvaContent/ttl/topics/topics2.ttl").text = s
//		def m = ju.loadFileModelFilespec("C:/temp/git/cwvaContent/ttl/topics/topics2.ttl")
//		println m.size()
	}
	
	
	@Test
	void testTtl2Tsh() {
		def ttlFile = "$base/topics.ttl"
		def m = ju.loadFileModelFilespec(""+ttlFile)
		def s = tsh.writeTsh(m)
		println s

	}
	
	
	//@Test
	void test0() {
		def ttlFile = "$base/topics.ttl"
		def m = ju.loadFileModelFilespec(""+ttlFile)
		def l = tsh.writeTsh(m)
		l.each {
			it.each{k,v->
				println """${k=="@id"?"":" "}$k $v"""
			}
		}
	}
}
