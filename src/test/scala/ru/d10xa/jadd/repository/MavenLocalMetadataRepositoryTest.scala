package ru.d10xa.jadd.repository

import org.scalatest.FunSuiteLike
import org.scalatest.Matchers
import ru.d10xa.jadd.Artifact
import ru.d10xa.jadd.troubles

class MavenLocalMetadataRepositoryTest extends FunSuiteLike with Matchers {

  test("m2 custom artifact") {

    val api = new MavenLocalMetadataRepositoryApi("src/test/resources/m2/repository/")

    val result: Either[troubles.MetadataLoadTrouble, MavenMetadata] =
      api.receiveRepositoryMeta(Artifact("com.example:projectname"))

    val meta = result.right.get
    meta.versions shouldEqual Seq("2.5", "12.5")
    meta.lastUpdated shouldEqual Some("20180604200622")
    meta.repository shouldEqual Some("src/test/resources/m2/repository/")
    val userDir = System.getProperty("user.dir")
    meta.url.get shouldEqual
      s"$userDir/src/test/resources/m2/repository/com/example/projectname/maven-metadata-local.xml"
  }

}
