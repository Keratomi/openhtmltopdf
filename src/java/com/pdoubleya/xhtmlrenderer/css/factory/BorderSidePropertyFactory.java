/*
 * {{{ header & license
 * BorderSidePropertyFactory.java
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
 * Description of the Class
 *
 * @author    Patrick Wright
 * @created   August 2, 2004
 */
public class BorderSidePropertyFactory extends AbstractPropertyFactory {
  /** Description of the Field */
  private static BorderSidePropertyFactory _instance;

  /** Description of the Field */
  private final static Map PROP_EXPLODE;


  /** Constructor for the BorderSidePropertyFactory object */
  private BorderSidePropertyFactory() { }


  /**
   * Description of the Method
   *
   * @return   Returns
   */
  public static synchronized PropertyFactory instance() {
    if ( _instance == null ) {
      _instance = new BorderSidePropertyFactory();
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

    List explodes = (List)PROP_EXPLODE.get( propName );
    if ( explodes == null ) {
      // TODO: error
      return list.iterator();
    }

    // CAREFUL: note that with steadyState parser impl, their value class impl
    // both primitive and value list interfaces! use getCssValueType(), not instanceof!!
    if ( cssValue.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE ) {
      // TODO
    } else {
      // is a value list
      CSSValueList vList = (CSSValueList)cssValue;

      // border-style explodes differently based on number of supplied values
      CSSPrimitiveValue primitive = null;
      XRValueImpl val = null;

      // treat this as exploded border-style, border-width and border-color
      switch ( vList.getLength() ) {
          case 3:
            primitive = (CSSPrimitiveValue)vList.item( 2 );
            list.add( newProperty( (String)explodes.get( 2 ), primitive, priority, style, sequence ) );
          // fall thru;

          case 2:
            primitive = (CSSPrimitiveValue)vList.item( 1 );
            list.add( newProperty( (String)explodes.get( 1 ), primitive, priority, style, sequence ) );
          // fall thru

          case 1:
            primitive = (CSSPrimitiveValue)vList.item( 0 );
            list.add( newProperty( (String)explodes.get( 0 ), primitive, priority, style, sequence ) );
            break;
          default:
          // TODO: error!
      }
    }
    return list.iterator();
  }

  static {
    PROP_EXPLODE = new HashMap();
    List l = new ArrayList();
    l.add( CSSName.BORDER_WIDTH_TOP );
    l.add( CSSName.BORDER_STYLE_TOP );
    l.add( CSSName.BORDER_COLOR_TOP );
    PROP_EXPLODE.put( CSSName.BORDER_TOP_SHORTHAND, l );

    l = new ArrayList();
    l.add( CSSName.BORDER_WIDTH_RIGHT );
    l.add( CSSName.BORDER_STYLE_RIGHT );
    l.add( CSSName.BORDER_COLOR_RIGHT );
    PROP_EXPLODE.put( CSSName.BORDER_RIGHT_SHORTHAND, l );

    l = new ArrayList();
    l.add( CSSName.BORDER_WIDTH_BOTTOM );
    l.add( CSSName.BORDER_STYLE_BOTTOM );
    l.add( CSSName.BORDER_COLOR_BOTTOM );
    PROP_EXPLODE.put( CSSName.BORDER_BOTTOM_SHORTHAND, l );

    l = new ArrayList();
    l.add( CSSName.BORDER_WIDTH_LEFT );
    l.add( CSSName.BORDER_STYLE_LEFT );
    l.add( CSSName.BORDER_COLOR_LEFT );
    PROP_EXPLODE.put( CSSName.BORDER_LEFT_SHORTHAND, l );
  }
}

