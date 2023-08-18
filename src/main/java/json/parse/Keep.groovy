package json.parse

class Keep {
	
	/**
	 * collect concepts from TKO-GKeep
	 *
	 * @param fn, filename
	 * @return
	 */
	def parseKeepConcepts(input) {
		
		def m = [:]
		def prev
		def text=false
		def title=false
		def key
		def header=true
		def topConceptMap = [text:"",ann:[:]]
		input.eachLine{
			def s = it.trim()
			if (header && s != "") {
				def ma = extractAnnotations(s) // TODO: this
				if (ma.isEmpty()) topConceptMap.text += "$s\n"
				topConceptMap.ann+= ma
				
				return
			}
			if (s=="" && prev=="") {
				text=false
				title=true
				header=false
				return
			}
			if (s=="") {
				text = true
				prev = s
				header=false
				return
			}
			if (title) {
				key = s.replaceAll(/[^A-Za-z_0-9]/,"")
				m[key]=[ann:[:],text:""]
				title=false
				prev = s
				return
			}
			if (text) {
				def ma = extractAnnotations(s)
				if (!key) return
				if (ma.isEmpty()) m[key].text += "$s\n"
				m[key].ann+= ma
				prev = s
			}
		}
		// capture only complete and consistent concepts
		def ld=[]
		m.each{k,v->
			if (!v.text && v.ann.isEmpty()) ld += k
		}
		ld.each{m.remove(it)}
		m.topConcept= topConceptMap
		m
	}
	
	// get any annotations in text
	// URL references (links) on discrete lines become rdfs:seeAlso
	def extractAnnotations(s) {
		def ma = [:]
		if (s.trim() =~ /^http[s]?:\/\/[A-Za-z_0-9\+\%\-\.\/]+$/) {
			ma[s.trim()] = "rdfs:seeAlso"
		} else {
			def m = (s =~ /^\[([A-Za-z0-9_.]+)[ \t]*[=:][ \t]*(.*)\]$/)
			if (m) {
				ma[m[0][1]] = m[0][2]
			}
		}
		ma
	}

}
