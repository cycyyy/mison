import com.sun.tools.javac.util.Pair;

import java.util.ArrayList;
import java.util.Stack;

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class WordIndex {

    private byte[] jsonString;
    private byte[] jsonStringMirror;
    private BitMap quoteBitmaps;
    private BitMap colonBitmaps;
    private BitMap backslashBitmaps;
    private BitMap leftBraceBitmaps;
    private BitMap rightBraceBitmaps;
    private BitMap structuralQuoteBitmaps;
    private BitMap stringMaskBitmaps;
    private BitMap structuralColonBitmaps;
    private BitMap structuralLeftBraceBitmaps;
    private BitMap structuralRightBraceBitmaps;
    private static int stringMaskNum = 0;

    public WordIndex(byte[] jsonString) {
        this.jsonString = jsonString;
        this.initBitmaps(jsonString.length);
        this.buildStructuralCharacterBitmaps(jsonString);
        this.structuralQuoteBitmaps = this.buildStructuralQuoteBitmaps();
        this.stringMaskBitmaps = this.buildStringMaskBitmaps();
        this.initStructuralBitmaps();
    }

    private void initBitmaps(int length) {
        this.quoteBitmaps = new BitMap(length);
        this.colonBitmaps = new BitMap(length);
        this.backslashBitmaps = new BitMap(length);
        this.leftBraceBitmaps = new BitMap(length);
        this.rightBraceBitmaps = new BitMap(length);
    }

    private void initStructuralBitmaps() {
        this.structuralColonBitmaps = this.buildStructuralBitmaps(this.colonBitmaps);
        this.structuralLeftBraceBitmaps = this.buildStructuralBitmaps(this.leftBraceBitmaps);
        this.structuralRightBraceBitmaps = this.buildStructuralBitmaps(this.rightBraceBitmaps);
    }

    /**
     * 4.2.1 Step 1: Building Structural Character Bitmaps
     */
    private void buildStructuralCharacterBitmaps(byte[] jsonString) {
        this.jsonStringMirror = new byte[jsonString.length];
        for (int i = jsonString.length - 1, index = 0; i >= 0; i--, index++) {
            this.jsonStringMirror[index] = jsonString[i];
            switch (jsonString[i]) {
            case '\\':
                if (index > 0)
                    this.backslashBitmaps.set(index - 1);
                break;
            case '{':
                this.leftBraceBitmaps.set(index);
                break;
            case '}':
                this.rightBraceBitmaps.set(index);
                break;
            case ':':
                this.colonBitmaps.set(index);
                break;
            case '"':
                this.quoteBitmaps.set(index);
                break;
            default:
                break;
            }
        }
    }

    /**
     * 4.2.2 Step 2: Building Structural Quote Bitmaps
     */
    public BitMap buildStructuralQuoteBitmaps() {
        BitMap structuralQuoteBitmaps = this.quoteBitmaps.clone();
        structuralQuoteBitmaps.xor(this.backslashBitmaps);
        return structuralQuoteBitmaps;
    }

    /**
     * 4.2.3 Step 3: Building String Mask Bitmaps
     *
     * @return stringMaskBitmaps
     */
    public BitMap buildStringMaskBitmaps() {
        BitMap stringMaskBitmaps = new BitMap(structuralQuoteBitmaps.getLength());
        BitMap structuralQuoteBitmapsCache = structuralQuoteBitmaps.clone();
        while (structuralQuoteBitmapsCache.cardinality() != 0) {
            BitMap cache = structuralQuoteBitmapsCache.clone();
            cache.S();
            stringMaskBitmaps.xor(cache);
            structuralQuoteBitmapsCache.R();
            stringMaskNum++;
        }
        if (stringMaskNum % 2 == 1)
            stringMaskBitmaps.flip(0, stringMaskBitmaps.getLength());
        return stringMaskBitmaps;
    }

    /**
     * 4.2.4 Step 4: Building Leveled Colon Bitmaps
     */
    public static BitMap[][] buidLeveledColonBitmaps(WordIndex[] words, int l) {
        BitMap[][] result = new BitMap[l][words.length];
        for (int i = 0; i < words.length; i++) {
            for (int k = 0; k < l; k++) {
                result[k][i] = words[i].structuralColonBitmaps.clone();
            }
        }
        Stack<Pair<Integer, BitMap>> S = new Stack<Pair<Integer, BitMap>>();
        for (int i = 0; i < words.length; i++) {
            BitMap mleft = words[i].structuralLeftBraceBitmaps.clone();
            BitMap mright = words[i].structuralRightBraceBitmaps.clone();
            while (true) {
                BitMap rightbit = mright.clone();
                rightbit.E();
                BitMap leftbit = mleft.clone();
                leftbit.E();
                while (leftbit.cardinality() != 0 && (rightbit.cardinality() == 0 || leftbit.length() > rightbit.length())) {
                    S.push(new Pair<Integer, BitMap>(i, leftbit));
                    mleft.R();
                    leftbit = mleft.clone();
                    leftbit.E();
                }
                if (rightbit.cardinality() != 0) {
                    Pair<Integer, BitMap> pair = S.pop();
                    leftbit = pair.snd;
                    int j = pair.fst;
                    int s = S.size();
                    if (s > 0 && s <= l) {
                        if (i == j) {
                            BitMap add = rightbit.add(leftbit.getNegative());
                            add.flip(0, add.getLength());
                            result[s - 1][i].and(add);
                        } else {
                            result[s - 1][j].and(leftbit.minusOne());
                            BitMap minusOne = rightbit.minusOne();
                            minusOne.flip(0, minusOne.getLength());
                            result[s - 1][i].and(minusOne);
                            for (int k = j + 1; k <= i - 1; k++) {
                                result[s - 1][k].clear();
                            }
                        }
                    }
                }
                mright.R();
                if (rightbit.cardinality() == 0)
                    break;
            }
        }
        for (int i = l - 2; i >= 0; i--) {
            for (int k = 0; k < words.length; k++) {
                result[i + 1][k].xor(result[i][k]);
            }
        }
//        for (int i = 0; i < words.length; i++) {
//            words[i].print();
//            System.out.println("Step 4");
//            for (int k = 0; k < l; k++) {
//                System.out.println(result[k][i]);
//            }
//        }
        return result;
    }

    /**
     * Algorithm 3 GenerateColonPositions(index, start, end, level)
     */
    public static ArrayList<Integer> generateColonPositions(BitMap[][] index, int start, int end, int level, int wsize) {
        ArrayList<Integer> result = new ArrayList<Integer>();
        int i = start / wsize;
        int k = end / wsize + 1;
        for (; i < k; i++) {
            BitMap mColon = index[level][i].clone();
            while (mColon.cardinality() != 0) {
                BitMap mBit = mColon.clone();
                mBit.E();
                int offset = i * wsize + mBit.minusOne().cardinality();
                if (offset <= end && offset >= start) {
                    result.add(offset);
                }
                mColon.R();
            }
        }
        return result;
    }

    public byte[] getJsonStringMirror() {
        return jsonStringMirror;
    }

    public BitMap getQuoteBitmaps() {
        return quoteBitmaps;
    }

    public BitMap getColonBitmaps() {
        return colonBitmaps;
    }

    public BitMap getBackslashBitmaps() {
        return backslashBitmaps;
    }

    public BitMap getLeftBraceBitmaps() {
        return leftBraceBitmaps;
    }

    public BitMap getRightBraceBitmaps() {
        return rightBraceBitmaps;
    }

    public BitMap getStructuralQuoteBitmaps() {
        return structuralQuoteBitmaps;
    }

    public BitMap getStringMaskBitmaps() {
        return stringMaskBitmaps;
    }

    public BitMap getStructuralColonBitmaps() {
        return structuralColonBitmaps;
    }

    public BitMap getStructuralLeftBraceBitmaps() {
        return structuralLeftBraceBitmaps;
    }

    public BitMap getStructuralRightBraceBitmaps() {
        return structuralRightBraceBitmaps;
    }

    /**
     *
     * @param originalBitmaps
     * @return
     */
    public BitMap buildStructuralBitmaps(BitMap originalBitmaps) {
        BitMap cache = originalBitmaps.clone();
        cache.and(stringMaskBitmaps);
        cache.flip(0, cache.getLength());
        BitMap result = originalBitmaps.clone();
        result.and(cache);
        return result;
    }

    public void print() {
        System.out.println(new String(jsonString));
        System.out.println(new String(jsonStringMirror));
        System.out.println("Step 1");
        System.out.println(this.backslashBitmaps + " \\");
        System.out.println(this.leftBraceBitmaps + "{");
        System.out.println(this.rightBraceBitmaps + "}");
        System.out.println(this.colonBitmaps + " :");
        System.out.println(this.quoteBitmaps + " \"");
        System.out.println("Step 2");
        System.out.println(structuralQuoteBitmaps);
        System.out.println("Step 3");
        System.out.println(stringMaskBitmaps);
    }

    public static void newJson() {
        stringMaskNum = 0;
    }

    public static void main(String[] args) {
        String b = "{\"id\":\"id:\\\"a\\\"\", \"reviews\":50, \"attributes\":{\"breakfast\":false, \"lunc";
        String c = "h\":true, \"dinner\":true, \"latenight\":true},\"categories\":[\"Restaurant\",";
        String p = "\"Bars\"], \"state\":\"WA\", \"city\":\"seattle\"}";
        WordIndex a = new WordIndex(b.getBytes());
        WordIndex e = new WordIndex(c.getBytes());
        WordIndex k = new WordIndex(p.getBytes());
        //                System.out.println(a.buildStringMaskBitmaps(a.buildStructuralQuoteBitmaps()));
        WordIndex[] list = new WordIndex[3];
        list[0] = a;
        list[1] = e;
        list[2] = k;
        System.out.println(new String(e.getJsonStringMirror()));
        System.out.println(e.getStringMaskBitmaps());
        System.out.println(e.getColonBitmaps());
        System.out.println(e.getStructuralColonBitmaps());
//        System.out.println(WordIndex.buidLeveledColonBitmaps(list, 2)[0][0]);
//        System.out.println(WordIndex.buidLeveledColonBitmaps(list, 2)[1][0]);
        //        System.out.println(WordIndex.buidLeveledColonBitmaps(list, 4)[2][0]);
        //        System.out.println(WordIndex.buidLeveledColonBitmaps(list, 4)[3][0]);
//        System.out.println(WordIndex.buidLeveledColonBitmaps(list, 2)[0][1]);
//        System.out.println(WordIndex.buidLeveledColonBitmaps(list, 2)[1][1]);
        //        System.out.println(WordIndex.buidLeveledColonBitmaps(list, 4)[2][1]);
        //        System.out.println(WordIndex.buidLeveledColonBitmaps(list, 4)[3][1]);
//        System.out.println(WordIndex.buidLeveledColonBitmaps(list, 2)[0][2]);
//        System.out.println(WordIndex.buidLeveledColonBitmaps(list, 2)[1][2]);
        //        System.out.println(WordIndex.buidLeveledColonBitmaps(list, 4)[2][2]);
        //        System.out.println(WordIndex.buidLeveledColonBitmaps(list, 4)[3][2]);
    }

}
