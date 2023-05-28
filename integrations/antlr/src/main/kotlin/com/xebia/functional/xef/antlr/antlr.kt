package com.xebia.functional.xef.antlr
import arrow.core.raise.Raise
import arrow.core.raise.ensureNotNull
import okio.Path.Companion.toOkioPath
import org.antlr.v4.runtime.*
import org.antlr.v4.tool.Grammar
import java.io.File
import java.lang.reflect.Method
import java.net.URL
import java.net.URLClassLoader
import javax.tools.JavaCompiler
import javax.tools.ToolProvider

fun Raise<ANTLRError>.G4ParserFromURLs(entryPoint: EntryPoint, vararg urls: String): (String) -> ParserResult {
  val tempDir = kotlin.io.path.createTempDirectory().toFile()
  // Collect all file paths for ANTLR tool
  val filePaths = mutableListOf<String>()
  createGrammarFiles(urls, tempDir, filePaths)
  // Generate lexer and parser code for all grammars
  generateParserAndLexerSources(filePaths, tempDir)
  // Compile all generated sources
  compileJavaSources(tempDir)
  return parserFn(tempDir, entryPoint)
}

private fun Raise<ANTLRError>.parserFn(
  tempDir: File,
  entryPoint: EntryPoint
): (String) -> ParserResult = { text: String ->
  val (lexerClass, parserClass) = loadLexerAndParser(tempDir, entryPoint)
  parseText(text, lexerClass, parserClass, entryPoint, tempDir)
}

private fun parseText(
  text: String,
  lexerClass: Class<*>,
  parserClass: Class<*>,
  entryPoint: EntryPoint,
  tempDir: File
): ParserResult {
  val tokensStream = CharStreams.fromString(text)
  val lexer = lexerClass.constructors.first().newInstance(tokensStream) as Lexer
  val tokens = CommonTokenStream(lexer)
  val parserCTor = parserClass.getConstructor(TokenStream::class.java)
  val parser = parserCTor?.newInstance(tokens) as Parser
  val entryPointMethod: Method? = parserClass.getMethod(entryPoint.method)
  val docs = mutableListOf<SourceDocument>()
  parser.addParseListener(DocumentsTreeListener(tempDir.toOkioPath(), entryPoint, docs))
  val context = entryPointMethod?.invoke(parser) as ParserRuleContext
  return ParserResult(parser, context, docs)
}

private fun Raise<ANTLRError>.loadLexerAndParser(
  tempDir: File,
  entryPoint: EntryPoint
): Pair<Class<*>, Class<*>> {
  val urlClassLoader = URLClassLoader(arrayOf(tempDir.toURI().toURL()))
  val lexerClass = urlClassLoader.loadClass(entryPoint.lexer)
  val parserClass = urlClassLoader.loadClass(entryPoint.parser)
  ensureNotNull(lexerClass) { ANTLRError.LexerError("Could not find lexer class") }
  ensureNotNull(parserClass) { ANTLRError.ParserError("Could not find parser class") }
  return Pair(lexerClass, parserClass)
}

private fun compileJavaSources(tempDir: File) {
  // Collect all .java files
  val javaFiles = tempDir.walk().filter { it.extension == "java" }.toList()

// Prepare an array of file paths
  val javaFilePaths = javaFiles.map { it.absolutePath }.toTypedArray()

// Compile all .java files
  val compiler: JavaCompiler = ToolProvider.getSystemJavaCompiler()
  compiler.run(null, System.out, System.err, *javaFilePaths)
}

private fun generateParserAndLexerSources(filePaths: MutableList<String>, tempDir: File) {
  filePaths.forEach { filePath ->
    val grammar: Grammar = Grammar.load(filePath)
    grammar.tool.outputDirectory = tempDir.path
    grammar.tool.gen_listener = true
    grammar.tool.process(grammar, true)
  }
}

private fun createGrammarFiles(
  urls: Array<out String>,
  tempDir: File,
  filePaths: MutableList<String>
) {
  urls.forEach { url ->
    val grammarName = url.substringAfterLast("/").substringBeforeLast(".")
    val grammarFile = File(tempDir, "$grammarName.g4")
    URL(url).openStream().bufferedReader().use { reader ->
      val grammarPart = reader.readText()
      grammarFile.writeText(grammarPart)
    }
    filePaths.add(grammarFile.path)
  }
}


