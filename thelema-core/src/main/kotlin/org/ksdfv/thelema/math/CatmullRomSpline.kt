/*
 * Copyright 2020 Anton Trushkov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ksdfv.thelema.math

import kotlin.math.sqrt

/** @author Xoppa, zeganstyl */
class CatmullRomSpline(var controlPoints: List<IVec>, var continuous: Boolean = false) {
    private val tmp: IVec = controlPoints[0].copy()
    private val tmp2 = controlPoints[0].copy()
    private val tmp3 = controlPoints[0].copy()
    var spanCount = if (continuous) controlPoints.size else controlPoints.size - 3

    fun valueAt(out: IVec, t: Float): IVec {
        val n = spanCount
        var u = t * n
        val i = if (t >= 1f) n - 1 else u.toInt()
        u -= i.toFloat()
        return valueAt(out, i, u)
    }

    /** @return The value of the spline at position u of the specified span */
    fun valueAt(out: IVec, span: Int, u: Float): IVec {
        return calculate(out, if (continuous) span else span + 1, u, controlPoints, continuous, tmp)
    }

    fun derivativeAt(out: IVec, t: Float): IVec {
        val n = spanCount
        var u = t * n
        val i = if (t >= 1f) n - 1 else u.toInt()
        u -= i.toFloat()
        return derivativeAt(out, i, u)
    }

    /** @return The derivative of the spline at position u of the specified span */
    fun derivativeAt(out: IVec, span: Int, u: Float): IVec {
        return derivative(out, if (continuous) span else span + 1, u, controlPoints, continuous, tmp)
    }
    /** @return The span closest to the specified value, restricting to the specified spans. */
    /** @return The span closest to the specified value */
    fun nearest(`in`: IVec?, start: Int = 0, count: Int = spanCount): Int {
        var start = start
        while (start < 0) start += spanCount
        var result = start % spanCount
        var dst = `in`!!.dst2(controlPoints[result])
        for (i in 1 until count) {
            val idx = (start + i) % spanCount
            val d = `in`.dst2(controlPoints[idx])
            if (d < dst) {
                dst = d
                result = idx
            }
        }
        return result
    }

    fun approximate(v: IVec): Float {
        return approximate(v, nearest(v))
    }

    fun approximate(`in`: IVec, start: Int, count: Int): Float {
        return approximate(`in`, nearest(`in`, start, count))
    }

    fun approximate(`in`: IVec?, near: Int): Float {
        var n = near
        val nearest = controlPoints[n]
        val previous = controlPoints[if (n > 0) n - 1 else spanCount - 1]
        val next = controlPoints[(n + 1) % spanCount]
        val dstPrev2 = `in`!!.dst2(previous)
        val dstNext2 = `in`.dst2(next)
        val P1: IVec
        val P2: IVec
        val P3: IVec?
        if (dstNext2 < dstPrev2) {
            P1 = nearest
            P2 = next
            P3 = `in`
        } else {
            P1 = previous
            P2 = nearest
            P3 = `in`
            n = if (n > 0) n - 1 else spanCount - 1
        }
        val L1Sqr = P1.dst2(P2)
        val L2Sqr = P3.dst2(P2)
        val L3Sqr = P3.dst2(P1)
        val L1 = sqrt(L1Sqr.toDouble()).toFloat()
        val s = (L2Sqr + L1Sqr - L3Sqr) / (2f * L1)
        val u = MATH.clamp((L1 - s) / L1, 0f, 1f)
        return (n + u) / spanCount
    }

    fun locate(v: IVec): Float {
        return approximate(v)
    }

    fun approxLength(samples: Int): Float {
        var tempLength = 0f
        for (i in 0 until samples) {
            tmp2.set(tmp3)
            valueAt(tmp3, i / (samples.toFloat() - 1))
            if (i > 0) tempLength += tmp2.dst(tmp3)
        }
        return tempLength
    }

