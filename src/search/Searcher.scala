package search

import java.net.URL
import java.util.concurrent.Callable

import edu.stanford.nlp.ling.CoreAnnotations.{LemmaAnnotation, SentencesAnnotation, TokensAnnotation}
import edu.stanford.nlp.ling.CoreLabel
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import edu.stanford.nlp.util.CoreMap

import scala.collection.JavaConversions._
import database.DBManager

import scala.math._


/**
  * Created by george on 29.11.16.
  */
class Searcher(query : String, pipeline: StanfordCoreNLP) extends Callable[SearchResult]{
  val numberOfPagesToReturn = 100
  val qDocument = pipeline.process(query)
  var qTerms : List[String] = List.empty[String]

  override def call(): SearchResult = {
    for(sentence : CoreMap <- qDocument.get(classOf[SentencesAnnotation])){
      for (token : CoreLabel <- sentence.get(classOf[TokensAnnotation])){
        qTerms = token.get(classOf[LemmaAnnotation])::qTerms
      }
    }

    val qTF = qTerms.groupBy(identity).mapValues(_.size)
      .map(e => e._1 ->  e._2.toDouble/qTerms.size.toDouble).map{
      case e => e._1 -> (if (e._2 == 0) 0 else 1 + log10(e._2))
    }.toList
    qTF.foreach(e => println(e))
    println("Query processed")
    val documents : List[(Int, Double)] = DBManager.getQueryRelatedDocs(qTF)
    println("Obtained related docs from db")
    val top100 = documents.groupBy(_._1).map{
      case(key, pairs) =>
        val values = pairs.map(_._2)
        key -> values.sum
    }.toList.sortWith(_._2 > _._2).take(numberOfPagesToReturn).map(e => e._1)
      .map(e => DBManager.getDocInfo(e))
    println("Results to output")
    SearchResult(top100)
  }
}
