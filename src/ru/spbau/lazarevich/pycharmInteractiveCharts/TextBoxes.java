package ru.spbau.lazarevich.pycharmInteractiveCharts;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Andrey
 */

public class TextBoxes extends AnAction {
    public TextBoxes() {
        super("Text _Boxes");
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        final Project project = e.getProject();
        final String txt = Messages.showInputDialog(project, "What's your name", "Name Asker", Messages.getQuestionIcon());
        Messages.showMessageDialog(project, "Hello, " + txt + "!\n I am glad to see you.", "Information",
                Messages.getInformationIcon());
    }
}
