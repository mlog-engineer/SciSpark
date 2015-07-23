package org.dia.core

import org.apache.spark.storage.StorageLevel
import org.dia.Constants._
import org.dia.TRMMUtils.HourlyTrmmUrlGenerator
import org.dia.partitioners.sPartitioner
import sPartitioner._
import org.dia.partitioners.sTrmmPartitioner._
import org.dia.loaders.NetCDFLoader._
import org.scalatest.FunSuite

import scala.io.Source

/**
 * Tests for creating different Rdd types.
 * Created by marroquin on 7/14/15.
 */
class sRDDTest extends FunSuite  {

  test("ArrayLibsSanityTest") {
    val dataUrls = Source.fromFile("TestLinks").mkString.split("\n").toList
    val sc = SparkTestConstants.sc

    // Breeze library
    sc.setLocalProperty(ARRAY_LIB, BREEZE_LIB)
    val sBreezeRdd = new sRDD[sciTensor] (sc, dataUrls, "TotCldLiqH2O_A", loadNetCDFNDVars, mapOneUrlToOneTensor)
    val sBreezeRdd2 = new sRDD[sciTensor] (sc, dataUrls, "TotCldLiqH2O_B", loadNetCDFNDVars, mapOneUrlToOneTensor)
    sBreezeRdd.persist(StorageLevel.MEMORY_AND_DISK_SER)
    var start = System.nanoTime()
    val breezeTensors = sBreezeRdd.collect()
    var end = System.nanoTime()
    var breezeTime = (end - start)/1000000000.0

    // Nd4j library
    sc.setLocalProperty(ARRAY_LIB, ND4J_LIB)
    val sNd4jRdd = new sRDD[sciTensor] (sc, dataUrls, "TotCldLiqH2O_A", loadNetCDFNDVars, mapOneUrlToOneTensor)
    sNd4jRdd.map(e => e.tensor.data)
    sNd4jRdd.persist(StorageLevel.MEMORY_AND_DISK_SER)
    start = System.nanoTime()
    val nd4jTensors = sNd4jRdd.collect()
    end = System.nanoTime()
    var nd4jTime = (end - start)/1000000000.0

    // element comparison
    var flg = true
    var cnt = 0
    nd4jTensors(0).tensor.data.map(e => {
      if(e != breezeTensors(0).tensor.data(cnt))
        flg = false
      cnt+=1
    })

    // printing out messages
    println("BREEZE : %.6f".format(breezeTime))
    println("ND4J : %.6f".format(nd4jTime))
    println("EQUAL ELEMENTS? %b".format(flg))
  }

  test("GroupingByYearPartitioning") {
    val urls = HourlyTrmmUrlGenerator.generateTrmmDaily(1999, 2000).toList
    val sc = SparkTestConstants.sc
    sc.getConf.set("log4j.configuration", "resources/log4j-defaults.properties")

    // Nd4j library
    sc.setLocalProperty(ARRAY_LIB, BREEZE_LIB)
    val sNd4jRdd = new sRDD[sciTensor] (sc, urls, "precipitation", loadNetCDFNDVars, mapOneYearToManyTensorTRMM)
    val nd4jTensor = sNd4jRdd.collect()(0)
    nd4jTensor.tensor.data.map(e => println(e))
    assert(true)
  }

  test("GroupingByDayPartitioning") {
    val urls = HourlyTrmmUrlGenerator.generateTrmmDaily(1999, 2000).toList
    val sc = SparkTestConstants.sc
    sc.getConf.set("log4j.configuration", "resources/log4j-defaults.properties")

    // Nd4j library
    sc.setLocalProperty(ARRAY_LIB, BREEZE_LIB)
    val sNd4jRdd = new sRDD[sciTensor] (sc, urls, "precipitation", loadNetCDFNDVars, mapOneDayToManyTensorTRMM)
    val nd4jTensor = sNd4jRdd.collect()(0)
    println(nd4jTensor.tensor.data)
  }

  test("BreezeRdd.basic") {
//    val sc = SparkTestConstants.sc
//    val datasetUrls = Source.fromFile("TestLinks").mkString.split("\n").toList
//    val datasetMapping = datasetUrls.foreach(element => (0, element)).asInstanceOf[Map[AnyVal, Any]]
//    val srdd = new sciBreezeRDD[DenseMatrix[Double]] (sc, datasetMapping, "TotCldLiqH2O_A")

//    val collected = srdd.collect
//    collected.map(p => println(p))
//    sc.stop()
    assert (true)
  }

  test("Nd4jRdd.basic") {
//    val sc = SparkTestConstants.sc
//    val datasetUrls = Source.fromFile("TestLinks").mkString.split("\n").toList
//    val datasetMapping = datasetUrls.foreach(element => (0, element)).asInstanceOf[Map[AnyVal, Any]]
//    val srdd = new sciNd4jRDD[INDArray](sc, datasetMapping, "TotCldLiqH2O_A")

//    val collected = srdd.collect
//    collected.map(p => println(p))
//    sc.stop()
    assert(true)
  }

}