package ai.huarong

import scala.collection.mutable._

object Calc {

  def apply(arr: Array[Array[String]]) {
    println("原始：" + stringOf(arr))

    Dao.truncate
    Dao.save(stringOf(arr))

    val list = ListBuffer[String]()

    do {
      list.clear
      (list ++= Dao.find).foreach {
        x =>
          val array = toArray(x)

          if (isWin(array)) {
            result(x)
            return
          }

          val empty = findEmpty(array)

          val moveSingle = moveCell(array)(_, _, _, _, _, _, _)
          val moveDouble = moveMerg(array, empty)

          empty.foreach {
            cur =>
              val x = cur._1
              val y = cur._2

              if (x != 0) moveSingle(x, x - 1, x - 2, y, y, y, x > 1)
              if (x != 4) moveSingle(x, x + 1, x + 2, y, y, y, x < 3)
              if (y != 0) moveSingle(x, x, x, y, y - 1, y - 2, y > 1)
              if (y != 3) moveSingle(x, x, x, y, y + 1, y + 2, y < 2)
          }

          println(findEmpty(array))
      }
    } while (list != null && list.size > 0)

    println("无解")
  }

  def moveCell(arr: Array[Array[String]])(x1: Int, x2: Int, x3: Int, y1: Int, y2: Int, y3: Int, op: Boolean) {
    val cell = arr(x2)(y2)
    val branch = arr.map(_.clone)

    val expression = {
      if (cell == "-1")
        false
      else op && ({
        if (x1 == x2 && x2 == x3)
          cell.substring(0, 1).toInt == x1
        else cell.substring(1).toInt == y1
      })
    }
    
    if (x2 + y2.toString == cell) {
      branch(x1)(y1) = x1 + y1.toString
      branch(x2)(y2) = "-1"
      println("单格：" + stringOf(branch))
    } else if (expression) {
      branch(x1)(y1) = x2 + y2.toString
      branch(x2)(y2) = x1 + y1.toString
      branch(x3)(y3) = "-1"
      println("单格：" + stringOf(branch))
    } else {
      return
    }

    memory(branch, arr)
  }

  def moveMerg(arr: Array[Array[String]], empty: ListBuffer[(Int, Int)]) {
    val x1 = empty(0)._1
    val x2 = empty(1)._1
    val y1 = empty(0)._2
    val y2 = empty(1)._2

    def move(colOrRow: Boolean, isEdge: Boolean,
             prevX1: Int, prevY1: Int, prevX2: Int, prevY2: Int,
             mainX1: Int, mainY1: Int, mainX2: Int, mainY2: Int) {
      val branch = arr.map(_.clone)

      if (colOrRow) {
        if (isEdge) {
          if (branch(prevX1)(prevY1) == prevX2 + prevY2.toString
            && branch(prevX2)(prevY2) == prevX1 + prevY1.toString) {
            branch(prevX1)(prevY1) = "-1"
            branch(prevX2)(prevY2) = "-1"

            branch(x1)(y1) = (x2) + (y2).toString
            branch(x2)(y2) = (x1) + (y1).toString
          } else if (branch(prevX1)(prevY1) == "99" && branch(prevX2)(prevY2) == "99") {
            branch(mainX1)(mainY1) = "-1"
            branch(mainX2)(mainY2) = "-1"

            branch(x1)(y1) = "99"
            branch(x2)(y2) = "99"
          } else {
            return
          }

          memory(branch, arr)
          
          println("双格：" + stringOf(branch))
        }
      }
    }

    val isCol = x1 == x2 && y1 + 1 == y2
    val isRow = y1 == y2 && x1 + 1 == x2

    move(isCol, x1 > 0, x1 - 1, y1, x2 - 1, y2, x1 - 2, y1, x2 - 2, y2)
    move(isCol, x1 < 4, x1 + 1, y1, x2 + 1, y2, x1 + 2, y1, x2 + 2, y2)
    move(isRow, y1 > 0, x1, y1 - 1, x2, y2 - 1, x1, y1 - 2, x2, y2 - 2)
    move(isRow, y1 < 3, x1, y1 + 1, x2, y2 + 1, x1, y1 + 2, x2, y2 + 2)
  }

  def result(arr: String) {
    var i = 1
    Dao.find(arr).foreach {
      x =>
        println("结果" + i + ":")
        
        var j = 0
        
        x.split(":").foreach {
          y =>
            y match {
              case _ if ((j / 4) + (j % 4).toString == y) => print("**")
              case _ if (y == "99")                       => print("##")
              case _ if (y == "-1")                       => print("  ")
              case _ if (y.endsWith((j % 4).toString))    => print("||")
              case _ if (y.startsWith((j / 4).toString))  => print("==")
              case _ if (y.endsWith((j / 4).toString))    => print("==")
            }

            if (j % 4 == 3) println
            j = j + 1
        }
        i = i + 1
    }
  }

  def memory(arr: Array[Array[String]], prev: Array[Array[String]]) {
    Dao.save(stringOf(arr), stringOf(prev))
    Dao.update(stringOf(prev), 1)
  }

  def toArray(arr: String) = {
    val result = Array.ofDim[String](5, 4)
    var i = 0
    arr.split(":").foreach {
      x =>
        result(i / 4)(i % 4) = x
        i = i + 1
    }
    result
  }

  /**
   * 查询两个空位
   */
  def findEmpty(arr: Array[Array[String]]) = {
    val empty = ListBuffer[(Int, Int)]()
    for (i <- 0 to arr.length - 1; j <- 0 to arr(i).length - 1)
      if (arr(i)(j) == "-1") empty += ((i, j))
    empty
  }

  /**
   * 判断是否通关
   */
  def isWin(arr: Array[Array[String]]) = {
    if (arr(4)(1) == arr(4)(2) && arr(4)(1) == "99")
      true
    else
      false
  }

  def stringOf(arr: Array[Array[String]]) = {
    val result = new StringBuilder
    arr.foreach(_.foreach(result ++= _ + ":"))
    result.toString
  }
}