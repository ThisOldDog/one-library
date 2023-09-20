package pers.dog;

import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import pers.dog.infra.control.MarkdownCodeArea;

class OneLibraryApplicationTest {
    @Test
    public void testTreeStyleSpansBuilder() {
        MarkdownCodeArea.TreeStyleSpansBuilder treeStyleSpansBuilder = new MarkdownCodeArea.TreeStyleSpansBuilder(0, 100);
        treeStyleSpansBuilder.addChild(0, 10, Collections.singletonList("1"));
        treeStyleSpansBuilder.addChild(1, 5, Collections.singletonList("2"));
        treeStyleSpansBuilder.addChild(4, 29, Collections.singletonList("3"));
        treeStyleSpansBuilder.addChild(15, 80, Collections.singletonList("4"));
    }

//    static Pattern PATTERN = Pattern.compile("(?<HEADER>^#{1,6} [ \\t\\S]*)|(?<BLOCKQUOTE>^>{1,3} [ \\t\\S]*)|(?<UNORDEREDLIS>^[ \\t>]*[-+*] [ \\t\\S]*)|(?<ORDEREDLIS>^[ \\t>]*\\d+\\. [ \\t\\S]*)|(?<LINEBREAK><br>)|(?<LINKIMAGE>!?\\[.*\\]\\(.*\\))|(?<CODEINLINE>`[ \\t\\S]*`)|(?<CODEFENCE>^[ \\t]*```[\\s\\S]*?```$)|(?<STRONG>(\\*{1,3}.+\\*{1,3})|(_{1,3}.+_{1,3})|-{1,3}.+-{1,3})");
    static Pattern PATTERN = Pattern.compile("[ \\t\\n]*```[\\s\\S^$]*?```$", Pattern.MULTILINE);
    static String TEXT = "# 标题\n" +
            "## 标题\n" +
            "### 标题\n" +
            "#### 标题\n" +
            "##### 标题\n" +
            "###### 标题" +
            "```java\n" +
            "代码块\n" +
            "```";

    @Test
    public void testPattern() {
        Matcher matcher = PATTERN.matcher(TEXT);
        while (matcher.find()) {
            System.out.printf("[%d - %d] %s%n", matcher.start(), matcher.end(), TEXT.substring(matcher.start(), matcher.end()));
        }
    }
}