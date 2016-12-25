package models.form

import org.scalatest.FunSuite
import play.api.data.validation.{Invalid, Valid, ValidationError}

class ConstraintHelperSuite extends FunSuite {
  test("passed") {
    val passed = ConstraintHelper.passed("app.error.passed")
    assert(Valid === passed(true))
    passed(false) match {
      case Invalid(x::xs) => assert(Seq("app.error.passed") === x.messages)
      case _ => fail()
    }
  }

  test("nonBlank") {
    val nonBlank = ConstraintHelper.nonBlank()

    assert(Valid !== nonBlank(null))

    Seq(
      ("\u0009", "HORIZONTAL TABULATION"),
      ("\u000A", "LINE FEED"),
      ("\u000B", "VERTICAL TABULATION"),
      ("\u000C", "FORM FEED"),
      ("\u000D", "CARRIAGE RETURN"),
      ("\u001C", "FILE SEPARATOR"),
      ("\u001D", "GROUP SEPARATOR"),
      ("\u001E", "RECORD SEPARATOR"),
      ("\u001F", "UNIT SEPARATOR"),
      ("\u0020", "SPACE"),
      //("\u00A0", "NO-BREAK SPACE"),
      ("\u1680", "OGHAM SPACE MARK"),
      ("\u180E", "MONGOLIAN VOWEL SEPARATOR"),
      ("\u2000", "EN QUAD"),
      ("\u2001", "EM QUAD"),
      ("\u2002", "EN SPACE"),
      ("\u2003", "EM SPACE"),
      ("\u2004", "THREE-PER-EM SPACE"),
      ("\u2005", "FOUR-PER-EM SPACE"),
      ("\u2006", "SIX-PER-EM SPACE"),
      //("\u2007", "FIGURE SPACE"),
      ("\u2008", "PUNCTUATION SPACE"),
      ("\u2009", "THIN SPACE"),
      ("\u200A", "HAIR SPACE"),
      //("\u200B", "ZERO WIDTH SPACE"),
      //("\u202F", "NARROW NO-BREAK SPACE"),
      ("\u205F", "MEDIUM MATHEMATICAL SPACE"),
      ("\u3000", "IDEOGRAPHIC SPACE")
      //("\uFEFF", "ZERO WIDTH NO-BREAK SPACE")
    ).foreach { space =>
      nonBlank(space._1) match {
        case Invalid(_) =>
        case _ => fail(space._2 + " must be detected as a blank character")
      }
    }
  }

  test("sameValue") {
    val sameValue = ConstraintHelper.sameValue()
    assert(Valid === sameValue((null, null)))
    assert(Valid !== sameValue((null, "")))
    assert(Valid !== sameValue(("", null)))
    assert(Valid === sameValue((false, false)))
    assert(Valid === sameValue((1, 1)))
    assert(Valid === sameValue((123L, 123L)))
    assert(Valid === sameValue(("", "")))
    assert(Valid === sameValue((None, None)))
    assert(Valid === sameValue((Some("foo"), Some("foo"))))
    assert(Valid === sameValue((Nil, Nil)))
    assert(Valid === sameValue((List.empty, Seq.empty)))
  }

  test("email") {
    val email = ConstraintHelper.email()
    assert(Valid === email("root@localhost"))
    assert(Valid === email("user@example.net"))
    assert(Valid === email("-u.s.e.r-@example.net"))
  }

  test("password") {
    val password = ConstraintHelper.password()
    assert(Valid === password(new String((0x21.to(0x7e)).map(_.toChar).toArray)))
    assert(Valid !== password(new String((0x20.to(0x7e)).map(_.toChar).toArray)))
    assert(Valid !== password(new String((0x21.to(0x7f)).map(_.toChar).toArray)))
  }
}
