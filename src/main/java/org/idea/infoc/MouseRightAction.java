package org.idea.infoc;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilBase;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.sun.istack.Nullable;

import javax.lang.model.element.Modifier;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class MouseRightAction extends AnAction {
    String dir;

    @Override
    public void actionPerformed(AnActionEvent event) {

        VirtualFile virtualFile = event.getData(CommonDataKeys.VIRTUAL_FILE);
        if (virtualFile != null) {
            if (!virtualFile.isDirectory()) {
                virtualFile = virtualFile.getParent();
            }
        }
        if (virtualFile == null) {
            showError(event, "获取不到文件");
            return;
        }
        dir = virtualFile.getPath();
//        if (!path.contains("/src/") && !path.contains("/java/") && !path.contains("/kotlin/")) {
//            showError(event, "找不到类路径，文件路径中必须包含src或者java");
//            return;
//        }
        createFrame();

    }

    private void createFrame() {
        // 创建 JFrame 实例
        JFrame frame = new JFrame("生成infoc类");
        // Setting the width and height of frame
        frame.setSize(500, 220);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Dimension displaySize = Toolkit.getDefaultToolkit().getScreenSize(); // 获得显示器大小对象
        Dimension frameSize = frame.getSize();             // 获得窗口大小对象
        if (frameSize.width > displaySize.width)
            frameSize.width = displaySize.width;           // 窗口的宽度不能大于显示器的宽度

        frame.setLocation((displaySize.width - frameSize.width) / 2,
                (displaySize.height - frameSize.height) / 2); // 设置窗口居中显示器显示



        /* 创建面板，这个类似于 HTML 的 div 标签
         * 我们可以创建多个面板并在 JFrame 中指定位置
         * 面板中我们可以添加文本字段，按钮及其他组件。
         */
        JPanel panel = new JPanel();
        // 添加面板
        frame.add(panel);
        /*
         * 调用用户定义的方法并添加组件到面板
         */
        placeComponents(panel);

        // 设置界面可见
        frame.setVisible(true);
    }


    private void placeComponents(JPanel panel) {

        /* 布局部分我们这边不多做介绍
         * 这边设置布局为 null
         */

        String packageName = getPkgName(dir);


        panel.setLayout(null);
        // 1
        JLabel pathLabel = new JLabel("路径：");
        pathLabel.setBounds(10, 20, 80, 25);
        panel.add(pathLabel);

        JTextField pathJText = new JTextField(20);
        pathJText.setBounds(100, 20, 350, 25);
        if (dir != null) {
            pathJText.setText(dir);
        }
        panel.add(pathJText);
        //2
        JLabel packageLabel = new JLabel("包名：");
        packageLabel.setBounds(10, 50, 80, 25);
        panel.add(packageLabel);

        JTextField packageJText = new JTextField(20);
        packageJText.setBounds(100, 50, 350, 25);
        if (packageName != null) {
            packageJText.setText(packageName);
        }
        panel.add(packageJText);

        //3
        JLabel passwordLabel = new JLabel("配置:");
        passwordLabel.setBounds(10, 80, 80, 25);
        panel.add(passwordLabel);

        JTextField infocJText = new JTextField(20);
        infocJText.setBounds(100, 80, 350, 25);
        panel.add(infocJText);

        // 创建登录按钮
        JButton loginButton = new JButton("生成");
        loginButton.setBounds(10, 110, 80, 25);
        panel.add(loginButton);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                //生成类
                loginButton.setEnabled(false);
                generateClass(pathJText.getText(), packageJText.getText(), infocJText.getText());


            }
        });

    }

    private void generateClass(String dir, String pkgName, String infocConfig) {
        MethodSpec main = MethodSpec.methodBuilder("main")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addParameter(String[].class, "args")
                .addStatement("$T.out.println($S)", System.class, "Hello, JavaPoet!")
                .build();

        TypeSpec helloWorld = TypeSpec.classBuilder(infocConfig)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(main)
                .build();

        JavaFile javaFile = JavaFile.builder(pkgName, helloWorld)
                .build();

        try {
            javaFile.writeToFile(new File(dir));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private String getPkgName(String path) {
        if (path.contains("/src/")) {
            int length = "/src/".length();
            int i = path.lastIndexOf("/src/");
            dir = path.substring(0, i + length);
            return path.substring(i + length).replace("/", ".");
        } else if (path.contains("/java/")) {
            int length = "/java/".length();
            int i = path.lastIndexOf("/java/");
            dir = path.substring(0, i + length);
            return path.substring(i + length).replace("/", ".");
        }
        return null;
    }

    private void showError(AnActionEvent event, String msg) {
        Messages.showMessageDialog(event.getProject(), msg, "错误信息", Messages.getInformationIcon());
    }
}
