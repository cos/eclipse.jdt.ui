package org.eclipse.jdt.internal.ui.viewsupport;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.ISourceRange;

import org.eclipse.jdt.internal.ui.JavaPlugin;

/**
 * Used by the JavaElementLabelProvider to evaluate the error tick state of
 * an element.
 */
public class MarkerErrorTickProvider implements IErrorTickProvider {
	
	/*
	 * @see IErrorTickProvider#getErrorInfo
	 */
	public int getErrorInfo(IJavaElement element) {
		int info= 0;
		try {
			IResource res= null;
			ISourceRange range= null;
			int depth= IResource.DEPTH_INFINITE;
			
			int type= element.getElementType();
			
			if (type < IJavaElement.TYPE) {
				res= element.getCorrespondingResource();
				if (type == IJavaElement.PACKAGE_FRAGMENT) {
					depth= IResource.DEPTH_ONE;
				} else if (type == IJavaElement.JAVA_PROJECT) {
					IMarker[] bpMarkers= res.findMarkers(IJavaModelMarker.BUILDPATH_PROBLEM_MARKER, true, IResource.DEPTH_ONE);
					info= accumulateProblems(bpMarkers, info, null);
				}
			} else if (type == IJavaElement.TYPE || type == IJavaElement.METHOD || type == IJavaElement.INITIALIZER) {
				// I assume that only source elements can have markers
				ICompilationUnit cu= ((IMember)element).getCompilationUnit();
				if (cu != null) {
					res= element.getUnderlyingResource();
					range= ((IMember)element).getSourceRange();
				}
			}
			
			if (res != null) {
				IMarker[] markers= res.findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true, depth);
				if (markers != null) {
					info= accumulateProblems(markers, info, range);
				}
			}
		} catch (CoreException e) {
			JavaPlugin.log(e.getStatus());
		}
		return info;
	}
	
	private int accumulateProblems(IMarker[] markers, int info, ISourceRange range) {
		if (markers != null) {	
			for (int i= 0; i < markers.length && (info != ERRORTICK_ERROR); i++) {
				IMarker curr= markers[i];
				if (range == null || isInRange(curr, range)) {
					int priority= curr.getAttribute(IMarker.SEVERITY, -1);
					if (priority == IMarker.SEVERITY_WARNING) {
						info= ERRORTICK_WARNING;
					} else if (priority == IMarker.SEVERITY_ERROR) {
						info= ERRORTICK_ERROR;
					}
				}
			}
		}
		return info;
	}
	
	private boolean isInRange(IMarker marker, ISourceRange range) {
		int pos= marker.getAttribute(IMarker.CHAR_START, -1);
		int offset= range.getOffset();
		return (offset <= pos && offset + range.getLength() > pos);
	}	

}

