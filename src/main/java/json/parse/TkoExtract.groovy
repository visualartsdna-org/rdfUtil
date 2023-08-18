package json.parse

import groovy.json.JsonSlurper
import util.Rson

class TkoExtract {
	
	// returns a map of filename:bitmap image
	def processZipImg(String src,base) {
		processZipImg(new File(src),base)
	}
	def processZipImg(File src,base,list) {

		def map = [:]
		def zipFile = new java.util.zip.ZipFile(src)
	
		zipFile.entries().each {
			if ((""+it) =~ /.*\.png$|.*\.jpg$/) {
				println it
				def fn = (""+it).substring("Takeout/Keep/".length())
				if (fn in list)
					map[fn] = zipFile.getInputStream(it)
			}
		}
		
		map.each{fn,v->
			def mt = fn.substring(fn.length()-3)
			def fos = new FileOutputStream("$base/$fn")
			copy(v,fos)
		}
	}
	
	void copy(InputStream source, OutputStream target) throws IOException {
		byte[] buf = new byte[8192];
		int length;
		while ((length = source.read(buf)) != -1) {
			target.write(buf, 0, length);
		}
	}
	
	// returns a map of text labels:empty list
	def processZipTxt(String src) {
		processZipTxt(new File(src))
	}
	def processZipTxt(File src) {

		def map = [:]
		def zipFile = new java.util.zip.ZipFile(src)
	
		zipFile.entries().each {
			if ((""+it).endsWith(".txt")) {
				println it
				zipFile.getInputStream(it).text.eachLine{
					map[it] = []
				}
			}
		}
		
		map
	}
	
	// returns map of note title:collection
	def processZipJson(String src) {
		processZipJson(new File(src))
	}
	def processZipJson(File src) {

		def map = [:]
		def zipFile = new java.util.zip.ZipFile(src)
	
		zipFile.entries().each {
			if ((""+it).endsWith(".json")) {
				println it
				def c = new JsonSlurper().parseText(zipFile.getInputStream(it).text)
				if (!hasLabel(c,"publish")) return
				if (c.isTrashed || c.isArchived) return

				if (map.containsKey(c.title))
					throw new RuntimeException("title already exists")
				map[c.title]=c
			}
		}
		
		map
	}
	
	// given a json file or directory of json files
	// returns map of note title:collection
	def process(src) {

		def map = [:]
		def f = new File(src)
		
		if (f.isDirectory())
			f.eachFile {file->
				
				if (!(file.name.endsWith(".json"))) return
	
				println "$file"
				
				def c = Rson.load(file.absolutePath)
				if (!hasLabel(c,"publish")) return
				if (c.isTrashed || c.isArchived) return
//				if (file.name =="Drawings collection.json") {
//					println "here"
//				}
				if (map.containsKey(c.title))
					throw new RuntimeException("title already exists")
				map[c.title]=c
			}
		else {
			def c = Rson.load(f.absolutePath)
			map[c.title]=c
		}
		
		map
	}
	
	def processLabels(base) {
		def labelsMap = [:]
		new File("$base/Labels.txt").text.eachLine{
			labelsMap[it] = []
		}
		labelsMap
	}


	def hasLabel(c,label) {
		def l = c.labels
		l.find{
			it.name == label
		}
	}
}
