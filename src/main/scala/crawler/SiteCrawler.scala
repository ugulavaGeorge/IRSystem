package main.scala.crawler

import java.net.URL

import akka.actor.{Actor, ActorRef, Props}
import akka.util.Timeout
import akka.pattern.{ask, pipe}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by george on 24.11.16.
  */
class SiteCrawler(supervisor: ActorRef, indexer: ActorRef) extends Actor{
  val process = "Process next url"

  val scraper = context actorOf Props(new Scraper(indexer))
  implicit val timeout = Timeout(2 second)
  val tick = context.system.scheduler.schedule(0 millis,1000 millis,self,process)
  var toProcess = List.empty[URL]
  def receive: Receive = {
    case Scrap(url) =>
      toProcess = url::toProcess
    case `process` =>
      toProcess match {
        case Nil =>
        case url :: list =>
          toProcess = list
          (scraper ? Scrap(url)).mapTo[ScrapFinished].recoverWith{case e => Future{ScrapFailure(url,e)} }
            .pipeTo(supervisor)
      }
  }
}

