package org.eclipse.jdt.ui.actions;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.jdt.internal.corext.refactoring.concurrency.ConvertToFJTaskRefactoring;

import org.eclipse.jdt.internal.ui.refactoring.concurrency.ConvertToFJTaskWizard;

public class ConvertToFJTaskAction implements IObjectActionDelegate {

	private Shell shell;
	private IMethod fRecursiveMethod;

	/**
	 * Constructor for Action1.
	 */
	public ConvertToFJTaskAction() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		shell = targetPart.getSite().getShell();
	}

	
	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		try {
			if (fRecursiveMethod != null && shell != null && isConvertToFJTaskAvailable()) {
				ConvertToFJTaskRefactoring refactoring= new ConvertToFJTaskRefactoring(fRecursiveMethod);
				run(new ConvertToFJTaskWizard(refactoring, "Convert to FJTask"), shell, "Convert to FJTask"); //$NON-NLS-1$ //$NON-NLS-2$
			} else
				MessageDialog.openError(shell, "Error ConvertToFJTask", "ConvertToFJTask not applicable for current selection");  //$NON-NLS-1$ //$NON-NLS-2$
			
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
	}
	
	public void run(RefactoringWizard wizard, Shell parent, String dialogTitle) {
		try {
			RefactoringWizardOpenOperation operation= new RefactoringWizardOpenOperation(wizard);
			operation.run(parent, dialogTitle);
		} catch (InterruptedException exception) {
			// Do nothing
		}
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		fRecursiveMethod= null;
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection extended= (IStructuredSelection) selection;
			Object[] elements= extended.toArray();
			if (elements.length == 1 && elements[0] instanceof IMethod) {
				fRecursiveMethod= (IMethod) elements[0];
			}
		}
//		try {
//			action.setEnabled(isConvertToConcurrentHashMapAvailable());
//		} catch (JavaModelException exception) {
//			action.setEnabled(false);
//		}
	}

	private boolean isConvertToFJTaskAvailable()
			throws JavaModelException {
		return fRecursiveMethod != null && fRecursiveMethod.exists() && fRecursiveMethod.isStructureKnown() && !fRecursiveMethod.getDeclaringType().isAnnotation();
	}
}
