/*
 *  Copyright 2020 Xiaomi
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.xiaomi.youpin.tesla.ip.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;

/**
 * @author goodjava@qq.com
 */
public class UltramanAction extends AnAction {


    @Override
    public void actionPerformed(AnActionEvent e) {
        //获取当前操作的类文件
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        //获取当前类文件的路径
        String classPath = "";
        if (null != psiFile) {
            classPath = psiFile.getVirtualFile().getPath();
        }
        String title = "";

        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (null != editor) {
            Document document = editor.getDocument();
            if (null != document) {
                Application applicationManager = ApplicationManager.getApplication();
                applicationManager.runWriteAction(() -> {
                    document.setText(title);
                });

            }
        }

    }
}
