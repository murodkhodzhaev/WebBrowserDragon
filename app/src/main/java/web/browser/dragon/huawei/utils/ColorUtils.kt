package web.browser.dragon.huawei.utils

fun isColorTooDark(color: Int): Boolean {
    val RED_CHANNEL: Byte = 16
    val GREEN_CHANNEL: Byte = 8
    //final byte BLUE_CHANNEL = 0;
    val r = ((color shr RED_CHANNEL.toInt() and 0xff).toFloat() * 0.3f).toInt() and 0xff
    val g = ((color shr GREEN_CHANNEL.toInt() and 0xff).toFloat() * 0.59).toInt() and 0xff
    val b = ((color /* >> BLUE_CHANNEL */ and 0xff).toFloat() * 0.11).toInt() and 0xff
    val gr = r + g + b and 0xff
    val gray =
        gr /* << BLUE_CHANNEL */ + (gr shl GREEN_CHANNEL.toInt()) + (gr shl RED_CHANNEL.toInt())
    return gray < 0x727272
}

fun mixTwoColors(color1: Int, color2: Int, amount: Float): Int {
    val ALPHA_CHANNEL: Byte = 24
    val RED_CHANNEL: Byte = 16
    val GREEN_CHANNEL: Byte = 8
    //final byte BLUE_CHANNEL = 0;
    val inverseAmount = 1.0f - amount
    val r =
        ((color1 shr RED_CHANNEL.toInt() and 0xff).toFloat() * amount + (color2 shr RED_CHANNEL.toInt() and 0xff).toFloat() * inverseAmount).toInt() and 0xff
    val g =
        ((color1 shr GREEN_CHANNEL.toInt() and 0xff).toFloat() * amount + (color2 shr GREEN_CHANNEL.toInt() and 0xff).toFloat() * inverseAmount).toInt() and 0xff
    val b =
        ((color1 and 0xff).toFloat() * amount + (color2 and 0xff).toFloat() * inverseAmount).toInt() and 0xff
    return 0xff shl ALPHA_CHANNEL.toInt() or (r shl RED_CHANNEL.toInt()) or (g shl GREEN_CHANNEL.toInt()) or b
}

fun mixColor(fraction: Float, startValue: Int, endValue: Int): Int {
    val startA = startValue shr 24 and 0xff
    val startR = startValue shr 16 and 0xff
    val startG = startValue shr 8 and 0xff
    val startB = startValue and 0xff
    val endA = endValue shr 24 and 0xff
    val endR = endValue shr 16 and 0xff
    val endG = endValue shr 8 and 0xff
    val endB = endValue and 0xff
    return startA + (fraction * (endA - startA)).toInt() shl 24 or (
            startR + (fraction * (endR - startR)).toInt() shl 16) or (
            startG + (fraction * (endG - startG)).toInt() shl 8) or
            startB + (fraction * (endB - startB)).toInt()
}