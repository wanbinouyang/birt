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

package org.eclipse.birt.report.model.api;

import org.eclipse.birt.report.model.api.activity.SemanticException;
import org.eclipse.birt.report.model.api.elements.DesignChoiceConstants;
import org.eclipse.birt.report.model.api.metadata.DimensionValue;
import org.eclipse.birt.report.model.api.metadata.PropertyValueException;
import org.eclipse.birt.report.model.api.util.Point;
import org.eclipse.birt.report.model.core.DesignElement;
import org.eclipse.birt.report.model.elements.MasterPage;
import org.eclipse.birt.report.model.elements.ReportDesign;
import org.eclipse.birt.report.model.elements.interfaces.IMasterPageModel;

/**
 * Represents a master page. The master page is an abstract element that defines
 * the basic properties of a printed page. The derived elements, Simple and
 * Graphic Master Pages, provide content that appears on the page itself.
 * 
 * 
 * @see org.eclipse.birt.report.model.elements.MasterPage
 * @see DimensionHandle
 */

public abstract class MasterPageHandle extends ReportElementHandle
		implements
			IMasterPageModel
{

	/**
	 * Constructs a master-page handle with the given design and the element.
	 * The application generally does not create handles directly. Instead, it
	 * uses one of the navigation methods available on other element handles.
	 * 
	 * @param design
	 *            the report design
	 * @param element
	 *            the model representation of the element
	 */

	public MasterPageHandle( ReportDesign design, DesignElement element )
	{
		super( design, element );
	}

	/**
	 * Returns the size of the page. The size is either one of the standard
	 * sizes, or a custom size. Note that the size returned <em>will not</em>
	 * match the <code>getWidth</code> and <code>getHeight</code> values
	 * unless the page uses a custom size.
	 * 
	 * @return the actual page size in application units
	 */

	public Point getSize( )
	{
		return ( (MasterPage) element ).getSize( design );
	}

	/**
	 * Returns the the effective width of the page.
	 * 
	 * @return the effective width of the page. Return value is a
	 *         DimensionValue, the measure of it is the width measure of the
	 *         page, unit is that set on the session.
	 * @deprecated
	 */

	public DimensionValue getEffectiveWidth( )
	{
		return new DimensionValue( getSize( ).x, design.getSession( )
				.getUnits( ) );
	}

	/**
	 * Returns the the effective height of the page.
	 * 
	 * @return the effective height of the page. Return value is a
	 *         DimensionValue, the measure of it is the height measure of the
	 *         page, unit is that set on the session.
	 * @deprecated
	 */

	public DimensionValue getEffectiveHeight( )
	{
		return new DimensionValue( getSize( ).y, design.getSession( )
				.getUnits( ) );
	}

	/**
	 * Returns the type of the page. The return type of the page is defined in
	 * <code>DesignChoiceConstants</code> and can be one of:
	 * 
	 * <ul>
	 * <li><code>PAGE_SIZE_CUSTOM</code>
	 * <li><code>PAGE_SIZE_US_LETTER</code>
	 * <li><code>PAGE_SIZE_US_LEGAL</code>
	 * <li><code>PAGE_SIZE_A4</code>
	 * </ul>
	 * 
	 * @return the type of the page
	 */

	public String getPageType( )
	{
		return getStringProperty( MasterPage.TYPE_PROP );
	}

	/**
	 * Sets the type of the page. The input type of the page is defined in
	 * <code>DesignChoiceConstants</code> and can be one of:
	 * 
	 * <ul>
	 * <li><code>PAGE_SIZE_CUSTOM</code>
	 * <li><code>PAGE_SIZE_US_LETTER</code>
	 * <li><code>PAGE_SIZE_US_LEGAL</code>
	 * <li><code>PAGE_SIZE_A4</code>
	 * </ul>
	 * 
	 * @param type
	 *            the type of the page
	 * 
	 * @throws SemanticException
	 *             if the property is locked or the input value is not one of
	 *             the above.
	 */

	public void setPageType( String type ) throws SemanticException
	{
		setStringProperty( MasterPage.TYPE_PROP, type );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.model.api.DesignElementHandle#setProperty(java.lang.String,
	 *      java.lang.Object)
	 */

	public void setProperty( String propName, Object value )
			throws SemanticException
	{
		// Special treatment for master page "type" property.

		if ( MasterPage.TYPE_PROP.equals( propName ) )
		{
			// If type set to A4, US Letter or US Legal, clear up height/width
			// value.
			try
			{
				getDesign( ).getActivityStack( ).startTrans( );

				// clear up height/width value when set type to .
				super.setProperty( MasterPage.TYPE_PROP, value );

				if ( !DesignChoiceConstants.PAGE_SIZE_CUSTOM
						.equalsIgnoreCase( (String) value ) )
				{

					super.setProperty( MasterPage.WIDTH_PROP, null );
					super.setProperty( MasterPage.HEIGHT_PROP, null );
				}

				getDesign( ).getActivityStack( ).commit( );
			}
			catch ( SemanticException e )
			{
				getDesign( ).getActivityStack( ).rollback( );
				throw e;
			}

			return;
		}

		super.setProperty( propName, value );
	}

	/**
	 * Returns the page orientation. The return type of the page is defined in
	 * <code>DesignChoiceConstants</code> can be one of:
	 * 
	 * <ul>
	 * <li><code>PAGE_ORIENTATION_AUTO</code>
	 * <li><code>PAGE_ORIENTATION_PORTRAIT</code>
	 * <li><code>PAGE_ORIENTATION_LANDSCAPE</code>
	 * </ul>
	 * 
	 * @return the page orientation
	 */

	public String getOrientation( )
	{
		return getStringProperty( MasterPage.ORIENTATION_PROP );
	}

	/**
	 * Returns the page orientation. The input type of the page is defined in
	 * <code>DesignChoiceConstants</code> can be one of:
	 * 
	 * <ul>
	 * <li><code>PAGE_ORIENTATION_AUTO</code>
	 * <li><code>PAGE_ORIENTATION_PORTRAIT</code>
	 * <li><code>PAGE_ORIENTATION_LANDSCAPE</code>
	 * </ul>
	 * 
	 * @param orientation
	 *            the page orientation
	 * @throws SemanticException
	 *             if the property is locked or the input value is not one of
	 *             the above.
	 */

	public void setOrientation( String orientation ) throws SemanticException
	{
		setStringProperty( MasterPage.ORIENTATION_PROP, orientation );
	}

	/**
	 * Gets a dimension handle to work with the height of the page.
	 * 
	 * @return a DimensionHandle to work with the height
	 */

	public DimensionHandle getHeight( )
	{
		return super.getDimensionProperty( MasterPage.HEIGHT_PROP );
	}

	/**
	 * Gets a dimension handle to work with the width of the page.
	 * 
	 * @return DimensionHandle to work with the width
	 */

	public DimensionHandle getWidth( )
	{
		return super.getDimensionProperty( MasterPage.WIDTH_PROP );
	}

	/**
	 * Gets a dimension handle to work with the margin on the bottom side.
	 * 
	 * @return a DimensionHandle for the bottom margin.
	 */

	public DimensionHandle getBottomMargin( )
	{
		return super.getDimensionProperty( MasterPage.BOTTOM_MARGIN_PROP );
	}

	/**
	 * Gets a dimension handle to work with the margin on the left side.
	 * 
	 * @return a DimensionHandle for the left margin.
	 */

	public DimensionHandle getLeftMargin( )
	{
		return super.getDimensionProperty( MasterPage.LEFT_MARGIN_PROP );
	}

	/**
	 * Gets a dimension handle to work with the margin on the right side.
	 * 
	 * @return a DimensionHandle for the right margin.
	 */

	public DimensionHandle getRightMargin( )
	{
		return super.getDimensionProperty( MasterPage.RIGHT_MARGIN_PROP );
	}

	/**
	 * Gets a dimension handle to work with the margin on the top side.
	 * 
	 * @return a DimensionHandle for the top margin.
	 */

	public DimensionHandle getTopMargin( )
	{
		return super.getDimensionProperty( MasterPage.TOP_MARGIN_PROP );
	}

	/**
	 * Gets a dimension handle to work with the height on page header.
	 * 
	 * @return a DimensionHandle for the header height.
	 */

	public DimensionHandle getHeaderHeight( )
	{
		return super.getDimensionProperty( MasterPage.HEADER_HEIGHT_PROP );
	}

	/**
	 * Gets a dimension handle to work with the height on page footer.
	 * 
	 * @return a DimensionHandle for the header footer.
	 */

	public DimensionHandle getFooterHeight( )
	{
		return super.getDimensionProperty( MasterPage.FOOTER_HEIGHT_PROP );
	}

	/**
	 * Gets the effective page height.
	 * 
	 * @return the page height
	 */

	public DimensionValue getPageHeight( )
	{
		String pageType = getPageType( );
		try
		{
			if ( DesignChoiceConstants.PAGE_SIZE_A4.equalsIgnoreCase( pageType ) )
				return DimensionValue.parse( MasterPage.A4_HEIGHT );
			else if ( DesignChoiceConstants.PAGE_SIZE_CUSTOM
					.equalsIgnoreCase( pageType ) )
				return (DimensionValue) getProperty( MasterPage.HEIGHT_PROP );
			else if ( DesignChoiceConstants.PAGE_SIZE_US_LEGAL
					.equalsIgnoreCase( pageType ) )
				return DimensionValue.parse( MasterPage.US_LEGAL_HEIGHT );
			else if ( DesignChoiceConstants.PAGE_SIZE_US_LETTER
					.equalsIgnoreCase( pageType ) )
				return DimensionValue.parse( MasterPage.US_LETTER_HEIGHT );
			else
				assert false;
		}
		catch ( PropertyValueException e )
		{
			assert false;
		}
		return null;
	}

	/**
	 * Gets the effective page width.
	 * 
	 * @return the page width
	 */

	public DimensionValue getPageWidth( )
	{
		String pageType = getPageType( );
		try
		{
			if ( DesignChoiceConstants.PAGE_SIZE_A4.equalsIgnoreCase( pageType ) )
				return DimensionValue.parse( MasterPage.A4_WIDTH );
			else if ( DesignChoiceConstants.PAGE_SIZE_CUSTOM
					.equalsIgnoreCase( pageType ) )
				return (DimensionValue) getProperty( MasterPage.WIDTH_PROP );
			else if ( DesignChoiceConstants.PAGE_SIZE_US_LEGAL
					.equalsIgnoreCase( pageType ) )
				return DimensionValue.parse( MasterPage.US_LEGAL_WIDTH );
			else if ( DesignChoiceConstants.PAGE_SIZE_US_LETTER
					.equalsIgnoreCase( pageType ) )
				return DimensionValue.parse( MasterPage.US_LETTER_WIDTH );
			else
				assert false;
		}
		catch ( PropertyValueException e )
		{
			assert false;
		}
		return null;
	}

}