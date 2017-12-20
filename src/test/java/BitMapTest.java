import junit.framework.Assert;

import org.junit.Test;

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

public class BitMapTest {
    @Test public void testS() throws Exception {
        BitMap a = new BitMap(8);
        a.set(0);
        a.set(1);
        a.set(2);
        a.set(4);
        a.S();
        for (int i = 0; i < 4; i++) {
            Assert.assertFalse(a.get(i));
        }
        for (int i = 4; i < 8; i++) {
            Assert.assertTrue(a.get(i));
        }
    }

    @Test public void testR() throws Exception {
        BitMap a = new BitMap(8);
        a.set(0);
        a.set(1);
        a.set(2);
        a.set(4);
        a.R();
        for (int i = 0; i < 3; i++) {
            Assert.assertTrue(a.get(i));
        }
        for (int i = 3; i < 8; i++) {
            Assert.assertFalse(a.get(i));
        }
    }

    @Test public void testE() throws Exception {
        BitMap a = new BitMap(8);
        a.set(0);
        a.set(1);
        a.set(2);
        a.set(4);
        a.E();
        for (int i = 0; i < 8; i++) {
            if (i == 4) {
                Assert.assertTrue(a.get(i));
                continue;
            }
            Assert.assertFalse(a.get(i));
        }
    }

    @Test public void testAdd() throws Exception {
        BitMap a = new BitMap(8);
        a.set(1);
        BitMap b = new BitMap(8);
        b.set(5);
        BitMap c = a.add(b.getNegative());
        for (int i = 2; i < 6; i++) {
            Assert.assertTrue(c.get(i));
        }
        Assert.assertFalse(c.get(0));
        Assert.assertFalse(c.get(1));
        Assert.assertFalse(c.get(6));
        Assert.assertFalse(c.get(7));
    }

}
