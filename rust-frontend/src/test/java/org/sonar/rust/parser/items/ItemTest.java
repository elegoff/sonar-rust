package org.sonar.rust.parser.items;

import org.junit.Test;
import org.sonar.rust.RustGrammar;

import static org.sonar.sslr.tests.Assertions.assertThat;

public class ItemTest {



        /*

Item:
   OuterAttribute*
      VisItem
   | MacroItem

VisItem:
   Visibility?
   (
         Module
      | ExternCrate
      | UseDeclaration
      | Function
      | TypeAlias
      | Struct
      | Enumeration
      | Union
      | ConstantItem
      | StaticItem
      | Trait
      | Implementation
      | ExternBlock
   )

MacroItem:
      MacroInvocationSemi
   | MacroRulesDefinition
         */


    @Test
    public void VisItem() {

        assertThat(RustGrammar.create().build().rule(RustGrammar.VISIT_ITEM))
                .matches("")
        ;
    }

    @Test
    public void MacroItem() {

        assertThat(RustGrammar.create().build().rule(RustGrammar.MACRO_ITEM))
                .matches("")
        ;
    }


        @Test
        public void testItem() {
            assertThat(RustGrammar.create().build().rule(RustGrammar.ITEM))
                    .matches("mod foo ;") //module item
                    .matches("extern crate pcre;") //extern crate item
                    .matches("use std::collections::hash_map::{self, HashMap};") //use item
                    .matches("fn answer_to_life_the_universe_and_everything() -> i32 {\n" +
                            "    let y=42;\n" +
                            "}")//function item
                    .matches("type Point = (u8, u8);") //type alias
                    .matches("struct Point {x:i32, y: i32}") //struct
                    .matches("enum Animal {\n" +
                            "    Dog,\n" +
                            "    Cat,\n" +
                            "}") //enumeration
                    .matches("union MyUnion {\n" +
                            "    f1: u32,\n" +
                            "    f2: f32,\n" +
                            "}") //union
                    .matches("const STRING: &'static str = \"bitstring\";")//const
                    .matches("static mut LEVELS: u32 = 0;") //static
                    .matches("trait Seq<T> {\n" +
                            "    fn len(&self) -> u32;\n" +
                            "    fn elt_at(&self, n: u32) -> T;\n" +
                            "    fn iter<F>(&self, f: F) where F: Fn(T);\n" +
                            "}") //trait
                    //implementation
                    .matches("impl <U> Color where 'a : 'b +'c +'d {#![inner] fn by_ref(self: &Self) {}}")
            ;
        }


}
