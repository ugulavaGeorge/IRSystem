package search

import java.net.URL

/**
  * Created by george on 29.11.16.
  * returns a List of 100 highest scored documents where tuple is link -> page header
  */
case class SearchResult(top100 : List[(String, String)], averagePrecision : Double)
