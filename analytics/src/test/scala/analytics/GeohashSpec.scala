package analytics

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class GeohashSpec extends AnyFlatSpec with Matchers {

  "encode" should "match the known geohash for a reference coordinate" in {
    Geohash.encode(57.64911, 10.40744, 5) shouldEqual "u4pru"
  }

  it should "produce a string whose length matches the requested precision" in {
    Geohash.encode(43.5, 5.2, 8).length shouldEqual 8
  }

  it should "be a stable prefix as precision increases" in {
    val short = Geohash.encode(48.8566, 2.3522, 5)
    val long = Geohash.encode(48.8566, 2.3522, 8)

    long.take(5) shouldEqual short
  }

  it should "produce different geohashes for distant coordinates" in {
    Geohash.encode(48.8566, 2.3522, 5) should not equal Geohash.encode(-33.8688, 151.2093, 5)
  }
}
