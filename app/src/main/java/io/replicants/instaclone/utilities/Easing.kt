package io.replicants.instaclone.utilities

object Easing {

    // time, beginning, change, duration
    fun easeInQuart(t: Float, b: Float, c: Float, d: Float): Float {
        val t2 = t/d
        return c * t2* t2 * t2 * t2 + b
    }

    fun easeOutQuart(t: Float, b: Float, c: Float, d: Float): Float {
        val t2 = t/d-1
        return -c * (t2 * t2 * t2 * t2 - 1) + b
    }

    fun easeInOutQuart(t: Float, b: Float, c: Float, d: Float): Float {
        val t2 = t/d/2
        val t3 = t2 -2f
        if (t2 < 1) return c / 2 * t2 * t2 * t2 * t2 + b
        return -c / 2 * (t3 * t3 * t3 * t3 - 2) + b
    }

    // generated from http://www.timotheegroleau.com/Flash/experiments/easing_function_generator.htm
    fun easeOutBack(t: Float, b: Float, c: Float, d: Float):Float{
        val td = t/d
        var ts=(td)*td;
        var tc=ts * t;
        return b+c*(-2*ts*ts + 2*tc + -3*ts + 8*t);
    }
}