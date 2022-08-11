package rdf

import groovy.json.JsonSlurper
import java.text.SimpleDateFormat
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.listeners.StatementListener
import org.apache.jena.rdf.model.*;
import org.apache.jena.query.*;
import org.apache.jena.sparql.core.Prologue
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.jena.update.UpdateRequest
import org.apache.jena.util.FileManager;
import org.apache.jena.util.ResourceUtils
import org.apache.jena.update.UpdateAction
import org.apache.jena.datatypes.xsd.XSDDatatype
import org.apache.jena.ontology.OntDocumentManager
import org.apache.jena.ontology.OntModel
import org.apache.jena.ontology.OntModelSpec
//import org.apache.jena.shacl.* // better to use jena shacl utility
/**
 * A set of utilities for common functions
 * based on Jena models and related data
 * @author rspates
 *
 */
class JenaUtils {
	
	
	def prefixes=""
	
	/**
	 * Init class instance
	 */
	public JenaUtils(){
	}

	/**
	 * Init class instance with prefixes
	 * @param pf
	 */
	public JenaUtils(pf){
		this.prefixes = pf
	}

	/**
	 * Return a new jena model
	 * @return model
	 */
	def newModel(){
		Model data = ModelFactory.createDefaultModel();
	}
	
	/**
	 * Create a SPARQL set of prefixes
	 * from the model
	 * @param m
	 * @return
	 */
	def getPrefixes(m) {
		def pres = ""
		def pm = m.getNsPrefixMap()
		pm.each{k,v->
			pres += "prefix $k: <$v>\n"
		}
		pres
	}
	
	/**
	 * Create a TTL set of prefixes
	 * from the model
	 * @param m
	 * @return
	 */
	def getPrefixesTTL(m) {
		def pres = ""
		def pm = m.getNsPrefixMap()
		pm.each{k,v->
			pres += "@prefix $k: <$v> .\n"
		}
		pres
	}
	
	/**
	 * Save a string representation of RDF
	 * in JSON-LD format to a model
	 * @param ttldata
	 * @return model
	 */
	def saveStringModel(ttldata){
		return saveStringModel(ttldata, "JSONLD")
	}
	
	/**
	 * Save a string representation of RDF 
	 * in type (e.g., TTL) format to a model
	 * @param ttldata
	 * @param type
	 * @return model
	 */
	def saveStringModel(ttldata, type){
		return saveStringModel(ttldata, type, java.nio.charset.StandardCharsets.UTF_8)
	}
	
	/**
	 * Save a string representation of RDF
	 * in type (e.g., TTL) format to a model
	 * @param ttldata
	 * @param type
	 * @param charset
	 * @return model
	 */
	def saveStringModel(ttldata, type, charset){
		Model data = ModelFactory.createDefaultModel();
		def is = new ByteArrayInputStream(
			ttldata.getBytes(
				charset))
		data.read(is,null,type)
		return data
	}
	
	/**
	 * Save a model to a JSON-LD string
	 * @param model
	 * @return
	 */
	def saveModelString(model){
		return saveModelString(model, "TTL")
	}
	
	/**
	 * Save a model to a string as a type
	 * where type is TTL, etc.
	 * @param model
	 * @param type
	 * @return
	 */
	def saveModelString(model,type){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		model.write(baos,type)
		String ld = baos.toString()
		//println "size=${ld.size()}"
		//println "${ld.substring(0,ld.size()>5000?5000:ld.size())}"
		return ld
	}
	
	/**
	 * Save a model to the file specified
	 * @param model
	 * @param folderspec
	 * @param filespec
	 * @param ext
	 * @return
	 */
	def saveModelFile(model, folderspec, filespec, ext){
		new File(folderspec, filespec)
		.withOutputStream { stream ->
			model.write(stream, ext)
		}
	}
	
	/**
	 * Save a model to the file specified
	 * @param model
	 * @param filespec
	 * @param ext
	 * @return
	 */
	def saveModelFile(model, filespec, ext){
		new File(filespec)
		.withOutputStream { stream ->
			model.write(stream, ext)
		}
	}
	