    companion object {
        /** Calculates the catmullrom value for the given position (t).
         * @param out The Vector to set to the result.
         * @param t The position (0<=t<=1) on the spline
         * @param points The control points
         * @param continuous If true the b-spline restarts at 0 when reaching 1
         * @param tmp A temporary vector used for the calculation
         * @return The value of out
         */
        fun calculate(out: IVec, t: Float, points: List<IVec>, continuous: Boolean, tmp: IVec): IVec {
            val n = if (continuous) points.size else points.size - 3
            var u = t * n
            val i = if (t >= 1f) n - 1 else u.toInt()
            u -= i.toFloat()
            return calculate(out, i, u, points, continuous, tmp)
        }

        /** Calculates the catmullrom value for the given span (i) at the given position (u).
         * @param out The Vector to set to the result.
         * @param i The span (0<=i<spanCount></spanCount>) spanCount = continuous ? points.length : points.length - degree
         * @param u The position (0<=u<=1) on the span
         * @param points The control points
         * @param continuous If true the b-spline restarts at 0 when reaching 1
         * @param tmp A temporary vector used for the calculation
         * @return The value of out
         */
        fun calculate(out: IVec, i: Int, u: Float, points: List<IVec>, continuous: Boolean, tmp: IVec): IVec {
            val n = points.size
            val u2 = u * u
            val u3 = u2 * u
            out.set(points[i]).scl(1.5f * u3 - 2.5f * u2 + 1.0f)
            if (continuous || i > 0) out.add(tmp.set(points[(n + i - 1) % n]).scl(-0.5f * u3 + u2 - 0.5f * u))
            if (continuous || i < n - 1) out.add(tmp.set(points[(i + 1) % n]).scl(-1.5f * u3 + 2f * u2 + 0.5f * u))
            if (continuous || i < n - 2) out.add(tmp.set(points[(i + 2) % n]).scl(0.5f * u3 - 0.5f * u2))
            return out
        }

        /** Calculates the derivative of the catmullrom spline for the given position (t).
         * @param out The Vector to set to the result.
         * @param t The position (0<=t<=1) on the spline
         * @param points The control points
         * @param continuous If true the b-spline restarts at 0 when reaching 1
         * @param tmp A temporary vector used for the calculation
         * @return The value of out
         */
        fun derivative(out: IVec, t: Float, points: List<IVec>, continuous: Boolean, tmp: IVec): IVec {
            val n = if (continuous) points.size else points.size - 3
            var u = t * n
            val i = if (t >= 1f) n - 1 else u.toInt()
            u -= i.toFloat()
            return derivative(out, i, u, points, continuous, tmp)
        }

        /** Calculates the derivative of the catmullrom spline for the given span (i) at the given position (u).
         * @param out The Vector to set to the result.
         * @param i The span (0<=i<spanCount></spanCount>) spanCount = continuous ? points.length : points.length - degree
         * @param u The position (0<=u<=1) on the span
         * @param points The control points
         * @param continuous If true the b-spline restarts at 0 when reaching 1
         * @param tmp A temporary vector used for the calculation
         * @return The value of out
         */
        fun derivative(out: IVec, i: Int, u: Float, points: List<IVec>, continuous: Boolean, tmp: IVec): IVec { /*
		 * catmull'(u) = 0.5 *((-p0 + p2) + 2 * (2*p0 - 5*p1 + 4*p2 - p3) * u + 3 * (-p0 + 3*p1 - 3*p2 + p3) * u * u)
		 */
            val n = points.size
            val u2 = u * u
            // final float u3 = u2 * u;
            out.set(points[i]).scl(-u * 5 + u2 * 4.5f)
            if (continuous || i > 0) out.add(tmp.set(points[(n + i - 1) % n]).scl(-0.5f + u * 2 - u2 * 1.5f))
            if (continuous || i < n - 1) out.add(tmp.set(points[(i + 1) % n]).scl(0.5f + u * 4 - u2 * 4.5f))
            if (continuous || i < n - 2) out.add(tmp.set(points[(i + 2) % n]).scl(-u + u2 * 1.5f))
            return out
        }
    }
}
