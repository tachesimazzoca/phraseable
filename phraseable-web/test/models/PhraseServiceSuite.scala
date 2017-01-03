package models

import org.scalatest.FunSuite

class PhraseServiceSuite extends FunSuite {
  test("parseKeywords") {
    assert(Seq("create", "produce", "generate",
      "yield", "make") === PhraseService.parseKeywords(
      "en: { create | produce | generate | yield } is to { make } something."))
  }
}
