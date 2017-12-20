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
package spective;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

public class TreeNode implements Comparable<TreeNode> {

    private String fieldName;
    private int weight = 0;
    private int index;
    private TreeNode parent;
    private TreeNode nestChildrenRoot;
    private TreeMap<String, TreeNode> children = new TreeMap<String, TreeNode>();
    private List<String> childrenNameOrderByWeight = new ArrayList<String>();
    private boolean isRoot = false;

    public TreeNode(String fieldName, int index, TreeNode parent) {
        this.fieldName = fieldName;
        this.index = index;
        this.parent = parent;
    }

    public TreeNode() {
        this.isRoot = true;
    }

    public void addWeight() {
        this.weight++;
    }

    public TreeNode addChild(String nodeName, int index, TreeNode parent) {
        nodeName = nodeName + "-" + index;
        TreeNode child = children.get(nodeName);
        if (child == null) {
            child = new TreeNode(nodeName, index, parent);
            children.put(nodeName, child);
            childrenNameOrderByWeight.add(nodeName);
        }
        child.addWeight();
        return child;
    }

    public void setNestChildrenRoot(TreeNode nestChildrenRoot) {
        this.nestChildrenRoot = nestChildrenRoot;
    }

    public String getFieldName() {
        return fieldName;
    }

    public int getWeight() {
        return weight;
    }

    public int getIndex() {
        return index;
    }

    public TreeNode getParent() {
        return parent;
    }

    public TreeNode getNestChildrenRoot() {
        return nestChildrenRoot;
    }

    public TreeMap<String, TreeNode> getChildren() {
        return children;
    }

    public List<String> getChildrenNameOrderByWeight() {
        return childrenNameOrderByWeight;
    }

    public boolean isRoot() {
        return isRoot;
    }

    public void orderChildrenByWeight() {
        Collections.sort(this.childrenNameOrderByWeight, new Comparator<String>() {
            public int compare(String o1, String o2) {
                Integer weight1 = children.get(o1).getWeight();
                Integer weight2 = children.get(o2).getWeight();
                return weight1.compareTo(weight2);
            }
        });
    }

    public int compareTo(TreeNode o) {
        if (this.weight < o.weight)
            return -1;
        else if (this.weight == o.weight)
            return 0;
        else
            return 1;
    }
}
