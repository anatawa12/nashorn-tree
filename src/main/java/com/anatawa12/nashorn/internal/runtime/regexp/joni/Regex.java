/*
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.anatawa12.nashorn.internal.runtime.regexp.joni;

import com.anatawa12.nashorn.internal.runtime.regexp.joni.constants.AnchorType;
import com.anatawa12.nashorn.internal.runtime.regexp.joni.constants.RegexState;
import com.anatawa12.nashorn.internal.runtime.regexp.joni.exception.ErrorMessages;
import com.anatawa12.nashorn.internal.runtime.regexp.joni.exception.ValueException;

@SuppressWarnings("javadoc")
public final class Regex implements RegexState {

    int captureHistory;     /* (?@...) flag (1-31) */
    int btMemStart;         /* need backtrack flag */
    int btMemEnd;           /* need backtrack flag */

    WarnCallback warnings;

    int options;
    final int caseFoldFlag;

    /* optimization info (string search, char-map and anchors) */
    SearchAlgorithm searchAlgorithm;        /* optimize flag */
    int thresholdLength;                    /* search str-length for apply optimize */
    int anchor;                             /* BEGIN_BUF, BEGIN_POS, (SEMI_)END_BUF */
    int anchorDmin;                         /* (SEMI_)END_BUF anchor distance */
    int anchorDmax;                         /* (SEMI_)END_BUF anchor distance */
    int subAnchor;                          /* start-anchor for exact or map */

    char[] exact;
    int exactP;
    int exactEnd;

    byte[] map;                              /* used as BM skip or char-map */
    int[] intMap;                            /* BM skip for exact_len > 255 */
    int[] intMapBackward;                    /* BM skip for backward search */
    int dMin;                               /* min-distance of exact or map */
    int dMax;                               /* max-distance of exact or map */

    // onig_new
    public Regex(final char[] chars, final int p, final int end, final int option, final Syntax syntax) {
        this(chars, p, end, option, Config.ENC_CASE_FOLD_DEFAULT, syntax, WarnCallback.DEFAULT);
    }

    // onig_alloc_init
    public Regex(final char[] chars, final int p, final int end, final int optionp, final int caseFoldFlag, final Syntax syntax, final WarnCallback warnings) {
        int option = optionp;

        if ((option & (Option.DONT_CAPTURE_GROUP | Option.CAPTURE_GROUP)) ==
            (Option.DONT_CAPTURE_GROUP | Option.CAPTURE_GROUP)) {
            throw new ValueException(ErrorMessages.ERR_INVALID_COMBINATION_OF_OPTIONS);
        }

        if ((option & Option.NEGATE_SINGLELINE) != 0) {
            option |= syntax.options;
            option &= ~Option.SINGLELINE;
        } else {
            option |= syntax.options;
        }

        this.options = option;
        this.caseFoldFlag = caseFoldFlag;
        this.warnings = warnings;

        new Analyser(new ScanEnvironment(this, syntax), chars, p, end).compile();

        this.warnings = null;
    }

    public WarnCallback getWarnings() {
        return warnings;
    }

    /* set skip map for Boyer-Moor search */
    void setupBMSkipMap() {
        final char[] chars = exact;
        final int p = exactP;
        final int end = exactEnd;
        final int len = end - p;

        if (len < Config.CHAR_TABLE_SIZE) {
            // map/skip
            if (map == null) {
                map = new byte[Config.CHAR_TABLE_SIZE];
            }

            for (int i=0; i<Config.CHAR_TABLE_SIZE; i++) {
                map[i] = (byte)len;
            }
            for (int i=0; i<len-1; i++)
             {
                map[chars[p + i] & 0xff] = (byte)(len - 1 -i); // oxff ??
            }
        } else {
            if (intMap == null) {
                intMap = new int[Config.CHAR_TABLE_SIZE];
            }

            for (int i=0; i<len-1; i++)
             {
                intMap[chars[p + i] & 0xff] = len - 1 - i; // oxff ??
            }
        }
    }

    void setExactInfo(final OptExactInfo e) {
        if (e.length == 0) {
            return;
        }

        // shall we copy that ?
        exact = e.chars;
        exactP = 0;
        exactEnd = e.length;

        if (e.ignoreCase) {
            searchAlgorithm = new SearchAlgorithm.SLOW_IC(this);
        } else {
            if (e.length >= 2) {
                setupBMSkipMap();
                searchAlgorithm = SearchAlgorithm.BM;
            } else {
                searchAlgorithm = SearchAlgorithm.SLOW;
            }
        }

        dMin = e.mmd.min;
        dMax = e.mmd.max;

        if (dMin != MinMaxLen.INFINITE_DISTANCE) {
            thresholdLength = dMin + (exactEnd - exactP);
        }
    }

    void setOptimizeMapInfo(final OptMapInfo m) {
        map = m.map;

        searchAlgorithm = SearchAlgorithm.MAP;
        dMin = m.mmd.min;
        dMax = m.mmd.max;

        if (dMin != MinMaxLen.INFINITE_DISTANCE) {
            thresholdLength = dMin + 1;
        }
    }

    void setSubAnchor(final OptAnchorInfo anc) {
        subAnchor |= anc.leftAnchor & AnchorType.BEGIN_LINE;
        subAnchor |= anc.rightAnchor & AnchorType.END_LINE;
    }

    void clearOptimizeInfo() {
        searchAlgorithm = SearchAlgorithm.NONE;
        anchor = 0;
        anchorDmax = 0;
        anchorDmin = 0;
        subAnchor = 0;

        exact = null;
        exactP = exactEnd = 0;
    }

    public String optimizeInfoToString() {
        final StringBuilder s = new StringBuilder();
        s.append("optimize: ").append(searchAlgorithm.getName()).append("\n");
        s.append("  anchor:     ").append(OptAnchorInfo.anchorToString(anchor));

        if ((anchor & AnchorType.END_BUF_MASK) != 0) {
            s.append(MinMaxLen.distanceRangeToString(anchorDmin, anchorDmax));
        }

        s.append("\n");

        if (searchAlgorithm != SearchAlgorithm.NONE) {
            s.append("  sub anchor: ").append(OptAnchorInfo.anchorToString(subAnchor)).append("\n");
        }

        s.append("dmin: ").append(dMin).append(" dmax: ").append(dMax).append("\n");
        s.append("threshold length: ").append(thresholdLength).append("\n");

        if (exact != null) {
            s.append("exact: [").append(exact, exactP, exactEnd - exactP).append("]: length: ").append(exactEnd - exactP).append("\n");
        } else if (searchAlgorithm == SearchAlgorithm.MAP) {
            int n=0;
            for (int i=0; i<Config.CHAR_TABLE_SIZE; i++) {
                if (map[i] != 0) {
                    n++;
                }
            }

            s.append("map: n = ").append(n).append("\n");
            if (n > 0) {
                int c=0;
                s.append("[");
                for (int i=0; i<Config.CHAR_TABLE_SIZE; i++) {
                    if (map[i] != 0) {
                        if (c > 0) {
                            s.append(", ");
                        }
                        c++;
                        // TODO if (enc.isPrint(i)
                        s.append((char)i);
                    }
                }
                s.append("]\n");
            }
        }

        return s.toString();
    }

    public int getOptions() {
        return options;
    }
}
