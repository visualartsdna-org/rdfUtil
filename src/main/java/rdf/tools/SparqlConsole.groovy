package rdf.tools

import static org.junit.jupiter.api.Assertions.*

import org.apache.jena.rdf.model.Model
import org.junit.jupiter.api.Test
import groovy.swing.SwingBuilder
import rdf.JenaUtils

import javax.swing.*
import javax.swing.event.MenuListener
import javax.swing.text.DefaultEditorKit
import javax.swing.undo.*
import java.awt.*
import java.awt.event.ActionListener
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import org.apache.jena.query.*
import org.apache.jena.tdb2.TDB2Factory
import org.apache.jena.query.ReadWrite

/**
 * Creates a SPARQL query console window
 * from a file, a model or model variable
 * in a debugging context (see show() methods)
 * @author ricks
 */
class SparqlConsole {

	public SparqlConsole() {
		myframe.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosing(WindowEvent e) {
						def t = """${tf1.getText().trim()}
"""
						setQueries(t)
						if (dataset) dataset.close()
					}
				})
	}
		
	def ju = new JenaUtils()
	def model
	Dataset dataset
	// Create a builder
	def myapp = new SwingBuilder()
	def prefixes = ""
	def prefixFile = "sparqlConsole.prfx"
	def queryFile = "sparqlConsole.qrys"

	def getPrefixes() {
		def fs = "${System.getProperty("user.home")}/$prefixFile"
		if (new File(fs).exists())
			return new File(fs).text
		return ""
	}
	def getQueries() {
		def fs = "${System.getProperty("user.home")}/$queryFile"
		if (new File(fs).exists())
			return new File(fs).text
		return ""
	}
	def setQueries(txt) {
		def fs = "${System.getProperty("user.home")}/$queryFile"
		new File(fs).text = txt
	}

	def cntLines(t) {
		int i=0
		t.eachLine{
			i++
		}
		i
	}
	
	def isUpdate(qs) {
		qs =~ /(?i)delete[ \t\n]*\{|delete[ \t\n]+data[ \t\n]*\{|insert[ \t\n]*\{|insert[ \t\n]+data[ \t\n]*\{/
	}
	
	def numLines(s) {
		def rs = ""
		int i=1
		s.eachLine{
			rs += "${i++}\t$it\n"
		}
		rs
	}

	def Run = {
		def mdl = ju.newModel()
		def t =tf1.getSelectedText()?:""
		if (t.trim()=="") {
			rowsLabel.setText("No query selected!")
			return
		}
		def l = ""
		Query query
		if (isUpdate(t)) {
			if (dataset) dataset.begin(ReadWrite.WRITE)
			try {
				ju.queryExecUpdate(model,prefixes,t)
				if (dataset) dataset.commit()
			} catch (Exception ex) {
				if (dataset) dataset.abort()
				l="""${numLines(prefixes + t)}
---
$ex"""
			}
		}
		else {
			try {
				if (dataset) dataset.begin(ReadWrite.READ)
				query = QueryFactory.create(prefixes + t) ;
				switch (query.queryType) {
					case Query.QueryTypeSelect:
						l=ju.query(model,prefixes,t)
						break;

					case Query.QueryTypeDescribe:
						mdl=ju.queryDescribe(model,prefixes,t)
						l = ju.saveModelString(mdl,"ttl")
						break;

					case Query.QueryTypeConstruct:
						mdl=ju.queryExecConstruct(model,prefixes,t)
						l = ju.saveModelString(mdl,"ttl")
						break;
				}
				if (dataset) dataset.end()
			} catch (Exception ex) {
				if (dataset) dataset.end()
				l="""${numLines(prefixes + t)}
---
$ex"""
			}
		}
		tf2.setText(l)
		tf2.setCaretPosition(0)
		if (query && query.queryType==Query.QueryTypeSelect)
			rowsLabel.setText("${cntLines(l)-4} rows")
		else if (query && (
			query.queryType==Query.QueryTypeDescribe 
			|| query.queryType==Query.QueryTypeConstruct))
			rowsLabel.setText("${mdl.size()} triples")
		else rowsLabel.setText("")
	}

	def SaveModel = {
		def dialog = myapp.fileChooser(
				dialogTitle: "Save to a File",
				dialogType: JFileChooser.SAVE_DIALOG,
				currentDirectory: new File("/temp"),
				fileSelectionMode: JFileChooser.FILES_ONLY,
				fileFilter: [getDescription: {-> "*.ttl"}, accept:{file-> file ==~ /.*?\.ttl/ || file.isDirectory() }] as javax.swing.filechooser.FileFilter
				)
		if (dialog.showSaveDialog() == JFileChooser.APPROVE_OPTION) {
			println "writing model to "+ dialog.selectedFile
			ju.saveModelFile(model, ""+dialog.selectedFile,"ttl")
		}
	}

	def SaveResults = {
		def dialog = myapp.fileChooser(
				dialogTitle: "Save to a File",
				dialogType: JFileChooser.SAVE_DIALOG,
				currentDirectory: new File("/temp"),
				fileSelectionMode: JFileChooser.FILES_ONLY,
				fileFilter: [getDescription: {-> "*.ttl"}, accept:{file-> file ==~ /.*?\.ttl/ || file.isDirectory() }] as javax.swing.filechooser.FileFilter
				)
		if (dialog.showSaveDialog() == JFileChooser.APPROVE_OPTION) {
			println "writing model to "+ dialog.selectedFile
			dialog.selectedFile.text = tf2.getText()
			
		}
	}

	// SHACL validation in jena lib is difficult to manage w/in one process
	// consider exec process on shacl utility instead
	// take care results model files may be extremely large
	def ShaclModel = {
		def dialog = myapp.fileChooser(
				dialogTitle: "SHACL file",
				dialogType: JFileChooser.OPEN_DIALOG,
				currentDirectory: new File("/temp"),
				fileSelectionMode: JFileChooser.FILES_ONLY,
				fileFilter: [getDescription: {-> "*.ttl"}, accept:{file-> file ==~ /.*?\.ttl/ || file.isDirectory() }] as javax.swing.filechooser.FileFilter
				)
		if (dialog.showSaveDialog() == JFileChooser.APPROVE_OPTION) {
			println "SHACL validating model with "+ dialog.getSelectedFiles()
			def model1 = ju.newModel()
			dialog.getSelectedFiles().each{
				model1.add(ju.loadFileModelFilespec(""+it))
			}
			def dm = ju.newModel()
			try {
//				dm = shaclValidation(this.model,model1).getModel()
			} catch (Exception ex) {
				System.err.println "$ex"
			}
			def s = ju.saveModelString(dm,"ttl")
			tf2.setText(s)
			rowsLabel.setText("${dm.size()} triples")
		}
	}

	def DiffModel = {
		def dialog = myapp.fileChooser(
				dialogTitle: "Files for diff",
				dialogType: JFileChooser.OPEN_DIALOG,
				currentDirectory: new File("/temp"),
				fileSelectionMode: JFileChooser.FILES_ONLY,
				multiSelectionEnabled: true,
				fileFilter: [getDescription: {-> "*.ttl"}, accept:{file-> file ==~ /.*?\.ttl/ || file.isDirectory() }] as javax.swing.filechooser.FileFilter
				)
		if (dialog.showSaveDialog() == JFileChooser.APPROVE_OPTION) {
			println "diffing model with "+ dialog.getSelectedFiles()
			def model1 = ju.newModel()
			dialog.getSelectedFiles().each{
				model1.add(ju.loadFileModelFilespec(""+it))
			}
			def dm = this.model.difference(model1)
			def s = ju.saveModelString(dm,"ttl")
			tf2.setText(s)
			tf2.setCaretPosition(0)
			rowsLabel.setText("${dm.size()} triples")
		}
	}

	def Help = {
		rowsLabel.setText("")
		tf2.setText("""
SparqlConsole Help

Invoke a SparqlConsole on a loaded Jena Model.  To query the Jena 
Model write a SPARQL query in the top text box, highlight the
query, click Run and see results in the bottom text box.  Supported queries 
include: Select, Describe, Construct and Update (delete and insert).

If an update is run on the Jena Model, the model can be saved to a TTL
file by hitting "Model Save."  If the model is a dataset from TDB, changes
are committed to TDB when the query is finished.  A named graph specification 
for updates is generally required for updates to TDB.

Results can be saved to a file with "Result Save." This is useful for describing 
data in the Jena Model, then saving the results to a TTL file.

The triple difference between the loaded model and the union of a model from 
one or more files is available with "Diff Model."  The results are the triples 
in the underlying model not found in the model derived from the selected files.

Queries saved between sessions are stored in {user.home}/sparqlConsole.qrys.
Prefixes used by default with all queries are stored in {user.home}/sparqlConsole.prfx,
this allows queries to be written without prefixes defined per query as long as any
prefixes used are in the default set.  Additional prefixes per query are allowed.

To begin, load a model in a Jena Model variable in java/groovy code and call a 
SparqlConsole show() method with the model as argument.  The triple count of the loaded 
model is displayed in the window title.  The SparqlConsole can be started against a 
TDB store using the showTdb() method and a descriptor as argument.

SparqlConsole can also be invoked on a Debug variable in an Eclipse Debug session:
upon hitting a breakpoint add the SparqlConsole fully qualified name with model 
variable reference to the Debug perspective Expressions.

See rdf.tools.SparqlConsoleTest for invocation options.
""")
		tf2.setCaretPosition(0)
	}
	
	def DisplayB = {
		def t =tf1.getSelectedText()?:""
		println "$t"
	}
	Font font = new Font("Courier new", Font.PLAIN, 14)
	def cb, rowsLabel
	def buttonPanel = {
		myapp.panel(constraints : BorderLayout.NORTH) {
			button(text : 'Run', actionPerformed : Run)
			rowsLabel = label(text : '', horizontalAlignment : JLabel.RIGHT)
		}
	}
	def tf1,tf2
	def mainPanel = {
		myapp.panel(layout : new BorderLayout()) {
				buttonPanel()
			panel(layout: new GridLayout(1, 1, 5, 5)) {

				splitPane(orientation: JSplitPane.VERTICAL_SPLIT, dividerLocation: 150){

					scrollPane(constraints: BorderLayout.CENTER){
						tf1=textArea(text : '', columns : 80)
						def txt = getQueries()
						prefixes = getPrefixes()
						tf1.setText(txt)
						tf1.setEditable(true)
						UndoTool.addUndo(tf1)
					}

					scrollPane(constraints: BorderLayout.CENTER){
						tf2=textArea(text : '', columns : 80)
						tf2.setEditable(false)
					}
				}
			}
			tf1.setFont(font)
			tf2.setFont(font)
		}
	}

	def myframe = myapp.frame(title : 'Tutorials Point', location : [100, 100],
	size : [800, 600], defaultCloseOperation : WindowConstants.EXIT_ON_CLOSE) {
		menuBar {
			def hmi = menu(id: 'modelSave', text:'Model Save')
			hmi.addMenuListener([ 
				menuCanceled: { e -> },
				menuDeselected: { e -> },
				menuSelected: { e -> SaveModel() } ] as MenuListener)
			
			def hmi2 = menu(id: 'resultSave', text:'Result Save')
			hmi2.addMenuListener([ 
				menuCanceled: { e -> },
				menuDeselected: { e -> },
				menuSelected: { e -> SaveResults() } ] as MenuListener)

			def hmi4 = menu(id: 'diffModel', text:'Diff Model')
			hmi4.addMenuListener([ 
				menuCanceled: { e -> },
				menuDeselected: { e -> },
				menuSelected: { e -> DiffModel() } ] as MenuListener)

//			def hmi5 = menu(id: 'shaclModel', text:'SHACL Model')
//			hmi5.addMenuListener([ 
//				menuCanceled: { e -> },
//				menuDeselected: { e -> },
//				menuSelected: { e -> ShaclModel() } ] as MenuListener)

			def hmi3=menu(id: 'help', text:'Help') 
			hmi3.addMenuListener([ 
				menuCanceled: { e -> },
				menuDeselected: { e -> },
				menuSelected: { e -> Help() } ] as MenuListener)

		}
				
		mainPanel()
	}


	// to show a query window
	// given a model filename
	// in TTL
	public void show(String mf) {
		def m2 = ju.loadFileModelFilespec(mf)
		show(m2)
	}

	// to show a query window
	// given a model filename
	// in TTL
	public void show(java.util.List lafs) {
		def m2 = ju.loadListFilespec(lafs)
		show(m2)
	}

	// to open a query window w/in code
	// on a model in context
	public void show(Model m) {
		model = m
		if (dataset) dataset.begin(ReadWrite.READ)
		myframe.setTitle "${m.size()} triples"
		if (dataset) dataset.end()
		myframe.setVisible(true)
		while (true) sleep 1*100
	}

	// for debugger evaluation of a
	// model variable in context
	public void show(Reference m) {
		def m2 = m as Model
		show(m2)
	}

	public void showTdb(assemblerDesc){

		dataset = TDB2Factory.assembleDataset(assemblerDesc)
		//show(dataset.getNamedModel(graph))
		show(dataset.getUnionModel())
		
	}

}