	/**
	 * Return a model from a resource on the classpath
	 * @param filespec
	 * @param ext
	 * @return model
	 */
	def loadResourceModel(filespec,ext){
		Model data = ModelFactory.createDefaultModel();
		InputStream is = JenaUtils.class.getClassLoader().getResourceAsStream(filespec)
		data.read(is, null, ext)
		return data;
	}
	
	/**
	 * Load a model from a file given
	 * folder, filename and extension
	 * @param folderspec
	 * @param filespec
	 * @param ext
	 * @return model
	 */
	def loadFileModel(folderspec,filespec,ext){
		Model data = ModelFactory.createDefaultModel();
		new File(folderspec,filespec)
		.withInputStream { stream ->
			data.read(stream, null, ext)
		}
//		println "dir=$folderspec; file=$filespec; size=${data.size()}"
		return data;
	}
	
	/**
	 * Return a model from a TTL file spec
	 * @param folderspec
	 * @param filespec
	 * @return model
	 */
	def loadFileModel(folderspec,filespec){
		Model data = ModelFactory.createDefaultModel();
		new File(folderspec,filespec)
		.withInputStream { stream ->
			data.read(stream, null, "ttl")
		}
//		println "dir=$folderspec; file=$filespec; size=${data.size()}"
		return data;
	}
	
	/**
	 * Load a TTL file returning a model
	 * @param absfilespec
	 * @return model
	 */
	def loadFileModelFilespec(absfilespec){
		return loadFileModelFilespec(absfilespec, "ttl")
	}
	
	/**
	 * Load a list of absolute TTL files returning a model
	 * @param absfilespec
	 * @return model
	 */
	def loadListFilespec(List list){
		Model m = ModelFactory.createDefaultModel()
		list.each{
			m.add(loadFileModelFilespec(it))
		}
		return m
	}
	
	/**
	 * Get a list of files from a path 
	 * matching the filter
	 * @param path
	 * @param filter
	 * @return a list
	 */
	public static def listFiles(path, filter){
		def list = []
		new File(path).eachFile {file ->
			if (file.isFile()
				&& file.name =~ filter)
				list += file.getPath()
		}
		return list
	}
	
	/**
	 * Load a model from a dir of ttl 
	 * or a single ttl file
	 * Handling is based on isDirectory(spec)
	 * @param dirSpec
	 * @return model
	 */
	def loadFiles(spec,ext){
		def model = newModel()
		if (new File(spec).isDirectory()) {
			model = loadDirModel(spec)
		} else {
			model = loadFileModelFilespec(spec,ext)
		}
		model
	}

	/**
	 * Load a model from a dir of ttl 
	 * or a single ttl file
	 * Handling is based on isDirectory(spec)
	 * @param dirSpec
	 * @return model
	 * @deprecated
	 */
	def loadFiles0(spec){
		def model = newModel()
		if (new File(spec).isDirectory()) {
			model = loadDirModel(spec)
		} else {
			model = loadFileModelFilespec(spec)
		}
		model
	}

	/**
	 * Load a model from a dir of files of any model type
	 * type is determined from file extension
	 * or a single file of any model type
	 * @param dirSpec or fileSpec
	 * @return model
	 */
	def loadFiles(spec){
		def model = newModel()
		if (new File(spec).isDirectory()) {
			new File(spec).eachFileRecurse(FileType.FILES) {
				model.add( loadFile(""+it) )
			}

		} else {

			model = loadFileModelFilespec(spec)
		}
		model
	}

	/**
	 * Load any model file
	 * Also translates JSON to analogous model file
	 * the subdir of the JSON profides the namespace context
	 * @param spec
	 * @return model
	 */
	def loadFile(spec) {
		def ext = (spec =~ /^.*\.([a-zA-Z-]+)$/)[0][1]
		def model = newModel()

		if (ext in [
					"ttl",
					"rdf",
					"jsonld",
					"json-ld",
					"nt",
					"nq",
					"trig",
					"trix",
					"rt",
					"trdf"
				]) {
			model = loadFileModelFilespec(spec,ext)
		}
		model
	}
	
