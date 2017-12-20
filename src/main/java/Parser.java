import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import spective.TreeNode;

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

public class Parser {

    protected static int WORD_SIZE = 128;
    protected final byte[] jsonArray;
    protected List<String> fields;
    protected int l;
    protected BitMap[][] leveledColonMaps;

    public Parser(byte[] jsonArray, List<String> fields) {
        this.jsonArray = jsonArray;
        this.fields = fields;
        this.initDeep();
        this.initWordIndex();
    }

    public Map<String, String> parse() {
        return this.parse(0, "", WordIndex.generateColonPositions(this.leveledColonMaps, 0, jsonArray.length, 0, WORD_SIZE));
    }


    private Map<String, String> parse(int level, String uppleLevelField, ArrayList<Integer> colons) {
        Map<String, String> result = new HashMap<String, String>();
        for (int i = 0; i < colons.size(); i++) {
            int colonPos = colons.get(i);
            int fieldOffset = this.findFieldOffset(colonPos);
            String field = new String(ArrayUtils.subarray(jsonArray, fieldOffset, colonPos));
            if (level != 0)
                field = uppleLevelField + "." + field;
            int endPos;
            if (i == colons.size() - 1) {
                endPos = this.findBoundaryOfLastField(colonPos);
            } else {
                endPos = this.findBoundaryOfField(colons.get(i + 1));
            }
            int levelLeft = this.levelLeft(field);
            if (levelLeft == -1)
                continue;
            else if (levelLeft == 0) {
                result.put(field, new String(ArrayUtils.subarray(jsonArray, colonPos + 1, endPos)));
            } else {
                result.putAll(this.parse(level + 1, field, WordIndex.generateColonPositions(this.leveledColonMaps, colonPos, endPos, level + 1, WORD_SIZE)));
            }
        }
        return result;
    }



    /**
     * @return -1:don't need to query , return the level left
     */
    protected int levelLeft(String field) {
        for (String queryField : fields) {
            if (queryField.contains(field)) {
                if (queryField.equals(field)) {
                    return 0;
                } else {
                    String[] queryFields = queryField.split("\\.");
                    for (int i = 0; i < queryFields.length; i++) {
                        if (queryFields[i].equals(field))
                            return queryFields.length - i - 1;
                    }
                    throw new RuntimeException();
                }
            }
        }
        return -1;
    }

    protected void initDeep() {
        for (String field : fields) {
            int number = field.split("\\.").length;
            l = Math.max(l, number);
        }
    }

    protected void initWordIndex() {
        WordIndex[] indexs = new WordIndex[jsonArray.length / WORD_SIZE + 1];
        WordIndex.newJson();
        for (int i = 0; i < indexs.length - 1; i++) {
            indexs[i] = new WordIndex(ArrayUtils.subarray(jsonArray, i * WORD_SIZE, (i + 1) * WORD_SIZE));
        }
        indexs[indexs.length - 1] = new WordIndex(ArrayUtils.subarray(jsonArray, jsonArray.length / WORD_SIZE * WORD_SIZE, jsonArray.length));
        this.leveledColonMaps = WordIndex.buidLeveledColonBitmaps(indexs, l);
    }

    /**
     * find the left quote pos of field ( "filed": )
     *
     * @param start colon's pos
     * @return the second quote pos at the left side of field
     */
    protected int findFieldOffset(int start) {
        boolean isFirstQuote = true;
        while (start > -1) {
            if (jsonArray[start] == '"' && jsonArray[start - 1] != '\\') {
                if (isFirstQuote)
                    isFirstQuote = false;
                else
                    return start;
            }
            start--;
        }
        return -1;
    }

    /**
     * (fieldA:dataA, fieldB:dataB) the boundary pos is comma's pos
     *
     * @param start colon's pos
     * @return the first comma at left side of the field
     */
    protected int findBoundaryOfField(int start) {
        while (start > -1) {
            if (jsonArray[start] == ',' && jsonArray[start - 1] != '\\') {
                return start;
            }
            start--;
        }
        return -1;
    }

    /**
     * ( {fieldB:dataB} ) the last field's boundary pos is right brace's pos
     *
     * @param start colon's pos
     * @return the first right brace at right side of the field
     */
    protected int findBoundaryOfLastField(int start) {
        while (start < this.jsonArray.length) {
            if (jsonArray[start] == '}' && jsonArray[start - 1] != '\\') {
                return start;
            }
            start++;
        }
        return -1;
    }

}
