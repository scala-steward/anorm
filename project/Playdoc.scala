/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

import sbt._
import sbt.Keys._
import sbt.io.IO

import xsbti.HashedVirtualFileRef

object Playdoc extends AutoPlugin {

  object autoImport {
    final val Docs = config("docs")

    val playdocDirectory = settingKey[File]("Base directory of play documentation")

    val playdocPackage = taskKey[HashedVirtualFileRef]("Package play documentation")
  }

  import autoImport.*

  override def requires = sbt.plugins.JvmPlugin

  override def trigger = noTrigger

  override def projectSettings =
    Defaults.packageTaskSettings(playdocPackage, playdocPackage / mappings) ++
      Seq(
        playdocDirectory          := (ThisBuild / baseDirectory).value / "docs" / "manual",
        playdocPackage / mappings := {
          val base      = playdocDirectory.value
          val converter = fileConverter.value

          base.allPaths.get().flatMap { f =>
            IO.relativize(base.getParentFile(), f).map { p =>
              converter.toVirtualFile(f.toPath) -> p
            }
          }
        },
        playdocPackage / artifactClassifier := Some("playdoc"),
        playdocPackage / artifact ~= { _.withConfigurations(Vector(Docs)) }
      ) ++
      addArtifact(playdocPackage / artifact, playdocPackage)
}
