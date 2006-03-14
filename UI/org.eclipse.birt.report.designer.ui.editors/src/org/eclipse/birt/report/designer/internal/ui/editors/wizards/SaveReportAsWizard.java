/*******************************************************************************
 * Copyright (c) 2004 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/

package org.eclipse.birt.report.designer.internal.ui.editors.wizards;

import org.eclipse.birt.report.designer.internal.ui.util.ExceptionHandler;
import org.eclipse.birt.report.designer.internal.ui.wizards.WizardReportSettingPage;
import org.eclipse.birt.report.designer.nls.Messages;
import org.eclipse.birt.report.model.api.ModuleHandle;
import org.eclipse.birt.report.model.api.ReportDesignHandle;
import org.eclipse.birt.report.model.api.activity.SemanticException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IEditorInput;

/**
 * 
 */

public class SaveReportAsWizard extends Wizard
{

	private static final String SaveAsWizardWindowTitle = Messages.getString( "SaveReportAsWizard.SaveAsWizardWindowTitle" ); //$NON-NLS-1$
	private static final String SaveAsWizardPageTitle = Messages.getString( "SaveReportAsWizard.SaveAsWizardPageTitle" ); //$NON-NLS-1$
	private static final String SaveAsWizardPageDesc = Messages.getString( "SaveReportAsWizard.SaveAsWizardPageDesc" ); //$NON-NLS-1$
	private static final String ReportSettingPageTitle = Messages.getString( "SaveReportAsWizard.SettingPage.message" ); //$NON-NLS-1$

	private ModuleHandle model;
	private IEditorInput orginalFile;
	private WizardSaveAsPage saveAsPage;
	private WizardReportSettingPage settingPage;
	private IPath saveAsPath;

	public SaveReportAsWizard( ModuleHandle model,
			IEditorInput orginalFile )
	{
		setWindowTitle( SaveAsWizardWindowTitle );
		this.model = model;
		this.orginalFile = orginalFile;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	public void addPages( )
	{

		saveAsPage = new WizardSaveAsPage( "WizardSaveAsPage" ); //$NON-NLS-1$
		saveAsPage.setOriginalFile( orginalFile );
		saveAsPage.setTitle( SaveAsWizardPageTitle );
		saveAsPage.setMessage( SaveAsWizardPageDesc );
		// saveAsPage.setImageDescriptor(
		// IDEInternalWorkbenchImages.getImageDescriptor(
		// IDEInternalWorkbenchImages.IMG_DLGBAN_SAVEAS_DLG ) );
		addPage( saveAsPage );
		if ( model instanceof ReportDesignHandle )
		{
			settingPage = new WizardReportSettingPage( (ReportDesignHandle)model );
			settingPage.setTitle( ReportSettingPageTitle );
			settingPage.setMessage( Messages.getString( "SaveReportAsWizard.SettingPage.message" ) ); //$NON-NLS-1$

			addPage( settingPage );
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#canFinish()
	 */
	public boolean canFinish( )
	{
		return saveAsPage.validatePage( );
	}

	public boolean performFinish( )
	{
		saveAsPath = saveAsPage.getResult( );
		if ( saveAsPath != null && model instanceof ReportDesignHandle )
		{
			ReportDesignHandle reportHandle = (ReportDesignHandle)model;
			try
			{
				reportHandle.setDisplayName( settingPage.getDisplayName( ) );
				reportHandle.setDescription( settingPage.getDescription( ) );
				reportHandle.setIconFile( settingPage.getPreviewImagePath( ) );
				reportHandle.setFileName( saveAsPath.toOSString( ) );
			}
			catch ( SemanticException e )
			{
				ExceptionHandler.handle( e );
			}
		}
		return true;
	}

	public IPath getSaveAsPath( )
	{
		return this.saveAsPath;
	}
}
