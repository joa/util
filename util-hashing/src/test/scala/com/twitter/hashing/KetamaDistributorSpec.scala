package com.twitter.hashing

import org.specs.mock.Mockito
import org.specs.Specification
import scala.collection.mutable
import _root_.java.io.{BufferedReader, InputStreamReader}



object KetamaDistributorSpec extends Specification with Mockito {
  "KetamaDistributor" should {
    "pick the correct node" in {
      // Test from Smile's KetamaNodeLocatorSpec.scala

      // Load known good results (key, hash(?), continuum ceiling(?), IP)
      val stream = getClass.getClassLoader.getResourceAsStream("ketama_results")
      val reader = new BufferedReader(new InputStreamReader(stream))
      val expected = new mutable.ListBuffer[Array[String]]
      var line: String = null
      do {
        line = reader.readLine
        if (line != null) {
          val segments = line.split(" ")
          segments.length mustEqual 4
          expected += segments
        }
      } while (line != null)
      expected.size mustEqual 99

      val nodes = Seq(
        KetamaNode("10.0.1.1", 600, 1),
        KetamaNode("10.0.1.2", 300, 2),
        KetamaNode("10.0.1.3", 200, 3),
        KetamaNode("10.0.1.4", 350, 4),
        KetamaNode("10.0.1.5", 1000, 5),
        KetamaNode("10.0.1.6", 800, 6),
        KetamaNode("10.0.1.7", 950, 7),
        KetamaNode("10.0.1.8", 100, 8)
      )

      // 160 is the hard coded value for libmemcached, which was this input data is from
      val ketamaClient = new KetamaDistributor(nodes, 160)

      // Test that ketamaClient.clientOf(key) == expected IP
      val handleToIp = nodes.map { n => n.handle -> n.identifier }.toMap
      for (testcase <- expected) {
        val handle = ketamaClient.nodeForHash(KeyHasher.KETAMA.hashKey(testcase(0).getBytes))
        val resultIp = handleToIp(handle)
        testcase(3) must be_==(resultIp)
      }
    }
  }
}