package crawler

import akka.actor.{Actor, ActorRef, ActorSystem, Props, _}
import java.net.URL

import scala.language.postfixOps
/**
  * Created by george on 24.11.16.
  */
class Supervisor(system: ActorSystem) extends Actor{
  val indexer = context actorOf Props(new Indexer(self))
  val maxPages = 100000
  val maxRetries = 2
  var scrapedPageCounter = 0
  var numberVisitedPages = 0
  var toScrap = Set.empty[URL]
  var scrapCounts = Map.empty[URL, Int]
  var hostToActor = Map.empty[String, ActorRef]

  def receive: Receive = {
    case Start(url) =>
      scrap(url)
    case ScrapFinished(url) =>
      scrapedPageCounter += 1
      println(s"$scrapedPageCounter scrapping finished $url")
    case IndexFinished(url, urls) =>
      if(numberVisitedPages < maxPages)
        urls.toSet.filter(l => !scrapCounts.contains(l)).foreach(scrap)
      checkAndShutDown(url)
    case ScrapFailure(url,reason) =>
      val retries : Int = scrapCounts(url)
      println(s"scraping failed $url, $retries, reason = $reason")
      if (retries < maxRetries){
        countVisits(url)
        hostToActor(url.getHost) ! Scrap(url)
      }else{
        checkAndShutDown(url)
      }
  }

  def scrap(url : URL) = {
      val host = url.getHost
      if (!host.isEmpty) {
        val actor = hostToActor.getOrElse(host, {
          val buff = system.actorOf(Props(new SiteCrawler(self, indexer)))
          hostToActor += (host -> buff)
          buff
        })
        numberVisitedPages += 1
        toScrap += url
        countVisits(url)
        actor ! (Scrap(url))
      }
    }

  def checkAndShutDown(url : URL): Unit ={
    toScrap -= url
    if(toScrap.isEmpty){
      self ! PoisonPill
      system.terminate()
    }
  }

  def countVisits(url : URL) : Unit = scrapCounts += (url -> (scrapCounts.getOrElse(url,0) + 1))
}