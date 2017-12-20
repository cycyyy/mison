import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
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

public class Main {

    public static void main(String[] args) throws IOException {
        ArrayList<String> list = new ArrayList<String>();
        list.add("\"attributes\".\"breakfast\"");
        list.add("\"reviews\"");
        list.add("\"city\"");
        URL url = Main.class.getClassLoader().getResource("test.json");
        File file = new File(url.getFile());
        InputStream is = new FileInputStream(file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String str = null;
        TreeNode root = new TreeNode();
        int i = 0;
        while (true) {
            str = reader.readLine();
            if (str != null) {
                i++;
                SpectiveParser parser = new SpectiveParser(str.getBytes(), list);
                System.out.println(str);
                System.out.println(parser.parseAndSpeculate(root));
            } else
                break;
        }

    }
}
