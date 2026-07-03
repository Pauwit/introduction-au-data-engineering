package analytics

object Geohash {

  private val base32 = "0123456789bcdefghjkmnpqrstuvwxyz"

  private final case class Range(low: Double, high: Double) {
    def mid: Double = (low + high) / 2
  }

  private final case class State(latRange: Range, lonRange: Range, isEven: Boolean, bit: Int, charValue: Int, chars: List[Char])

  private def step(latitude: Double, longitude: Double, state: State): State = {
    val (latRange, lonRange, bitValue) =
      if (state.isEven) {
        val mid = state.lonRange.mid
        if (longitude >= mid) (state.latRange, Range(mid, state.lonRange.high), 1)
        else (state.latRange, Range(state.lonRange.low, mid), 0)
      } else {
        val mid = state.latRange.mid
        if (latitude >= mid) (Range(mid, state.latRange.high), state.lonRange, 1)
        else (Range(state.latRange.low, mid), state.lonRange, 0)
      }

    val charValue = (state.charValue << 1) | bitValue
    val bit = state.bit + 1

    if (bit == 5)
      State(latRange, lonRange, !state.isEven, 0, 0, base32.charAt(charValue) :: state.chars)
    else
      State(latRange, lonRange, !state.isEven, bit, charValue, state.chars)
  }

  private def loop(latitude: Double, longitude: Double, state: State, remaining: Int): State =
    if (remaining == 0) state else loop(latitude, longitude, step(latitude, longitude, state), remaining - 1)

  def encode(latitude: Double, longitude: Double, precision: Int): String = {
    val initial = State(Range(-90.0, 90.0), Range(-180.0, 180.0), isEven = true, bit = 0, charValue = 0, chars = Nil)
    loop(latitude, longitude, initial, precision * 5).chars.reverse.mkString
  }
}
