
/*******************************************************************************
 * Copyright (c) 2004, 2005 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/
package org.eclipse.birt.data.engine.olap.data.impl;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.eclipse.birt.data.engine.olap.data.api.DimLevel;
import org.eclipse.birt.data.engine.olap.data.api.IAggregationResultRow;
import org.eclipse.birt.data.engine.olap.data.api.IAggregationResultSet;
import org.eclipse.birt.data.engine.olap.data.impl.aggregation.AggregationResultRow;
import org.eclipse.birt.data.engine.olap.data.util.BufferedStructureArray;
import org.eclipse.birt.data.engine.olap.data.util.DataType;
import org.eclipse.birt.data.engine.olap.data.util.IDiskArray;


/**
 * 
 */

public class CachedAggregationResultSet implements IAggregationResultSet
{
	private int currentPosition;
	private int length;
	private DimLevel[] levels;
	private String[][] keyNames;
	private String[][] attributeNames;
	private int[][] keyDataTypes;
	private int[][] attributeDataTypes;
	private Map aggregationResultNameMap = null;
	private int[] aggregationDataType;
	private IDiskArray aggregationResultRow;
	private AggregationResultRow resultObject;
	private int[] sortType;
	private DataInputStream inputStream;
	private String[] aggregationNames;
	private static Logger logger = Logger.getLogger( CachedAggregationResultSet.class.getName( ) );

