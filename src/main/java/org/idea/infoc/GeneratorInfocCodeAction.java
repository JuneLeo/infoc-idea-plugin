package org.idea.infoc;

import com.intellij.ide.IdeBundle;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import javax.swing.*;
import javax.swing.event.ListDataListener;
import javax.swing.plaf.ComboBoxUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class GeneratorInfocCodeAction extends AnAction {

    String dir;
    AnActionEvent event;
    VirtualFile virtualFile;


    InfocParentClass[] parents = {new InfocParentClass() {
        @Override
        public String getName() {
            return "com.cleanmaster.pluginscommonlib.supportsdk.CommonSupportReportBase";
        }

        @Override
        public String getSimpleName() {
            return "CommonSupportReportBase";
        }

        @Override
        public String getPkgName() {
            return "com.cleanmaster.pluginscommonlib.supportsdk";
        }
    }, new InfocParentClass() {
        @Override
        public String getName() {
            return "com.cleanmaster.hpsharelib.report.BaseTracer";
        }

        @Override
        public String getSimpleName() {
            return "BaseTracer";
        }

        @Override
        public String getPkgName() {
            return "com.cleanmaster.hpsharelib.report";
        }
    }};


    @Override
    public void actionPerformed(AnActionEvent event) {
        this.event = event;
        virtualFile = event.getData(CommonDataKeys.VIRTUAL_FILE);
        if (virtualFile != null) {
            if (!virtualFile.isDirectory()) {
                virtualFile = virtualFile.getParent();
            }
        }
        if (virtualFile == null) {
            showError(event, "not found file");
            return;
        }
        dir = virtualFile.getPath();
        createFrame();
    }

    private void refresh() {
        Runnable command = new Runnable() {
            @Override
            public void run() {
                ApplicationManager.getApplication().runWriteAction(
                        new Runnable() {
                            @Override
                            public void run() {
                                if (!event.getProject().isDisposed()) {
                                    virtualFile.refresh(false, false);
                                    PsiManager.getInstance(event.getProject()).reloadFromDisk(PsiManager.getInstance(event.getProject()).findFile(virtualFile));
                                }
                            }
                        }
                );
            }
        };
        CommandProcessor.getInstance().executeCommand(event.getProject(), command, IdeBundle.message("command.reload.from.disk"), null);
    }

    private void createFrame() {
        JFrame frame = new JFrame("Auto Generator Infoc Class");
        // Setting the width and height of frame
        frame.setSize(500, 220);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Dimension displaySize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = frame.getSize();
        if (frameSize.width > displaySize.width)
            frameSize.width = displaySize.width;

        frame.setLocation((displaySize.width - frameSize.width) / 2,
                (displaySize.height - frameSize.height) / 2);
        JPanel panel = new JPanel();
        frame.add(panel);
        panel.setLayout(null);

        String packageName = getPkgName(dir);

        // 1
        JLabel pathLabel = new JLabel("Path:");
        pathLabel.setBounds(10, 20, 80, 25);
        panel.add(pathLabel);

        JTextField pathJText = new JTextField(20);
        pathJText.setBounds(100, 20, 350, 25);
        if (dir != null) {
            pathJText.setText(dir);
        }
        panel.add(pathJText);
        //2
        JLabel packageLabel = new JLabel("Package:");
        packageLabel.setBounds(10, 50, 80, 25);
        panel.add(packageLabel);

        JTextField packageJText = new JTextField(20);
        packageJText.setBounds(100, 50, 350, 25);
        if (packageName != null) {
            packageJText.setText(packageName);
        }
        panel.add(packageJText);

        //3
        JLabel configLabel = new JLabel("Config:");
        configLabel.setBounds(10, 80, 80, 25);
        panel.add(configLabel);

        JTextField infocJText = new JTextField(20);
        infocJText.setBounds(100, 80, 350, 25);
        panel.add(infocJText);


        //3
        JLabel parentLabel = new JLabel("Parent:");
        parentLabel.setBounds(10, 110, 80, 25);
        panel.add(parentLabel);


        JComboBox parentBox = new JComboBox();

        ArrayList<String> strings = new ArrayList<>();
        for (org.idea.infoc.GeneratorInfocCodeAction.InfocParentClass parent : parents) {
            strings.add(parent.getName());
        }

        parentBox.setModel(new ArrayListComboBoxModel(strings));

        parentBox.setBounds(100, 110, 350, 25);
        panel.add(parentBox);

        // generator
        JButton mGeneratorButton = new JButton("Generator");
        mGeneratorButton.setBounds(10, 140, 80, 25);
        panel.add(mGeneratorButton);

        mGeneratorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                //Generator code
                mGeneratorButton.setEnabled(false);
                String parentName = (String) parentBox.getSelectedItem();
                InfocParentClass parentClass = null;
                for (org.idea.infoc.GeneratorInfocCodeAction.InfocParentClass parent : parents) {
                    if (parent.getName().equals(parentName)) {
                        parentClass = parent;
                    }
                }
                if (parentClass == null) {
                    showError(event, "please select parent class");
                }


                generateClass(pathJText.getText(), packageJText.getText(), infocJText.getText(), parentClass);
                mGeneratorButton.setEnabled(false);
                refresh();

            }
        });
        mGeneratorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //close
                frame.setVisible(false);
                frame.dispose();
            }
        });

        JButton mCloseButton = new JButton("Close");
        mCloseButton.setBounds(120, 140, 80, 25);
        panel.add(mCloseButton);
        mCloseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.setVisible(false);
                frame.dispose();
            }
        });

        frame.setVisible(true);
    }


    class ArrayListComboBoxModel extends AbstractListModel implements ComboBoxModel {
        private Object selectedItem;

        private ArrayList<String> anArrayList;

        public ArrayListComboBoxModel(ArrayList<String> arrayList) {
            anArrayList = arrayList;
        }

        @Override
        public void setSelectedItem(Object anItem) {
            selectedItem = anItem;
        }

        public Object getSelectedItem() {
            return selectedItem;
        }


        public int getSize() {
            return anArrayList.size();
        }

        public String getElementAt(int i) {
            return anArrayList.get(i);
        }
    }

    private void generateClass(String dir, String pkgName, String infocConfig, InfocParentClass parent) {
        //cm_cn_privacy_clean_recommend:6378 uptime2:int item:byte op_:byte pagetype:byte
        // byte,string,int,int64,short
        GeneratorInfocCodeAction.InfocTable infocTable = parseConfig(infocConfig);
        if (infocTable == null) {
            showError(event, "infoc config error");
            return;
        }

        ClassName returnName = ClassName.get(pkgName, infocTable.tableName);
        ArrayList<MethodSpec> methodSpecs = new ArrayList<MethodSpec>();
        for (Map.Entry<String, String> entry : infocTable.infocMap.entrySet()) {

            MethodSpec.Builder builder = MethodSpec.methodBuilder(entry.getKey())
                    .addModifiers(Modifier.PUBLIC)
                    .returns(returnName);
            if (entry.getValue().equals("int")) {
                builder.addParameter(int.class, entry.getKey());
            } else if (entry.getValue().equals("byte")) {
                builder.addParameter(byte.class, entry.getKey());
            } else if (entry.getValue().equals("int64")) {
                builder.addParameter(long.class, entry.getKey());
            } else if (entry.getValue().equals("string")) {
                builder.addParameter(String.class, entry.getKey());
            } else if (entry.getValue().equals("short")) {
                builder.addParameter(short.class, entry.getKey());
            } else if (entry.getValue().equals("boolean")) {
                builder.addParameter(boolean.class, entry.getKey());
            }
            builder.addStatement("set($S,$N)", entry.getKey(), entry.getKey());
            builder.addStatement("return this");
            methodSpecs.add(builder.build());
        }


        try {

            TypeSpec helloWorld = TypeSpec.classBuilder(infocTable.tableName)
                    .addMethod(MethodSpec.constructorBuilder()
                            .addModifiers(Modifier.PUBLIC)
                            .addStatement("super($S)", infocTable.tableName)
                            .build())
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .superclass(ClassName.get(parent.getPkgName(), parent.getSimpleName()))
                    .addMethods(methodSpecs)
                    .build();

            JavaFile javaFile = JavaFile.builder(pkgName, helloWorld)
                    .build();
            javaFile.writeToFile(new File(dir));
        } catch (Exception e) {
            showError(event, "generator code error");
            e.printStackTrace();
        }

    }


    public static abstract class InfocParentClass {
        public abstract String getName();

        public abstract String getSimpleName();

        public abstract String getPkgName();
    }

    public static class InfocTable {
        public String tableName;
        public int tableIndex;
        public Map<String, String> infocMap = new HashMap<>();

        public void put(String key, String value) {
            infocMap.put(key, value);
        }
    }


    private GeneratorInfocCodeAction.InfocTable parseConfig(String infocConfig) {
        try {
            String[] infocArray = infocConfig.split(" ");
            String[] split = infocArray[0].split(":");
            GeneratorInfocCodeAction.InfocTable infocTable = new GeneratorInfocCodeAction.InfocTable();
            infocTable.tableName = split[0];
            infocTable.tableIndex = Integer.parseInt(split[1]);
            for (int i = 1; i < infocArray.length; i++) {
                String attrs = infocArray[i];
                String[] attrArray = attrs.split(":");
                if (attrArray != null && attrArray.length == 2) {
                    infocTable.put(attrArray[0], attrArray[1]);
                }
            }
            if (infocTable.infocMap.isEmpty()) {
                return null;
            }
            return infocTable;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private String getPkgName(String path) {
        if (path.contains("/java/")) {
            int length = "/java/".length();
            int i = path.lastIndexOf("/java/");
            dir = path.substring(0, i + length);
            return path.substring(i + length).replace("/", ".");
        } else if (path.contains("/src/")) {
            int length = "/src/".length();
            int i = path.lastIndexOf("/src/");
            dir = path.substring(0, i + length);
            return path.substring(i + length).replace("/", ".");
        }
        return null;
    }

    private void showError(AnActionEvent event, String msg) {
        Messages.showMessageDialog(event.getProject(), msg, "错误信息", Messages.getInformationIcon());
    }
}
