package idl.bdp.scrashlocator.processing

import idl.bdp.scrashlocator.bucketing.json.Loader
import idl.bdp.scrashlocator.Ranking


object Main extends App {

  if(args.length == 3) {
    /* LOAD NECESSARY FILES */
    Loader pathJsonReport = args(0)
    Loader bucketFilePath = args(1)
    Loader lengthMethodPath = args(2)

    val listBugReports = Loader allReports match {
      case Left(error) => throw new IllegalArgumentException(error.getMessage)
      case Right(list) => println(Console.BLUE + "End loading eclipse reports"); list
    }
    val listMethodLength = Loader methodLengthFile match {
      case Left(error) => throw new IllegalArgumentException(error.getMessage)
      case Right(list) => println(Console.BLUE + "End loading length method"); list
    }

    /* PROCESS RANKING */
    val ranking = new Ranking(listBugReports, listMethodLength)
    println(ranking computeRankingScore(args(2)))
  }
  else {
   throw new IllegalArgumentException("Expects 3 parameters : [absolute-path-eclipse-reports-folder] [absolute-path-bucket-file] [absolute-path-method-length-file]")
  }
}