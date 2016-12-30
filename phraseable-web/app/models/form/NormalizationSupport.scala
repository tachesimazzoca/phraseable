package models.form

import org.apache.commons.lang3.StringUtils

trait NormalizationSupport {

  val nonBlankFields: Seq[String] = Seq.empty

  final def normalize(implicit request: play.api.mvc.Request[_]): Map[String, Seq[String]] = {
    normalize(request.body match {
      case body: play.api.mvc.AnyContent if body.asFormUrlEncoded.isDefined => body.asFormUrlEncoded.get
      case _ => Map.empty[String, Seq[String]]
    }) ++ request.queryString
  }

  final def normalize(data: Map[String, Seq[String]]): Map[String, Seq[String]] = {
    // Trim white spaces
    nonBlankFields.foldLeft(data) { (acc, x) =>
      if (acc.contains(x))
        acc.updated(x, data(x).map(StringUtils.stripToEmpty))
      else
        acc
    }
  }
}
