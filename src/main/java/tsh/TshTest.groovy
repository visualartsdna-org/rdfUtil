package tsh

import static org.junit.jupiter.api.Assertions.*

import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import org.junit.jupiter.api.Test
import rdf.JenaUtils
import static groovy.io.FileType.FILES

class TshTest {

	def ju = new JenaUtils()
	def tsh = new TopicShorthand()
	def base = "C:/temp/git/cwvaContent/ttl/topics"

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
