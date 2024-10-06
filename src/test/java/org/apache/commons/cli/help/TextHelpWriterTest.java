/*
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */
package org.apache.commons.cli.help;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public final class TextHelpWriterTest {

    private StringBuilder sb;
    private TextHelpWriter underTest;

    @BeforeEach
    public void setUp() {
        sb = new StringBuilder();
        underTest = new TextHelpWriter(sb);
    }

    @Test
    public void testWriteTitle() throws IOException {
        String[] expected = {" Hello World", " ###########", ""};

        sb.setLength(0);
        underTest.writeTitle("Hello World");
        List<String> actual = IOUtils.readLines(new StringReader(sb.toString()));
        assertEquals(Arrays.asList(expected), actual);

        sb.setLength(0);
        underTest.writeTitle("");
        assertEquals(0, sb.length(), "empty string test failed");

        sb.setLength(0);
        underTest.writeTitle(null);
        assertEquals(0, sb.length(), "null test failed");

    }

    @Test
    public void testWritePara() throws IOException {
        String[] expected = {" Hello World", ""};

        sb.setLength(0);
        underTest.writePara("Hello World");
        List<String> actual = IOUtils.readLines(new StringReader(sb.toString()));
        assertEquals(Arrays.asList(expected), actual);

        sb.setLength(0);
        underTest.writePara("");
        assertEquals(0, sb.length(), "empty string test failed");

        sb.setLength(0);
        underTest.writePara(null);
        assertEquals(0, sb.length(), "null test failed");
    }

    @Test
    public void testWriteHeader() throws IOException {
        String[] expected = {" Hello World", " ===========", ""};

        sb.setLength(0);
        underTest.writeHeader(1, "Hello World");
        List<String> actual = IOUtils.readLines(new StringReader(sb.toString()));
        assertEquals(Arrays.asList(expected), actual, "header 1 failed");

        sb.setLength(0);
        underTest.writeHeader(2, "Hello World");
        actual = IOUtils.readLines(new StringReader(sb.toString()));
        expected[1] = " %%%%%%%%%%%";
        assertEquals(Arrays.asList(expected), actual, "header 2 failed");

        sb.setLength(0);
        underTest.writeHeader(3, "Hello World");
        actual = IOUtils.readLines(new StringReader(sb.toString()));
        expected[1] = " +++++++++++";
        assertEquals(Arrays.asList(expected), actual, "header 3 failed");

        sb.setLength(0);
        underTest.writeHeader(4, "Hello World");
        actual = IOUtils.readLines(new StringReader(sb.toString()));
        expected[1] = " ___________";
        assertEquals(Arrays.asList(expected), actual, "header 4 failed");

        sb.setLength(0);
        underTest.writeHeader(5, "Hello World");
        actual = IOUtils.readLines(new StringReader(sb.toString()));
        assertEquals(Arrays.asList(expected), actual, "header 5 failed");

        sb.setLength(0);
        assertThrows(IllegalArgumentException.class, () -> underTest.writeHeader(0, "Hello World"));

        sb.setLength(0);
        underTest.writeHeader(5, "");
        assertEquals(0, sb.length(), "empty string test failed");

        sb.setLength(0);
        underTest.writeHeader(5, null);
        assertEquals(0, sb.length(), "null test failed");
    }

    @Test
    public void testWriteList() throws IOException {
        List<String> expected = new ArrayList<>();
        String[] entries = {"one", "two", "three"};
        for (int i = 0; i < entries.length; i++) {
            expected.add(format("  %s. %s", i + 1, entries[i]));
        }
        expected.add("");

        sb.setLength(0);
        underTest.writeList(true, Arrays.asList(entries));
        List<String> actual = IOUtils.readLines(new StringReader(sb.toString()));
        assertEquals(expected, actual, "ordered list failed");

        sb.setLength(0);
        expected.clear();
        for (int i = 0; i < entries.length; i++) {
            expected.add(format("  * %s", entries[i]));
        }
        expected.add("");
        underTest.writeList(false, Arrays.asList(entries));
        actual = IOUtils.readLines(new StringReader(sb.toString()));
        assertEquals(expected, actual, "unordered list failed");

        sb.setLength(0);
        expected.clear();
        underTest.writeList(false, Collections.emptyList());
        actual = IOUtils.readLines(new StringReader(sb.toString()));
        assertEquals(expected, actual, "empty list failed");

        sb.setLength(0);
        expected.clear();
        underTest.writeList(false, null);
        actual = IOUtils.readLines(new StringReader(sb.toString()));
        assertEquals(expected, actual, "null list failed");
    }

    @Test
    public void testWriteTable() throws IOException {
        TextStyle.Builder styleBuilder = new TextStyle.Builder();
        List<TextStyle> styles = new ArrayList<>();
        styles.add(styleBuilder.setIndent(2).get());
        styles.add(styleBuilder.setIndent(0).setLeftPad(5).setAlignment(TextStyle.Alignment.RIGHT).get());

        String[] headers = { "fox", "time"};

        List[] rows = {
                Arrays.asList("The quick brown fox jumps over the lazy dog",
                        "Now is the time for all good people to come to the aid of their country"),
                Arrays.asList("Léimeann an sionnach donn gasta thar an madra leisciúil",
                        "Anois an t-am do na daoine maithe go léir teacht i gcabhair ar a dtír"),
        };

        List<String> expected = new ArrayList<>();
        expected.add(" Common Phrases");
        expected.add("");
        expected.add("               fox                                       time                   ");
        expected.add(" The quick brown fox jumps over           Now is the time for all good people to");
        expected.add("   the lazy dog                                 come to the aid of their country");
        expected.add(" Léimeann an sionnach donn gasta       Anois an t-am do na daoine maithe go léir");
        expected.add("   thar an madra leisciúil                           teacht i gcabhair ar a dtír");
        expected.add("");

        TableDefinition table = TableDefinition.from("Common Phrases", styles, Arrays.asList(headers), Arrays.asList(rows));
        sb.setLength(0);
        underTest.setMaxWidth(80);
        underTest.writeTable(table);
        List<String> actual = IOUtils.readLines(new StringReader(sb.toString()));
        assertEquals(expected, actual, "full table failed");

        table = TableDefinition.from(null, styles, Arrays.asList(headers), Arrays.asList(rows));
        expected.remove(1);
        expected.remove(0);
        sb.setLength(0);
        underTest.writeTable(table);
        actual = IOUtils.readLines(new StringReader(sb.toString()));
        assertEquals(expected, actual);

        table = TableDefinition.from(null, styles, Arrays.asList(headers), Collections.emptyList());
        expected = new ArrayList<>();
        expected.add(" fox     time");
        expected.add("");
        sb.setLength(0);
        underTest.writeTable(table);
        actual = IOUtils.readLines(new StringReader(sb.toString()));
        assertEquals(expected, actual, "no rows test failed");
    }

    @Test
    public void tesstMakeColumnQueue() {
        String text = "The quick brown fox jumps over the lazy dog";
        TextStyle.Builder styleBuilder = new TextStyle.Builder().setMaxWidth(10).setIndent(0).setLeftPad(0);

        Queue<String> expected = new LinkedList<>();
        expected.add("The quick ");
        expected.add("brown fox ");
        expected.add("jumps over");
        expected.add("the lazy  ");
        expected.add("dog       ");

        Queue<String> result = underTest.makeColumnQueue(text, styleBuilder.get());
        assertEquals(expected, result, "left aligned failed");

        expected.clear();
        expected.add(" The quick");
        expected.add(" brown fox");
        expected.add("jumps over");
        expected.add("  the lazy");
        expected.add("       dog");
        styleBuilder.setAlignment(TextStyle.Alignment.RIGHT);

        result = underTest.makeColumnQueue(text, styleBuilder.get());
        assertEquals(expected, result, "right aligned failed");

        expected.clear();
        expected.add("The quick ");
        expected.add("brown fox ");
        expected.add("jumps over");
        expected.add(" the lazy ");
        expected.add("   dog    ");
        styleBuilder.setAlignment(TextStyle.Alignment.CENTER);

        result = underTest.makeColumnQueue(text, styleBuilder.get());
        assertEquals(expected, result, "center aligned failed");

        expected = new LinkedList<>();
        expected.add("      The quick");
        expected.add("          brown");
        expected.add("            fox");
        expected.add("          jumps");
        expected.add("       over the");
        expected.add("       lazy dog");
        styleBuilder.setAlignment(TextStyle.Alignment.RIGHT).setLeftPad(5).setIndent(2);

        result = underTest.makeColumnQueue(text, styleBuilder.get());
        assertEquals(expected, result, "right aligned failed");
    }


    @Test
    public void testWriteColumnQueues() throws IOException {
        List<Queue<String>> queues = new ArrayList<>();

        Queue<String> queue = new LinkedList<>();
        queue.add("The quick ");
        queue.add("brown fox ");
        queue.add("jumps over");
        queue.add("the lazy  ");
        queue.add("dog       ");

        queues.add(queue);

        queue = new LinkedList<>();
        queue.add("     Now is the");
        queue.add("     time for  ");
        queue.add("     all good  ");
        queue.add("     people to ");
        queue.add("     come to   ");
        queue.add("     the aid of");
        queue.add("     their     ");
        queue.add("     country   ");

        queues.add(queue);

        TextStyle.Builder styleBuilder = new TextStyle.Builder().setMaxWidth(10).setIndent(0).setLeftPad(0);

        List<TextStyle> columns = new ArrayList<>();
        columns.add(styleBuilder.get());
        columns.add(styleBuilder.setLeftPad(5).get());

        List<String> expected = new ArrayList<>();
        expected.add(" The quick      Now is the");
        expected.add(" brown fox      time for  ");
        expected.add(" jumps over     all good  ");
        expected.add(" the lazy       people to ");
        expected.add(" dog            come to   ");
        expected.add("                the aid of");
        expected.add("                their     ");
        expected.add("                country   ");

        sb.setLength(0);
        underTest.writeColumnQueues(queues, columns);
        List<String> actual = IOUtils.readLines(new StringReader(sb.toString()));
        assertEquals(expected, actual);
    }

    @Test
    public void testResize() {
        TextStyle.Builder tsBuilder = new TextStyle.Builder().setIndent(2).setMaxWidth(3);
        underTest.resize(tsBuilder, 0.5);
        assertEquals(0, tsBuilder.getIndent());

        tsBuilder = new TextStyle.Builder().setIndent(4).setMaxWidth(6);
        underTest.resize(tsBuilder, 0.5);
        assertEquals(1, tsBuilder.getIndent());
    }

    @Test
    public void testResizeTableFormat() {
        underTest.setMaxWidth(150);
        TableDefinition tableDefinition = TableDefinition.from("Caption", Arrays.asList(new TextStyle.Builder().setMinWidth(20)
                .setMaxWidth(100).get()),
                Arrays.asList("header"), Arrays.asList(Arrays.asList("one")));
        TableDefinition result = underTest.adjustTableFormat(tableDefinition);
        assertEquals(20, result.columnStyle().get(0).getMinWidth(), "Minimum width should not be reset");
        assertEquals(100, result.columnStyle().get(0).getMaxWidth(), "Maximum width should not be reset");
    }

    @Test
    public void testPrintWrapped() throws IOException {
        String text = "The quick brown fox jumps over the lazy dog";
        TextStyle.Builder styleBuilder = new TextStyle.Builder().setMaxWidth(10).setIndent(0).setLeftPad(0);

        List<String> expected = new ArrayList<>();
        expected.add("The quick");
        expected.add("brown fox");
        expected.add("jumps over");
        expected.add("the lazy");
        expected.add("dog");
        underTest.printWrapped(text, styleBuilder.get());
        List<String> actual = IOUtils.readLines(new StringReader(sb.toString()));
        assertEquals(expected, actual, "left aligned failed");

        sb.setLength(0);
        expected.clear();
        expected.add(" The quick");
        expected.add(" brown fox");
        expected.add("jumps over");
        expected.add("  the lazy");
        expected.add("       dog");
        styleBuilder.setAlignment(TextStyle.Alignment.RIGHT);

        underTest.printWrapped(text, styleBuilder.get());
        actual = IOUtils.readLines(new StringReader(sb.toString()));
        assertEquals(expected, actual, "right aligned failed");

        sb.setLength(0);
        expected.clear();
        expected.add("The quick");
        expected.add("brown fox");
        expected.add("jumps over");
        expected.add(" the lazy");
        expected.add("   dog");
        styleBuilder.setAlignment(TextStyle.Alignment.CENTER);

        underTest.printWrapped(text, styleBuilder.get());
        actual = IOUtils.readLines(new StringReader(sb.toString()));
        assertEquals(expected, actual, "center aligned failed");

        sb.setLength(0);
        expected.clear();
        expected.add(" The quick brown fox jumps over the lazy dog");

        assertEquals(1, underTest.getLeftPad(), "unexpected page left pad");
        assertEquals(3, underTest.getIndent(), "unexpected page indent");
        assertEquals(74, underTest.getMaxWidth(), "unexpected page width");
        underTest.printWrapped(text);
        actual = IOUtils.readLines(new StringReader(sb.toString()));
        assertEquals(expected, actual, "default format aligned failed");

        sb.setLength(0);
        text += ".\nNow is the time for all good people to come to the aid of their country.";
        expected.clear();
        expected.add(" The quick brown fox jumps over the lazy dog.");
        expected.add("    Now is the time for all good people to come to the aid of their");
        expected.add("    country.");
        underTest.printWrapped(text);
        actual = IOUtils.readLines(new StringReader(sb.toString()));
        assertEquals(expected, actual, "default format aligned failed");
    }

    @Test
    public void testAppend() throws IOException {
        char c = (char) 0x1F44D;
        underTest.append(c);
        assertEquals(1, sb.length());
        assertEquals(String.valueOf(c), sb.toString());

        sb.setLength(0);
        underTest.append("Hello");
        assertEquals("Hello", sb.toString());
    }

    @Test
    public void testAdjustTableFormat() {
        // test width smaller than header
        TableDefinition tableDefinition = TableDefinition.from("Testing",
                Arrays.asList(new TextStyle.Builder().setMaxWidth(3).get()),
                Arrays.asList("header"),
                // "data" shorter than "header"
                Arrays.asList(Arrays.asList("data"))
        );
        TableDefinition actual = underTest.adjustTableFormat(tableDefinition);
        assertEquals("header".length(), actual.columnStyle().get(0).getMaxWidth());
        assertEquals("header".length(), actual.columnStyle().get(0).getMinWidth());
    }

    @Test
    public void testSetIndent() {
        assertEquals(TextHelpWriter.DEFAULT_INDENT, underTest.getIndent(), "Default indent value was changed, some tests may fail");
        underTest.setIndent(TextHelpWriter.DEFAULT_INDENT + 2);
        assertEquals(underTest.getIndent(), TextHelpWriter.DEFAULT_INDENT + 2);
    }

    @Test
    public void testGetStyleBuilder() {
        TextStyle.Builder builder = underTest.getStyleBuilder();
        assertEquals(TextHelpWriter.DEFAULT_INDENT, builder.getIndent(), "Default indent value was changed, some tests may fail");
        assertEquals(TextHelpWriter.DEFAULT_LEFT_PAD, builder.getLeftPad(), "Default left pad value was changed, some tests may fail");
        assertEquals(TextHelpWriter.DEFAULT_WIDTH, builder.getMaxWidth(),  "Default width value was changed, some tests may fail");
    }
}