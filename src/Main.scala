/**
  * Created by george on 28.11.16.
  */
import java.net.URL

import akka.actor.{Actor, ActorRef}
import database.DBManager
import edu.stanford.nlp.ling.CoreAnnotations.{LemmaAnnotation, SentencesAnnotation, TokensAnnotation}
import edu.stanford.nlp.ling.CoreLabel
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import edu.stanford.nlp.util.CoreMap
import java.util.Properties

import scala.math.log10

import crawler.Content
import org.jsoup.Jsoup

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
/**
  * Created by george on 28.11.16.
  */
object Main extends App{
  val properties: Properties = new Properties()
  properties.put("annotators","tokenize, ssplit, pos, lemma")
  val pipeline : StanfordCoreNLP = new StanfordCoreNLP(properties)

  var docID = 10925

  while (docID < 14232) {
    val url = DBManager.getDocInfo(docID)._1

    val responce = Jsoup.connect(url).ignoreContentType(true)
      .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.1")
      .referrer("http://www.google.com").execute()

    val contentType : String = responce.contentType
    if(contentType.startsWith("text/html")){
      val doc = responce.parse()
      val text = doc.text()
      val document = pipeline.process(text)
      var terms = List.empty[String]

      for(sentence : CoreMap <- document.get(classOf[SentencesAnnotation])){
        for (token : CoreLabel <- sentence.get(classOf[TokensAnnotation])){
          val term = token.get(classOf[LemmaAnnotation]).toLowerCase
          if (term.matches("[A-Za-z-]+")) terms = term::terms
        }
      }

      terms.groupBy(identity).mapValues(_.size)
        .map(e => e._1 ->  e._2.toDouble/terms.size.toDouble)
        .foreach(e => DBManager.insertTerm(e._1, docID, if (e._2 == 0) 0 else 1 + log10(e._2)))

      println(s"${docID} indexing finished")
    }

    docID += 1
  }
}
