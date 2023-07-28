import scala.io.Source

import $ivy.`io.circe::circe-parser:0.13.0`
import $ivy.`com.github.pathikrit::better-files:3.9.1`
import io.circe._
import io.circe.parser._
import better.files.{File => BFile}

@main
def main(path: os.Path): Unit = {

  val files = BFile(path.toString)
    .glob("**/*.json")
    .toList
    .filter(
      _.path.toAbsolutePath().toString().contains("src/test/resources/wiremock")
    )

  files.foreach(println)

  files.foreach(file =>
    parse(file.contentAsString).foreach(json =>
      json.hcursor
        .downField("response")
        .downField("headers")
        .withFocus(j =>
          j.mapObject(jo => jo.filter { case (k, j) => k == "Content-Type" })
        )
        .top
        .flatMap(j => j.hcursor.downField("uuid").delete.top)
        .flatMap(j => j.hcursor.downField("id").delete.top)
        .foreach(newJson => file.write(newJson.spaces2))
    )
  )
}