	/**
	 * Load a dir of RDF files into a model
	 * Assumes TTL
	 * @param dirSpec
	 * @param data
	 * @return
	 */
	def loadDirModel(dirSpec,Model data){
		def l = listFiles(dirSpec,".ttl")
		l.each{
			//println "$it"
				new File(it)
				.withInputStream { stream ->
					data.read(stream, null, "ttl")
				}
			//println data.size()
		}
	}
	
	/**
	 * Load a dir of RDF files into a model
	 * Assumes TTL
	 * @param dirSpec
	 * @param data
	 * @return
	 */
	def loadDirModel(dirSpec,Model data,ext){
		def l = listFiles(dirSpec,".$ext")
		l.each{
			//println "$it"
				new File(it)
				.withInputStream { stream ->
					data.read(stream, null, ext)
				}
			//println data.size()
		}
	}
	
	/**
	 * Load a model from a dir of files
	 * @param dirSpec
	 * @return model
	 */
	def loadDirModel(dirSpec){
		def model = ModelFactory.createDefaultModel()
		loadDirModel(dirSpec,model)
		return model
	}
	
	/**
	 * Load a model from a dir of files
	 * @param dirSpec
	 * @return model
	 */
	def loadDirModel(dirSpec,ext){
		def model = ModelFactory.createDefaultModel()
		loadDirModel(dirSpec,model,ext)
		return model
	}
	
	/**
	 * Return a list of files from a URL spec
	 * matching the filter
	 * @param urlSpec
	 * @param filter
	 * @return list
	 */
	public static def listUrlFiles(urlSpec, filter){
		def list = []
		new URL(urlSpec).eachLine {file ->
			if (file  =~ filter)
				list += file.getPath()
		}
		return list
	}
	/**
	 * Load files from a url spec into a model
	 * @param urlSpec
	 * @param data a model to receive the data
	 * @return the model
	 */
	def loadUrlDirModel(urlSpec,data){
		def l = listUrlFiles(urlSpec,".ttl")
		l.each{
			//println "$it"
				new File(it)
				.withInputStream { stream ->
					data.read(stream, null, "ttl")
				}
			//println data.size()
		}
		data
	}
	
	/**
	 * Load a model from a file spec
	 * ext is the model file type, e.g., TTL or JSON-LD, see jena doc
	 * @param absfilespec
	 * @param ext
	 * @return
	 */
	def loadFileModelFilespec(absfilespec, ext){
		Model data = ModelFactory.createDefaultModel();
		new File(absfilespec)
		.withInputStream { stream ->
			data.read(stream, null, ext)
		}
//		println "file=$filespec; size=${data.size()}"
		return data;
	}
	
	/**
	 * Save the result of a SPARQL describe to 
	 * a TTL representation of the resulting model
	 * @param model
	 * @param qs
	 * @return model as TTL string
	 */
	def modelExtract(model, qs) {
		Model resultModel = queryDescribe(model,prefixes, qs)
		def ld = saveModelString(resultModel,"TTL")
		return ld
	}

	/**
	 * Return a model representing a SPARQL describe
	 * @param model
	 * @param prefixes
	 * @param queryString
	 * @return model
	 */
	def queryDescribe(Model model, prefixes, queryString){
		Query query = QueryFactory.create(prefixes + queryString) ;
		QueryExecution qexec = QueryExecutionFactory.create(query, model)
		Model resultModel = qexec.execDescribe()
		return resultModel;
	}

	/**
	 * Print a formatted result of the query on the model
	 * @param model
	 * @param prefixes
	 * @param queryString
	 * @return string
	 */
	def query(Model model, prefixes, queryString){
		Query query = QueryFactory.create(prefixes + queryString) ;
		QueryExecution qexec = QueryExecutionFactory.create(query, model)
		ResultSet results = qexec.execSelect()
		return ResultSetFormatter.asText(results, new Prologue(model));
	}

