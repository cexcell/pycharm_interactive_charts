package ru.spbau.lazarevich.pycharmInteractiveCharts;

import com.intellij.openapi.actionSystem.AnAction;
        import com.intellij.openapi.actionSystem.AnActionEvent;
        import com.intellij.openapi.project.Project;
        import com.intellij.openapi.ui.Messages;


/**
 * Created by Andrey
 */
public class MyShowDialog extends AnAction {
    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getProject();
        Messages.showMessageDialog(project, "You did the action. Gratz.", "Dummy Action", Messages.getWarningIcon());
    }
}
