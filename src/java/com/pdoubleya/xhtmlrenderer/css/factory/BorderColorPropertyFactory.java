/*
 * {{{ header & license
 * BorderColorPropertyFactory.java
 * Copyright (c) 2004 Patrick Wright
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package com.pdoubleya.xhtmlrenderer.css.factory;

import java.util.*;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;

import com.pdoubleya.xhtmlrenderer.css.*;
import com.pdoubleya.xhtmlrenderer.css.constants.*;
import com.pdoubleya.xhtmlrenderer.css.impl.*;

/**
 * Used to pull 'border-color' properties out of CSS. Explosion of shorthand is
 * same as for margin, but leaving this in a separate class in case later want
 * to do other things here, like initial values, validations, etc.
 *
 * @author    Patrick Wright
 * @created   August 2, 2004
 */
public class BorderColorPropertyFactory extends AbstractPropertyFactory {
  /** Description of the Field */
  private static BorderColorPropertyFactory _instance;


  /** Constructor for the BorderColorPropertyFactory object */
  private BorderColorPropertyFactory() { }


  /**
   * Description of the Method
   *
   * @return   Returns
   */
  public static synchronized PropertyFactory instance() {
    if ( _instance == null ) {
      _instance = new BorderColorPropertyFactory();
    }
    return _instance;
  }

  // thread-safe
  /**
   * Description of the Method
   *
   * @param style     PARAM
   * @param propName  PARAM
   * @param sequence  PARAM
   * @return          Returns
   */
  public Iterator explodeProperties( CSSStyleDeclaration style, String propName, int sequence ) {
    List list = new ArrayList();
    CSSValue cssValue = style.getPropertyCSSValue( propName );
    String priority = style.getPropertyPriority( propName );

    // CAREFUL: note that with steadyState parser impl, their value class impl
    // both primitive and value list interfaces! use getCssValueType(), not instanceof!!
    if ( cssValue.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE ) {
      list.addAll( explodeOne( (CSSPrimitiveValue)cssValue, priority, style, sequence ) );
    } else {
      // is a value list
      CSSValueList vList = (CSSValueList)cssValue;

      // border-color explodes differently based on number of supplied values
      CSSPrimitiveValue primitive = null;
      XRValueImpl val = null;
      switch ( vList.getLength() ) {
          case 1:
            // bug in CSSValue implementation! but who cares
            primitive = (CSSPrimitiveValue)vList.item( 0 );
            list.addAll( explodeOne( primitive, priority, style, sequence ) );
            break;
          case 2:
            primitive = (CSSPrimitiveValue)vList.item( 0 );
            list.add( newProperty( CSSName.BORDER_COLOR_TOP, primitive, priority, style, sequence ) );
            list.add( newProperty( CSSName.BORDER_COLOR_BOTTOM, primitive, priority, style, sequence ) );

            primitive = (CSSPrimitiveValue)vList.item( 1 );
            list.add( newProperty( CSSName.BORDER_COLOR_RIGHT, primitive, priority, style, sequence ) );
            list.add( newProperty( CSSName.BORDER_COLOR_LEFT, primitive, priority, style, sequence ) );
            break;
          case 3:
            primitive = (CSSPrimitiveValue)vList.item( 0 );
            list.add( newProperty( CSSName.BORDER_COLOR_TOP, primitive, priority, style, sequence ) );

            primitive = (CSSPrimitiveValue)vList.item( 1 );
            list.add( newProperty( CSSName.BORDER_COLOR_RIGHT, primitive, priority, style, sequence ) );
            list.add( newProperty( CSSName.BORDER_COLOR_LEFT, primitive, priority, style, sequence ) );

            primitive = (CSSPrimitiveValue)vList.item( 2 );
            list.add( newProperty( CSSName.BORDER_COLOR_BOTTOM, primitive, priority, style, sequence ) );
            break;
          case 4:
            primitive = (CSSPrimitiveValue)vList.item( 0 );
            list.add( newProperty( CSSName.BORDER_COLOR_TOP, primitive, priority, style, sequence ) );

            primitive = (CSSPrimitiveValue)vList.item( 1 );
            list.add( newProperty( CSSName.BORDER_COLOR_RIGHT, primitive, priority, style, sequence ) );

            primitive = (CSSPrimitiveValue)vList.item( 2 );
            list.add( newProperty( CSSName.BORDER_COLOR_BOTTOM, primitive, priority, style, sequence ) );

            primitive = (CSSPrimitiveValue)vList.item( 3 );
            list.add( newProperty( CSSName.BORDER_COLOR_LEFT, primitive, priority, style, sequence ) );
            break;
          default:
            System.err.println( "Property '" + propName + "' has more than 4 values assigned." );
            break;
      }
    }// is a value list

    return list.iterator();
  }


  /**
   * Description of the Method
   *
   * @param primitive  PARAM
   * @param priority   PARAM
   * @param style      PARAM
   * @param sequence   PARAM
   * @return           Returns
   */
  private List explodeOne( CSSPrimitiveValue primitive,
      String priority,
      CSSStyleDeclaration style,
      int sequence ) {

    List list = new ArrayList();
    XRValueImpl val = null;
    val = new XRValueImpl( primitive, priority );
    list.add( new XRPropertyImpl( style, CSSName.BORDER_COLOR_TOP, sequence, val ) );
    list.add( new XRPropertyImpl( style, CSSName.BORDER_COLOR_RIGHT, sequence, val ) );
    list.add( new XRPropertyImpl( style, CSSName.BORDER_COLOR_BOTTOM, sequence, val ) );
    list.add( new XRPropertyImpl( style, CSSName.BORDER_COLOR_LEFT, sequence, val ) );
    return list;
  }
}

