package ru.d10xa.jadd

case class ArtifactInfo(
  groupId:String,
  artifactId: String,
  scope: Option[String]
)
