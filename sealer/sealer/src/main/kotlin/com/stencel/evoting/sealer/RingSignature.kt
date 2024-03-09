package com.stencel.evoting.sealer

import java.math.BigInteger
import java.security.PublicKey

data class RingSignature(
    val keys:List<PublicKey>,
    val startValue:BigInteger,
    val ringValues:List<BigInteger>,
){
    val size = keys.size
}
