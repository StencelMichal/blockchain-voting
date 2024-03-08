package com.stencel.evoting.sealer

import java.security.PublicKey

data class RingSignature(
    val keys:List<PublicKey>,
    val startValue:String,
    val ringValues:List<String>,
){
    val size = keys.size
}
