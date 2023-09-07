package tsh

import static org.junit.jupiter.api.Assertions.*

import groovy.json.JsonSlurper
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.impl.RDFListImpl
import org.junit.jupiter.api.Test
import org.markdownj.MarkdownProcessor
import rdf.JenaUtils

//Markdown Viewer
//https://chrome.google.com/webstore/detail/markdown-viewer/ckkdlimhmcjmikdlpkmbgfkaikojcbjk
class TshTest0 {

	def ju = new JenaUtils()
	def tsh = new TopicShorthand()
	def base = "C:/temp/git/cwvaContent/ttl/model"
	
	@Test
	void testRewrite() {
		def fn = "C:/temp/git/cwvaContent/ttl/model/topics2.txt"
			
		// generate ttl from the tsh
		def sb = tsh.parseTsh2Ttl(new File(fn))
		println ""+sb
		Model m = ju.saveStringModel(""+sb,"ttl")
		//println m.size()
		
		def s = tsh.writeTsh(m)
		println s
			
		
		def sb2 = tsh.parseTsh2Ttl(s)
		println ""+sb2
		Model m2= ju.saveStringModel(""+sb,"ttl")
		//println m.size()
		
		// extract the model graph from json-ld returning collection
		def c = tsh.getGraph(m2)
		println tsh.printTopics(c, "the:abc")
		

	}
	
	@Test
	void testMarkdown() {
		def fn = "C:/temp/git/cwvaContent/ttl/model/topics2.txt"
		
		// generate ttl from the tsh
		def sb = tsh.parseTsh2Ttl(new File(fn))
		println ""+sb
		Model m = ju.saveStringModel(""+sb,"ttl")
		//println m.size()
		
		// extract the model graph from json-ld returning collection
		def c = tsh.getGraph(m)
		def md = tsh.printTopics(c, "the:abc")
		MarkdownProcessor markup = new MarkdownProcessor()
		def s = markup.markdown(md)

		new File("C:/temp/git/cwvaContent/ttl/model/topic.html").text = s

	}
	
	@Test
	void testParse() {
		def fn = "C:/temp/git/cwvaContent/ttl/model/topics2.txt"
		
		// generate ttl from the tsh
		def sb = tsh.parseTsh2Ttl(new File(fn))
		println ""+sb
		Model m = ju.saveStringModel(""+sb,"ttl")
		//println m.size()
		
		// extract the model graph from json-ld returning collection
		def c = tsh.getGraph(m)
		println tsh.printTopics(c, "the:abc")

	}
	
	@Test
	void testParse0() {
		def fn = "C:/temp/git/cwvaContent/ttl/model/topics.txt"
		def sb = tsh.parseTsh2Ttl(fn)
		println ""+sb

	}
	
	@Test
	void test() {
		Model m = ju.loadFileModelFilespec("C:/temp/git/cwvaContent/ttl/model/topics.ttl")
		//rdfl.nsl = ["the","skos"]
		def c = tsh.getGraph(m)
				
		printTopics(c, "the:abc")
	}
	
	@Test
	void testNoNs() {
		Model m = ju.loadFileModelFilespec("C:/temp/git/cwvaContent/ttl/model/topics.ttl")
		def rdfl = new TopicShorthand()
		//rdfl.nsl = ["the","skos"]	// little value to remove NS
		def c = rdfl.getGraph(m)
				
		tsh.printTopics(c, "abc")
	}
	
	@Test
	void test2() {
		Model m = ju.loadFileModelFilespec("C:/temp/git/cwvaContent/ttl/model/topics.ttl")
		//rdfl.nsl = ["the","skos"]
		def c = tsh.getGraph(m)
		
		//println c
		
		c.abc.each{k,v->
			if (k=="memberList") v.each	{
				println it
			} else {
				println "${k}, ${v}"
			}
		}
	}
	
	@Test
	void test1() {
		Model m = ju.loadFileModelFilespec("C:/temp/git/cwvaContent/ttl/model/topics.ttl")
		tsh.nsl = ["the","skos"]
		def c = rdfl.getGraph(m)
		//println c
		c.abc.memberList.each{
			println "${c[c[it].head].label}, ${c[c[it].head].definition}"
		}
	}

	@Test
	void test0() {
		Model m = ju.loadFileModelFilespec("C:/temp/git/cwvaContent/ttl/model/topics.ttl")
		def c = new TopicShorthand().getGraph(m)
		c.each{
			println it
			println ""
			}
	}

	@Test
	void testJson() {
		Model m = ju.loadFileModelFilespec("C:/temp/git/cwvaContent/ttl/model/topics.ttl")
		
		def s = ju.saveModelString(m,"json-ld")
		//println s
		def c = new JsonSlurper().parseText(s)
		
		c["@graph"].each{
			println it
			println ""
			}
	}

	@Test
	void testRdfList() {
		Model m = ju.loadFileModelFilespec("C:/temp/git/cwvaContent/ttl/model/topics.ttl")
		def l = ju.getRdfList(m,
			"http://visualartsdna.org/thesaurus/mno",
			"http://www.w3.org/2004/02/skos/core#memberList")
		l.each{ 
			println it
			}
	}
	
	@Test
	void testRdfList0() {
		Model m = ju.loadFileModelFilespec("C:/temp/git/cwvaContent/ttl/model/topics.ttl")
		println m.size()
		// List<RDFNode>	new RDFListImpl(Node n, EnhGraph g).asJavaList()
		Resource res = m.getResource("http://visualartsdna.org/thesaurus/mno")
		println res
		Property prop = m.createProperty("http://www.w3.org/2004/02/skos/core#memberList")
		Resource head = (Resource) res.getProperty(prop)
                                .getObject();
		println head.getId() // blank
		def l = new RDFListImpl(head.asNode(), m).asJavaList()
		println l
	}
	
}
