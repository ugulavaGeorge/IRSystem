package main.scala.crawler


import akka.actor.{Actor, ActorRef}
import main.scala.database.DBManager
import edu.stanford.nlp.ling.CoreAnnotations.{LemmaAnnotation, SentencesAnnotation, TokensAnnotation}
import edu.stanford.nlp.ling.CoreLabel
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import edu.stanford.nlp.util.CoreMap
import java.util.Properties

import scala.collection.JavaConversions._
/**
  * Created by george on 24.11.16.
  */
class Indexer(supervisor: ActorRef) extends Actor {
  var pagesCount = 0
//  val properties: Properties = new Properties()
//  val pipeline : StanfordCoreNLP = new StanfordCoreNLP(properties)
//  properties.put("annotators","tokenize, ssplit, pos, lemma")

  def receive:Receive = {
    case Index(url, content) =>
      pagesCount += 1
//      var terms = List.empty[String]
      if (pagesCount > 11791) {
        val docID = DBManager.insertDocument(url.toString, content.title)
      }
//      val document = pipeline.process(content.text)
//      for(sentence : CoreMap <- document.get(classOf[SentencesAnnotation])){
//        for (token : CoreLabel <- sentence.get(classOf[TokensAnnotation])){
//           terms = token.get(classOf[LemmaAnnotation])::terms
//        }
//      }
//      terms.groupBy(identity).mapValues(_.size)
//        .map(e => e._1 ->  e._2.toDouble/terms.size.toDouble)
//        .foreach(e => DBManager.insertTerm(e._1,docID,e._2))

      supervisor ! IndexFinished(url,content.urls)
      //with this message create idf
  }



  @throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    super.postStop()
    println(pagesCount)
  }
}
