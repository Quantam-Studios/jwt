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
			double stackY, int xRow, int xColumn, int yRow, int yColumn) {
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

	public static void setPenColor(final WPen pen, final WDataSeries series,
			int xRow, int xColumn, int yRow, int yColumn, int colorRole) {
		WColor color = null;
		if (yRow >= 0 && yColumn >= 0) {
			if (colorRole == ItemDataRole.MarkerPenColorRole) {
				color = series.getModel().getMarkerPenColor(yRow, yColumn);
			} else {
				if (colorRole == ItemDataRole.MarkerBrushColorRole) {
					color = series.getModel()
							.getMarkerBrushColor(yRow, yColumn);
				}
			}
		}
		if (!(color != null) && xRow >= 0 && xColumn >= 0) {
			if (colorRole == ItemDataRole.MarkerPenColorRole) {
				color = series.getModel().getMarkerPenColor(xRow, xColumn);
			} else {
				if (colorRole == ItemDataRole.MarkerBrushColorRole) {
					color = series.getModel()
							.getMarkerBrushColor(xRow, xColumn);
				}
			}
		}
		if (color != null) {
			pen.setColor(color);
		}
	}

	public static void setBrushColor(final WBrush brush,
			final WDataSeries series, int xRow, int xColumn, int yRow,
			int yColumn, int colorRole) {
		WColor color = null;
		if (yRow >= 0 && yColumn >= 0) {
			if (colorRole == ItemDataRole.MarkerBrushColorRole) {
				color = series.getModel().getMarkerBrushColor(yRow, yColumn);
			} else {
				if (colorRole == ItemDataRole.BarBrushColorRole) {
					color = series.getModel().getBarBrushColor(yRow, yColumn);
				}
			}
		}
		if (!(color != null) && xRow >= 0 && xColumn >= 0) {
			if (colorRole == ItemDataRole.MarkerBrushColorRole) {
				color = series.getModel().getMarkerBrushColor(xRow, xColumn);
			} else {
				if (colorRole == ItemDataRole.BarBrushColorRole) {
					color = series.getModel().getBarBrushColor(xRow, xColumn);
				}
			}
		}
		if (color != null) {
			brush.setColor(color);
		}
	}

	private int currentXSegment_;
	private int currentYSegment_;

	static WJavaScriptPreamble wtjs2() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"ChartCommon",
				"function(v){function z(a,b,e,c){function d(f){return e?b[f]:b[l-1-f]}function j(f){for(;d(f)[2]===t||d(f)[2]===w;)f--;return f}var q=h;if(c)q=i;var l=b.length;c=Math.floor(l/2);c=j(c);var x=0,m=l,k=false;if(d(0)[q]>a)return e?-1:l;if(d(l-1)[q]<a)return e?l:-1;for(;!k;){var g=c+1;if(g<l&&(d(g)[2]===t||d(g)[2]===w))g+=2;if(d(c)[q]>a){m=c;c=Math.floor((m+x)/2);c=j(c)}else if(d(c)[q]===a)k=true;else if(g<l&&d(g)[q]>a)k=true;else if(g<l&&d(g)[q]=== a){c=g;k=true}else{x=c;c=Math.floor((m+x)/2);c=j(c)}}return e?c:l-1-c}function C(a,b){return b[0][a]<b[b.length-1][a]}var t=2,w=3,h=0,i=1,A=this;v=v.WT.gfxUtils;var D=v.rect_top,E=v.rect_bottom,B=v.rect_left,F=v.rect_right,G=v.transform_mult;this.findClosestPoint=function(a,b,e){var c=h;if(e)c=i;var d=C(c,b);e=z(a,b,d,e);if(e<0)e=0;if(e>=b.length)return[b[b.length-1][h],b[b.length-1][i]];if(e>=b.length)e=b.length-2;if(b[e][c]===a)return[b[e][h],b[e][i]];var j=d?e+1:e-1;if(d&&b[j][2]==t)j+=2;if(!d&& j<0)return[b[e][h],b[e][i]];if(!d&&j>0&&b[j][2]==w)j-=2;d=Math.abs(a-b[e][c]);a=Math.abs(b[j][c]-a);return d<a?[b[e][h],b[e][i]]:[b[j][h],b[j][i]]};this.minMaxY=function(a,b){b=b?h:i;for(var e=a[0][b],c=a[0][b],d=1;d<a.length;++d)if(a[d][2]!==t&&a[d][2]!==w&&a[d][2]!==5){if(a[d][b]>c)c=a[d][b];if(a[d][b]<e)e=a[d][b]}return[e,c]};this.projection=function(a,b){var e=Math.cos(a);a=Math.sin(a);var c=e*a,d=-b[0]*e-b[1]*a;return[e*e,c,c,a*a,e*d+b[0],a*d+b[1]]};this.distanceSquared=function(a,b){a=[b[h]- a[h],b[i]-a[i]];return a[h]*a[h]+a[i]*a[i]};this.distanceLessThanRadius=function(a,b,e){return e*e>=A.distanceSquared(a,b)};this.toZoomLevel=function(a){return Math.floor(Math.log(a)/Math.LN2+0.5)+1};this.isPointInRect=function(a,b){var e;if(a.x!==undefined){e=a.x;a=a.y}else{e=a[0];a=a[1]}return e>=B(b)&&e<=F(b)&&a>=D(b)&&a<=E(b)};this.toDisplayCoord=function(a,b,e,c,d){if(e){a=[(a[h]-d[0])/d[2],(a[i]-d[1])/d[3]];c=[c[0]+a[i]*c[2],c[1]+a[h]*c[3]]}else{a=[(a[h]-d[0])/d[2],1-(a[i]-d[1])/d[3]];c=[c[0]+ a[h]*c[2],c[1]+a[i]*c[3]]}return G(b,c)};this.findYRange=function(a,b,e,c,d,j,q){if(a.length!==0){var l=A.toDisplayCoord([b,0],[1,0,0,1,0,0],c,d,j),x=A.toDisplayCoord([e,0],[1,0,0,1,0,0],c,d,j),m=c?i:h,k=c?h:i,g=C(m,a),f=z(l[m],a,g,c),n=z(x[m],a,g,c),o,p,r=Infinity,s=-Infinity,y=f===n&&f===a.length||f===-1&&n===-1;if(!y){if(g)if(f<0)f=0;else{f++;if(a[f]&&a[f][2]===t)f+=2}else if(f>=a.length-1)f=a.length-2;if(!g&&n<0)n=0;for(o=Math.min(f,n);o<=Math.max(f,n)&&o<a.length;++o)if(a[o][2]!==t&&a[o][2]!== w){if(a[o][k]<r)r=a[o][k];if(a[o][k]>s)s=a[o][k]}if(g&&f>0||!g&&f<a.length-1){if(g){p=f-1;if(a[p]&&a[p][2]===w)p-=2}else{p=f+1;if(a[p]&&a[p][2]===t)p+=2}o=(l[m]-a[p][m])/(a[f][m]-a[p][m]);f=a[p][k]+o*(a[f][k]-a[p][k]);if(f<r)r=f;if(f>s)s=f}if(g&&n<a.length-1||!g&&n>0){if(g){g=n+1;if(a[g][2]===t)g+=2}else{g=n-1;if(a[g][2]===w)g-=2}o=(x[m]-a[n][m])/(a[g][m]-a[n][m]);f=a[n][k]+o*(a[g][k]-a[n][k]);if(f<r)r=f;if(f>s)s=f}}var u;a=j[2]/(e-b);b=c?2:3;if(!y){u=d[b]/(s-r);u=d[b]/(d[b]/u+20);if(u>q[k])u=q[k]}c= c?[l[i]-D(d),!y?(r+s)/2-d[2]/u/2-B(d):0]:[l[h]-B(d),!y?-((r+s)/2+d[3]/u/2-E(d)):0];return{xZoom:a,yZoom:u,panPoint:c}}}}");
	}

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"WCartesianChart",
				"function(na,E,w,l){function J(a){return a===undefined}function n(){return l.modelArea}function Fa(){return l.followCurve}function oa(){return l.crosshair||Fa()!==-1}function y(){return l.isHorizontal}function e(a){if(a===g)return l.xTransform;if(a===f)return l.yTransform}function i(){return l.area}function m(){return l.insideArea}function Y(a){return J(a)?l.series:l.series[a]}function T(a){return Y(a).transform}function $a(a){return y()? z([0,1,1,0,0,0],z(T(a),[0,1,1,0,0,0])):T(a)}function Ga(a){return Y(a).curve}function ab(){return l.seriesSelection}function bb(){return l.sliders}function cb(){return l.hasToolTips}function db(){return l.coordinateOverlayPadding}function wa(){return l.curveManipulation}function G(){return l.maxZoom}function F(){return l.pens}function ia(){return l.selectedCurve}function pa(a){a.preventDefault&&a.preventDefault()}function U(a,b){E.addEventListener(a,b)}function N(a,b){E.removeEventListener(a,b)}function A(a){return a.length} function Pa(a){return a.pointerType===2||a.pointerType===3||a.pointerType===\"pen\"||a.pointerType===\"touch\"}function Ha(){if(o){if(o.tooltipTimeout){clearTimeout(o.tooltipTimeout);o.tooltipTimeout=null}if(!o.overTooltip)if(o.tooltipOuterDiv){document.body.removeChild(o.tooltipOuterDiv);o.tooltipEl=null;o.tooltipOuterDiv=null}}}function xa(){if(l.notifyTransform.x||l.notifyTransform.y){if(Ia){window.clearTimeout(Ia);Ia=null}Ia=setTimeout(function(){if(l.notifyTransform.x&&!eb(Qa,e(g))){na.emit(w.widget, \"xTransformChanged\");Z(Qa,e(g))}if(l.notifyTransform.y&&!eb(Ra,e(f))){na.emit(w.widget,\"yTransformChanged\");Z(Ra,e(f))}},ob)}}function aa(){var a,b;if(y()){a=p(i());b=t(i());return z([0,1,1,0,a,b],z(e(g),z(e(f),[0,1,1,0,-b,-a])))}else{a=p(i());b=u(i());return z([1,0,0,-1,a,b],z(e(g),z(e(f),[1,0,0,-1,-a,b])))}}function K(){return z(aa(),m())}function ba(a,b){if(J(b))b=false;a=b?a:z(ya(aa()),a);a=y()?[(a[f]-i()[1])/i()[3],(a[g]-i()[0])/i()[2]]:[(a[g]-i()[0])/i()[2],1-(a[f]-i()[1])/i()[3]];return[n()[0]+ a[g]*n()[2],n()[1]+a[f]*n()[3]]}function za(a,b){if(J(b))b=false;return ca.toDisplayCoord(a,b?[1,0,0,1,0,0]:aa(),y(),i(),n())}function Aa(){var a,b;if(y()){a=(ba([0,t(i())])[0]-n()[0])/n()[2];b=(ba([0,u(i())])[0]-n()[0])/n()[2]}else{a=(ba([p(i()),0])[0]-n()[0])/n()[2];b=(ba([q(i()),0])[0]-n()[0])/n()[2]}var c;for(c=0;c<A(bb());++c){var h=$(\"#\"+bb()[c]);if(h)(h=h.data(\"sobj\"))&&h.changeRange(a,b)}}function O(){Ha();if(cb()&&o.tooltipPosition)o.tooltipTimeout=setTimeout(function(){fb()},gb);da&&hb(function(){w.repaint(); oa()&&Sa()})}function Sa(){if(da){var a=D.getContext(\"2d\");a.clearRect(0,0,D.width,D.height);a.save();a.beginPath();a.moveTo(p(i()),t(i()));a.lineTo(q(i()),t(i()));a.lineTo(q(i()),u(i()));a.lineTo(p(i()),u(i()));a.closePath();a.clip();var b=z(ya(aa()),v),c=v[g],h=v[f];if(Fa()!==-1){b=pb(y()?b[f]:b[g],Ga(Fa()),y());h=z(aa(),z($a(Fa()),b));c=h[g];h=h[f];v[g]=c;v[f]=h}b=y()?[(b[f]-i()[1])/i()[3],(b[g]-i()[0])/i()[2]]:[(b[g]-i()[0])/i()[2],1-(b[f]-i()[1])/i()[3]];b=[n()[0]+b[g]*n()[2],n()[1]+b[f]*n()[3]]; a.fillStyle=a.strokeStyle=l.crosshairColor;a.font=\"16px sans-serif\";a.textAlign=\"right\";a.textBaseline=\"top\";var d=b[0].toFixed(2);b=b[1].toFixed(2);if(d===\"-0.00\")d=\"0.00\";if(b===\"-0.00\")b=\"0.00\";a.fillText(\"(\"+d+\",\"+b+\")\",q(i())-db()[0],t(i())+db()[1]);a.setLineDash&&a.setLineDash([1,2]);a.beginPath();a.moveTo(Math.floor(c)+0.5,Math.floor(t(i()))+0.5);a.lineTo(Math.floor(c)+0.5,Math.floor(u(i()))+0.5);a.moveTo(Math.floor(p(i()))+0.5,Math.floor(h)+0.5);a.lineTo(Math.floor(q(i()))+0.5,Math.floor(h)+ 0.5);a.stroke();a.restore()}}function qb(a){return t(a)<=t(i())+Ja&&u(a)>=u(i())-Ja&&p(a)<=p(i())+Ja&&q(a)>=q(i())-Ja}function V(a){var b=K();if(y())if(a===qa)a=ra;else if(a===ra)a=qa;if(J(a)||a===qa)if(e(g)[0]<1){e(g)[0]=1;b=K()}if(J(a)||a===ra)if(e(f)[3]<1){e(f)[3]=1;b=K()}if(J(a)||a===qa){if(p(b)>p(m())){b=p(m())-p(b);if(y())e(f)[5]=e(f)[5]+b;else e(g)[4]=e(g)[4]+b;b=K()}if(q(b)<q(m())){b=q(m())-q(b);if(y())e(f)[5]=e(f)[5]+b;else e(g)[4]=e(g)[4]+b;b=K()}}if(J(a)||a===ra){if(t(b)>t(m())){b=t(m())- t(b);if(y())e(g)[4]=e(g)[4]+b;else e(f)[5]=e(f)[5]-b;b=K()}if(u(b)<u(m())){b=u(m())-u(b);if(y())e(g)[4]=e(g)[4]+b;else e(f)[5]=e(f)[5]-b;K()}}xa()}function fb(){na.emit(w.widget,\"loadTooltip\",o.tooltipPosition[g],o.tooltipPosition[f])}function rb(){if(oa()&&(J(D)||w.canvas.width!==D.width||w.canvas.height!==D.height)){if(D){D.parentNode.removeChild(D);jQuery.removeData(E,\"oobj\");D=undefined}var a=document.createElement(\"canvas\");a.setAttribute(\"width\",w.canvas.width);a.setAttribute(\"height\",w.canvas.height); a.style.position=\"absolute\";a.style.display=\"block\";a.style.left=\"0\";a.style.top=\"0\";if(window.MSPointerEvent||window.PointerEvent){a.style.msTouchAction=\"none\";a.style.touchAction=\"none\"}w.canvas.parentNode.appendChild(a);D=a;jQuery.data(E,\"oobj\",D)}else if(!J(D)&&!oa()){D.parentNode.removeChild(D);jQuery.removeData(E,\"oobj\");D=undefined}v||(v=za([(p(n())+q(n()))/2,(t(n())+u(n()))/2]))}function ib(){return D?D:w.canvas}function Ta(a,b){if(sa){var c=Date.now();if(J(b))b=c-ea;var h={x:0,y:0},d=K(), k=sb;if(b>2*Ba){da=false;var r=Math.floor(b/Ba-1),s;for(s=0;s<r;++s){Ta(a,Ba);if(!sa){da=true;O();return}}b-=r*Ba;da=true}if(j.x===Infinity||j.x===-Infinity)j.x=j.x>0?fa:-fa;if(isFinite(j.x)){j.x/=1+jb*b;d[0]+=j.x*b;if(p(d)>p(m())){j.x+=-k*(p(d)-p(m()))*b;j.x*=0.7}else if(q(d)<q(m())){j.x+=-k*(q(d)-q(m()))*b;j.x*=0.7}if(Math.abs(j.x)<Ua)if(p(d)>p(m()))j.x=Ua;else if(q(d)<q(m()))j.x=-Ua;if(Math.abs(j.x)>fa)j.x=(j.x>0?1:-1)*fa;h.x=j.x*b}if(j.y===Infinity||j.y===-Infinity)j.y=j.y>0?fa:-fa;if(isFinite(j.y)){j.y/= 1+jb*b;d[1]+=j.y*b;if(t(d)>t(m())){j.y+=-k*(t(d)-t(m()))*b;j.y*=0.7}else if(u(d)<u(m())){j.y+=-k*(u(d)-u(m()))*b;j.y*=0.7}if(Math.abs(j.y)<0.001)if(t(d)>t(m()))j.y=0.001;else if(u(d)<u(m()))j.y=-0.001;if(Math.abs(j.y)>fa)j.y=(j.y>0?1:-1)*fa;h.y=j.y*b}d=K();P(h,ta);a=K();if(p(d)>p(m())&&p(a)<=p(m())){j.x=0;P({x:-h.x,y:0},ta);V(qa)}if(q(d)<q(m())&&q(a)>=q(m())){j.x=0;P({x:-h.x,y:0},ta);V(qa)}if(t(d)>t(m())&&t(a)<=t(m())){j.y=0;P({x:0,y:-h.y},ta);V(ra)}if(u(d)<u(m())&&u(a)>=u(m())){j.y=0;P({x:0,y:-h.y}, ta);V(ra)}if(Math.abs(j.x)<kb&&Math.abs(j.y)<kb&&qb(a)){V();sa=false;B=null;j.x=0;j.y=0;ea=null;x=[]}else{ea=c;da&&Ka(Ta)}}}function La(){var a,b,c=lb(e(g)[0])-1;if(c>=A(F().x))c=A(F().x)-1;for(a=0;a<A(F().x);++a)if(c===a)for(b=0;b<A(F().x[a]);++b)F().x[a][b].color[3]=l.penAlpha.x[b];else for(b=0;b<A(F().x[a]);++b)F().x[a][b].color[3]=0;c=lb(e(f)[3])-1;if(c>=A(F().y))c=A(F().y)-1;for(a=0;a<A(F().y);++a)if(c===a)for(b=0;b<A(F().y[a]);++b)F().y[a][b].color[3]=l.penAlpha.y[b];else for(b=0;b<A(F().y[a]);++b)F().y[a][b].color[3]= 0}function P(a,b){if(J(b))b=0;var c=ba(v);if(y())a={x:a.y,y:-a.x};if(b&ta){e(g)[4]=e(g)[4]+a.x;e(f)[5]=e(f)[5]-a.y;xa()}else if(b&mb){b=K();if(p(b)>p(m())){if(a.x>0)a.x/=1+(p(b)-p(m()))*Ma}else if(q(b)<q(m()))if(a.x<0)a.x/=1+(q(m())-q(b))*Ma;if(t(b)>t(m())){if(a.y>0)a.y/=1+(t(b)-t(m()))*Ma}else if(u(b)<u(m()))if(a.y<0)a.y/=1+(u(m())-u(b))*Ma;e(g)[4]=e(g)[4]+a.x;e(f)[5]=e(f)[5]-a.y;v[g]+=a.x;v[f]+=a.y;xa()}else{e(g)[4]=e(g)[4]+a.x;e(f)[5]=e(f)[5]-a.y;v[g]+=a.x;v[f]+=a.y;V()}a=za(c);v[g]=a[g];v[f]= a[f];O();Aa()}function Ca(a,b,c){var h=ba(v),d;d=y()?[a.y-t(i()),a.x-p(i())]:z(ya([1,0,0,-1,p(i()),u(i())]),[a.x,a.y]);a=d[0];d=d[1];var k=Math.pow(1.2,y()?c:b);b=Math.pow(1.2,y()?b:c);if(e(g)[0]*k>G()[g])k=G()[g]/e(g)[0];if(k<1||e(g)[0]!==G()[g])Na(e(g),z([k,0,0,1,a-k*a,0],e(g)));if(e(f)[3]*b>G()[f])b=G()[f]/e(f)[3];if(b<1||e(f)[3]!==G()[f])Na(e(f),z([1,0,0,b,0,d-b*d],e(f)));V();h=za(h);v[g]=h[g];v[f]=h[f];La();O();Aa()}jQuery.data(E,\"cobj\",this);var ga=this,C=na.WT;ga.config=l;var H=C.gfxUtils, z=H.transform_mult,ya=H.transform_inverted,Z=H.transform_assign,eb=H.transform_equal,tb=H.transform_apply,t=H.rect_top,u=H.rect_bottom,p=H.rect_left,q=H.rect_right,ca=C.chartCommon,ub=ca.minMaxY,pb=ca.findClosestPoint,vb=ca.projection,nb=ca.distanceLessThanRadius,lb=ca.toZoomLevel,ua=ca.isPointInRect,wb=ca.findYRange,Ba=17,Ka=function(){return window.requestAnimationFrame||window.webkitRequestAnimationFrame||window.mozRequestAnimationFrame||function(a){window.setTimeout(a,Ba)}}(),Va=false,hb=function(a){if(!Va){Va= true;Ka(function(){a();Va=false})}};if(window.MSPointerEvent||window.PointerEvent){E.style.touchAction=\"none\";w.canvas.style.msTouchAction=\"none\";w.canvas.style.touchAction=\"none\"}var ta=1,mb=2,qa=1,ra=2,g=0,f=1,ob=250,gb=500,jb=0.003,sb=2.0E-4,Ma=0.07,Ja=3,Ua=0.001,fa=1.5,kb=0.02,ja=jQuery.data(E,\"eobj2\");if(!ja){ja={};ja.contextmenuListener=function(a){pa(a);N(\"contextmenu\",ja.contextmenuListener)}}jQuery.data(E,\"eobj2\",ja);var Q={},ka=false;if(window.MSPointerEvent||window.PointerEvent)(function(){function a(){ka= A(d)>0}function b(r){if(Pa(r)){pa(r);d.push(r);a();Q.start(E,{touches:d.slice(0)})}}function c(r){if(ka)if(Pa(r)){pa(r);var s;for(s=0;s<A(d);++s)if(d[s].pointerId===r.pointerId){d.splice(s,1);break}a();Q.end(E,{touches:d.slice(0),changedTouches:[]})}}function h(r){if(Pa(r)){pa(r);var s;for(s=0;s<A(d);++s)if(d[s].pointerId===r.pointerId){d[s]=r;break}a();Q.moved(E,{touches:d.slice(0)})}}var d=[],k=jQuery.data(E,\"eobj\");if(k)if(window.PointerEvent){N(\"pointerdown\",k.pointerDown);N(\"pointerup\",k.pointerUp); N(\"pointerout\",k.pointerUp);N(\"pointermove\",k.pointerMove)}else{N(\"MSPointerDown\",k.pointerDown);N(\"MSPointerUp\",k.pointerUp);N(\"MSPointerOut\",k.pointerUp);N(\"MSPointerMove\",k.pointerMove)}jQuery.data(E,\"eobj\",{pointerDown:b,pointerUp:c,pointerMove:h});if(window.PointerEvent){U(\"pointerdown\",b);U(\"pointerup\",c);U(\"pointerout\",c);U(\"pointermove\",h)}else{U(\"MSPointerDown\",b);U(\"MSPointerUp\",c);U(\"MSPointerOut\",c);U(\"MSPointerMove\",h)}})();var D=jQuery.data(E,\"oobj\"),v=null,da=true,B=null,x=[],W=false, ha=false,M=null,Wa=null,Xa=null,j={x:0,y:0},S=null,ea=null,o=jQuery.data(E,\"tobj\");if(!o){o={overTooltip:false};jQuery.data(E,\"tobj\",o)}var va=null,sa=false,Ia=null,Qa=[0,0,0,0,0,0];Z(Qa,e(g));var Ra=[0,0,0,0,0,0];Z(Ra,e(f));var Na=function(a,b){Z(a,b);xa()};w.combinedTransform=aa;this.updateTooltip=function(a){Ha();if(a)if(o.tooltipPosition){o.toolTipEl=document.createElement(\"div\");o.toolTipEl.className=l.ToolTipInnerStyle;o.toolTipEl.innerHTML=a;o.tooltipOuterDiv=document.createElement(\"div\"); o.tooltipOuterDiv.className=l.ToolTipOuterStyle;document.body.appendChild(o.tooltipOuterDiv);o.tooltipOuterDiv.appendChild(o.toolTipEl);var b=C.widgetPageCoordinates(w.canvas);a=o.tooltipPosition[g]+b.x;b=o.tooltipPosition[f]+b.y;C.fitToWindow(o.tooltipOuterDiv,a+10,b+10,a-10,b-10);$(o.toolTipEl).mouseenter(function(){o.overTooltip=true});$(o.toolTipEl).mouseleave(function(){o.overTooltip=false})}};this.mouseMove=function(a,b){setTimeout(function(){setTimeout(Ha,200);if(!ka){var c=C.widgetCoordinates(w.canvas, b);if(ua(c,i())){if(!o.tooltipEl&&cb()){o.tooltipPosition=[c.x,c.y];o.tooltipTimeout=setTimeout(function(){fb()},gb)}if(oa()&&da){v=[c.x,c.y];hb(Sa)}}}},0)};this.mouseOut=function(){setTimeout(Ha,200)};this.mouseDown=function(a,b){if(!ka){a=C.widgetCoordinates(w.canvas,b);if(ua(a,i()))B=a}};this.mouseUp=function(){ka||(B=null)};this.mouseDrag=function(a,b){if(!ka)if(B!==null){a=C.widgetCoordinates(w.canvas,b);if(ua(a,i())){if(C.buttons===1)if(wa()&&Y(ia())){b=ia();var c;c=y()?a.x-B.x:a.y-B.y;Z(T(b), z([1,0,0,1,0,c/e(f)[3]],T(b)));O()}else l.pan&&P({x:a.x-B.x,y:a.y-B.y});B=a}}};this.clicked=function(a,b){if(!ka)if(B===null)if(ab()){a=C.widgetCoordinates(w.canvas,b);na.emit(w.widget,\"seriesSelected\",a.x,a.y)}};this.mouseWheel=function(a,b){a=(b.metaKey<<3)+(b.altKey<<2)+(b.ctrlKey<<1)+b.shiftKey;var c=l.wheelActions[a];if(!J(c)){var h=C.widgetCoordinates(w.canvas,b);if(ua(h,i())){var d=C.normalizeWheel(b);if(a===0&&wa()){a=ia();var k=-d.spinY;if(Y(a)){c=$a(a);c=tb(c,Ga(a));c=ub(c,y());c=(c[0]+ c[1])/2;C.cancelEvent(b);b=Math.pow(1.2,k);Z(T(a),z([1,0,0,b,0,c-b*c],T(a)));O();return}}if((c===4||c===5||c===6)&&l.pan){a=e(g)[4];k=e(f)[5];if(c===6)P({x:-d.pixelX,y:-d.pixelY});else if(c===5)P({x:0,y:-d.pixelX-d.pixelY});else c===4&&P({x:-d.pixelX-d.pixelY,y:0});if(a!==e(g)[4]||k!==e(f)[5])C.cancelEvent(b)}else if(l.zoom){C.cancelEvent(b);k=-d.spinY;if(k===0)k=-d.spinX;if(c===1)Ca(h,0,k);else if(c===0)Ca(h,k,0);else if(c===2)Ca(h,k,k);else if(c===3)d.pixelX!==0?Ca(h,k,0):Ca(h,0,k)}}}};var xb=function(){ab()&& na.emit(w.widget,\"seriesSelected\",B.x,B.y)};Q.start=function(a,b,c){W=A(b.touches)===1;ha=A(b.touches)===2;if(W){sa=false;a=C.widgetCoordinates(w.canvas,b.touches[0]);if(!ua(a,i()))return;va=oa()&&nb(v,[a.x,a.y],30)?1:0;ea=Date.now();B=a;if(va!==1){c||(S=window.setTimeout(xb,200));U(\"contextmenu\",ja.contextmenuListener)}C.capture(null);C.capture(ib())}else if(ha&&(l.zoom||wa())){if(S){window.clearTimeout(S);S=null}sa=false;x=[C.widgetCoordinates(w.canvas,b.touches[0]),C.widgetCoordinates(w.canvas, b.touches[1])].map(function(h){return[h.x,h.y]});if(!x.every(function(h){return ua(h,i())})){ha=null;return}C.capture(null);C.capture(ib());M=Math.atan2(x[1][1]-x[0][1],x[1][0]-x[0][0]);Wa=[(x[0][0]+x[1][0])/2,(x[0][1]+x[1][1])/2];c=Math.abs(Math.sin(M));a=Math.abs(Math.cos(M));M=c<Math.sin(0.125*Math.PI)?0:a<Math.cos(0.375*Math.PI)?Math.PI/2:Math.tan(M)>0?Math.PI/4:-Math.PI/4;Xa=vb(M,Wa)}else return;pa(b)};Q.end=function(a,b){if(S){window.clearTimeout(S);S=null}window.setTimeout(function(){N(\"contextmenu\", ja.contextmenuListener)},0);var c=Array.prototype.slice.call(b.touches),h=A(c)===0;h||function(){var d;for(d=0;d<A(b.changedTouches);++d)(function(){for(var k=b.changedTouches[d].identifier,r=0;r<A(c);++r)if(c[r].identifier===k){c.splice(r,1);return}})()}();h=A(c)===0;W=A(c)===1;ha=A(c)===2;if(h){Oa=null;if(va===0&&(isFinite(j.x)||isFinite(j.y))&&l.rubberBand){ea=Date.now();sa=true;Ka(Ta)}else{va===1&&ga.mouseUp(null,null);c=[];ea=Xa=Wa=M=null}va=null}else if(W||ha)Q.start(a,b,true)};var Oa=null, la=null,Ya=null;Q.moved=function(a,b){if(W||ha)if(!(W&&B==null)){pa(b);la=C.widgetCoordinates(w.canvas,b.touches[0]);if(A(b.touches)>1)Ya=C.widgetCoordinates(w.canvas,b.touches[1]);if(W&&S&&!nb([la.x,la.y],[B.x,B.y],3)){window.clearTimeout(S);S=null}Oa||(Oa=setTimeout(function(){if(W&&wa()&&Y(ia())){var c=ia();if(Y(c)){var h=la,d;d=y()?(h.x-B.x)/e(f)[3]:(h.y-B.y)/e(f)[3];T(c)[5]+=d;B=h;O()}}else if(W){h=la;d=Date.now();c={x:h.x-B.x,y:h.y-B.y};var k=d-ea;ea=d;if(va===1){v[g]+=c.x;v[f]+=c.y;oa()&&da&& Ka(Sa)}else if(l.pan){j.x=c.x/k;j.y=c.y/k;P(c,l.rubberBand?mb:0)}B=h}else if(ha&&wa()&&Y(ia())){var r=y()?g:f;d=[la,Ya].map(function(I){return y()?[I.x,ma]:[Da,I.y]});c=Math.abs(x[1][r]-x[0][r]);k=Math.abs(d[1][r]-d[0][r]);var s=c>0?k/c:1;if(k===c)s=1;var ma=z(ya(aa()),[0,(x[0][r]+x[1][r])/2])[1],Ea=z(ya(aa()),[0,(d[0][r]+d[1][r])/2])[1];c=ia();if(Y(c)){Z(T(c),z([1,0,0,s,0,-s*ma+Ea],T(c)));B=h;O();x=d}}else if(ha&&l.zoom){h=ba(v);var Da=(x[0][0]+x[1][0])/2;ma=(x[0][1]+x[1][1])/2;d=[la,Ya].map(function(I){return M=== 0?[I.x,ma]:M===Math.PI/2?[Da,I.y]:z(Xa,[I.x,I.y])});c=Math.abs(x[1][0]-x[0][0]);k=Math.abs(d[1][0]-d[0][0]);var X=c>0?k/c:1;if(k===c||M===Math.PI/2)X=1;var Za=(d[0][0]+d[1][0])/2;c=Math.abs(x[1][1]-x[0][1]);k=Math.abs(d[1][1]-d[0][1]);s=c>0?k/c:1;if(k===c||M===0)s=1;Ea=(d[0][1]+d[1][1])/2;y()&&function(){var I=X;X=s;s=I;I=Za;Za=Ea;Ea=I;I=Da;Da=ma;ma=I}();if(e(g)[0]*X>G()[g])X=G()[g]/e(g)[0];if(e(f)[3]*s>G()[f])s=G()[f]/e(f)[3];if(X!==1&&(X<1||e(g)[0]!==G()[g]))Na(e(g),z([X,0,0,1,-X*Da+Za,0],e(g))); if(s!==1&&(s<1||e(f)[3]!==G()[f]))Na(e(f),z([1,0,0,s,0,-s*ma+Ea],e(f)));V();h=za(h);v[g]=h[g];v[f]=h[f];x=d;La();O();Aa()}Oa=null},1))}};this.setXRange=function(a,b,c,h){b=n()[0]+n()[2]*b;c=n()[0]+n()[2]*c;if(p(n())>q(n())){if(b>p(n()))b=p(n());if(c<q(n()))c=q(n())}else{if(b<p(n()))b=p(n());if(c>q(n()))c=q(n())}a=Ga(a);a=wb(a,b,c,y(),i(),n(),G());b=a.xZoom;c=a.yZoom;a=a.panPoint;var d=ba(v);e(g)[0]=b;if(c&&h)e(f)[3]=c;e(g)[4]=-a[g]*b;if(c&&h)e(f)[5]=-a[f]*c;xa();h=za(d);v[g]=h[g];v[f]=h[f];V();La(); O();Aa()};this.getSeries=function(a){return Ga(a)};this.rangeChangedCallbacks=[];this.updateConfig=function(a){for(var b in a)if(a.hasOwnProperty(b))l[b]=a[b];rb();La();O();Aa()};this.updateConfig({});if(window.TouchEvent&&!window.MSPointerEvent&&!window.PointerEvent){ga.touchStart=Q.start;ga.touchEnd=Q.end;ga.touchMoved=Q.moved}else{H=function(){};ga.touchStart=H;ga.touchEnd=H;ga.touchMoved=H}}");
	}

	private static final int TICK_LENGTH = 5;
	private static final int CURVE_LABEL_PADDING = 10;
	private static final int DEFAULT_CURVE_LABEL_WIDTH = 100;
	private static final int CURVE_SELECTION_DISTANCE_SQUARED = 400;

	static int toZoomLevel(double zoomFactor) {
		return (int) Math.floor(Math.log(zoomFactor) / Math.log(2.0) + 0.5) + 1;
	}
}
