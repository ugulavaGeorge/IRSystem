package main.scala.crawler
import java.net.URL

import akka.actor.{ActorSystem, PoisonPill, Props }

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by george on 23.11.16.
  */


object StartPoint extends App{
  val system = ActorSystem()
  val supervisor = system.actorOf(Props(new Supervisor(system)))

  supervisor ! Start(new URL("https://en.wikipedia.org/wiki/Manhattan_Project"))
  Await.result(system.whenTerminated, 10 minutes)

  supervisor ! PoisonPill
  system.terminate()
}
