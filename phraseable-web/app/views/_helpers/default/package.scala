package views.html._helpers

import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import views.html.helper.{FieldConstructor, FieldElements}

package object default {
  implicit val field = new FieldConstructor {
    def apply(elts: FieldElements) = defaultFieldConstructor(elts)
  }
}

package object markdown {
  private val markdownParser = Parser.builder().build()
  private val htmlRenderer = HtmlRenderer.builder().build()
  def md2html(input: String): String =
    htmlRenderer.render(markdownParser.parse(input))
}
