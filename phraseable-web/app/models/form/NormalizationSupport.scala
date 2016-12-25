package models.form

trait NormalizationSupport {

  def normalizeRequest(implicit request: play.api.mvc.Request[_]): Map[String, Seq[String]] = {
    normalize (request.body match {
      case body: play.api.mvc.AnyContent if body.asFormUrlEncoded.isDefined => body.asFormUrlEncoded.get
      case _ => Map.empty[String, Seq[String]]
    }) ++ request.queryString
  }

  def normalize(data: Map[String, Seq[String]]): Map[String, Seq[String]]
}
