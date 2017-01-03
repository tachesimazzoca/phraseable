package models

import org.scalatest.FunSuite

class PhraseServiceSuite extends FunSuite {
  test("parseKeywords") {
    assert(Seq("create", "produce", "generate",
      "yield", "make", "build", "construct", "foo") === PhraseService.parseKeywords(
      "* { create | produce | generate | yield } something = { make } something\n" +
      "* {build, construct} something = {foo}"))
  }
}
