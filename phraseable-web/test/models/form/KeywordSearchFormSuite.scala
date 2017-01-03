package models.form

import org.scalatest.FunSuite

class KeywordSearchFormSuite extends FunSuite {

  test("parseSearchQuery") {
    assert(Seq("fuga qux", "foo", "bar", "baz") ===
      KeywordSearchForm.parseSearchQuery(""" foo  bar " fuga  qux " baz  """))
  }

  test("convertToSearchQuery") {
    assert(""""fuga qux" foo bar baz""" ===
      KeywordSearchForm.convertToSearchQuery(Seq("fuga qux", "foo", "bar", "baz")))
  }
}
