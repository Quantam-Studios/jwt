/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.chart;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.lang.ref.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import javax.servlet.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for iterating over series data in a chart.
 * <p>
 * 
 * This class is specialized for rendering series data.
 * <p>
 */
public class SeriesIterator {
	private static Logger logger = LoggerFactory
			.getLogger(SeriesIterator.class);

	/**
	 * Start handling a new segment.
	 * <p>
	 * Because of a &apos;break&apos; specified in an axis, axes may be divided
	 * in one or two segments (in fact only the API limits this now to two). The
	 * iterator will iterate all segments seperately, but each time with a
	 * different clipping region specified in the painter, corresponding to that
	 * segment.
	 * <p>
	 * The <i>currentSegmentArea</i> specifies the clipping area.
	 */
	public void startSegment(int currentXSegment, int currentYSegment,
			final WRectF currentSegmentArea) {
		this.currentXSegment_ = currentXSegment;
		this.currentYSegment_ = currentYSegment;
	}

	/**
	 * End handling a particular segment.
	 * <p>
	 * 
	 * @see SeriesIterator#startSegment(int currentXSegment, int
	 *      currentYSegment, WRectF currentSegmentArea)
	 */
	public void endSegment() {
	}

	/**
	 * Start iterating a particular series.
	 * <p>
	 * Returns whether the series values should be iterated. The
	 * <i>groupWidth</i> is the width (in pixels) of a single bar group. The
	 * chart contains <i>numBarGroups</i>, and the current series is in the
	 * <i>currentBarGroup</i>&apos;th group.
	 */
	public boolean startSeries(final WDataSeries series, double groupWidth,
			int numBarGroups, int currentBarGroup) {
		return true;
	}

	/**
	 * End iterating a particular series.
	 */
	public void endSeries() {
	}

	/**
	 * Process a value.
	 * <p>
	 * Processes a value with model coordinates (<i>x</i>, <i>y</i>). The y
	 * value may differ from the model&apos;s y value, because of stacked
	 * series. The y value here corresponds to the location on the chart, after
	 * stacking.
	 * <p>
	 * The <i>stackY</i> argument is the y value from the previous series (also
	 * after stacking). It will be 0, unless this series is stacked.
	 */
	public void newValue(final WDataSeries series, double x, double y,
			double stackY, final WModelIndex xIndex, final WModelIndex yIndex) {
	}

	/**
	 * Returns the current X segment.
	 */
	public int getCurrentXSegment() {
		return this.currentXSegment_;
	}

	/**
	 * Returns the current Y segment.
	 */
	public int getCurrentYSegment() {
		return this.currentYSegment_;
	}

	public static void setPenColor(final WPen pen, final WModelIndex xIndex,
			final WModelIndex yIndex, int colorRole) {
		Object color = new Object();
		if ((yIndex != null)) {
			color = yIndex.getData(colorRole);
		}
		if ((color == null) && (xIndex != null)) {
			color = xIndex.getData(colorRole);
		}
		if (!(color == null)) {
			pen.setColor((WColor) color);
		}
	}

	public static void setBrushColor(final WBrush brush,
			final WModelIndex xIndex, final WModelIndex yIndex, int colorRole) {
		Object color = new Object();
		if ((yIndex != null)) {
			color = yIndex.getData(colorRole);
		}
		if ((color == null) && (xIndex != null)) {
			color = xIndex.getData(colorRole);
		}
		if (!(color == null)) {
			brush.setColor((WColor) color);
		}
	}

