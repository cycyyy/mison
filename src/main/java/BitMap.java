import java.util.BitSet;

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

public class BitMap extends BitSet {

    private final int length;

    public BitMap(int length) {
        this.length = length;
    }

    /**
     * R(x) = x∧(x−1) Remove the rightmost 1 in x
     */
    public void R() {
        this.and(this.minusOne());
    }

    /**
     * E(x) = x∧ −x Extract the rightmost 1 in x
     */
    public void E() {
        this.and(getNegative());
    }

    /**
     * S(x) = x⊕(x−1)
     * Extract the rightmost 1 in x and smear it to the right
     */
    public void S() {
        this.xor(this.minusOne());
    }

    /**
     * @return x-1
     */
    public BitMap minusOne() {
        BitMap cache = this.clone();
        int index = length - 1;
        while (index >= 0) {
            cache.flip(index);
            if (!cache.get(index))
                break;
            index--;
        }
        return cache;
    }

    /**
     * @return -x
     */
    public BitMap getNegative() {
        BitMap flipOne = this.clone();
        flipOne.flip(0, length);
        int index = length - 1;
        while (index >= 0) {
            flipOne.flip(index);
            if (flipOne.get(index))
                break;
            index--;
        }
        return flipOne;
    }

    public BitMap clone() {
        return (BitMap) super.clone();
    }

    public BitMap add(BitMap map) {
        BitMap result = this.clone();
        boolean carry = false;
        for (int i = map.getLength() - 1; i >= 0; i--) {
            if (result.get(i) && map.get(i)) {
                carry = true;
                result.set(i, false);
            } else {
                result.set(i, result.get(i) || map.get(i));
            }
            if (carry && i > 0) {
                if (!result.get(i - 1))
                    carry = false;
                result.flip(i - 1);
            }
        }
        return result;
    }

    public int getLength() {
        return length;
    }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if (this.get(i))
                sb.append(1);
            else
                sb.append(0);
        }
        return sb.toString();
    }

}
