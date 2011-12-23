package com.tamingtext.texttamer.solr;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import com.tamingtext.TamingTextTestJ4;
import junit.framework.TestCase;

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.junit.Test;


public class SentenceTokenizerTest extends TamingTextTestJ4 {
  @Test public void tokenizerTest() throws IOException {
    String inputString = "A man, a plan, a canal, Panama! " +
     "No matter where you go, there you are. " +
     "You are in a maze of twisty, little, passages. " +
     "Use the force Luke. ";
    
    String[] expectedStrings = {
        "A man, a plan, a canal, Panama! ",
        "No matter where you go, there you are. ",
        "You are in a maze of twisty, little, passages. ",
        "Use the force Luke. "
    };


    
    File modelsDir = getModelDir();
    
    SentenceTokenizerFactory factory = 
      new SentenceTokenizerFactory();
    
    Map<String, String> args = new HashMap<String, String>();
    
    args.put("modelDirectory", modelsDir.getAbsolutePath());
    factory.init(args);
    
    SentenceTokenizer tok = factory.create(new StringReader(inputString));
    
    CharTermAttribute cta;
    PositionIncrementAttribute pta;
    OffsetAttribute oa; 

    int pass = 0;
    
    while (pass < 2) { // test reuse
      int pos = 0;
      int offset = 0;
      
      while (tok.incrementToken()) {
        cta = (CharTermAttribute) tok.getAttribute(CharTermAttribute.class);
        pta = (PositionIncrementAttribute) tok.getAttribute(PositionIncrementAttribute.class);
        oa  = (OffsetAttribute) tok.getAttribute(OffsetAttribute.class);
        
        System.err.println("'" + cta.toString() + "'");
        System.err.println(pta.toString());
        System.err.println(oa.toString());
        System.err.println("--- pass: " + pass);
        
        String expected = expectedStrings[pos];
        TestCase.assertEquals(expected, cta.toString());
        TestCase.assertEquals(1, pta.getPositionIncrement());
        TestCase.assertEquals(offset, oa.startOffset());
        TestCase.assertEquals(offset + expected.length(), oa.endOffset());
        
        offset += expected.length();
        pos++;
      }
      
      tok.end();
      tok.reset(new StringReader(inputString));
      pass++;
    }
    
  }
}
