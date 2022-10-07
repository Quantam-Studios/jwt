/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.servlet.*;
import eu.webtoolkit.jwt.utils.*;
import java.io.*;
import java.lang.ref.*;
import java.time.*;
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SizeHandle {
  private static Logger logger = LoggerFactory.getLogger(SizeHandle.class);

  public static void loadJavaScript(WApplication app) {
    app.loadJavaScript("js/SizeHandle.js", wtjs1());
  }

  static WJavaScriptPreamble wtjs1() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptConstructor,
        "SizeHandle",
        "(function(e,t,n,o,u,r,s,c,d,a,i,l,m){var p=document.createElement(\"div\");p.style.position=\"absolute\";p.style.zIndex=\"100\";if(\"v\"==t){p.style.width=o+\"px\";p.style.height=n+\"px\"}else{p.style.height=o+\"px\";p.style.width=n+\"px\"}var v,y=e.widgetPageCoordinates(d),h=e.widgetPageCoordinates(a);if(i.touches)v=e.widgetCoordinates(d,i.touches[0]);else{v=e.widgetCoordinates(d,i);e.capture(null);e.capture(p)}l-=e.px(d,\"marginLeft\");m-=e.px(d,\"marginTop\");y.x+=l-h.x;y.y+=m-h.y;v.x-=l-h.x;v.y-=m-h.y;p.style.left=y.x+\"px\";p.style.top=y.y+\"px\";p.className=s;a.appendChild(p);e.cancelEvent(i);function x(n){var o,s=e.pageCoordinates(n);o=\"h\"==t?s.x-v.x-y.x:s.y-v.y-y.y;return Math.min(Math.max(o,u),r)}function E(n){var o=x(n);\"h\"==t?p.style.left=y.x+o+\"px\":p.style.top=y.y+o+\"px\";e.cancelEvent(n)}function f(e){if(null!=p.parentNode){p.parentNode.removeChild(p);c(x(e))}}if(document.addEventListener){var g=$(\".Wt-domRoot\")[0];g||(g=a);var L=g.style[\"pointer-events\"];L||(L=\"all\");g.style[\"pointer-events\"]=\"none\";var C=document.body.style.cursor;C||(C=\"auto\");document.body.style.cursor=\"h\"==t?\"ew-resize\":\"ns-resize\";function w(e){g.style[\"pointer-events\"]=L;document.body.style.cursor=C;document.removeEventListener(\"mousemove\",E,{capture:!0});document.removeEventListener(\"mouseup\",w,{capture:!0});document.removeEventListener(\"touchmove\",E,{capture:!0});document.removeEventListener(\"touchend\",w,{capture:!0});f(e)}document.addEventListener(\"mousemove\",E,{capture:!0});document.addEventListener(\"mouseup\",w,{capture:!0});document.addEventListener(\"touchmove\",E,{capture:!0});document.addEventListener(\"touchend\",w,{capture:!0})}else{p.onmousemove=a.ontouchmove=E;p.onmouseup=a.ontouchend=function(e){a.ontouchmove=null;a.ontouchend=null;f(e)}}})");
  }
}
