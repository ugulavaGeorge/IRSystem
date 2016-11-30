package main.scala.database

import java.sql.{Connection, DriverManager, Statement}

/**
  * Created by dmitriy on 23/11/16.
  */
object DBManager {
  val connectionString = "jdbc:sqlserver://ir-system.database.windows.net:1433;" +
    "database=ir-system;" +
    "user=d.kapitun@ir-system;" +
    "password=Innopolis16;" +
    "encrypt=true;" +
    "trustServerCertificate=false;" +
    "hostNameInCertificate=*.database.windows.net;" +
    "loginTimeout=30;"

  var connection = DriverManager.getConnection(connectionString)

  /**
    * Inserts term into main.scala.database
    * @param term – term to insert
    * @param docID – id of a document the term is into
    * @param tf – term frequency in the document with docID
    * @return true if term was inserted, false otherwise
    */
  def insertTerm(term: String, docID: Int, tf: Double): Boolean = {
    if (connection == null || connection.isClosed) connection = DriverManager.getConnection(connectionString)

    val insertSql = "INSERT INTO Terms " +
      s"VALUES ('${term}', '${docID}', '${tf}');"

    val prepsInsertProduct = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)
    prepsInsertProduct.execute()

    val resultSet = prepsInsertProduct.getGeneratedKeys()

    if (resultSet.next()) true
    else false
  }

  /**
    * Insert document into main.scala.database
    * @param url – url of the document
    * @param header – header info of the document
    * @return docID if the document was inserted, -1 otherwise
    */
  def insertDocument(url: String, header: String): Int = {
    if (connection == null || connection.isClosed) connection = DriverManager.getConnection(connectionString)

    val insertSql = "INSERT INTO Documents (url, header) " +
      s"VALUES ('${url}', '${header}');"

    val prepsInsertProduct = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)
    prepsInsertProduct.execute()

    val resultSet = prepsInsertProduct.getGeneratedKeys()

    if (resultSet.next()) resultSet.getInt(1)
    else -1
  }

  /**
    * Insert document frequency of the term into main.scala.database
    * @param term – term for which frequency is inserted
    * @param idf – inverted document frequency of the term
    * @return true if insert was successful, false otherwise
    */
  def insertIDF(term: String, idf: Double): Boolean = {
    if (connection == null || connection.isClosed) connection = DriverManager.getConnection(connectionString)

    val insertSql = "INSERT INTO DocumentFrequency " +
      s"VALUES ('${term}', '${idf}');"

    val prepsInsertProduct = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)
    prepsInsertProduct.execute()

    val resultSet = prepsInsertProduct.getGeneratedKeys()

    if (resultSet.next()) true
    else false
  }

  def update(): Unit = {

  }

  def delete(): Unit = {

  }

  /**
    * Get url and header of document from main.scala.database
    * @param docID – id of document to get info about
    * @return tuple (url, header) if there is such document, null otherwise
    */
  def getDocInfo(docID: Int): (String, String) = {
    if (connection == null || connection.isClosed) connection = DriverManager.getConnection(connectionString)

    val selectSql = "SELECT url, header " +
      "FROM Documents " +
      s"WHERE docID = ${docID}"
    val statement = connection.createStatement()
    val resultSet = statement.executeQuery(selectSql)

    if (resultSet.next()) (resultSet.getString(1), resultSet.getString(2))
    else null
  }

  def getDocInfo(docs: List[Int]): List[(String, String)] = {
    if (docs.isEmpty) return null
    if (connection == null || connection.isClosed) connection = DriverManager.getConnection(connectionString)

    var selectSql = "SELECT url, header " +
      s"FROM Documents WHERE docID = ${docs.head}"

    docs.tail.foreach(e => selectSql += s" OR docID = ${e}")

    val statement = connection.createStatement()
    val resultSet = statement.executeQuery(selectSql)

    var info = List.empty[(String, String)]

    while (resultSet.next()) {
      info = info ::: List((resultSet.getString(1), resultSet.getString(2)))
    }

    info
  }


//  def F(term: String, qTF : Double): List[(Int, Double)] = {
//    if (connection == null || connection.isClosed) connection = DriverManager.getConnection(connectionString)
//
//    val selectSql = "SELECT docID " +
//      "FROM Terms " +
//      s"WHERE term = '${term}'"
//    val statement = connection.createStatement()
//    val resultSet = statement.executeQuery(selectSql)
//
//    var postings = List.empty[Int]
//
//    while (resultSet.next()) postings = resultSet.getInt(1) :: postings
//    val termIDF = getIDF(term)
//
//    postings.map(e => e -> getTF(term, e))
//  }

  /**
    * Get term frequency for specified term and document
    * @param term – term to get tf for
    * @param docID – document for which frequency of term calculated
    * @return term frequency if there is such pair (term, docID), -1 otherwise
    */
  def getTF(term: String, docID: Int): Double = {
    if (connection == null || connection.isClosed) connection = DriverManager.getConnection(connectionString)
    val selectSql = "SELECT tf " +
      "FROM Terms " +
      s"WHERE term = '${term}' and docID = '${docID}'"
    val statement = connection.createStatement()
    val resultSet = statement.executeQuery(selectSql)

    if (resultSet.next()) resultSet.getDouble(1)
    else -1
  }

  /**
    * Get all postings and term frequencies for specified term
    * @param term – term to get info for
    * @return list of tuples (term, docID, tf)
    */
  def getPostingsWithTFIDF(term: String, qtF : Double): List[(Int, Double)] = {
    if (connection == null || connection.isClosed) connection = DriverManager.getConnection(connectionString)

    val selectSql = "SELECT * " +
      "FROM Terms " +
      s"WHERE term = '${term}'"
    val statement = connection.createStatement()
    val resultSet = statement.executeQuery(selectSql)

    var tuples = List.empty[(String, Int, Double)]

    while (resultSet.next()) {
      tuples = (resultSet.getString(1), resultSet.getInt(2), resultSet.getDouble(3)) :: tuples
    }
    val termIDF = getIDF(term)
    tuples.map(e => e._2 -> e._3*termIDF*qtF)
  }

  /**
    * Get inverted term frequency of specified term
    * @param term – term to get idf for
    * @return idf of term if there is such term or -1 otherwise
    */
  def getIDF(term: String): Double = {
    if (connection == null || connection.isClosed) connection = DriverManager.getConnection(connectionString)

    val selectSql = "SELECT idf " +
      "FROM DocumentFrequency " +
      s"WHERE term = '${term}'"
    val statement = connection.createStatement()
    val resultSet = statement.executeQuery(selectSql)

    if (resultSet.next()) resultSet.getDouble(1)
    else -1
  }

  /**
    *
    * @param terms - List of terms with TF value from query.
    * @return - List of docIDs and value TFIDF computed for each term, doc pair
    * which means that docID in List is not unique.
    */
  def getQueryRelatedDocs(terms : List[(String, Double)]) : List[(Int,Double)] = {

    var relatedDocs :List[(Int,Double)] = List.empty[(Int,Double)]
    terms.foreach( e => {
      val positngs : List[(Int, Double)] = getPostingsWithTFIDF(e._1,e._2)
      relatedDocs = relatedDocs:::positngs
    })
    relatedDocs
  }
}
