package org.sonar.rust.model.expression;

import org.sonar.plugins.rust.api.tree.LiteralTree;
import org.sonar.plugins.rust.api.tree.Tree;
import org.sonar.plugins.rust.api.tree.TreeVisitor;
import org.sonar.rust.model.AbstractTypedTree;
import org.sonar.rust.model.InternalSyntaxToken;
import org.sonar.rust.tree.SyntaxToken;

import java.util.Collections;
import java.util.Objects;

public class LiteralTreeImpl extends AbstractTypedTree implements LiteralTree {

    private final Kind kind;
    private final InternalSyntaxToken token;

    public LiteralTreeImpl(Kind kind, InternalSyntaxToken token) {
        super(kind);
        this.kind = Objects.requireNonNull(kind);
        this.token = token;
    }

    @Override
    public Kind kind() {
        return kind;
    }

    @Override
    public String value() {
        return token.text();
    }

    @Override
    public SyntaxToken token() {
        return token;
    }

    @Override
    public void accept(TreeVisitor visitor) {
        visitor.visitLiteral(this);
    }

    @Override
    public Iterable<Tree> children() {
        return Collections.<Tree>singletonList(token);
    }

}