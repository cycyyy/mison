import org.junit.Before;

import java.util.ArrayList;

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

public class MisonTest extends BaseTest {

    private ArrayList<String> list = new ArrayList<String>();
    private TreeNode root = new TreeNode();

    @Before
    public void init() {
        list.add("\"business_id\"");
        super.fileNames = new String[] {"business.json", "photos.json", "tip.json", "checkin.json"};
    }

    protected void parse(String string) {
        SpectiveParser parser = new SpectiveParser(string.getBytes(), list);
        parser.parseAndSpeculate(this.root);
    }
}
