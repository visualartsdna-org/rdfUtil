package rdf.util

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import rdf.JenaUtilities

/**
 * JSON-LD Conversion
 * Engine Swap: Jena 3.17 used the jsonld-java library, 
 * which focused on JSON-LD 1.0. Jena 5.x uses the 
 * Titanium JSON-LD engine for its default processing
 * which uses the JSON-LD 1.1 spec.
 * Requires Java 17
 * 
 */
class JsonLd11To10ConverterTest {

	@Test
	void test() {
		def ju = new JenaUtilities()
		def m = ju.loadFiles("/stage/server/cwvaContent/ttl/data/4b3d2b42-0273-44dc-9b4a-1c431d96ab3f.ttl")

		def s0 = ju.saveModelString(m,"JSON-LD")
		def s1 = """
{
  "@context" : {
    "@version": 1.1,
    "height" : {
      "@id" : "https://schema.org/height",
      "@type" : "http://www.w3.org/2001/XMLSchema#float"
    },
    "hasArtistProfile" : {
      "@id" : "http://visualartsdna.org/model/hasArtistProfile",
      "@type" : "@id"
    },
    "image" : {
      "@id" : "https://schema.org/image",
      "@type" : "@id"
    },
    "width" : {
      "@id" : "https://schema.org/width",
      "@type" : "http://www.w3.org/2001/XMLSchema#float"
    },
    "media" : {
      "@id" : "http://visualartsdna.org/model/media"
    },
    "dateCreated" : {
      "@id" : "https://schema.org/dateCreated",
      "@type" : "http://www.w3.org/2001/XMLSchema#dateTime"
    },
    "label" : {
      "@id" : "http://www.w3.org/2000/01/rdf-schema#label"
    },
    "location" : {
      "@id" : "https://schema.org/location"
    },
    "description" : {
      "@id" : "https://schema.org/description"
    },
    "datePublished" : {
      "@id" : "https://schema.org/datePublished",
      "@type" : "http://www.w3.org/2001/XMLSchema#dateTime"
    },
    "qrcode" : {
      "@id" : "http://visualartsdna.org/model/qrcode",
      "@type" : "@id"
    },
    "hasPaper" : {
      "@id" : "http://visualartsdna.org/model/hasPaper",
      "@type" : "@id"
    },
    "identifier" : {
      "@id" : "https://schema.org/identifier"
    },
    "note" : {
      "@id" : "http://www.w3.org/2004/02/skos/core#note"
    },
    "schema" : "https://schema.org/",
    "owl" : "http://www.w3.org/2002/07/owl#",
    "work" : "http://visualartsdna.org/work/",
    "xsd" : "http://www.w3.org/2001/XMLSchema#",
    "skos" : "http://www.w3.org/2004/02/skos/core#",
    "rdfs" : "http://www.w3.org/2000/01/rdf-schema#",
    "the" : "http://visualartsdna.org/thesaurus/",
    "dct" : "http://purl.org/dc/terms/",
    "rdf" : "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
    "vad" : "http://visualartsdna.org/model/",
    "tko" : "http://visualartsdna.org/takeout/",
    "xs" : "http://www.w3.org/2001/XMLSchema#",
    "foaf" : "http://xmlns.com/foaf/0.1/"
  },
  "@id" : "work:4b3d2b42-0273-44dc-9b4a-1c431d96ab3f",
  "@type" : "vad:Watercolor",
  "hasArtistProfile" : "work:ebab5e0c-cc32-4928-b326-1ddb4dd62c22",
  "hasPaper" : "the:FabrianoArtisticoGranaFinaColdPress",
  "media" : "Watercolor",
  "qrcode" : "http://visualartsdna.org/images/qrc_4b3d2b42-0273-44dc-9b4a-1c431d96ab3f.jpg",
  "label" : "Wisteria",
  "note" : "Wisteria was introduced to Europe from China in the 19th century and sometime after found its way to the Alhambra.",
  "dateCreated" : "2025-10-06T12:44:02",
  "datePublished" : "2025-10-06T12:44:02",
  "description" : "The wisteria is fragrant in a courtyard of the Alhambra.",
  "height" : "11",
  "identifier" : "4b3d2b42-0273-44dc-9b4a-1c431d96ab3f",
  "image" : "http://visualartsdna.org/images/Wisteria.jpg",
  "location" : "The Alhambra, Granada, Spain",
  "width" : "15"
}
"""
		def s1to0 = JsonLd11To10Converter.convert(s1)
		
		def m2 = ju.saveStringModel(s1to0, "JSON-LD")
		
		def mDiff = m.difference(m2)
		println mDiff.size()
		
	}

	@Test
	void test0() {
// Example usage
		def example11 = '''
		{
		  "@context": {
		    "ex": "http://example.org/",
		    "name": "ex:name",
		    "age": "ex:age"
		  },
		  "@id": "ex:person1",
		  "name": "Alice",
		  "age": 30
		}
		'''
		
		println "JSON-LD 1.1 Input:"
		println example11
		println "\nJSON-LD 1.0 Output:"
		println JsonLd11To10Converter.convert(example11)
	}

}
