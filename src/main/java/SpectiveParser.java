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

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import spective.TreeNode;

public class SpectiveParser extends Parser {
    public SpectiveParser(byte[] jsonArray, List<String> fields) {
        super(jsonArray, fields);
    }

    public Map<String, String> parseAndSpeculate(TreeNode root) {
        Map<String, String> result =
                this.speculativeParse(0, root, "", WordIndex.generateColonPositions(this.leveledColonMaps, 0, jsonArray.length, 0, WORD_SIZE));
        if (result == null)
            return this.parseAndTrain(root);
        else
            return result;
    }

    /**
     * parse and train the tree
     */
    private Map<String, String> parseAndTrain(TreeNode root) {
        return this.parseAndTrain(0, "", WordIndex.generateColonPositions(this.leveledColonMaps, 0, jsonArray.length, 0, WORD_SIZE), root);
    }

    /**
     * if result == null, speculate fail.
     */
    private Map<String, String> speculativeParse(int level, TreeNode root, String uppleLevelField, ArrayList<Integer> colons) {
        root.orderChildrenByWeight();
        for (String name : root.getChildrenNameOrderByWeight()) {
            boolean success = false;
            Map<String, String> result = new HashMap<String, String>();
            TreeNode node = root.getChildren().get(name);
            int i = node.getIndex();
            if (i > colons.size() - 1)
                continue;
            int colonPos = colons.get(i);
            int fieldOffset = this.findFieldOffset(colonPos);
            String field = new String(ArrayUtils.subarray(jsonArray, fieldOffset, colonPos));
            int endPos;
            if (i == colons.size() - 1) {
                endPos = this.findBoundaryOfLastField(colonPos);
            } else {
                endPos = this.findBoundaryOfField(colons.get(i + 1));
            }
            success = this.verify(node, field);
            if (level != 0)
                field = uppleLevelField + "." + field;
            if (success) {
                if (node.getChildrenNameOrderByWeight().size() == 0) {
                    result.put(field, new String(ArrayUtils.subarray(jsonArray, colonPos + 1, endPos)));
                } else {
                    result.put(field, new String(ArrayUtils.subarray(jsonArray, colonPos + 1, endPos)));
                    Map<String, String> sonResult = this.speculativeParse(level, node, uppleLevelField, colons);
                    if (sonResult == null)
                        success = false;
                    else
                        result.putAll(sonResult);
                }
                if (node.getNestChildrenRoot() != null) {
                    result.remove(field);
                    Map<String, String> sonResult = this.speculativeParse(level + 1, node.getNestChildrenRoot(), field,
                            WordIndex.generateColonPositions(this.leveledColonMaps, colonPos, endPos, level + 1, WORD_SIZE));
                    if (sonResult == null)
                        success = false;
                    else
                        result.putAll(sonResult);
                }
            }
            if (success)
                return result;
        }
        return null;
    }

    private Map<String, String> parseAndSpective(TreeNode root) {
        return this.speculativeParse(0, root, "", WordIndex.generateColonPositions(this.leveledColonMaps, 0, jsonArray.length, 0, WORD_SIZE));
    }

    private boolean verify(TreeNode root, String field) {
        return root.getFieldName().split("-")[0].equals(field);
    }

    private Map<String, String> parseAndTrain(int level, String uppleLevelField, ArrayList<Integer> colons, TreeNode root) {
        if (!root.isRoot()) {
            root.setNestChildrenRoot(new TreeNode());
            root = root.getNestChildrenRoot();
        }
        Map<String, String> result = new HashMap<String, String>();
        for (int i = 0; i < colons.size(); i++) {
            int colonPos = colons.get(i);
            int fieldOffset = this.findFieldOffset(colonPos);
            String field = new String(ArrayUtils.subarray(jsonArray, fieldOffset, colonPos));
            String orginalField = field;
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
                root = root.addChild(orginalField, i, root);
                result.put(field, new String(ArrayUtils.subarray(jsonArray, colonPos + 1, endPos)));
            } else {
                root = root.addChild(orginalField, i, root);
                result.putAll(
                        this.parseAndTrain(level + 1, field, WordIndex.generateColonPositions(this.leveledColonMaps, colonPos, endPos, level + 1, WORD_SIZE),
                                root));
            }
        }
        return result;
    }

}
