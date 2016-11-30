import java.util.Properties
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{ExecutorService, Executors, Future}

import edu.stanford.nlp.pipeline.StanfordCoreNLP
import search.{SearchResult, Searcher}

/**
  * Created by george on 30.11.16.
  */
object SearchEngine  extends App{
  val props : Properties = new Properties()
  props.put("annotators","tokenize, ssplit, pos, lemma")
  val pipeline : StanfordCoreNLP = new StanfordCoreNLP(props)

  val executor : ExecutorService = Executors.newCachedThreadPool()

  val queryCount = new AtomicInteger(0)

  while (true) {
    val query = scala.io.StdIn.readLine("Query: ");
    val thread = new Thread {
      override def run(): Unit = {
        val results : Future[SearchResult] = executor.submit(new Searcher("project Manhattan",pipeline))
        while (!results.isDone){
          val num = queryCount.incrementAndGet()
          var counter = 0
          results.get().top100.foreach( e => {
            counter+=1
            println(s"$num - $counter ${e._1}\n${e._2}")
          })
          println(s"$num - Average precision = ${results.get().averagePrecision}%")
        }
      }
    }

    thread.start()
  }
  executor.shutdown()
}
