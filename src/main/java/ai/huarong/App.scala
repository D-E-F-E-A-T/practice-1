package ai.huarong

import java.sql.DriverManager

object App {

  def main(arr: Array[String]) {
    val init = Array(
      Array("10", "99", "99", "13"),
      Array("00", "99", "99", "03"),
      Array("30", "22", "21", "33"),
      Array("20", "31", "32", "23"),
      Array("40", "-1", "-1", "43"))
    
//    val init = Array(
//      Array("00", "99", "99", "03"),
//      Array("20", "99", "99", "23"),
//      Array("10", "22", "21", "13"),
//      Array("30", "32", "31", "33"),
//      Array("-1", "42", "41", "-1"))

    Calc(init)
//      Dao.conn = DriverManager.getConnection(Dao.url, Dao.username, Dao.password)
//      Calc.result("10:11:12:13:00:01:02:03:20:21:23:22:-1:99:99:33:-1:99:99:43:")
  }
}