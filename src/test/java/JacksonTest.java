import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Before;

import java.io.IOException;
import java.util.Iterator;

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

public class JacksonTest extends BaseTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void init() {
        super.fileNames = new String[] {"business1.json", "business2.json", "photos.json", "tip1.json", "tip2.json"};
    }

    protected void parse(String string) throws IOException {
        JsonNode node = mapper.readTree(string);
        Iterator<String> fieldNames = node.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            if (fieldName.equals("business_id"))
                node.get(fieldName).asText();
        }
    }
}
