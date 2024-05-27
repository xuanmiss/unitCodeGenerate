package com.miss.unitcodegenerate

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.ui.Messages
import com.miss.unitcodegenerate.service.UnitCodeGenerateService
import io.ktor.client.*
import io.ktor.client.request.*

class GenerateUnitTestAction : AnAction() {

    private val unitCodeGenerateService = UnitCodeGenerateService()

    override fun actionPerformed(e: AnActionEvent) {
//        val project = e.getData(PlatformDataKeys.VIRTUAL_FILE)
        val file = e.getData(PlatformDataKeys.VIRTUAL_FILE)

        val filePath = e.getData(PlatformDataKeys.VIRTUAL_FILE)?.path
        val fileType = e.getData(PlatformDataKeys.VIRTUAL_FILE)?.fileType
        if (file?.isDirectory == true) {
            Messages.showMessageDialog("暂不支持文件夹直接生成，请使用单个Java文件", "UnitCodeGenerate", Messages.getInformationIcon())
        }else if(fileType?.name == "JAVA") {
            unitCodeGenerateService.generateUnitTestCode(filePath!!)
            Messages.showMessageDialog("Hello ~~~!!!$filePath, fileType is $fileType", "UnitCodeGenerate", Messages.getInformationIcon())
        }else {
            Messages.showMessageDialog("当前仅支持Java类型的文件生成单元测试~~~", "UnitCodeGenerate", Messages.getInformationIcon())
        }

    }
}
