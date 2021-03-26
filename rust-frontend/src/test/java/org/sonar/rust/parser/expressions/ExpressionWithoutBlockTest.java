/**
 *
 * Sonar Rust Plugin (Community)
 * Copyright (C) 2020 Eric Le Goff
 * http://github.com/elegoff/sonar-rust
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.rust.parser.expressions;

import org.junit.Test;
import org.sonar.rust.RustGrammar;

import static org.sonar.sslr.tests.Assertions.assertThat;

public class ExpressionWithoutBlockTest {
    @Test
    public void testExpressionWithoutBlock() {
        assertThat(RustGrammar.create().build().rule(RustGrammar.EXPRESSION_WITHOUT_BLOCK))
                .notMatches("== b")
                //TODO Litteral Expression
                //TODO Path expression
                //Operator expressdions
                //borrow expr
                .matches("&7")
                .matches("&mut array")
                .matches("&& 10")
                .matches("& & 10")
                .matches("&&&& mut 10")
                .matches("&& && mut 10")
                .matches("& & & & mut 10")
                //deref
                .matches("*thing")
                //err propagation
                .matches("foo?")
                .matches("None?")
                .matches("Some(42)?")
                //negation expression
                .matches("!foo")
                .matches("-5")
                .matches("-bar")
                .notMatches("== b")
                //arith expr
                .matches("1<<0")
                .matches("1+1")
                .matches("1+1+1+1")
                .matches("3*2")
                .matches("3*2*8")
                .matches("22/7")
                .matches("2^4")
                .matches("1-3")
                .matches("1-3-2")
                .matches("22 % 7")
                .matches("m.get(i) + 1")
                //comparisons
                .matches("a == b")
                .matches("small < big")
                //lazy bool
                .matches("false || true")
                //type cast
                .matches("value as f64")
                //assignment
                .matches("value = 42")
                //compound assignment
                .matches("counter += 1")


                //TODO grouped expression
                //TODO   ArrayExpression
                //TODO   AwaitExpression
                //TODO   IndexExpression
                //TODO   TupleExpression
                //TODO   TupleIndexingExpression
                //TODO   StructExpression
                //TODO   EnumerationVariantExpression
                //  CallExpression


                .matches("foo()")
                .matches("abc()")
                .matches("add(1i32,2i32)")
                //MethodCallExpression
                .matches("\"123\".parse()")
                .matches("node_fetch::create_http_client(user_agent.clone(), my_data.clone()).unwrap()")
                //FieldExpression
                .matches("other.major")
                //TODO ClosureExpression
                //TODO ContinueExpression
                //TODO BreakExpression
                //TODO RangeExpression
                //TODO ReturnExpression
                //MacroInvocation
                .matches("panic!()")
                .matches("println!(\"{}, {}\", word, j)")


                .matches("calc(get(i) + 1)")
                .matches("Numeric(n)")
                .matches("Vec::new")
                .matches("Identifier::Numeric")
                .matches("return None")







        ;
    }
}
