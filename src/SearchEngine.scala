import java.util.Properties
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
  val results : Future[SearchResult] = executor.submit(new Searcher("Project Manhattan",pipeline))
  while (!results.isDone){
    var counter = 0
    results.get().top100.foreach( e => {
      counter+=1
      println(s"$counter ${e._1}\n${e._2}")
    })
  }
}