	/**
	 * Return list of result bindings	 * @param model
	 * @param prefixes
	 * @param queryString
	 * @return
	 */
	def queryToList(Model model, prefixes, queryString){
		Query query = QueryFactory.create(prefixes + queryString) ;
		QueryExecution qexec = QueryExecutionFactory.create(query, model)
		ResultSet results = qexec.execSelect()
		return ResultSetFormatter.toList(results);
	}

	/**
	 * Return list of maps
	 * Each map entry is for the list of values for one variable	 * o=[191342, Rul...]
	 * @param model
	 * @param prefixes
	 * @param queryStr
	 * @return list of maps
	 */
	public Map queryListMap(Model model,prefixes, queryStr) {

		Query query = QueryFactory.create(prefixes + queryStr) ;
		QueryExecution qexec = QueryExecutionFactory.create(query, model)
		ResultSet results = qexec.execSelect()

		ByteArrayOutputStream baos = new ByteArrayOutputStream()
		// Output query results
		ResultSetFormatter.outputAsJSON(baos, results)

		def slurper = new JsonSlurper()
		def map = slurper.parseText(baos.toString())

		def numap = [:]

		map.head.vars.each {
			if (!numap.containsKey(it))
				numap[it] = []
		}
		map.results.bindings.each{
			it.each{k,v->
				numap[k] += v.value
			}
		}
		return numap
	}
	
	// preferred
	/**
	 * Return a list of maps
	 * Each map is one row of the result	 * @param model
	 * @param prefixes
	 * @param queryStr
	 * @return list of maps
	 */
	public List queryListMap1(Model model,prefixes, queryStr) {

		Query query = QueryFactory.create(prefixes + queryStr) ;
		QueryExecution qexec = QueryExecutionFactory.create(query, model)
		ResultSet results = qexec.execSelect()

		ByteArrayOutputStream baos = new ByteArrayOutputStream()
		// Output query results
		ResultSetFormatter.outputAsJSON(baos, results)

		def slurper = new JsonSlurper()
		def map = slurper.parseText(baos.toString())

		def list = []
		map.results.bindings.each{m->
			def numap = [:]
			list += numap
			m.each{k,v->
				numap[k] = v.value
			}
		}
		return list
	}
	
	// preferred
	/**
	 * Return a list of maps, the map values have full type info
	 * Each map is one row of the result	 * @param model
	 * @param prefixes
	 * @param queryStr
	 * @return
	 */
	public List queryListMap3(Model model,prefixes, queryStr) {

		Query query = QueryFactory.create(prefixes + queryStr) ;
		QueryExecution qexec = QueryExecutionFactory.create(query, model)
		ResultSet results = qexec.execSelect()

		ByteArrayOutputStream baos = new ByteArrayOutputStream()
		// Output query results
		ResultSetFormatter.outputAsJSON(baos, results)

		def slurper = new JsonSlurper()
		def map = slurper.parseText(baos.toString())

		def list = []
		map.results.bindings.each{m->
			def numap = [:]
			list += numap
			m.each{k,v->
				if (v.type=="uri") {
					numap[k]="<${v.value}>"
				} else if (v.type=="literal"){
					def dt = v.datatype
					if (dt!=null) {
						numap[k] = """'${v.value}'^^<$dt>"""
					}
					else {
						numap[k] = """'${v.value}'^^<http://www.w3.org/2001/XMLSchema#string>"""
					}
				}
			}
		}
		return list
	}
	