	private int currentXSegment_;
	private int currentYSegment_;

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"WCartesianChart",
				"function(G,z,w,b){function U(){return b.crosshair||b.followCurve!==-1}function ma(a){return a.pointerType===2||a.pointerType===3||a.pointerType===\"pen\"||a.pointerType===\"touch\"}function j(a){if(a===g)return b.xTransform;if(a===h)return b.yTransform}function da(){if(b.isHorizontal){var a=n(b.area),d=p(b.area);return y([0,1,1,0,a,d],y(j(g),y(j(h),[0,1,1,0,-d,-a])))}else{a=n(b.area);d=r(b.area);return y([1,0,0,-1,a,d],y(j(g),y(j(h),[1,0, 0,-1,-a,d])))}}function D(){return y(da(),b.area)}function M(a,d){if(d===undefined)d=false;a=d?a:y(na(da()),a);a=b.isHorizontal?[(a[h]-b.area[1])/b.area[3],(a[g]-b.area[0])/b.area[2]]:[(a[g]-b.area[0])/b.area[2],1-(a[h]-b.area[1])/b.area[3]];return[b.modelArea[0]+a[g]*b.modelArea[2],b.modelArea[1]+a[h]*b.modelArea[3]]}function S(a,d){if(d===undefined)d=false;if(b.isHorizontal){a=[(a[g]-b.modelArea[0])/b.modelArea[2],(a[h]-b.modelArea[1])/b.modelArea[3]];a=[b.area[0]+a[h]*b.area[2],b.area[1]+a[g]* b.area[3]]}else{a=[(a[g]-b.modelArea[0])/b.modelArea[2],1-(a[h]-b.modelArea[1])/b.modelArea[3]];a=[b.area[0]+a[g]*b.area[2],b.area[1]+a[h]*b.area[3]]}return d?a:y(da(),a)}function Aa(a,d){var f=g;if(b.isHorizontal)f=h;var e=oa(a,d);if(e<0)e=0;if(e>=d.length)e=d.length-2;if(d[e][f]===a)return[d[e][g],d[e][h]];var i=e+1;if(d[i][2]==V)i+=2;return a-d[e][f]<d[i][f]-a?[d[e][g],d[e][h]]:[d[i][g],d[i][h]]}function oa(a,d){function f(q){d[q][2]===pa&&--q;d[q][2]===V&&--q;return q}var e=g;if(b.isHorizontal)e= h;var i=d.length,k=Math.floor(i/2);k=f(k);var o=0,m=i,A=false;if(d[0][e]>a)return-1;if(d[i-1][e]<a)return i;for(;!A;){i=k+1;if(d[i][2]===V)i+=2;if(d[k][e]>a){m=k;k=Math.floor((m+o)/2);k=f(k)}else if(d[k][e]===a)A=true;else if(d[i][e]>a)A=true;else if(d[i][e]===a){k=i;A=true}else{o=k;k=Math.floor((m+o)/2);k=f(k)}}return k}function ea(){var a,d;if(b.isHorizontal){a=(M([0,p(b.area)])[0]-b.modelArea[0])/b.modelArea[2];d=(M([0,r(b.area)])[0]-b.modelArea[0])/b.modelArea[2]}else{a=(M([n(b.area),0])[0]-b.modelArea[0])/ b.modelArea[2];d=(M([s(b.area),0])[0]-b.modelArea[0])/b.modelArea[2]}var f;for(f=0;f<b.sliders.length;++f){var e=$(\"#\"+b.sliders[f]);if(e)(e=e.data(\"sobj\"))&&e.changeRange(a,d)}}function W(){N&&fa(function(){w.repaint();U()&&qa()})}function qa(){if(N){var a=B.getContext(\"2d\");a.clearRect(0,0,B.width,B.height);a.save();a.beginPath();a.moveTo(n(b.area),p(b.area));a.lineTo(s(b.area),p(b.area));a.lineTo(s(b.area),r(b.area));a.lineTo(n(b.area),r(b.area));a.closePath();a.clip();var d=y(na(da()),t),f=t[g], e=t[h];if(b.followCurve!==-1){d=Aa(b.isHorizontal?d[h]:d[g],b.series[b.followCurve]);e=y(da(),d);f=e[g];e=e[h];t[g]=f;t[h]=e}d=b.isHorizontal?[(d[h]-b.area[1])/b.area[3],(d[g]-b.area[0])/b.area[2]]:[(d[g]-b.area[0])/b.area[2],1-(d[h]-b.area[1])/b.area[3]];d=[b.modelArea[0]+d[g]*b.modelArea[2],b.modelArea[1]+d[h]*b.modelArea[3]];a.font=\"16px sans-serif\";a.textAlign=\"right\";a.textBaseline=\"top\";var i=d[0].toFixed(2);d=d[1].toFixed(2);if(i==\"-0.00\")i=\"0.00\";if(d==\"-0.00\")d=\"0.00\";a.fillText(\"(\"+i+\",\"+ d+\")\",s(b.area)-5,p(b.area)+5);a.setLineDash&&a.setLineDash([1,2]);a.beginPath();a.moveTo(Math.floor(f)+0.5,Math.floor(p(b.area))+0.5);a.lineTo(Math.floor(f)+0.5,Math.floor(r(b.area))+0.5);a.moveTo(Math.floor(n(b.area))+0.5,Math.floor(e)+0.5);a.lineTo(Math.floor(s(b.area))+0.5,Math.floor(e)+0.5);a.stroke();a.restore()}}function X(a,d){var f;if(a.x!==undefined){f=a.x;a=a.y}else{f=a[0];a=a[1]}return f>=n(d)&&f<=s(d)&&a>=p(d)&&a<=r(d)}function Ba(a){return p(a)<=p(b.area)+ha&&r(a)>=r(b.area)-ha&&n(a)<= n(b.area)+ha&&s(a)>=s(b.area)-ha}function H(a){var d=D();if(b.isHorizontal)if(a===Y)a=Z;else if(a===Z)a=Y;if(a===undefined||a===Y)if(j(g)[0]<1){j(g)[0]=1;d=D()}if(a===undefined||a===Z)if(j(h)[3]<1){j(h)[3]=1;d=D()}if(a===undefined||a===Y){if(n(d)>n(b.area)){d=n(b.area)-n(d);if(b.isHorizontal)j(h)[5]=j(h)[5]+d;else j(g)[4]=j(g)[4]+d;d=D()}if(s(d)<s(b.area)){d=s(b.area)-s(d);if(b.isHorizontal)j(h)[5]=j(h)[5]+d;else j(g)[4]=j(g)[4]+d;d=D()}}if(a===undefined||a===Z){if(p(d)>p(b.area)){d=p(b.area)-p(d); if(b.isHorizontal)j(g)[4]=j(g)[4]+d;else j(h)[5]=j(h)[5]-d;d=D()}if(r(d)<r(b.area)){d=r(b.area)-r(d);if(b.isHorizontal)j(g)[4]=j(g)[4]+d;else j(h)[5]=j(h)[5]-d;D()}}}function Ca(){if(U&&(B===undefined||w.canvas.width!==B.width||w.canvas.height!==B.height)){if(B){B.parentNode.removeChild(B);jQuery.removeData(z,\"oobj\");B=undefined}c=document.createElement(\"canvas\");c.setAttribute(\"width\",w.canvas.width);c.setAttribute(\"height\",w.canvas.height);c.style.position=\"absolute\";c.style.display=\"block\";c.style.left= \"0\";c.style.top=\"0\";c.style.msTouchAction=\"none\";w.canvas.parentNode.appendChild(c);B=c;jQuery.data(z,\"oobj\",B)}else if(B!==undefined&&!U()){B.parentNode.removeChild(B);jQuery.removeData(z,\"oobj\");B=undefined}if(t===null)t=S([(n(b.modelArea)+s(b.modelArea))/2,(p(b.modelArea)+r(b.modelArea))/2])}function Da(a,d){var f=Math.cos(a);a=Math.sin(a);var e=f*a,i=-d[0]*f-d[1]*a;return[f*f,e,e,a*a,f*i+d[0],a*i+d[1]]}function Ea(a,d,f){a=[d[g]-a[g],d[h]-a[h]];return f*f>=a[g]*a[g]+a[h]*a[h]}function ra(a,d){if(aa){var f= Date.now();if(d===undefined)d=f-I;var e={x:0,y:0},i=D(),k=Fa;if(d>2*ga){N=false;var o=Math.floor(d/ga-1),m;for(m=0;m<o;++m){ra(a,ga);if(!aa){N=true;W();return}}d-=o*ga;N=true}if(l.x===Infinity||l.x===-Infinity)l.x=l.x>0?O:-O;if(isFinite(l.x)){l.x/=1+wa*d;i[0]+=l.x*d;if(n(i)>n(b.area)){l.x+=-k*(n(i)-n(b.area))*d;l.x*=0.7}else if(s(i)<s(b.area)){l.x+=-k*(s(i)-s(b.area))*d;l.x*=0.7}if(Math.abs(l.x)<sa)if(n(i)>n(b.area))l.x=sa;else if(s(i)<s(b.area))l.x=-sa;if(Math.abs(l.x)>O)l.x=(l.x>0?1:-1)*O;e.x=l.x* d}if(l.y===Infinity||l.y===-Infinity)l.y=l.y>0?O:-O;if(isFinite(l.y)){l.y/=1+wa*d;i[1]+=l.y*d;if(p(i)>p(b.area)){l.y+=-k*(p(i)-p(b.area))*d;l.y*=0.7}else if(r(i)<r(b.area)){l.y+=-k*(r(i)-r(b.area))*d;l.y*=0.7}if(Math.abs(l.y)<0.001)if(p(i)>p(b.area))l.y=0.001;else if(r(i)<r(b.area))l.y=-0.001;if(Math.abs(l.y)>O)l.y=(l.y>0?1:-1)*O;e.y=l.y*d}i=D();P(e,ba);a=D();if(n(i)>n(b.area)&&n(a)<=n(b.area)){l.x=0;P({x:-e.x,y:0},ba);H(Y)}if(s(i)<s(b.area)&&s(a)>=s(b.area)){l.x=0;P({x:-e.x,y:0},ba);H(Y)}if(p(i)> p(b.area)&&p(a)<=p(b.area)){l.y=0;P({x:0,y:-e.y},ba);H(Z)}if(r(i)<r(b.area)&&r(a)>=r(b.area)){l.y=0;P({x:0,y:-e.y},ba);H(Z)}if(Math.abs(l.x)<xa&&Math.abs(l.y)<xa&&Ba(a)){H();aa=false;C=null;l.x=0;l.y=0;I=null;u=[]}else{I=f;N&&fa(ra)}}}function ya(a){return Math.floor(Math.log(a)/Math.LN2+0.5)+1}function ia(){var a,d,f=ya(j(g)[0])-1;if(f>=b.pens.x.length)f=b.pens.x.length-1;for(a=0;a<b.pens.x.length;++a)if(f===a)for(d=0;d<b.pens.x[a].length;++d)b.pens.x[a][d].color[3]=b.penAlpha.x[d];else for(d=0;d< b.pens.x[a].length;++d)b.pens.x[a][d].color[3]=0;f=ya(j(h)[3])-1;if(f>=b.pens.y.length)f=b.pens.y.length-1;for(a=0;a<b.pens.y.length;++a)if(f===a)for(d=0;d<b.pens.y[a].length;++d)b.pens.y[a][d].color[3]=b.penAlpha.y[d];else for(d=0;d<b.pens.y[a].length;++d)b.pens.y[a][d].color[3]=0}function P(a,d){var f=M(t);if(b.isHorizontal)a={x:a.y,y:-a.x};if(d&ba){j(g)[4]=j(g)[4]+a.x;j(h)[5]=j(h)[5]-a.y}else if(d&za){d=D();if(n(d)>n(b.area)){if(a.x>0)a.x/=1+(n(d)-n(b.area))*ja}else if(s(d)<s(b.area))if(a.x<0)a.x/= 1+(s(b.area)-s(d))*ja;if(p(d)>p(b.area)){if(a.y>0)a.y/=1+(p(d)-p(b.area))*ja}else if(r(d)<r(b.area))if(a.y<0)a.y/=1+(r(b.area)-r(d))*ja;j(g)[4]=j(g)[4]+a.x;j(h)[5]=j(h)[5]-a.y;t[g]+=a.x;t[h]+=a.y}else{j(g)[4]=j(g)[4]+a.x;j(h)[5]=j(h)[5]-a.y;t[g]+=a.x;t[h]+=a.y;H()}a=S(f);t[g]=a[g];t[h]=a[h];W();ea()}function ta(a,d,f){var e=M(t),i;i=b.isHorizontal?[a.y-p(b.area),a.x-n(b.area)]:y(na([1,0,0,-1,n(b.area),r(b.area)]),[a.x,a.y]);a=i[0];i=i[1];var k=Math.pow(1.2,b.isHorizontal?f:d);d=Math.pow(1.2,b.isHorizontal? d:f);if(j(g)[0]*k>b.maxZoom[g])k=b.maxZoom[g]/j(g)[0];if(k<1||j(g)[0]!==b.maxZoom[g])ka(j(g),y([k,0,0,1,a-k*a,0],j(g)));if(j(h)[3]*d>b.maxZoom[h])d=b.maxZoom[h]/j(h)[3];if(d<1||j(h)[3]!==b.maxZoom[h])ka(j(h),y([1,0,0,d,0,i-d*i],j(h)));H();e=S(e);t[g]=e[g];t[h]=e[h];ia();W();ea()}var ga=17,fa=function(){return window.requestAnimationFrame||window.webkitRequestAnimationFrame||window.mozRequestAnimationFrame||function(a){window.setTimeout(a,ga)}}();w.canvas.style.msTouchAction=\"none\";var V=2,pa=3,ba= 1,za=2,Y=1,Z=2,g=0,h=1,J=false;if(!window.TouchEvent&&(window.MSPointerEvent||window.PointerEvent))(function(){function a(){if(pointers.length>0&&!J)J=true;else if(pointers.length<=0&&J)J=false}function d(i){if(ma(i)){i.preventDefault();pointers.push(i);a();ca.touchStart(z,{touches:pointers.slice(0)})}}function f(i){if(J)if(ma(i)){i.preventDefault();var k;for(k=0;k<pointers.length;++k)if(pointers[k].pointerId===i.pointerId){pointers.splice(k,1);break}a();ca.touchEnd(z,{touches:pointers.slice(0),changedTouches:[]})}} function e(i){if(ma(i)){i.preventDefault();var k;for(k=0;k<pointers.length;++k)if(pointers[k].pointerId===i.pointerId){pointers[k]=i;break}a();ca.touchMoved(z,{touches:pointers.slice(0)})}}pointers=[];if(window.PointerEvent){z.addEventListener(\"pointerdown\",d);z.addEventListener(\"pointerup\",f);z.addEventListener(\"pointerout\",f);z.addEventListener(\"pointermove\",e)}else{z.addEventListener(\"MSPointerDown\",d);z.addEventListener(\"MSPointerUp\",f);z.addEventListener(\"MSPointerOut\",f);z.addEventListener(\"MSPointerMove\", e)}})();var wa=0.003,Fa=2.0E-4,ja=0.07,ha=3,sa=0.001,O=1.5,xa=0.02;jQuery.data(z,\"cobj\",this);var ca=this,x=G.WT;ca.config=b;var B=jQuery.data(z,\"oobj\"),t=null,N=true,C=null,u=[],T=false,Q=false,E=null,ua=null,va=null,l={x:0,y:0},I=null,la=null;G=x.gfxUtils;var y=G.transform_mult,na=G.transform_inverted,ka=G.transform_assign,p=G.rect_top,r=G.rect_bottom,n=G.rect_left,s=G.rect_right,aa=false;this.mouseMove=function(a,d){setTimeout(function(){if(!J){var f=x.widgetCoordinates(w.canvas,d);if(X(f,b.area))if(U()&& N){t=[f.x,f.y];fa(qa)}}},0)};this.mouseDown=function(a,d){if(!J){a=x.widgetCoordinates(w.canvas,d);if(X(a,b.area))C=a}};this.mouseUp=function(){J||(C=null)};this.mouseDrag=function(a,d){if(!J)if(C!==null){a=x.widgetCoordinates(w.canvas,d);if(X(a,b.area)){x.buttons===1&&b.pan&&P({x:a.x-C.x,y:a.y-C.y});C=a}}};this.mouseWheel=function(a,d){var f=x.widgetCoordinates(w.canvas,d);if(X(f,b.area)){a=x.normalizeWheel(d);if(!d.ctrlKey&&b.pan){f=j(g)[4];var e=j(h)[5];P({x:-a.pixelX,y:-a.pixelY});if(f!==j(g)[4]|| e!==j(h)[5])x.cancelEvent(d)}else if(d.ctrlKey&&b.zoom){x.cancelEvent(d);e=-a.spinY;if(e===0)e=-a.spinX;if(d.shiftKey&&!d.altKey)ta(f,0,e);else d.altKey&&!d.shiftKey?ta(f,e,0):ta(f,e,e)}}};this.touchStart=function(a,d){T=d.touches.length===1;Q=d.touches.length===2;if(T){aa=false;a=x.widgetCoordinates(w.canvas,d.touches[0]);if(!X(a,b.area))return;la=U()&&Ea(t,[a.x,a.y],30)?1:0;I=Date.now();C=a;x.capture(null);x.capture(w.canvas)}else if(Q&&b.zoom){aa=false;u=[x.widgetCoordinates(w.canvas,d.touches[0]), x.widgetCoordinates(w.canvas,d.touches[1])].map(function(e){return[e.x,e.y]});if(!u.every(function(e){return X(e,b.area)})){Q=null;return}x.capture(null);x.capture(w.canvas);E=Math.atan2(u[1][1]-u[0][1],u[1][0]-u[0][0]);ua=[(u[0][0]+u[1][0])/2,(u[0][1]+u[1][1])/2];a=Math.abs(Math.sin(E));var f=Math.abs(Math.cos(E));E=a<Math.sin(0.125*Math.PI)?0:f<Math.cos(0.375*Math.PI)?Math.PI/2:Math.tan(E)>0?Math.PI/4:-Math.PI/4;va=Da(E,ua)}else return;d.preventDefault&&d.preventDefault()};this.touchEnd=function(a, d){var f=Array.prototype.slice.call(d.touches),e=f.length===0;T=f.length===1;Q=f.length===2;e||function(){var i;for(i=0;i<d.changedTouches.length;++i)(function(){for(var k=d.changedTouches[i].identifier,o=0;o<f.length;++o)if(f[o].identifier===k){f.splice(o,1);return}})()}();e=f.length===0;T=f.length===1;Q=f.length===2;if(e){if(la===0&&(isFinite(l.x)||isFinite(l.y))&&b.rubberBand){I=Date.now();aa=true;fa(ra)}else{ca.mouseUp(null,null);f=[];va=ua=E=null;if(I!=null){Date.now();I=null}}la=null}else if(T|| Q)ca.touchStart(a,d)};this.touchMoved=function(a,d){if(T||Q)if(T){if(C!==null){a=x.widgetCoordinates(w.canvas,d.touches[0]);var f=Date.now(),e={x:a.x-C.x,y:a.y-C.y},i=f-I;I=f;if(la===1){t[g]+=e.x;t[h]+=e.y;U()&&N&&fa(qa)}else if(b.pan){if(a.x<b.area[0]||a.x>b.area[0]+b.area[2]){l={x:0,y:0};return}if(a.y<b.area[1]||a.y>b.area[1]+b.area[3]){l={x:0,y:0};return}l.x=e.x/i;l.y=e.y/i;P(e,b.rubberBand?za:0)}d.preventDefault&&d.preventDefault();C=a}}else if(Q&&b.zoom){d.preventDefault&&d.preventDefault(); a=M(t);var k=(u[0][0]+u[1][0])/2,o=(u[0][1]+u[1][1])/2;d=[x.widgetCoordinates(w.canvas,d.touches[0]),x.widgetCoordinates(w.canvas,d.touches[1])].map(function(v){return E===0?[v.x,o]:E===Math.PI/2?[k,v.y]:y(va,[v.x,v.y])});f=Math.abs(u[1][0]-u[0][0]);e=Math.abs(d[1][0]-d[0][0]);var m=f>0?e/f:1;if(e===f||E===Math.PI/2)m=1;var A=(d[0][0]+d[1][0])/2;f=Math.abs(u[1][1]-u[0][1]);e=Math.abs(d[1][1]-d[0][1]);var q=f?e/f:1;if(e===f||E===0)q=1;var F=(d[0][1]+d[1][1])/2;b.isHorizontal&&function(){var v=m;m= q;q=v;v=A;A=F;F=v;v=k;k=o;o=v}();if(j(g)[0]*m>b.maxZoom[g])m=b.maxZoom[g]/j(g)[0];if(j(h)[3]*q>b.maxZoom[h])q=b.maxZoom[h]/j(h)[3];if(m!==1&&(m<1||j(g)[0]!==b.maxZoom[g]))ka(j(g),y([m,0,0,1,-m*k+A,0],j(g)));if(q!==1&&(q<1||j(h)[3]!==b.maxZoom[h]))ka(j(h),y([1,0,0,q,0,-q*o+F],j(h)));H();a=S(a);t[g]=a[g];t[h]=a[h];u=d;ia();W();ea()}};this.setXRange=function(a,d,f){d=b.modelArea[0]+b.modelArea[2]*d;f=b.modelArea[0]+b.modelArea[2]*f;if(d<n(b.modelArea))d=n(b.modelArea);if(f>s(b.modelArea))f=s(b.modelArea); var e=b.series[a];if(e.length!==0){a=S([d,0],true);var i=S([f,0],true),k=b.isHorizontal?h:g,o=b.isHorizontal?g:h,m=oa(a[k],e);if(m<0)m=0;else{m++;if(e[m][2]===V)m+=2}var A=oa(i[k],e),q,F,v=Infinity,K=-Infinity;for(q=m;q<=A&&q<e.length;++q)if(e[q][2]!==V&&e[q][2]!==pa){if(e[q][o]<v)v=e[q][o];if(e[q][o]>K)K=e[q][o]}if(m>0){F=m-1;if(e[F][2]===pa)F-=2;q=(a[k]-e[F][k])/(e[m][k]-e[F][k]);m=e[F][o]+q*(e[m][o]-e[F][o]);if(m<v)v=m;if(m>K)K=m}if(A<e.length-1){m=A+1;if(e[m][2]===V)m+=2;q=(i[k]-e[A][k])/(e[m][k]- e[A][k]);m=e[A][o]+q*(e[m][o]-e[A][o]);if(m<v)v=m;if(m>K)K=m}d=b.modelArea[2]/(f-d);e=b.isHorizontal?2:3;f=b.area[e]/(K-v);f=b.area[e]/(b.area[e]/f+20);if(f>b.maxZoom[o])f=b.maxZoom[o];a=b.isHorizontal?[a[h]-p(b.area),(v+K)/2-b.area[2]/f/2-n(b.area)]:[a[g]-n(b.area),-((v+K)/2+b.area[3]/f/2-r(b.area))];o=M(t);j(g)[0]=d;j(h)[3]=f;j(g)[4]=-a[g]*d;j(h)[5]=-a[h]*f;a=S(o);t[g]=a[g];t[h]=a[h];H();ia();W();ea()}};this.getSeries=function(a){return b.series[a]};this.rangeChangedCallbacks=[];this.updateConfig= function(a){for(var d in a)if(a.hasOwnProperty(d))b[d]=a[d];Ca();ia();W();ea()};this.updateConfig({})}");
	}

	private static final int TICK_LENGTH = 5;

	static int toZoomLevel(double zoomFactor) {
		return (int) Math.floor(Math.log(zoomFactor) / Math.log(2.0) + 0.5) + 1;
	}
}
