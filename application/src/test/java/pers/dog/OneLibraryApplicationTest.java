package pers.dog;

import java.util.Collections;

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
}