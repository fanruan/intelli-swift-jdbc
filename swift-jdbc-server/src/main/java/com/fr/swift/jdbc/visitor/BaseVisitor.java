package com.fr.swift.jdbc.visitor;

import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.antlr.v4.runtime.tree.ErrorNode;

/**
 * @author yee
 * @date 2019-07-26
 */
public class BaseVisitor<T> extends AbstractParseTreeVisitor<T> {
    @Override
    public T visitErrorNode(ErrorNode node) {
        throw new RuntimeException(node.getText());
    }
}