	CachedAggregationResultSet( DataInputStream inputStream, int length,
			DimLevel[] levels, int[] sortTypes, String[][] keyNames,
			String[][] attributeNames, int[][] keyDataTypes,
			int[][] attributeDataTypes, String[] aggregationNames,
			int[] aggregationDataType ) throws IOException
	{
		Object[] params = {
				inputStream,
				new Integer( length ),
				levels,
				sortTypes,
				keyNames,
				attributeNames,
				keyDataTypes,
				attributeDataTypes,
				aggregationNames,
				aggregationDataType
		};
		logger.entering( CachedAggregationResultSet.class.getName( ),
				"CachedAggregationResultSet",
				params );
		this.inputStream = inputStream;
		this.currentPosition = 0;
		this.length = length;
		this.levels = levels;
		this.sortType = sortTypes;
		this.keyNames = keyNames;
		this.attributeNames = attributeNames;
		this.keyDataTypes = keyDataTypes;
		this.attributeDataTypes = attributeDataTypes;
		this.aggregationDataType = aggregationDataType;
		this.aggregationResultNameMap = new HashMap( );
		if ( aggregationNames != null )
		{
			this.aggregationNames = new String[aggregationNames.length];
			for ( int i = 0; i < aggregationNames.length; i++ )
			{
				this.aggregationNames[i] = aggregationNames[i];
				aggregationResultNameMap.put( aggregationNames[i],
						new Integer( i ) );
			}
		}
		aggregationResultRow =  new BufferedStructureArray( AggregationResultRow.getCreator( ), Constants.LIST_BUFFER_SIZE );
		if ( this.length > 0 )
			seek( 0 );
		logger.exiting( CachedAggregationResultSet.class.getName( ),
				"CachedAggregationResultSet" );
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.birt.data.olap.data.api.IAggregationResultSet#getAggregationDataType(int)
	 */
	public int getAggregationDataType( int aggregationIndex )
			throws IOException
	{
		if ( aggregationDataType == null || aggregationIndex < 0 || aggregationIndex >= aggregationDataType.length )
			return DataType.UNKNOWN_TYPE;
		return aggregationDataType[aggregationIndex];
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.birt.data.engine.olap.data.api.IAggregationResultSet#getAggregationIndex(java.lang.String)
	 */
	public int getAggregationIndex( String name ) throws IOException
	{
		Object index = aggregationResultNameMap.get( name );
		if( index == null )
		{
			return -1;
		}
		return ((Integer)index).intValue( );
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	protected void finalize() throws Throwable
	{
		inputStream.close( );
		super.finalize();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.birt.data.olap.data.api.IAggregationResultSet#getAggregationValue(int)
	 */
	public Object getAggregationValue( int aggregationIndex )
			throws IOException
	{
		if ( resultObject.getAggregationValues() == null || aggregationIndex < 0 )
			return null;
		return resultObject.getAggregationValues()[aggregationIndex];
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.birt.data.olap.data.api.IAggregationResultSet#getAllAttributes(int)
	 */
	public String[] getLevelAttributes( int levelIndex )
	{
		if ( attributeNames == null )
		{
			return null;
		}
		return attributeNames[levelIndex];
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.birt.data.engine.olap.data.api.IAggregationResultSet#getAllLevels()
	 */
	public DimLevel[] getAllLevels( )
	{
		return levels;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.birt.data.olap.data.api.IAggregationResultSet#getLevelAttribute(int, int)
	 */
	public Object getLevelAttribute( int levelIndex, int attributeIndex )
	{
		if ( resultObject.getLevelMembers() == null || levelIndex < 0
				|| resultObject.getLevelMembers()[levelIndex].getAttributes() == null )
		{
			return null;
		}
		return resultObject.getLevelMembers()[levelIndex].getAttributes()[attributeIndex];
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.birt.data.olap.data.api.IAggregationResultSet#getLevelAttributeColCount(int)
	 */
	public int getLevelAttributeColCount( int levelIndex )
	{
		if ( attributeNames == null || attributeNames[levelIndex] == null )
			return 0;
		return attributeNames[levelIndex].length;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.birt.data.olap.data.api.IAggregationResultSet#getLevelAttributeDataType(java.lang.String, java.lang.String)
	 */
	public int getLevelAttributeDataType( DimLevel level, String attributeName )
	{
		int levelIndex = getLevelIndex( level );
		if ( attributeDataTypes == null || attributeDataTypes[levelIndex] == null )
		{
			return DataType.UNKNOWN_TYPE;
		}
		return this.attributeDataTypes[levelIndex][getLevelAttributeIndex( level,
				attributeName )];
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.birt.data.olap.data.api.IAggregationResultSet#getLevelAttributeDataType(int, java.lang.String)
	 */
	public int getLevelAttributeDataType( int levelIndex, String attributeName )
	{
		if ( attributeDataTypes == null
				|| levelIndex < 0 || attributeDataTypes[levelIndex] == null )
		{
			return DataType.UNKNOWN_TYPE;
		}
		return attributeDataTypes[levelIndex][getLevelAttributeIndex( levelIndex,
				attributeName )];
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.birt.data.olap.data.api.IAggregationResultSet#getLevelAttributeIndex(int, java.lang.String)
	 */
	public int getLevelAttributeIndex( int levelIndex, String attributeName )
	{
		if ( attributeNames == null || levelIndex < 0 || attributeNames[levelIndex] == null )
		{
			return -1;
		}
		for ( int i = 0; i < attributeNames[levelIndex].length; i++ )
		{
			if ( attributeNames[levelIndex][i].equals( attributeName ) )
			{
				return i;
			}
		}
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.birt.data.olap.data.api.IAggregationResultSet#getLevelAttributeIndex(java.lang.String, java.lang.String)
	 */
	public int getLevelAttributeIndex( DimLevel level, String attributeName )
	{
		int levelIndex = getLevelIndex( level );
		if ( attributeNames == null || attributeNames[levelIndex] == null )
		{
			return -1;
		}
		for ( int i = 0; i < attributeNames[levelIndex].length; i++ )
		{
			if ( attributeNames[levelIndex][i].equals( attributeName ) )
			{
				return i;
			}
		}
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.birt.data.olap.data.api.IAggregationResultSet#getLevelCount()
	 */
	public int getLevelCount( )
	{
		if ( keyNames == null )
			return 0;
		return keyNames.length;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.birt.data.olap.data.api.IAggregationResultSet#getLevelIndex(java.lang.String)
	 */
	public int getLevelIndex( DimLevel level )
	{
		if ( levels == null )
		{
			return -1;
		}
		for ( int i = 0; i < levels.length; i++ )
		{
			if ( levels[i].equals( level ) )
			{
				return i;
			}
		}
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.birt.data.olap.data.api.IAggregationResultSet#getLevelKeyColCount(int)
	 */
	public int getLevelKeyColCount( int levelIndex )
	{
		if ( keyNames == null || keyNames[levelIndex] == null )
			return 0;
		return keyNames[levelIndex].length;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.birt.data.olap.data.api.IAggregationResultSet#getLevelKeyDataType(java.lang.String, java.lang.String)
	 */
	public int getLevelKeyDataType( DimLevel level, String keyName )
	{
		if ( keyDataTypes == null )
		{
			return DataType.UNKNOWN_TYPE;
		}
		return getLevelKeyDataType( getLevelIndex( level ), keyName );
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.birt.data.olap.data.api.IAggregationResultSet#getLevelKeyDataType(int, java.lang.String)
	 */
	public int getLevelKeyDataType( int levelIndex, String keyName )
	{
		if ( keyDataTypes == null
				|| levelIndex < 0 || keyDataTypes[levelIndex] == null )
		{
			return DataType.UNKNOWN_TYPE;
		}
		return keyDataTypes[levelIndex][getLevelKeyIndex( levelIndex, keyName )];
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.birt.data.olap.data.api.IAggregationResultSet#getLevelKeyIndex(int, java.lang.String)
	 */
	public int getLevelKeyIndex( int levelIndex, String keyName )
	{
		if ( keyNames == null || levelIndex < 0 || keyNames[levelIndex] == null )
		{
			return DataType.UNKNOWN_TYPE;
		}
		for ( int i = 0; i < keyNames[levelIndex].length; i++ )
		{
			if ( keyNames[levelIndex][i].equals( keyName ) )
			{
				return i;
			}
		}
		return DataType.UNKNOWN_TYPE;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.birt.data.olap.data.api.IAggregationResultSet#getLevelKeyIndex(java.lang.String, java.lang.String)
	 */
	public int getLevelKeyIndex( DimLevel level, String keyName )
	{
		if ( keyNames == null )
		{
			return -1;
		}
		return getLevelKeyIndex( getLevelIndex( level ), keyName );
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.birt.data.olap.data.api.IAggregationResultSet#getLevelKeyValue(int)
	 */
	public Object[] getLevelKeyValue( int levelIndex )
	{
		if ( resultObject.getLevelMembers( ) == null
				|| levelIndex < 0
				|| levelIndex > resultObject.getLevelMembers( ).length - 1 )
		{
			return null;
		}
		return resultObject.getLevelMembers()[levelIndex].getKeyValues();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.birt.data.olap.data.api.IAggregationResultSet#getSortType(int)
	 */
	public int getSortType( int levelIndex )
	{
		if ( sortType == null || sortType.length < levelIndex )
		{
			return -100;
		}
		return sortType[levelIndex];
	}

	public int length( )
	{
		return length;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.birt.data.olap.data.api.IAggregationResultSet#seek(int)
	 */
	public void seek( int index ) throws IOException
	{
		if ( index >= length )
		{
			throw new IndexOutOfBoundsException( "Index: "
					+ index + ", Size: " + length );
		}
		if ( index >= aggregationResultRow.size( ) )
		{
			for ( int i = 0; i <= index - aggregationResultRow.size( ); i++ )
			{
				aggregationResultRow.add( AggregationResultSetSaveUtil.loadAggregationRow( inputStream ) );
			}
		}
		currentPosition = index;
		resultObject = (AggregationResultRow) aggregationResultRow.get( index );
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.birt.data.engine.olap.data.api.IAggregationResultSet#getPosition()
	 */
	public int getPosition( )
	{
		return currentPosition;
	}

	public String[][] getAttributeNames( )
	{
		return this.attributeNames;
	}

	public IAggregationResultRow getCurrentRow( ) throws IOException
	{
		return this.resultObject;
	}

	public String[][] getKeyNames( )
	{
		return this.keyNames;
	}

	public String getLevelKeyName( int levelIndex, int keyIndex )
	{
		return this.keyNames[levelIndex][keyIndex];
	}

	public DimLevel getLevel( int levelIndex )
	{
		return this.levels[levelIndex];
	}

	public AggregationDefinition getAggregationDefinition( )
	{
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.birt.data.engine.olap.data.api.IAggregationResultSet#close()
	 */
	public void close( ) throws IOException
	{
		inputStream.close( );
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.birt.data.engine.olap.data.api.IAggregationResultSet#clear()
	 */
	public void clear( ) throws IOException
	{
		inputStream.close( );
		length = 0;
	}

	public int getAggregationCount( )
	{
		return aggregationResultNameMap.size( ); 
	}

	public String getAggregationName( int index )
	{
		if (this.aggregationNames != null)
		{
			return aggregationNames[index];
		}
		return null;
	}

	public int[] getAggregationDataType( )
	{
		return this.aggregationDataType;
	}

	public int[][] getLevelAttributeDataType( )
	{
		return this.attributeDataTypes;
	}

	public String[][] getLevelAttributes( )
	{
		return this.attributeNames;
	}

	public int[][] getLevelKeyDataType( )
	{
		return keyDataTypes;
	}

	public String[][] getLevelKeys( )
	{
		return keyNames;
	}

	public int[] getSortType( )
	{
		return this.sortType;
	}
}
