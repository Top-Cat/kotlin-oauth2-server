package nl.myndocs.oauth2.device.code

import kotlin.math.pow
import kotlin.random.Random

class NumericCodeGenerator(private val length: Int) : UserCodeGenerator {
    override fun generate() = Random.nextInt(10.0.pow(length.toDouble()).toInt() - 1)
        .toString()
        .padStart(length, '0')
}
