package json.parse

import static org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class KeepTest {
	static def verbose=false

	@Test
	void test() {
		
		def base = "C:/temp/Takeout/results"
		def src = "C:/temp/Takeout"
		def result = getTakeout(src)
		result.each{zip->
			println zip
			def id = ((""+zip) =~ /^.*[\\]([a-zA-Z0-9\-\.]+)[\\][a-zA-Z0-9\-\.]+$/)[0][1]
			def m0= new TkoExtract().processZipJson(zip)
			def labels = new TkoExtract().processZipTxt(zip)
			def imgList = new TtlBuilder().process(m0,labels,"$base/$id")
			new TkoExtract().processZipImg(zip,base,imgList)
		}
	}
	
	@Test
	void testId() {
		def s="C:\\temp\\Takeout\\rickspatesart\\takeout-20220628T010048Z-001.zip"
		def id = (s =~ /^.*[\\]([a-zA-Z0-9\-\.]+)[\\][a-zA-Z0-9\-\.]+$/)[0][1]
		println id
	}
	
	@Test
	void test3() {
		
		def base = "C:/temp/Takeout/results"
		//def zip = "C:/temp/Takeout/rickspatesart/takeout-20220628T010048Z-001.zip"
		def zip = "C:/temp/Takeout/rspates.art/takeout-20230925T223227Z-001.zip"
		def m0= new TkoExtract().processZipJson(zip)
		def labels = new TkoExtract().processZipTxt(zip)
		def s = new TtlBuilder().process(m0,labels,"$base/results")
		new TkoExtract().processZipImg(zip,base)
	}
	
	@Test
	void test2() {
		
//		def base = "C:/temp/Takeout"
//		def rsa = "rickspatesart"
//		def rspates = "rspates"
//		def rs = "rick.spates"
//		def file = "$base/$rsa//Takeout/Keep"
		
		def base = "C:/temp/Takeout/Takeout/Keep"
//		def base = "C:/temp/rsart/Takeout/Keep"
//		def m0= new TkoExtract().process("$base/Test Extinction.json")
		def m0= new TkoExtract().process("$base")
		def labels = new TkoExtract().processLabels("$base")
		
		def s = new TtlBuilder().process(m0,labels,"$base/test")
	}
	

	@Test
	void test1() {
		
//		def base = "C:/temp/Takeout/Takeout/Keep/"
		def base = "C:/temp/rsart/Takeout/Keep"
		def m0= new TkoExtract().process("$base/Test Extinction.json")
//		def m0= new TkoExtract().process("$base")
		
		m0.each{k1,v1->
			

			if (verbose) println "$k1"
		def m =new Keep().parseKeepConcepts(v1)
			if (m.topConcept) if (verbose) println "${m.topConcept}\n"
		
		try {
		m.each{k,v->
			if (k=="topConcept") return
			if (verbose) println "$k\n"

			if (v.containsKey("ann"))
			v.ann.each{k2,v2->
				if (verbose) println "\t$k2=$v2"
			}
			if (verbose) println "${v.text}\n"
		}
		} catch (Exception e) {
			println e
		}
		}
	}

	@Test
	void test0() {
		
		def fn = "./tkoTest2.txt"
		def s = new File(fn).text
		def m = new Keep().parseKeepConcepts(s)
		
		m.each{k,v->
			//println "$k\n\n${v.ann?v.ann:''}\n${v.text}\n"
			println "$k\n"
			v.ann.each{k2,v2->
				println "\t$k2=$v2"
			}
			println "${v.text}\n"
		}
	}

	@Test
	void testExtract() {
		def base = "C:/temp/Takeout/Takeout/Keep/"
		def m= new TkoExtract().process("$base/Asheville Trip Suggestions.json")
		m.each{k,v->
			println "$k=$v\n"
		}
	}
		
	// support reference to list of URIs
	@Test
	void testUriList() {
		[
			member:"the:abc,the:def",
			member2:"<abc>,<def>"
			].each{k2,v2->
		println """
		${TtlBuilder.nsMap[k2]}:$k2 ${v2=~/^<[A-Za-z_0-9\-\.]+>$|^[a-z]+:.*$/?v2:"\"$v2\""} ;
		"""
		}
	}
	
	@Test
	void testUSeconds() {
		def i = 1656256466731000
		def inst = Util.getInstantFromMicros(i)		
		println inst
		
	}
	
	
	// zip extraction
	// *.json -> processed
	// labels.txt -> processed
	// *.png -> results
	// *.jpg -> results
	
	// results dir represents stage/temp (then distribute)

	@Test
	void testZip() {
	
		//def fn = "C:/temp/Takeout/takeout-20230812T132523Z-001.zip"
		def fn = "C:/temp/rsart/takeout-20230813T164319Z-001.zip"
		def zipFile = new java.util.zip.ZipFile(new File(fn))
	
		zipFile.entries().each {
			if ((""+it).endsWith(".json")) {
				println it
				println zipFile.getInputStream(it).text
			}
		}
	}
	
	@Test
	void testZipDir() {
		def base = "C:/temp/Takeout"
		def result = getTakeout(base)
		result.each{ println it}
	}
	
	def getTakeout(base) {
		def results = []
		(base as File).eachFile(groovy.io.FileType.DIRECTORIES) {dir->
			def files=[]
			
			dir.eachFile(groovy.io.FileType.FILES) {file->
				if(file.name.endsWith('.zip')) {
					files << file
				}
			}
			def result = files.sort{ a,b -> b.lastModified() <=> a.lastModified() }//*.name
			if (!result.isEmpty())
				results << result.first()
		}
		results
	}
	

}