	// preferred
	// values with full type info
	/**
	 * Return a list of maps, the map values have full type info.
	 * All values in triple-double quotes, better handle embedded quotes
	 * Each map is one row of the result
	 * @param model
	 * @param prefixes
	 * @param queryStr
	 * @return
	 */
	public List queryListMap4(Model model,prefixes, queryStr) {

		Query query = QueryFactory.create(prefixes + queryStr) ;
		QueryExecution qexec = QueryExecutionFactory.create(query, model)
		ResultSet results = qexec.execSelect()

		ByteArrayOutputStream baos = new ByteArrayOutputStream()
		// Output query results
		ResultSetFormatter.outputAsJSON(baos, results)

		def slurper = new JsonSlurper()
		def map = slurper.parseText(baos.toString())

		def list = []
		map.results.bindings.each{m->
			def numap = [:]
			list += numap
			m.each{k,v->
				if (v.type=="uri") {
					numap[k]="<" + v.value + ">"
				} else if (v.type=="literal"){
					def dt = v.datatype
					if (dt!=null) {
						numap[k] = """"${v.value}"^^<$dt>"""
					}
					else {
						def val = v.value.replaceAll(~/"/,"\\\\\"")
						numap[k] = """\"\"\"${val}\"\"\"^^<http://www.w3.org/2001/XMLSchema#string>"""
					}
				}
			}
		}
		return list
	}
	
	/**
	 * Returns a model with constructed triples
	 * @param model
	 * @param prefixes
	 * @param queryStr
	 * @param bindMap, initial binding map, variable : value
	 * @return a model
	 */
	public Model queryExecConstruct(Model model,prefixes, queryStr, bindMap) {
		
				Query query = QueryFactory.create(prefixes + queryStr) ;
				QueryExecution qexec = QueryExecutionFactory.create(query, model)
				
				QuerySolutionMap init = new QuerySolutionMap() ;
				bindMap.each{k,v->
					init.add(k, model.createLiteral(v));
					qexec.setInitialBinding(init) ;
				}
				def m = qexec.execConstruct()
				return m
	}
	
	/**
	 * Returns a model with constructed triples
	 * @param model
	 * @param prefixes
	 * @param queryStr
	 * @return a model
	 */
	public Model queryExecConstruct(Model model,prefixes, queryStr) {
		
				Query query = QueryFactory.create(prefixes + queryStr) ;
				QueryExecution qexec = QueryExecutionFactory.create(query, model)
				def m = qexec.execConstruct()
				return m
	}
	
	/**
	 * Performs a SPARQL update on a model
	 * @param model
	 * @param prefixes
	 * @param queryStr
	 * @return model
	 */
	public Model queryExecUpdate(Model model,prefixes, queryStr) {
		
				UpdateAction.parseExecute(prefixes + queryStr, model)
	}
	
	/**
	 * Return list of maps with datatype info
	 * Each map entry is for the list of values for one variable	 * @param model
	 * @param prefixes
	 * @param queryStr
	 * @return list of maps
	 */
	public Map queryListMap2(Model model,prefixes, queryStr) {

		Query query = QueryFactory.create(prefixes + queryStr) ;
		QueryExecution qexec = QueryExecutionFactory.create(query, model)
		ResultSet results = qexec.execSelect()

		ByteArrayOutputStream baos = new ByteArrayOutputStream()
		// Output query results
		ResultSetFormatter.outputAsJSON(baos, results)

		def slurper = new JsonSlurper()
		def map = slurper.parseText(baos.toString())

		def numap = [:]

		map.head.vars.each {
			if (!numap.containsKey(it))
				numap[it] = null
		}
		map.results.bindings.each{
			it.each{k,v->
				if (numap[k]==null)
					numap[k] = v.value
				else if (numap[k] instanceof List) {
					numap[k] += v
				}
				else {
					def tmp = numap[k]
					numap[k] = []
					numap[k] += tmp
					numap[k] += v
				}
			}
		}
		return numap
	}
	
	/**
	 * model difference
	 * @param a model
	 * @param b model
	 * @return boolean true if no differences
	 */
	public static boolean compareModels(Model a, Model b){
		def n0 = a.difference(b).size()
		def n1 = b.difference(a).size()
		return n0 == n1 && n0 == 0L
	}
	
	/**
	 * Finds file type from extension
	 * @param filespec
	 * @return
	 */
	public static String detectType(String filespec){
		def extNdx = filespec.lastIndexOf(".")
		String ext = filespec.substring(extNdx + 1,filespec.size())
		return ext.toUpperCase()
		
	}

