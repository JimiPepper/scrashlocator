package idl.bdp.scrashlocator


import idl.bdp.scrashlocator.bucketing.json._

import scala.collection.immutable.ListSet
import scala.collection.parallel.mutable
import scala.collection.parallel.mutable.ParHashMap
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global // for concurrency with future - do not remove
import scala.util.control.Breaks._
import scala.math.{log, floor}

import java.util.concurrent.{ CountDownLatch => CDL } // use as barrier

/**
 * Handles ranking score parrallel computation for all methods in a bucket
 *
 * @author PHILIPPON Romain
 * Created on 26/11/2014.
 */
class Ranking(buckets : Map[Int, List[BugReport]], listMethodLength : Map[String, Int] ) {

  private var averageSizeMethod : Double = .0

  /**
   * Computes the ranking score for each method inside a bucket
   * @param bucketId the bucket id to analyze
   */
  def computeRankingScore(bucketId : Int): List[(String, Double)] = {
    val rankingScore = new ParHashMap[String, Double]
    var methodSet = new ListSet[String]()

    /*  COMPUTE AVERAGE SIZE METHOD */
    var totalSize = 0

    listMethodLength. foreach { m =>
      totalSize += m._2
    }

    averageSizeMethod = floor(totalSize.toDouble / listMethodLength.size.toDouble)

    /* EXTRACT ALL JAVA METHOD IN THE BUCKET */
    buckets.get(bucketId) match {
      case Some(listBucket) => listBucket foreach { bugReport =>
        bugReport.traces foreach { stackTraceInfo =>
          stackTraceInfo.elements.foreach { methodCalled =>
            methodSet = methodSet.+(methodCalled.method) // add method in set
          }
        }
      }
      case None => Console.err.println(bucketId +" does not exist")
    }

    val ranking = new mutable.ParHashMap[String, Double]()

    /* COMPUTE SCORE */
    val latch = new CDL(methodSet size) // barrier intialized with the number of method to compute the ranking score

    methodSet foreach  { methodName =>
      Future {
        val futureComputeFF: Future[Double] = Future {
          functionFrequenceScore(bucketId, methodName)
        }
        val futureComputeIBF: Future[Double] = Future {
          functionInverseBucketFrequency(bucketId, methodName)
        }
        val futureComputeIAD: Future[Double] = Future {
          functionInverseAverageDistanceCrashPoint(bucketId, methodName)
        }
        val futureComputeFLOC: Future[Double] = Future {
          functionLineOfCode(methodName)
        }

        for {
          ff <- futureComputeFF
          ibf <- futureComputeIBF
          iad <- futureComputeIAD
          floc <- futureComputeFLOC
        } yield {
          ranking.put(methodName, ff * ibf * iad * floc)
          latch countDown
        }
      }
    }

    latch await // returns the result when all computations are over

    val rankingList = ranking.toList match {
      case list : List[(String, Double)] => list
      case _ => throw new RuntimeException("An error occured when transforming result data type mutable.ParHashMap[String, Int] to collection.List[(String, Int)]")
    }

    rankingList.sortWith(_._2 > _._2).take(5) // sort and return the 10 first biggest scores
  }

  /**
   * Computes the function frequency contained in a bucket
   * @param bucketId the bucket id to analyze
   * @param methodName the method name
   * @return the function frequency score of a method
   */
  private def functionFrequenceScore(bucketId : Int, methodName : String): Double = {
    val bucket = buckets.get(bucketId) match {
      case Some(listBugReport) => listBugReport
      case None => throw new RuntimeException(bucketId +" does not exist")
    }

    val nbStackTrace = bucket size
    var FFscore = .0

    bucket foreach { bugReport =>
      bugReport.traces foreach { stackTraceInfo =>
        stackTraceInfo.elements foreach { trace =>
          if(methodName == trace.method) FFscore += + 1.0 // increments by one if method is detected in the stacktrace
        }
      }
    }

    FFscore / nbStackTrace
  }

  /**
   * Computes the inverse frequency bucket of a method
   * @param bucketId the bucket id to analyze
   * @param methodName the method name
   * @return the inverse bucket frequency of a method
   */
  private def functionInverseBucketFrequency(bucketId : Int, methodName : String) : Double = {
    val nbTotalBucket : Int = buckets size
    var nbTimeMethodCalled : Int = 0

    buckets foreach { case (bucketId, listBugReport) =>
      listBugReport foreach { bugReport =>
        bugReport.traces foreach { stackTrace =>
          breakable { // stop when found one called for methodName
            stackTrace.elements.foreach { methodCalled =>
              methodCalled match {
                case MethodCalled(methodName, _) => nbTimeMethodCalled += 1 ; break // find it, stop searching further
              }
            }
          }
        }
      }
    }

    log((nbTotalBucket.toDouble / nbTimeMethodCalled.toDouble) + 1)
  }

  /**
   * Computes the inverse average distance crash point score
   * @param bucketId the bucket id to analyze
   * @param methodName the method name
   * @return the inverse average distance of a method
   */
  private def functionInverseAverageDistanceCrashPoint(bucketId : Int, methodName : String): Double = {
    val bucket = buckets.get(bucketId) match {
      case Some(listBugReport) => listBugReport
      case None => throw new RuntimeException(bucketId +" does not exist")
    }

    val nbBugReport = bucket size
    var sumCallDepth = 0

    bucket foreach { bugReport =>
      bugReport.traces foreach { stackTrace =>
        breakable { // stop when found the called for methodName
          stackTrace.elements.foreach { methodCalled =>
            methodCalled match {
              case MethodCalled(methodName, details) => sumCallDepth += stackTrace.elements.size - stackTrace.elements.indexOf(MethodCalled(methodName, details)) + 1 ; break // find it, stop searching further
              // add +1 because indexOf returns a value between [0 ; List.size]
            }
          }
        }
      }
    }

    nbBugReport.toDouble / (1 + sumCallDepth.toDouble)
  }

  /**
   * Computes the number of line code score
   * @param methodName the bucket id to analyze
   * @return the line of code of a method
   */
  private def functionLineOfCode(methodName : String) : Double = {
    listMethodLength.get(methodName) match {
      case Some(length) => log(length.toDouble + 1)
      case None => averageSizeMethod
    }
  }
}