package ai.huarong

import java.sql._
import scala.collection.mutable._

object Dao {
  
  classOf[org.mariadb.jdbc.Driver]

  val url = "jdbc:mariadb://localhost:3306/ai"
  val username = "root"
  val password = "123456"
  
  var conn: Connection = null

  def save(arr: String, prev: String = null) = {
    select("select count(*) from huarong t where t.arr = ?")(_.setString(1, arr)) {
      rs =>
        if (rs.getInt(1) == 0)
          exec("insert into huarong(arr, prev, status) values(?, ?, ?)") {
            ps =>
              ps.setString(1, arr)
              ps.setString(2, prev)
              ps.setInt(3, 0)
          }
    }
    Dao
  }

  def update(arr: String, status: Int) {
    exec("update huarong set status = ? where arr = ?") {
      ps =>
        ps.setInt(1, status)
        ps.setString(2, arr)
    }
  }

  def find = {
    val result = ListBuffer[String]()
    select("select t.arr, t.prev, t.status from huarong t where t.status = ?")(_.setInt(1, 0)) {
      rs => result += rs.getString("arr")
    }
    result
  }

  def find(arr: String, list: ListBuffer[String] = ListBuffer[String]()): ListBuffer[String] = {
    var result: (String, String) = null
    select("select t.arr, t.prev, t.status from huarong t where t.arr = ?")(_.setString(1, arr)) {
      rs => result = (rs.getString("arr"), rs.getString("prev"))
    }

    if (result._2 == null) {
      conn.close
      list.+=:(result._1)
    } else {
      find(result._2, list.+=:(result._1))
    }
  }

  def truncate = {
    conn = DriverManager.getConnection(url, username, password)
    exec("truncate table huarong")(x => Unit)
    Dao
  }
  
  def exec(sql: String)(ps: PreparedStatement => Unit) = {
    var pstmt: PreparedStatement = null
    try {
      pstmt = conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY)
      ps(pstmt)

      pstmt.executeUpdate
    } finally {
      if (pstmt != null) pstmt.close
    }
  }
  
  def select(sql: String)(ps: PreparedStatement => Unit)(rs: ResultSet => Unit) {
    var pstmt: PreparedStatement = null
    var rset: ResultSet = null
    try {
      pstmt = conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
      ps(pstmt)
      rset = pstmt.executeQuery

      while (rset.next) rs(rset)
    } finally {
      if (rset != null) rset.close
      if (pstmt != null) pstmt.close
    }
  }
}