	/**
	 * datetime xsd literal for now
	 * @param model
	 * @return
	 */
	static def getDateTimeNow(model) {
			return getDateTimeNow(model,new Date())
	}
	/**
	 * datetime xsd literal for given date
	 * local timezone
	 * @param model
	 * @param date
	 * @return
	 */
	static def getDateTimeNow(model,Date date) {
		model.createTypedLiteral(
			new SimpleDateFormat( "yyyy-MM-dd'T'hh:mm:ssX" ).format(date),
		"http://www.w3.org/2001/XMLSchema#dateTime")
	}
	
	/**
	 * <p>Answer a statement list that contains all of the resources reachable from a given
	 * resource by any property, transitively.  The returned graph is the sub-graph
	 * of the parent graph of root, whose root node is the given root. Cycles are
	 * permitted in the sub-graph.</p>
	 * @param root The root node of the sub-graph to extract
	 * @return A statement list containing all reachable RDFNodes from root by any property.
	 */
	static def reachableClosure( Resource root ) {

		// set of resources we have passed through already (i.e. the occurs check)
		def seen = [:]
		def stmtList = []
		if (root==null) return stmtList
		// queue of resources we have not yet visited
		List<RDFNode> queue = new LinkedList<>();
		queue.add( root );

		while (!queue.isEmpty()) {
			Resource r = (Resource) queue.remove( 0 );

			// check for multiple paths arriving at this queue node
			if (!seen.containsKey( r )) {
				seen[r]= null;

				// add the statements to the output model, and queue any new resources
				for (StmtIterator i = r.listProperties(); i.hasNext(); ) {
					Statement s = i.nextStatement();

					// don't do the occurs check now in case of reflexive statements
					stmtList.add( s );

					if (s.getObject() instanceof Resource) {
						queue.add( s.getObject() );
					}
				}
			}
		}

		return stmtList
	}

	/**
	 * Given a model and a starting property and literal value
	 * of a typically blank node graph, traverse up torward
	 * the root of the graph returning the root node.
	 * @param model
	 * @param ns namespace
	 * @param prop the property containing the identifier
	 * @param value the identifier
	 * @param value the datatype of the identifier (e.g., XSDInt)
	 * @return
	 */
	static def findRootNode(model, ns, prop, value, valType) {
		def result=value
		if(prop
			&& !(prop instanceof Property)) 
			prop=model.getProperty(ns, prop)
		if(value
			&& !(value instanceof Resource))
			value = model.createTypedLiteral(value,valType)
		
		model.listStatements(null,prop,value).each { 
			result =findRootNode(model,null,null,it.getSubject())
		}
		result instanceof Resource ? result : null
	}

	/**
	 * This version without the XSDType is less 
	 * certain in matching the identifier "value"
	 * @param model
	 * @param ns
	 * @param prop
	 * @param value
	 * @return
	 */
	static def findRootNode(model, ns, prop, value) {
		def result=value
		if(prop
			&& !(prop instanceof Property)) 
			prop=model.getProperty(ns, prop)
		if(value
			&& !(value instanceof Resource))
			value = model.createTypedLiteral(value)
		
		model.listStatements(null,prop,value).each { 
			result =findRootNode(model,null,null,it.getSubject())
		}
		result instanceof Resource ? result : null
	}
	
	/**
	 * Load an ontology and all 
	 * referenced imports
	 * @param meta
	 * @return
	 */
	def loadOntImports(meta) {
		def importSet = [:]
		OntDocumentManager mgr = new OntDocumentManager();
		OntModelSpec s = new OntModelSpec( OntModelSpec.RDFS_MEM );
		s.setDocumentManager( mgr );
		loadOntImports(s,meta,importSet)
	}
	
	/**
	 * Load referenced imports
	 * w/r.t. an ontology model
	 * @param meta
	 * @return
	 */
	def loadOntImports(s,meta,importSet) {
//		println meta
		OntModel m = ModelFactory.createOntologyModel( s );
		m.read(meta)
//		println m.size()
		def qm = new JenaUtils().queryListMap1(m,"", """
prefix owl:   <http://www.w3.org/2002/07/owl#> 

select *{
		?s owl:imports ?o
}
""")
			qm.each{
				if (!importSet.containsKey(it.o)) {
					importSet[it.o] = null
					m.add(loadOntImports(s,it.o,importSet))
				}
			}
		m
	}
	
