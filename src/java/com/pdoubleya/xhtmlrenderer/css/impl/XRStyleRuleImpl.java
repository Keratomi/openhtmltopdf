/*
 * {{{ header & license
 * XRStyleRuleImpl.java
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
package com.pdoubleya.xhtmlrenderer.css.impl;

import java.util.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSStyleRule;
import org.w3c.dom.css.CSSStyleSheet;

import com.pdoubleya.xhtmlrenderer.css.*;

import org.joshy.html.box.Box;

/**
 * An styling rule with selector; wraps a DOM CSSStyleRule, for example from a
 * SAC parser. Note the assumption is that selectors are always single
 * selectors, not comma-separated multi-selectors.
 *
 * @author    Patrick Wright
 * @created   August 1, 2004
 */
// NOTE: most of the rule-parsing happens in superclass, not here
public class XRStyleRuleImpl extends XRSheetRuleImpl implements XRStyleRule {
  public static final StyleRuleComparator STYLE_RULE_COMPARATOR = new StyleRuleComparator();
  
  /** Constant list of CSS pseudo-class names, without leading ":" */
  private final static List PSEUDO_CLASS_NAMES;

  /** Constant list of pseudo-element names, without leading ":" */
  private final static List PSEUDO_ELEMENT_NAMES;

  /**
   * The CSSStyleRule we are wrapping--be careful as our superclass also stores
   * a reference but to CSSRule (HMM)
   */
  private CSSStyleRule _domStyleRule;

  /** The selector text. */
  private String _selector;

  /** The specificity for this selector. */
  private int _specificity;


  /**
   * seq of 0
   *
   * @param sheet        PARAM
   * @param cssRule      PARAM
   * @param propNames    PARAM
   * @param isImportant  PARAM
   */
  public XRStyleRuleImpl( XRStyleSheet sheet, CSSRule cssRule, List propNames, boolean isImportant ) {
    this( sheet, cssRule, propNames, 0, isImportant );
  }


  /**
   * Constructor for the XRStyleRuleImpl object
   *
   * @param sheet        PARAM
   * @param cssRule      PARAM
   * @param sequence     PARAM
   * @param propNames    PARAM
   * @param isImportant  PARAM
   */
  public XRStyleRuleImpl( XRStyleSheet sheet, CSSRule cssRule, List propNames, int sequence, boolean isImportant ) {
    this( sheet, cssRule, ( (CSSStyleRule)cssRule ).getSelectorText(), propNames, sequence, isImportant );
  }


  /**
   * call this when the CSSStyleRule has more than one (comma-sep) selector, and
   * you want to specify wich selector this is a rule for
   *
   * @param sheet        PARAM
   * @param cssRule      PARAM
   * @param selector     PARAM
   * @param sequence     PARAM
   * @param propNames    PARAM
   * @param isImportant  PARAM
   */
  public XRStyleRuleImpl( XRStyleSheet sheet, CSSRule cssRule, String selector, List propNames, int sequence, boolean isImportant ) {
    super( sheet, cssRule, propNames, sequence, isImportant );
    _domStyleRule = (CSSStyleRule)cssRule;
    _selector = normalizeSelector( selector );
  }


  /**
   * The string selector
   *
   * @return   Returns
   */
  public String cssSelectorText() {
    return _selector;
  }


  /**
   * Specificity of the selector -- See CSS2 spec section 6.4.3
   *
   * @return   Returns
   */
  public synchronized int selectorSpecificity() {
    if ( _specificity == 0 ) {
      int idCnt = 0;
      int elemCnt = 0;
      int otherCnt = 0;

      String parts[] = _selector.split( " " );
      for ( int i = 0, len = parts.length; i < len; i++ ) {
        String p = parts[i];

        // handle single-char elements: + > * . #
        char fc = p.charAt( 0 );
        switch ( fc ) {
            case '+':// fall thru
            case '>':
              continue;
            case '*':
              if ( p.length() == 1 ) {
                continue;
              }
              break;
            case '.':// fall thru
            case '#':
              // not an element, don't count
              break;
            default:
              // should be an element
              elemCnt++;
        }

        // count ids
        if ( p.indexOf( "#" ) >= 0 ) {
          idCnt++;
        }

        // and attrs
        if ( p.indexOf( "[" ) >= 0 ) {
          otherCnt++;
        }

        // and classes
        String cls[] = p.split( "\\." );
        otherCnt += ( cls.length > 0 ? cls.length - 1 : 0 );

        // and pseudo classes
        int n = p.indexOf( ":" );
        if ( n > 0 ) {
          String pseudo = p.substring( n );
          if ( PSEUDO_CLASS_NAMES.contains( pseudo ) ) {
            otherCnt++;
          }
        }
      }
      /*
       * CLEAN: DBG
       * System.out.println("  idCnt    " + idCnt);
       * System.out.println("  otherCnt " + otherCnt);
       * System.out.println("  elemCnt  " + elemCnt);
       */
      _specificity = new Integer( idCnt + "" + otherCnt + "" + elemCnt ).intValue();
    }
    return _specificity;
  }


  /**
   * String representation of this stylerule, intended for visual review
   * (printing and reading).
   *
   * @return   Returns
   */
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append( cssSelectorText() + " ( " + getStyleSheet().orginLabel() + " )\n" );
    sb.append( "  specificity: " + selectorSpecificity() + "\n" );
    sb.append( "  " + ( isImportant() ? "" : "not " ) + "important" + "\n" );
    Iterator iter = listXRProperties();
    while ( iter.hasNext() ) {
      sb.append( "  " + iter.next() + "\n" );
    }
    return sb.toString();
  }


  /**
   * makes sure that descendant and adjacent selectors have surrounding
   * whitespace e.g. H1>LI becomes H1 > LI, this to make specificity check
   * easier
   *
   * @param selector  PARAM
   * @return          Returns
   */
  private String normalizeSelector( String selector ) {
    StringBuffer sb = new StringBuffer();
    for ( int i = 0, len = selector.length(); i < len; i++ ) {
      char c = selector.charAt( i );
      if ( c == '>' || c == '+' ) {
        if ( i > 0 && selector.charAt( i - 1 ) != ' ' ) {
          sb.append( ' ' );
        }
        sb.append( c );
        if ( i < ( len - 1 ) && selector.charAt( i + 1 ) != ' ' ) {
          sb.append( ' ' );
        }
      } else {
        sb.append( c );
      }
    }
    return sb.toString();
  }


  static {
    PSEUDO_CLASS_NAMES = new ArrayList();
    PSEUDO_CLASS_NAMES.add( "first-child" );
    PSEUDO_CLASS_NAMES.add( "link" );
    PSEUDO_CLASS_NAMES.add( "visited" );
    PSEUDO_CLASS_NAMES.add( "hover" );
    PSEUDO_CLASS_NAMES.add( "active" );
    PSEUDO_CLASS_NAMES.add( "lang" );

    PSEUDO_ELEMENT_NAMES = new ArrayList();
    PSEUDO_ELEMENT_NAMES.add( "first-line" );
    PSEUDO_ELEMENT_NAMES.add( "first-letter" );
    PSEUDO_ELEMENT_NAMES.add( "before" );
    PSEUDO_ELEMENT_NAMES.add( "after" );
  }
}


// :folding=indent:collapseFolds=2:
