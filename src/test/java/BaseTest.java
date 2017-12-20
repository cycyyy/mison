import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

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

public abstract class BaseTest {

    protected String[] fileNames;

    @Test public void test() {
        if (fileNames != null) {
            for (String file : fileNames) {
                long time = System.currentTimeMillis();
                this.testFile(file);
                System.out.println("File: " + file + " " + (System.currentTimeMillis() - time));
            }
        }
    }

    protected void testFile(String fileName) {
        URL url = Main.class.getClassLoader().getResource(fileName);
        File file = new File(url.getFile());
        InputStream is = null;
        BufferedReader reader = null;
        String str = null;
        try {
            is = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(is));
            while (true) {
                str = reader.readLine();
                if (str != null) {
                    this.parse(str);
                } else
                    break;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (reader != null)
                    reader.close();
                if (is != null)
                    is.close();
            } catch (IOException e1) {

            }

        }
    }

    protected abstract void parse(String string) throws IOException;
}
