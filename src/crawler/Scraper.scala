package crawler

import java.net.URL

import akka.actor.{Actor, ActorRef}
import org.apache.commons.validator.routines.UrlValidator
import org.jsoup.Jsoup
import scala.collection.JavaConverters._

/**
  * Created by george on 24.11.16.
  */
class Scraper (indexer: ActorRef) extends  Actor{
  val urlValidator = new UrlValidator()
  val specAdder : String = "https://en.wikipedia.org";
  def receive : Receive = {
    case Scrap(url) =>
      val content = parse(url)
      sender() ! ScrapFinished(url)
      indexer ! Index(url, content)
  }

  def parse(url: URL): Content ={
    val link : String = url.toString
    val responce = Jsoup.connect(link).ignoreContentType(true)
      .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.1")
      .referrer("http://www.google.com").execute()

    val contentType : String = responce.contentType
    if(contentType.startsWith("text/html")){
      val doc = responce.parse()
      val title: String  = doc.getElementsByTag("title").asScala.map(e => e.text()).head
      val descriptionTag = doc.getElementsByTag("meta").asScala.filter(e => e.attr("name") == "description")
      val description = if(descriptionTag.isEmpty)"" else descriptionTag.map(e => e.attr("content")).head
      val links : List[URL] = doc.getElementsByTag("a").asScala.map(e => e.attr("href")).
        map(e => processWikiNestedUrl(e)).filter(s=> s.contains(specAdder)).filter(s => isValid(s))
          .filter(s => urlValidator.isValid(s)).map(link => new URL(link)).toList
      val text = doc.text()
      Content(title,description,links,text)
    }else{
      //e.g this is image or something
      Content(link,contentType,List(), null)
    }
  }

  def processWikiNestedUrl(url : String) : String = {
    if(url.contains("/wiki"))
       specAdder.concat(url)
    else
      url
  }

  def isValid(url : String) : Boolean = {
    val pattern = "/[A-Z]\\S+:".r
    pattern.findFirstIn(url).isEmpty
  }
}