	// findroots semantics
	// finding roots by parent of child
	// 'in', if parent is in set of subjects in the model
	// 'out', if parent is not in set of subjects
	// not {'in'|'out'}, if including both in and out
	def findRoots(m) {
		findRoots(m,"in")
	}
		
	def findOutsideRoots(m) {
		findRoots(m,"out")
	}
		
	def findAllRoots(m) {
		findRoots(m,"")
	}
		
	/**
	 * Find roots in an ontology
Set of all children in ?child owl:subClassOf parent, S1
Set of all parents, ?child owl:subClassOf ?parent, S2
Set of all subjects, S3

Given the set of all parents, 
if a parent is not in S1 children
and that parent is in S3 all subjects
then that parent is a root class in the model

	 * @param m, a model
	 * @return a result set as list
	 */
	def findRoots(m,b) {
		def qm = new JenaUtils().queryListMap1(m,"", """
prefix owl:   <http://www.w3.org/2002/07/owl#> 
prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
prefix xsd:   <http://www.w3.org/2001/XMLSchema#> 
prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> 

select distinct ?s ?o {
		?s rdfs:subClassOf ?o .
		filter(!isblank(?o))
} order by ?s
""")
		def pSet = [:]
		def cSet = [:]
		def allSet = [:]
		qm.each{
			pSet[it.o] = null
			cSet[it.s] = null
		}

		def qm2 = new JenaUtils().queryListMap1(m,"", """
prefix owl:   <http://www.w3.org/2002/07/owl#> 
prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
prefix xsd:   <http://www.w3.org/2001/XMLSchema#> 
prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> 

select distinct ?s  {
		?s a owl:Class .
		filter(!isblank(?s))
} order by ?s
""")
		qm2.each{
			allSet[it.s] = null
		}

		def result = []
		pSet.each{k,v->
			
			switch (b) {
				case "in":
					if (!(k in cSet.keySet()) && (k in allSet.keySet())) result += k
					break;
				case "out":
					if (!(k in cSet.keySet()) && !(k in allSet.keySet())) result += k
					break;
				default:
					if (!(k in cSet.keySet())) result += k
			}
		}
		result
	}

	// given a FQN URI for a given model
	// return the respective qname
	// i.e., with prefix
	def getPrefix(m,s) {
		def ls=s.split(/#/)
		def ns
		def name
		if (ls.size()==2) {
			ns = "${ls[0]}#"
			name = ls[1]
		} else {
			ns = s.substring(0,s.lastIndexOf('/')+1)
			name = s.substring(s.lastIndexOf('/')+1)
		}
		def prefix = m ? m.getNsURIPrefix( ns) + ":" : null
		[prefix,name]
	}
	
	// given a QName for a given model
	// return the respective FQN URI
	def getNSURI(m,s) {
		def ls=s.split(/:/)
		def prefix
		def name
		if (ls.size()==2) {
			prefix = ls[0]
			name = ls[1]
		}
		def ns = m ? m.getNsPrefixURI( prefix)  : null
		[ns,name]
	}
	
	/**
	 * Given a list of roots
	 * of the specified type
	 * return the qname version
	 * of the list
	 * @param m model
	 * @param b roots type specifier (see findroots semantics)
	 * @return
	 */
	def findRootsList(m,b) {
		def r = new JenaUtils().findRoots(m,b)
		def qnameList = []
		r.each{
			def ls = getPrefix(m,it)
			qnameList += "${ls[0]}${ls[1]}"
			}
		qnameList
	}

	def findRootsList(m) {
		findRootsList(m,"in")
	}
	
	def findOutsideRootsList(m) {
		findRootsList(m,"out")
	}
		
	def findAllRootsList(m) {
		findRootsList(m,"")
	}
		
}
