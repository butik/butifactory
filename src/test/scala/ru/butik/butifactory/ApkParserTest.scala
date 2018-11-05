package ru.butik.butifactory

import net.dongliu.apk.parser.ApkFile

import org.scalatest.FunSpec

class ApkParserTest extends FunSpec {

  it("should parse apk file") {
    val apkFile = new ApkFile(this.getClass.getClassLoader.getResource("apk/app-release.apk").getFile)
    try {
      val apkMeta = apkFile.getApkMeta
      assert(apkMeta.getPackageName === "ru.butik.fitassist")
      assert(apkMeta.getName === "Fit-Assist")
      assert(apkMeta.getVersionCode === 6)
      assert(apkMeta.getVersionName === "1.1.3")
    } finally {
      if (apkFile != null) {
        apkFile.close()
      }
    }
  }
}
