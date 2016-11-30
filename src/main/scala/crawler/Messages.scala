package main.scala.crawler

import java.net.URL

/**
  * Created by george on 24.11.16.
  */
case class Start(url : URL)
case class Scrap(url : URL)
case class Index(url : URL, content : Content )
//add text to index
case class Content(title:String, meta :String, urls : List[URL], text : String)
case class ScrapFinished(url: URL)
case class IndexFinished(url: URL, urls : List[URL])
case class ScrapFailure(url: URL, reason : Throwable)