package org.sonar.rust.parser.items;

import org.junit.Test;
import org.sonar.rust.RustGrammar;

import static org.sonar.sslr.tests.Assertions.assertThat;

public class LifeTimeTest {


    @Test
    public void testForLifetimes(){
        assertThat(RustGrammar.create().build().rule(RustGrammar.FOR_LIFETIMES))
                .matches("for <'a>")
                .matches("for <'ABC>")
                .matches("for <'a>")
                .matches("for <'ABC>")
               //FIXME .notMatches("for <'as>") //no keyword allowed
                //FIXME .notMatches("for<'trait>") //no keyword allowed
        ;

    }

    @Test
    public void testTypeBoundWhereClauseItem(){
        assertThat(RustGrammar.create().build().rule(RustGrammar.TYPE_BOUND_CLAUSE_ITEM))
                .matches("i32 :")
                .matches("for <'ABC> i32 :")
                .matches("for <'ABC> i32 : 'a")
                .matches("for <'ABC> i32 : 'a + 'b +'c")
                ;
    }

    @Test
    public void testLifetimeWhereClauseItem(){
        assertThat(RustGrammar.create().build().rule(RustGrammar.LIFETIME_WHERE_CLAUSE_ITEM))
                .matches("'a:'b+'c+'d")
                .matches("'a : 'b+'c+'d")
                .matches("'ABC : 'b+ 'c+ 'd")

        ;
    }



    @Test
    public void testWhereClauseItem(){
        assertThat(RustGrammar.create().build().rule(RustGrammar.WHERE_CLAUSE_ITEM))
                //type bound clause item
                .matches("i32 :")
                .matches("for <'ABC> i32 :")
                .matches("for <'ABC> i32 : 'a")
                .matches("for <'ABC> i32 : 'a + 'b +'c")
                //LifetimeWhereClauseItem
                .matches("'a:'b+'c+'d")
                .matches("'a : 'b+'c+'d")
                .matches("'ABC : 'b+ 'c+ 'd")
        ;
    }

    @Test
    public void testWhereClause(){
        assertThat(RustGrammar.create().build().rule(RustGrammar.WHERE_CLAUSE))
                .matches("where i32 :")
                .matches("where i32 :,f64:")
                .matches("where for <'ABC> i32 :, i32:")
                .matches("where for <'ABC> i32 : 'a")
                .matches("where for <'ABC> i32 : 'a + 'b +'c")
                .matches("where 'a:'b+'c+'d")
                .matches("where 'a : 'b +'c +'d")
                .matches("where 'ABC : 'a+ 'b+ 'c, 'DEF : 'd + 'e + 'f")
                .notMatches("where 'a : 'b +'c +'d {}")
        ;
    }



    @Test
    public void testLifetime(){
        assertThat(RustGrammar.create().build().rule(RustGrammar.LIFETIME))
                .matches("'a")
                .matches("'ABC")
                .notMatches("'trait") //no keyword allowed
                .matches("'static")
                .matches("'_")
                .notMatches("'a self")
                ;

    }

    @Test
    public void testTraitBound(){
        assertThat(RustGrammar.create().build().rule(RustGrammar.TRAIT_BOUND))
                .matches("abc::")
                .matches("? abc::")
                .matches("for <'a> abc::")
                .matches("? for <'a> abc::")
                .matches("(abc::)")
                .matches("(? abc::)")
                .matches("( for <'a> abc:: )")
                .matches("(? for <'a> abc::)")

        ;

    }

    @Test
    public void testTypeParamBound(){
        assertThat(RustGrammar.create().build().rule(RustGrammar.TYPE_PARAM_BOUND))
                //lifetime
                .matches("'a")
                .matches("'ABC")
                .notMatches("'trait") //no keyword allowed
                .matches("'static")
                .matches("'_")
                .notMatches("'a self")
                //trait bound
                .matches("abc::")
                .matches("? abc::")
                .matches("for <'a> abc::")
                .matches("? for <'a> abc::")
                .matches("(abc::)")
                .matches("(? abc::)")
                .matches("( for <'a> abc:: )")
                .matches("(? for <'a> abc::)")
        ;

    }

    @Test
    public void testTypeParamBounds(){
        assertThat(RustGrammar.create().build().rule(RustGrammar.TYPE_PARAM_BOUNDS))
                .matches("'a")
                .matches("'a+'a")
                .matches("'a + 'b + 'c")
                .matches("'ABC")
                .matches("'ABC+'DCE")
                .notMatches("'trait") //no keyword allowed
                .matches("'static")
                .matches("'_")
                .matches("'_+'a")
                .matches("'_+'a+'ABC")
                .notMatches("'a self")
                .matches("abc::")
                .matches("abc::+def::")
                .matches("(abc::)+(def::)")
                .matches("(? for <'a> abc::)+'a+abc::")
        ;

    }

    @Test
    public void testTypeParam(){
        assertThat(RustGrammar.create().build().rule(RustGrammar.TYPE_PARAM))
                .matches("AAA")
                .matches("#[test] AAA")
                .matches("#[test] AAA : i32")
                .matches("T")
        ;
    }

    @Test
    public void testTypeParams(){
        assertThat(RustGrammar.create().build().rule(RustGrammar.TYPE_PARAMS))
                .matches("")
                .matches("AAA")
                .matches("AAA,BBB,CCC")
                .matches("#[test] AAA")
                .matches("#[test] AAA, BBB")
                .matches("#[test] AAA : i32, BBB : f64, CCC : bool")
                .matches("T")
        ;
    }

    @Test
    public void testLifetimeParam(){
        assertThat(RustGrammar.create().build().rule(RustGrammar.LIFETIME_PARAM))
                .matches("'a")
                .matches("'ABC")
                .notMatches("'trait")
                .notMatches("'as")
                .notMatches("'as>")
                .notMatches("<'as")
                .notMatches("<'as>")
                .notMatches("'trait>")
                .matches("#[outer]'ABC")
        ;
    }

    @Test
    public void testLifetimeParams(){
        assertThat(RustGrammar.create().build().rule(RustGrammar.LIFETIME_PARAMS))
                .matches("'a")
                .matches("'a,'b")
                .matches("'ABC")
                .matches("'ABC , 'DEF")
                .notMatches("'trait")
                .notMatches("'as")
                .notMatches("'as>")
                .notMatches("<'as")
                .notMatches("<'as>")
                .matches("#[outer]'ABC")
        ;
    }


    @Test
    public void testLifetimeBounds(){
        assertThat(RustGrammar.create().build().rule(RustGrammar.LIFETIME_BOUNDS))
                .matches("'a+'b+'c")
                .matches("'a + 'b + 'c")
                .matches("'BCD")
                ;
    }

    @Test
    public void testGenericParams(){
        assertThat(RustGrammar.create().build().rule(RustGrammar.GENERIC_PARAMS))

                .matches("T")
                .matches("'ABC,U")
                .matches("'a,T")
                .notMatches("'trait")
                .notMatches("'trait>")
                .matches("#[outer]'ABC,V")

        ;
    }


    @Test
    public void testGenerics() {
        assertThat(RustGrammar.create().build().rule(RustGrammar.GENERICS))
                .matches("<T>")
                .matches("<'a,T>")



        ;

    }
}
