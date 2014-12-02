package idl.bdp.scrashlocator.bucketing

import java.io.FileWriter
import idl.bdp.scrashlocator.bucketing.json.BugReport
import net.liftweb.json._

import scala.collection.mutable
import scala.reflect.io.File


/**
 * Buckets all bug reports according to eclipse bucketing technique and exports it to JSON format
 * @note Generates a buckets.json file in parent of the analyzed folder
 * @author Romain Philippon
 */
object Bucketing extends App {
  implicit val formats = DefaultFormats

  val bugReportsFolder = File(args(0))

  /* CLASSIFY ALL BUG REPORTS IN THE CORRESPONDING BUCKET */
  if(bugReportsFolder.exists) {
    var buckets = new mutable.HashMap[String, List[String]]()

    bugReportsFolder.toDirectory.files.toList.map { bugFile =>
      val bugReport = parse(bugFile.lines.mkString).extract[BugReport]

      buckets.contains(bugReport.groupId) match {
        // update bucket
        case false => buckets.put(bugReport.groupId, List[String](bugReport.bugId))
        // append bucket
        case true => buckets.get(bugReport.groupId) match {
          case Some(listBugId: List[String]) => buckets.put(bugReport.groupId, listBugId :+ bugReport.bugId) ;

          case None => Console.err.println(Console.RED +"Can't get the bucket :" + bugReport.groupId)
        }
      }
    }

    /* KEEP ONLY BUCKET THAT CONTAINS MORE THAN ONE BUG REPORT */
    buckets = buckets retain { (key, value) => value.size > 1}

    /* EXPORTS BUCKETING TO JSON FORMAT */
    val jsonResult = Extraction.decompose(buckets.toMap) // to have an immutable map instead of mutable
    val jsonStrOut = Printer.pretty(JsonAST.render(jsonResult))
    val fileName = bugReportsFolder.parent.path + File.separator +"buckets.json"
    val fw = new FileWriter(fileName)
    fw.write(jsonStrOut)
    fw.close()

    println(Console.GREEN +"Bucketing is over - File generated at : "+ fileName)
  }
  else {
    Console.err.println(Console.RED +"Not found the bug reports directory at " + args(0))
  }
}