package idl.bdp.scrashlocator.spoon

import java.io.{FileNotFoundException, FileWriter, File}

import net.liftweb.json.{DefaultFormats, JsonAST, Printer, Extraction}
import spoon.Launcher
import spoon.processing.ProcessingManager
import spoon.support.QueueProcessingManager
import spoon.support.compiler.FileSystemFolder

import scala.collection.JavaConverters._


/**
 * Computes for each method of a java project its number of line code
 * @note it uses Spoon library for the analysis
 * @author Romain Philippon
 */
object MethodAnalyzer {

  @Override
  def main (args: Array[String]) {

    if(args.length == 1) {
      try {
        runMethodLengthAnalysis(args(0))
      }
      catch {
        case fnfe: FileNotFoundException => Console.err.println(Console.RED + fnfe.getMessage)
      }
    }
    else {
      Console.err.println(Console.RED +"Parameter expected -- absolute path to java files source folder")
    }
  }

  /**
   * Run a spoon analysis to get for each method its length and exports results in a json file
   * @note The result file is generated in the parent folder with filename : length-method.json
   * @param path Is the path location for the project to analyze
   */
  def runMethodLengthAnalysis(path : String): Unit = {

    lazy val projectFolder = new File(path)

    if(projectFolder exists) {
      /* INIT SPOON CONTEXT */
      lazy val spoonContext = new Launcher
      spoonContext.getFactory.getEnvironment.setNoClasspath(true) // to avoid dependency errors during analysis
      spoonContext addInputResource(new FileSystemFolder(projectFolder))
      spoonContext run

      /* RUN ANALYSIS */
      lazy val manager : ProcessingManager = new QueueProcessingManager(spoonContext getFactory)
      lazy val processor = new MethodLengthProcessor
      manager addProcessor(processor)
      manager process

      /* EXPORT RESULT */
      implicit val formats = DefaultFormats // to export in json

      val jsonResult = Extraction.decompose(processor.getListMethodLength.asScala.toMap.mapValues(_.toInt))
      val jsonStrOut = Printer.pretty(JsonAST.render(jsonResult))

      val fileName = projectFolder.getParentFile.getAbsolutePath + File.separator +"length-method.json"
      val fw = new FileWriter(fileName)
      fw.write(jsonStrOut)
      fw.close()

      println(Console.GREEN +"Computing length is over - File generated at : "+ fileName)
    }
    else {
      throw new FileNotFoundException("Not found project folder at"+ path)
    }
  }
}