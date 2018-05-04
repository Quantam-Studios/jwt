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
 * A curve label.
 * <p>
 * 
 * Curve labels can be added with
 * {@link WCartesianChart#addCurveLabel(CurveLabel label)
 * WCartesianChart#addCurveLabel()}. They are associated with a particular
 * series, and are drawn at the given point in model coordinates. When the chart
 * is transformed (zoom or pan) or the associated series is manipulated, the
 * curve label&apos;s position will change, but not its size.
 * <p>
 * <div align="center"> <img src="doc-files//CurveLabel.png"
 * alt="A curve label">
 * <p>
 * <strong>A curve label</strong>
 * </p>
 * </div>
 */
public class CurveLabel {
	private static Logger logger = LoggerFactory.getLogger(CurveLabel.class);

	/**
	 * Create a new curve label.
	 * <p>
	 * Create a new curve label for given series, at the given point with the
	 * given text.
	 */
	public CurveLabel(final WDataSeries series, final WPointF point,
			final String label) {
		this.series_ = series;
		this.point_ = point;
		this.label_ = label;
		this.offset_ = new WPointF(60, -20);
		this.width_ = 0;
		this.linePen_ = new WPen(new WColor(0, 0, 0));
		this.textPen_ = new WPen(new WColor(0, 0, 0));
		this.boxBrush_ = new WBrush(new WColor(255, 255, 255));
		this.markerBrush_ = new WBrush(new WColor(0, 0, 0));
	}

	/**
	 * Set the series this curve label is associated with.
	 */
	public void setSeries(final WDataSeries series) {
		this.series_ = series;
	}

	/**
	 * Get the series this curve label is associated with.
	 * <p>
	 * 
	 * @see CurveLabel#setSeries(WDataSeries series)
	 */
	public WDataSeries getSeries() {
		return this.series_;
	}

	/**
	 * Set the point in model coordinates this label is associated with.
	 */
	public void setPoint(final WPointF point) {
		this.point_ = point;
	}

	/**
	 * Get the point in model coordinates this label is associated with.
	 * <p>
	 * 
	 * @see CurveLabel#setPoint(WPointF point)
	 */
	public WPointF getPoint() {
		return this.point_;
	}

	/**
	 * Set the label that should be drawn in the box.
	 */
	public void setLabel(final String label) {
		this.label_ = label;
	}

	/**
	 * Get the label that should be drawn in the box.
	 * <p>
	 * 
	 * @see CurveLabel#setLabel(String label)
	 */
	public String getLabel() {
		return this.label_;
	}

	/**
	 * Set the offset the text should be placed at.
	 * <p>
	 * The offset is defined in pixels, with x values going from left to right,
	 * and y values from top to bottom.
	 * <p>
	 * The default offset is (60, -20), which means the middle of the
	 * {@link CurveLabel#getLabel() getLabel()} is drawn 60 pixels to the right,
	 * and 20 pixels above the point.
	 */
	public void setOffset(final WPointF offset) {
		this.offset_ = offset;
	}

	/**
	 * Get the offset the text should be placed at.
	 * <p>
	 * 
	 * @see CurveLabel#setOffset(WPointF offset)
	 */
	public WPointF getOffset() {
		return this.offset_;
	}

	/**
	 * Set the width of the box in pixels.
	 * <p>
	 * If the width is 0 (the default), server side font metrics will be used to
	 * determine the size of the box.
	 */
	public void setWidth(int width) {
		this.width_ = width;
	}

	/**
	 * Get the width of the box in pixels.
	 * <p>
	 * 
	 * @see CurveLabel#setWidth(int width)
	 */
	public int getWidth() {
		return this.width_;
	}

	/**
	 * Set the pen to use for the connecting line.
	 * <p>
	 * This sets the pen to use for the line connecting the
	 * {@link CurveLabel#getPoint() point} to the box with the
	 * {@link CurveLabel#getLabel() label} at {@link CurveLabel#getOffset()
	 * offset} pixels from the point.
	 */
	public void setLinePen(final WPen pen) {
		this.linePen_ = pen;
	}

	/**
	 * Get the pen to use for the connecting line.
	 * <p>
	 * 
	 * @see CurveLabel#setLinePen(WPen pen)
	 */
	public WPen getLinePen() {
		return this.linePen_;
	}

	/**
	 * Set the pen for the text in the box.
	 */
	public void setTextPen(final WPen pen) {
		this.textPen_ = pen;
	}

	/**
	 * Get the pen for the text in the box.
	 * <p>
	 * 
	 * @see CurveLabel#setTextPen(WPen pen)
	 */
	public WPen getTextPen() {
		return this.textPen_;
	}

	/**
	 * Set the brush to use for the box around the text.
	 * <p>
	 * This sets the brush used to fill the box with the text defined in
	 * {@link CurveLabel#getLabel() getLabel()}.
	 */
	public void setBoxBrush(final WBrush brush) {
		this.boxBrush_ = brush;
	}

	/**
	 * Get the brush to use for the box around the text.
	 * <p>
	 * 
	 * @see CurveLabel#setBoxBrush(WBrush brush)
	 */
	public WBrush getBoxBrush() {
		return this.boxBrush_;
	}

	/**
	 * Set the brush used to fill the circle at {@link CurveLabel#getPoint()
	 * getPoint()}.
	 */
	public void setMarkerBrush(final WBrush brush) {
		this.markerBrush_ = brush;
	}

	/**
	 * Get the brush used to fill the circle at {@link CurveLabel#getPoint()
	 * getPoint()}.
	 * <p>
	 * 
	 * @see CurveLabel#setMarkerBrush(WBrush brush)
	 */
	public WBrush getMarkerBrush() {
		return this.markerBrush_;
	}

	public void render(final WPainter painter) {
		WRectF rect = null;
		{
			double rectWidth = DEFAULT_CURVE_LABEL_WIDTH;
			if (this.getWidth() != 0) {
				rectWidth = this.getWidth();
			} else {
				if (!EnumUtils.mask(painter.getDevice().getFeatures(),
						WPaintDevice.FeatureFlag.HasFontMetrics).isEmpty()) {
					WMeasurePaintDevice device = new WMeasurePaintDevice(
							painter.getDevice());
					WPainter measPainter = new WPainter(device);
					measPainter.drawText(new WRectF(0, 0, 100, 100), EnumSet
							.of(AlignmentFlag.AlignMiddle,
									AlignmentFlag.AlignCenter),
							TextFlag.TextSingleLine, this.getLabel(),
							(WPointF) null);
					rectWidth = device.getBoundingRect().getWidth()
							+ CURVE_LABEL_PADDING / 2;
				}
			}
			rect = new WRectF(this.getOffset().getX() - rectWidth / 2, this
					.getOffset().getY() - 10, rectWidth, 20).getNormalized();
		}
		WPointF closestAnchor = new WPointF();
		{
			List<WPointF> anchorPoints = new ArrayList<WPointF>();
			anchorPoints.add(new WPointF(rect.getLeft(), rect.getCenter()
					.getY()));
			anchorPoints.add(new WPointF(rect.getRight(), rect.getCenter()
					.getY()));
			anchorPoints
					.add(new WPointF(rect.getCenter().getX(), rect.getTop()));
			anchorPoints.add(new WPointF(rect.getCenter().getX(), rect
					.getBottom()));
			double minSquareDist = Double.POSITIVE_INFINITY;
			for (int k = 0; k < anchorPoints.size(); ++k) {
				final WPointF anchorPoint = anchorPoints.get(k);
				double d = anchorPoint.getX() * anchorPoint.getX()
						+ anchorPoint.getY() * anchorPoint.getY();
				if (d < minSquareDist
						&& (k == 0 || !checkIntersectVertical(new WPointF(),
								anchorPoint, rect.getTop(), rect.getBottom(),
								rect.getLeft()))
						&& (k == 1 || !checkIntersectVertical(new WPointF(),
								anchorPoint, rect.getTop(), rect.getBottom(),
								rect.getRight()))
						&& (k == 2 || !checkIntersectHorizontal(new WPointF(),
								anchorPoint, rect.getLeft(), rect.getRight(),
								rect.getTop()))
						&& (k == 3 || !checkIntersectHorizontal(new WPointF(),
								anchorPoint, rect.getLeft(), rect.getRight(),
								rect.getBottom()))) {
					closestAnchor = anchorPoint;
					minSquareDist = d;
				}
			}
		}
		WTransform translation = painter.getWorldTransform();
		painter.setWorldTransform(new WTransform());
		WPainterPath connectorLine = new WPainterPath();
		connectorLine.moveTo(0, 0);
		connectorLine.lineTo(closestAnchor);
		painter.strokePath(translation.map(connectorLine).getCrisp(),
				this.getLinePen());
		WPainterPath circle = new WPainterPath();
		circle.addEllipse(-2.5, -2.5, 5, 5);
		painter.fillPath(translation.map(circle), this.getMarkerBrush());
		WPainterPath rectPath = new WPainterPath();
		rectPath.addRect(rect);
		painter.fillPath(translation.map(rectPath), this.getBoxBrush());
		painter.strokePath(translation.map(rectPath).getCrisp(),
				this.getLinePen());
		painter.setPen(this.getTextPen());
		painter.drawText(translation.map(rect), EnumSet.of(
				AlignmentFlag.AlignMiddle, AlignmentFlag.AlignCenter),
				TextFlag.TextSingleLine, this.getLabel(), (WPointF) null);
	}

	private WDataSeries series_;
	private WPointF point_;
	private String label_;
	private WPointF offset_;
	private int width_;
	private WPen linePen_;
	private WPen textPen_;
	private WBrush boxBrush_;
	private WBrush markerBrush_;

	private static boolean checkIntersectHorizontal(final WPointF p1,
			final WPointF p2, double minX, double maxX, double y) {
		if (p1.getY() == p2.getY()) {
			return p1.getY() == y;
		}
		double t = (y - p1.getY()) / (p2.getY() - p1.getY());
		if (t <= 0 || t >= 1) {
			return false;
		}
		double x = p1.getX() * (1 - t) + p2.getX() * t;
		return x > minX && x < maxX;
	}

	private static boolean checkIntersectVertical(final WPointF p1,
			final WPointF p2, double minY, double maxY, double x) {
		return checkIntersectHorizontal(new WPointF(p1.getY(), p1.getX()),
				new WPointF(p2.getY(), p2.getX()), minY, maxY, x);
	}

	static WJavaScriptPreamble wtjs2() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"ChartCommon",
				"function(A){function E(a,c,b,d){function e(j){return b?c[j]:c[n-1-j]}function i(j){for(;e(j)[2]===w||e(j)[2]===B;)j--;return j}var k=l;if(d)k=m;var n=c.length;d=Math.floor(n/2);d=i(d);var q=0,s=n,f=false;if(e(0)[k]>a)return b?-1:n;if(e(n-1)[k]<a)return b?n:-1;for(;!f;){var h=d+1;if(h<n&&(e(h)[2]===w||e(h)[2]===B))h+=2;if(e(d)[k]>a){s=d;d=Math.floor((s+q)/2);d=i(d)}else if(e(d)[k]===a)f=true;else if(h<n&&e(h)[k]>a)f=true;else if(h<n&&e(h)[k]=== a){d=h;f=true}else{q=d;d=Math.floor((s+q)/2);d=i(d)}}return b?d:n-1-d}function G(a,c){return c[0][a]<c[c.length-1][a]}var w=2,B=3,l=0,m=1,F=this;A=A.WT.gfxUtils;var x=A.rect_top,y=A.rect_bottom,t=A.rect_left,C=A.rect_right,H=A.transform_mult;this.findClosestPoint=function(a,c,b){var d=l;if(b)d=m;var e=G(d,c);b=E(a,c,e,b);if(b<0)b=0;if(b>=c.length)return[c[c.length-1][l],c[c.length-1][m]];if(b>=c.length)b=c.length-2;if(c[b][d]===a)return[c[b][l],c[b][m]];var i=e?b+1:b-1;if(e&&c[i][2]==w)i+=2;if(!e&& i<0)return[c[b][l],c[b][m]];if(!e&&i>0&&c[i][2]==B)i-=2;e=Math.abs(a-c[b][d]);a=Math.abs(c[i][d]-a);return e<a?[c[b][l],c[b][m]]:[c[i][l],c[i][m]]};this.minMaxY=function(a,c){c=c?l:m;for(var b=a[0][c],d=a[0][c],e=1;e<a.length;++e)if(a[e][2]!==w&&a[e][2]!==B&&a[e][2]!==5){if(a[e][c]>d)d=a[e][c];if(a[e][c]<b)b=a[e][c]}return[b,d]};this.projection=function(a,c){var b=Math.cos(a);a=Math.sin(a);var d=b*a,e=-c[0]*b-c[1]*a;return[b*b,d,d,a*a,b*e+c[0],a*e+c[1]]};this.distanceSquared=function(a,c){a=[c[l]- a[l],c[m]-a[m]];return a[l]*a[l]+a[m]*a[m]};this.distanceLessThanRadius=function(a,c,b){return b*b>=F.distanceSquared(a,c)};this.toZoomLevel=function(a){return Math.floor(Math.log(a)/Math.LN2+0.5)+1};this.isPointInRect=function(a,c){var b;if(a.x!==undefined){b=a.x;a=a.y}else{b=a[0];a=a[1]}return b>=t(c)&&b<=C(c)&&a>=x(c)&&a<=y(c)};this.toDisplayCoord=function(a,c,b,d,e){if(b){a=[(a[l]-e[0])/e[2],(a[m]-e[1])/e[3]];d=[d[0]+a[m]*d[2],d[1]+a[l]*d[3]]}else{a=[(a[l]-e[0])/e[2],1-(a[m]-e[1])/e[3]];d=[d[0]+ a[l]*d[2],d[1]+a[m]*d[3]]}return H(c,d)};this.findYRange=function(a,c,b,d,e,i,k,n){if(a.length!==0){var q=F.toDisplayCoord([b,0],[1,0,0,1,0,0],e,i,k),s=F.toDisplayCoord([d,0],[1,0,0,1,0,0],e,i,k),f=e?m:l,h=e?l:m,j=G(f,a),g=E(q[f],a,j,e),o=E(s[f],a,j,e),p,r,u=Infinity,v=-Infinity,D=g===o&&g===a.length||g===-1&&o===-1;if(!D){if(j)if(g<0)g=0;else{g++;if(a[g]&&a[g][2]===w)g+=2}else if(g>=a.length-1)g=a.length-2;if(!j&&o<0)o=0;for(p=Math.min(g,o);p<=Math.max(g,o)&&p<a.length;++p)if(a[p][2]!==w&&a[p][2]!== B){if(a[p][h]<u)u=a[p][h];if(a[p][h]>v)v=a[p][h]}if(j&&g>0||!j&&g<a.length-1){if(j){r=g-1;if(a[r]&&a[r][2]===B)r-=2}else{r=g+1;if(a[r]&&a[r][2]===w)r+=2}p=(q[f]-a[r][f])/(a[g][f]-a[r][f]);g=a[r][h]+p*(a[g][h]-a[r][h]);if(g<u)u=g;if(g>v)v=g}if(j&&o<a.length-1||!j&&o>0){if(j){j=o+1;if(a[j][2]===w)j+=2}else{j=o-1;if(a[j][2]===B)j-=2}p=(s[f]-a[o][f])/(a[j][f]-a[o][f]);g=a[o][h]+p*(a[j][h]-a[o][h]);if(g<u)u=g;if(g>v)v=g}}var z;a=k[2]/(d-b);b=e?2:3;if(!D){z=i[b]/(v-u);z=i[b]/(i[b]/z+20);if(z>n.y[c])z=n.y[c]}c= e?[q[m]-x(i),!D?(u+v)/2-i[2]/z/2-t(i):0]:[q[l]-t(i),!D?-((u+v)/2+i[3]/z/2-y(i)):0];return{xZoom:a,yZoom:z,panPoint:c}}};this.matchesXAxis=function(a,c,b,d,e){if(e){if(c<x(b)||c>y(b))return false;if((d.side===\"min\"||d.side===\"both\")&&a>=t(b)-d.width&&a<=t(b))return true;if((d.side===\"max\"||d.side===\"both\")&&a<=C(b)+d.width&&a>=C(b))return true}else{if(a<t(b)||a>C(b))return false;if((d.side===\"min\"||d.side===\"both\")&&c<=y(b)+d.width&&c>=y(b))return true;if((d.side===\"max\"||d.side===\"both\")&&c>=x(b)- d.width&&c<=x(b))return true}return false};this.matchYAxis=function(a,c,b,d,e){function i(){return d.length}function k(h){return d[h].side}function n(h){return d[h].width}function q(h){return d[h].minOffset}function s(h){return d[h].maxOffset}if(e){if(a<t(b)||a>C(b))return-1}else if(c<x(b)||c>y(b))return-1;for(var f=0;f<i();++f)if(e)if((k(f)===\"min\"||k(f)===\"both\")&&c>=x(b)-q(f)-n(f)&&c<=x(b)-q(f))return f;else{if((k(f)===\"max\"||k(f)===\"both\")&&c>=y(b)+s(f)&&c<=y(b)+s(f)+n(f))return f}else if((k(f)=== \"min\"||k(f)===\"both\")&&a>=t(b)-q(f)-n(f)&&a<=t(b)-q(f))return f;else if((k(f)===\"max\"||k(f)===\"both\")&&a>=C(b)+s(f)&&a<=C(b)+s(f)+n(f))return f;return-1}}");
	}

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"WCartesianChart",
				"function(wa,J,z,m){function M(a){return a===undefined}function o(a){return m.modelAreas[a]}function U(){return m.followCurve}function xa(){return m.crosshair||U()!==-1}function A(){return m.isHorizontal}function j(){return m.xTransform}function h(a){return m.yTransforms[a]}function g(){return m.area}function n(){return m.insideArea}function ca(a){return M(a)?m.series:m.series[a]}function da(a){return ca(a).transform}function jb(a){return A()? w([0,1,1,0,0,0],w(da(a),[0,1,1,0,0,0])):da(a)}function Pa(a){return ca(a).curve}function O(a){return ca(a).axis}function kb(){return m.seriesSelection}function lb(){return m.sliders}function mb(){return m.hasToolTips}function nb(){return m.coordinateOverlayPadding}function Ga(){return m.curveManipulation}function ia(){return m.maxZoom.x}function V(a){return m.maxZoom.y[a]}function N(){return m.pens}function ob(){return m.penAlpha}function ea(){return m.selectedCurve}function ya(a){a.preventDefault&& a.preventDefault()}function fa(a,b){J.addEventListener(a,b)}function W(a,b){J.removeEventListener(a,b)}function C(a){return a.length}function K(){return C(m.yTransforms)}function zb(){if(m.notifyTransform.x)return true;for(var a=0;a<K();++a)if(m.notifyTransform.y[a])return true;return false}function P(){return m.crosshairAxis}function ab(a){return a.pointerType===2||a.pointerType===3||a.pointerType===\"pen\"||a.pointerType===\"touch\"}function Qa(){if(p){if(p.tooltipTimeout){clearTimeout(p.tooltipTimeout); p.tooltipTimeout=null}if(!p.overTooltip)if(p.tooltipOuterDiv){document.body.removeChild(p.tooltipOuterDiv);p.tooltipEl=null;p.tooltipOuterDiv=null}}}function Ha(){if(zb()){if(Ra){window.clearTimeout(Ra);Ra=null}Ra=setTimeout(function(){if(m.notifyTransform.x&&!pb(bb,j())){wa.emit(z.widget,\"xTransformChanged\");ja(bb,j())}for(var a=0;a<K();++a)if(m.notifyTransform.y[a]&&!pb(Sa[a],h(a))){wa.emit(z.widget,\"yTransformChanged\"+a);ja(Sa[a],h(a))}},Ab)}}function ka(a){var b,c;if(A()){b=q(g());c=x(g());return w([0, 1,1,0,b,c],w(j(),w(h(a),[0,1,1,0,-c,-b])))}else{b=q(g());c=y(g());return w([1,0,0,-1,b,c],w(j(),w(h(a),[1,0,0,-1,-b,c])))}}function F(a){return w(ka(a),n())}function la(a,b,c){if(M(c))c=false;a=c?a:w(Ia(ka(b)),a);a=A()?[(a[u]-g()[1])/g()[3],(a[v]-g()[0])/g()[2]]:[(a[v]-g()[0])/g()[2],1-(a[u]-g()[1])/g()[3]];return[o(b)[0]+a[v]*o(b)[2],o(b)[1]+a[u]*o(b)[3]]}function Ta(a,b,c){if(M(c))c=false;return X.toDisplayCoord(a,c?[1,0,0,1,0,0]:ka(b),A(),g(),o(b))}function Ja(){var a,b;if(A()){a=(la([0,x(g())], 0)[0]-o(0)[0])/o(0)[2];b=(la([0,y(g())],0)[0]-o(0)[0])/o(0)[2]}else{a=(la([q(g()),0],0)[0]-o(0)[0])/o(0)[2];b=(la([s(g()),0],0)[0]-o(0)[0])/o(0)[2]}var c;for(c=0;c<C(lb());++c){var d=$(\"#\"+lb()[c]);if(d)(d=d.data(\"sobj\"))&&d.changeRange(a,b)}}function Y(){Qa();if(mb()&&p.tooltipPosition)p.tooltipTimeout=setTimeout(function(){qb()},rb);ma&&sb(function(){z.repaint();xa()&&cb()})}function cb(){if(ma){var a=I.getContext(\"2d\");a.clearRect(0,0,I.width,I.height);a.save();a.beginPath();a.moveTo(q(g()),x(g())); a.lineTo(s(g()),x(g()));a.lineTo(s(g()),y(g()));a.lineTo(q(g()),y(g()));a.closePath();a.clip();var b=w(Ia(ka(P())),B),c=B[v],d=B[u];if(U()!==-1){b=Bb(A()?b[u]:b[v],Pa(U()),A());d=w(ka(O(U())),w(jb(U()),b));c=d[v];d=d[u];B[v]=c;B[u]=d}b=A()?[(b[u]-g()[1])/g()[3],(b[v]-g()[0])/g()[2]]:[(b[v]-g()[0])/g()[2],1-(b[u]-g()[1])/g()[3]];b=U()!==-1?[o(O(U()))[0]+b[v]*o(O(U()))[2],o(O(U()))[1]+b[u]*o(O(U()))[3]]:[o(P())[0]+b[v]*o(P())[2],o(P())[1]+b[u]*o(P())[3]];a.fillStyle=a.strokeStyle=m.crosshairColor;a.font= \"16px sans-serif\";a.textAlign=\"right\";a.textBaseline=\"top\";var e=b[0].toFixed(2);b=b[1].toFixed(2);if(e===\"-0.00\")e=\"0.00\";if(b===\"-0.00\")b=\"0.00\";a.fillText(\"(\"+e+\",\"+b+\")\",s(g())-nb()[0],x(g())+nb()[1]);a.setLineDash&&a.setLineDash([1,2]);a.beginPath();a.moveTo(Math.floor(c)+0.5,Math.floor(x(g()))+0.5);a.lineTo(Math.floor(c)+0.5,Math.floor(y(g()))+0.5);a.moveTo(Math.floor(q(g()))+0.5,Math.floor(d)+0.5);a.lineTo(Math.floor(s(g()))+0.5,Math.floor(d)+0.5);a.stroke();a.restore()}}function Cb(a){return x(a)<= x(n())+Ua&&y(a)>=y(n())-Ua&&q(a)<=q(n())+Ua&&s(a)>=s(n())-Ua}function ga(a){for(var b=0;b<K();++b){var c=F(b);if(A())if(a===za)a=Aa;else if(a===Aa)a=za;if(M(a)||a===za)if(j()[0]<1){j()[0]=1;c=F(b)}if(M(a)||a===Aa)if(h(b)[3]<1){h(b)[3]=1;c=F(b)}if(M(a)||a===za){if(q(c)>q(n())){c=q(n())-q(c);if(A())h(b)[5]=h(b)[5]+c;else j()[4]=j()[4]+c;c=F(b)}if(s(c)<s(n())){c=s(n())-s(c);if(A())h(b)[5]=h(b)[5]+c;else j()[4]=j()[4]+c;c=F(b)}}if(M(a)||a===Aa){if(x(c)>x(n())){c=x(n())-x(c);if(A())j()[4]=j()[4]+c;else h(b)[5]= h(b)[5]-c;c=F(b)}if(y(c)<y(n())){c=y(n())-y(c);if(A())j()[4]=j()[4]+c;else h(b)[5]=h(b)[5]-c;F(b)}}}Ha()}function qb(){wa.emit(z.widget,\"loadTooltip\",p.tooltipPosition[v],p.tooltipPosition[u])}function Db(){if(xa()&&(M(I)||z.canvas.width!==I.width||z.canvas.height!==I.height)){if(I){I.parentNode.removeChild(I);jQuery.removeData(J,\"oobj\");I=undefined}var a=document.createElement(\"canvas\");a.setAttribute(\"width\",z.canvas.width);a.setAttribute(\"height\",z.canvas.height);a.style.position=\"absolute\";a.style.display= \"block\";a.style.left=\"0\";a.style.top=\"0\";if(window.MSPointerEvent||window.PointerEvent){a.style.msTouchAction=\"none\";a.style.touchAction=\"none\"}z.canvas.parentNode.appendChild(a);I=a;jQuery.data(J,\"oobj\",I)}else if(!M(I)&&!xa()){I.parentNode.removeChild(I);jQuery.removeData(J,\"oobj\");I=undefined}B=[(q(g())+s(g()))/2,(x(g())+y(g()))/2]}function tb(){return I?I:z.canvas}function db(a,b){if(Ba){var c=Date.now();if(M(b))b=c-na;var d={x:0,y:0},e;if(G)e=F(0);else if(t===-1){e=F(0);for(var f=1;f<K();++f)e= Va(e,F(f))}else e=F(t);f=Eb;if(b>2*Ka){ma=false;var i=Math.floor(b/Ka-1),l;for(l=0;l<i;++l){db(a,Ka);if(!Ba){ma=true;Y();return}}b-=i*Ka;ma=true}if(k.x===Infinity||k.x===-Infinity)k.x=k.x>0?oa:-oa;if(isFinite(k.x)){k.x/=1+ub*b;e[0]+=k.x*b;if(q(e)>q(n())){k.x+=-f*(q(e)-q(n()))*b;k.x*=0.7}else if(s(e)<s(n())){k.x+=-f*(s(e)-s(n()))*b;k.x*=0.7}if(Math.abs(k.x)<eb)if(q(e)>q(n()))k.x=eb;else if(s(e)<s(n()))k.x=-eb;if(Math.abs(k.x)>oa)k.x=(k.x>0?1:-1)*oa;d.x=k.x*b}if(k.y===Infinity||k.y===-Infinity)k.y= k.y>0?oa:-oa;if(isFinite(k.y)){k.y/=1+ub*b;e[1]+=k.y*b;if(x(e)>x(n())){k.y+=-f*(x(e)-x(n()))*b;k.y*=0.7}else if(y(e)<y(n())){k.y+=-f*(y(e)-y(n()))*b;k.y*=0.7}if(Math.abs(k.y)<0.001)if(x(e)>x(n()))k.y=0.001;else if(y(e)<y(n()))k.y=-0.001;if(Math.abs(k.y)>oa)k.y=(k.y>0?1:-1)*oa;d.y=k.y*b}if(G)e=F(0);else if(t===-1){e=F(0);for(f=1;f<K();++f)e=Va(e,F(f))}else e=F(t);Z(d,Ca,t,G);if(G)a=F(0);else if(t===-1){a=F(0);for(f=1;f<K();++f)a=Va(a,F(f))}else a=F(t);if(q(e)>q(n())&&q(a)<=q(n())){k.x=0;Z({x:-d.x, y:0},Ca,t,G);ga(za)}if(s(e)<s(n())&&s(a)>=s(n())){k.x=0;Z({x:-d.x,y:0},Ca,t,G);ga(za)}if(x(e)>x(n())&&x(a)<=x(n())){k.y=0;Z({x:0,y:-d.y},Ca,t,G);ga(Aa)}if(y(e)<y(n())&&y(a)>=y(n())){k.y=0;Z({x:0,y:-d.y},Ca,t,G);ga(Aa)}if(Math.abs(k.x)<vb&&Math.abs(k.y)<vb&&Cb(a)){ga();Ba=false;D=null;k.x=0;k.y=0;na=null;r=[]}else{na=c;ma&&Wa(db)}}}function Xa(){var a,b,c=wb(j()[0])-1;if(c>=C(N().x))c=C(N().x)-1;for(a=0;a<C(N().x);++a)if(c===a)for(b=0;b<C(N().x[a]);++b)N().x[a][b].color[3]=ob().x[b];else for(b=0;b< C(N().x[a]);++b)N().x[a][b].color[3]=0;for(c=0;c<C(N().y);++c){var d=wb(h(c)[3])-1;if(d>=C(N().y[c]))d=C(N().y[c])-1;for(a=0;a<C(N().y[c]);++a)if(d===a)for(b=0;b<C(N().y[c][a]);++b)N().y[c][a][b].color[3]=ob().y[c][b];else for(b=0;b<C(N().y[c][a]);++b)N().y[c][a][b].color[3]=0}}function Z(a,b,c,d){if(M(b))b=0;if(M(c))c=-1;if(M(d))d=false;var e=la(B,P());if(A())a={x:a.y,y:-a.x};if(b&Ca){if(d)j()[4]=j()[4]+a.x;else if(c===-1){j()[4]=j()[4]+a.x;for(b=0;b<K();++b)h(b)[5]=h(b)[5]-a.y}else h(c)[5]=h(c)[5]- a.y;Ha()}else if(b&xb){var f;if(d)f=F(0);else if(c===-1){f=F(0);for(b=1;b<K();++b)f=Va(f,F(b))}else f=F(c);if(q(f)>q(n())){if(a.x>0)a.x/=1+(q(f)-q(n()))*Ya}else if(s(f)<s(n()))if(a.x<0)a.x/=1+(s(n())-s(f))*Ya;if(x(f)>x(n())){if(a.y>0)a.y/=1+(x(f)-x(n()))*Ya}else if(y(f)<y(n()))if(a.y<0)a.y/=1+(y(n())-y(f))*Ya;if(d)j()[4]=j()[4]+a.x;else if(c===-1){j()[4]=j()[4]+a.x;for(b=0;b<K();++b)h(b)[5]=h(b)[5]-a.y}else h(c)[5]=h(c)[5]-a.y;if(c===-1)B[v]+=a.x;d||(B[u]+=a.y);Ha()}else{if(d)j()[4]=j()[4]+a.x;else if(c=== -1){j()[4]=j()[4]+a.x;for(b=0;b<K();++b)h(b)[5]=h(b)[5]-a.y}else h(c)[5]=h(c)[5]-a.y;if(c===-1)B[v]+=a.x;d||(B[u]+=a.y);ga()}a=Ta(e,P());B[v]=a[v];B[u]=a[u];Y();Ja()}function La(a,b,c,d,e){if(M(d))d=-1;if(M(e))e=false;var f=la(B,P());a=A()?[a.y-x(g()),a.x-q(g())]:w(Ia([1,0,0,-1,q(g()),y(g())]),[a.x,a.y]);var i=a[0];a=a[1];var l=Math.pow(1.2,A()?c:b);b=Math.pow(1.2,A()?b:c);if(j()[0]*l>ia())l=ia()/j()[0];if(e){if(l<1||j()[0]!==ia())pa(j(),w([l,0,0,1,i-l*i,0],j()))}else if(d===-1){if(l<1||j()[0]!== ia())pa(j(),w([l,0,0,1,i-l*i,0],j()));for(d=0;d<K();++d){e=b;if(h(d)[3]*b>V(d))e=V(d)/h(d)[3];if(e<1||h(d)[3]!==V(d))pa(h(d),w([1,0,0,e,0,a-e*a],h(d)))}}else{if(h(d)[3]*b>V(d))b=V(d)/h(d)[3];if(b<1||h(d)[3]!=V(d))pa(h(d),w([1,0,0,b,0,a-b*a],h(d)))}ga();f=Ta(f,P());B[v]=f[v];B[u]=f[u];Xa();Y();Ja()}jQuery.data(J,\"cobj\",this);var qa=this,E=wa.WT;qa.config=m;var H=E.gfxUtils,w=H.transform_mult,Ia=H.transform_inverted,ja=H.transform_assign,pb=H.transform_equal,Fb=H.transform_apply,x=H.rect_top,y=H.rect_bottom, q=H.rect_left,s=H.rect_right,Va=H.rect_intersection,X=E.chartCommon,Gb=X.minMaxY,Bb=X.findClosestPoint,Hb=X.projection,yb=X.distanceLessThanRadius,wb=X.toZoomLevel,Ma=X.isPointInRect,Ib=X.findYRange,Na=function(a,b){return X.matchesXAxis(a,b,g(),m.xAxis,A())},Oa=function(a,b){return X.matchYAxis(a,b,g(),m.yAxes,A())},Ka=17,Wa=function(){return window.requestAnimationFrame||window.webkitRequestAnimationFrame||window.mozRequestAnimationFrame||function(a){window.setTimeout(a,Ka)}}(),fb=false,sb=function(a){if(!fb){fb= true;Wa(function(){a();fb=false})}};if(window.MSPointerEvent||window.PointerEvent){J.style.touchAction=\"none\";z.canvas.style.msTouchAction=\"none\";z.canvas.style.touchAction=\"none\"}var Ca=1,xb=2,za=1,Aa=2,v=0,u=1,Ab=250,rb=500,ub=0.003,Eb=2.0E-4,Ya=0.07,Ua=3,eb=0.001,oa=1.5,vb=0.02,ta=jQuery.data(J,\"eobj2\");if(!ta){ta={};ta.contextmenuListener=function(a){ya(a);W(\"contextmenu\",ta.contextmenuListener)}}jQuery.data(J,\"eobj2\",ta);var aa={},ua=false;if(window.MSPointerEvent||window.PointerEvent)(function(){function a(){ua= C(e)>0}function b(i){if(ab(i)){ya(i);e.push(i);a();aa.start(J,{touches:e.slice(0)})}}function c(i){if(ua)if(ab(i)){ya(i);var l;for(l=0;l<C(e);++l)if(e[l].pointerId===i.pointerId){e.splice(l,1);break}a();aa.end(J,{touches:e.slice(0),changedTouches:[]})}}function d(i){if(ab(i)){ya(i);var l;for(l=0;l<C(e);++l)if(e[l].pointerId===i.pointerId){e[l]=i;break}a();aa.moved(J,{touches:e.slice(0)})}}var e=[],f=jQuery.data(J,\"eobj\");if(f)if(window.PointerEvent){W(\"pointerdown\",f.pointerDown);W(\"pointerup\",f.pointerUp); W(\"pointerout\",f.pointerUp);W(\"pointermove\",f.pointerMove)}else{W(\"MSPointerDown\",f.pointerDown);W(\"MSPointerUp\",f.pointerUp);W(\"MSPointerOut\",f.pointerUp);W(\"MSPointerMove\",f.pointerMove)}jQuery.data(J,\"eobj\",{pointerDown:b,pointerUp:c,pointerMove:d});if(window.PointerEvent){fa(\"pointerdown\",b);fa(\"pointerup\",c);fa(\"pointerout\",c);fa(\"pointermove\",d)}else{fa(\"MSPointerDown\",b);fa(\"MSPointerUp\",c);fa(\"MSPointerOut\",c);fa(\"MSPointerMove\",d)}})();var I=jQuery.data(J,\"oobj\"),B=null,ma=true,D=null,G= false,t=-1,r=[],ha=false,ra=false,T=null,gb=null,hb=null,k={x:0,y:0},ba=null,na=null,p=jQuery.data(J,\"tobj\");if(!p){p={overTooltip:false};jQuery.data(J,\"tobj\",p)}var Da=null,Ba=false,Ra=null,bb=[0,0,0,0,0,0];ja(bb,j());var Sa=[];for(H=0;H<K();++H){Sa.push([0,0,0,0,0,0]);ja(Sa[H],h(H))}var pa=function(a,b){ja(a,b);Ha()};z.combinedTransform=ka;this.updateTooltip=function(a){Qa();if(a)if(p.tooltipPosition){p.toolTipEl=document.createElement(\"div\");p.toolTipEl.className=m.ToolTipInnerStyle;p.toolTipEl.innerHTML= a;p.tooltipOuterDiv=document.createElement(\"div\");p.tooltipOuterDiv.className=m.ToolTipOuterStyle;document.body.appendChild(p.tooltipOuterDiv);p.tooltipOuterDiv.appendChild(p.toolTipEl);var b=E.widgetPageCoordinates(z.canvas);a=p.tooltipPosition[v]+b.x;b=p.tooltipPosition[u]+b.y;E.fitToWindow(p.tooltipOuterDiv,a+10,b+10,a-10,b-10);$(p.toolTipEl).mouseenter(function(){p.overTooltip=true});$(p.toolTipEl).mouseleave(function(){p.overTooltip=false})}};this.mouseMove=function(a,b){setTimeout(function(){setTimeout(Qa, 200);if(!ua){var c=E.widgetCoordinates(z.canvas,b);if(Ma(c,g())){if(!p.tooltipEl&&mb()){p.tooltipPosition=[c.x,c.y];p.tooltipTimeout=setTimeout(function(){qb()},rb)}if(D===null&&xa()&&ma){B=[c.x,c.y];sb(cb)}}}},0)};this.mouseOut=function(){setTimeout(Qa,200)};this.mouseDown=function(a,b){if(!ua){a=E.widgetCoordinates(z.canvas,b);b=Oa(a.x,a.y);var c=Ma(a,g()),d=Na(a.x,a.y);if(!(b===-1&&!d&&!c)){D=a;G=d;t=b}}};this.mouseUp=function(){if(!ua){D=null;G=false;t=-1}};this.mouseDrag=function(a,b){if(!ua)if(D!== null){a=E.widgetCoordinates(z.canvas,b);if(E.buttons===1)if(t===-1&&!G&&Ga()&&ca(ea())){b=ea();var c;c=A()?a.x-D.x:a.y-D.y;ja(da(b),w([1,0,0,1,0,c/h(O(seriesNb))[3]],da(b)));Y()}else m.pan&&Z({x:a.x-D.x,y:a.y-D.y},0,t,G);D=a}};this.clicked=function(a,b){if(!ua)if(D===null)if(kb()){a=E.widgetCoordinates(z.canvas,b);wa.emit(z.widget,\"seriesSelected\",a.x,a.y)}};this.mouseWheel=function(a,b){var c=(b.metaKey<<3)+(b.altKey<<2)+(b.ctrlKey<<1)+b.shiftKey;a=m.wheelActions[c];if(!M(a)){var d=E.widgetCoordinates(z.canvas, b),e=Na(d.x,d.y),f=Oa(d.x,d.y),i=Ma(d,g());if(!(!e&&f===-1&&!i)){var l=E.normalizeWheel(b);if(i&&c===0&&Ga()){c=ea();i=-l.spinY;if(ca(c)){a=jb(c);a=Fb(a,Pa(c));a=Gb(a,A());a=(a[0]+a[1])/2;E.cancelEvent(b);b=Math.pow(1.2,i);ja(da(c),w([1,0,0,b,0,a-b*a],da(c)));Y();return}}if((a===4||a===5||a===6)&&m.pan){c=j()[4];i=[];for(d=0;d<K();++d)i.push(h(d)[5]);if(a===6)Z({x:-l.pixelX,y:-l.pixelY},0,f,e);else if(a===5)Z({x:0,y:-l.pixelX-l.pixelY},0,f,e);else a===4&&Z({x:-l.pixelX-l.pixelY,y:0},0,f,e);c!==j()[4]&& E.cancelEvent(b);for(d=0;d<K();++d)i[d]!==h(d)[5]&&E.cancelEvent(b)}else if(m.zoom){E.cancelEvent(b);i=-l.spinY;if(i===0)i=-l.spinX;if(a===1)La(d,0,i,f,e);else if(a===0)La(d,i,0,f,e);else if(a===2)La(d,i,i,f,e);else if(a===3)l.pixelX!==0?La(d,i,0,f,e):La(d,0,i,f,e)}}}};var Jb=function(){kb()&&wa.emit(z.widget,\"seriesSelected\",D.x,D.y)};aa.start=function(a,b,c){ha=C(b.touches)===1;ra=C(b.touches)===2;if(ha){Ba=false;var d=E.widgetCoordinates(z.canvas,b.touches[0]);a=Oa(d.x,d.y);var e=Ma(d,g()),f=Na(d.x, d.y);if(a===-1&&!f&&!e)return;Da=a===-1&&!f&&xa()&&yb(B,[d.x,d.y],30)?1:0;na=Date.now();D=d;t=a;G=f;if(Da!==1){if(!c&&e)ba=window.setTimeout(Jb,200);fa(\"contextmenu\",ta.contextmenuListener)}E.capture(null);E.capture(tb())}else if(ra&&(m.zoom||Ga())){if(ba){window.clearTimeout(ba);ba=null}Ba=false;r=[E.widgetCoordinates(z.canvas,b.touches[0]),E.widgetCoordinates(z.canvas,b.touches[1])].map(function(i){return[i.x,i.y]});f=false;a=-1;if(!r.every(function(i){return Ma(i,g())})){(f=Na(r[0][v],r[0][u])&& Na(r[1][v],r[1][u]))||(a=Oa(r[0][v],r[0][u]));if(!f&&(a===-1||Oa(r[1][v],r[1][u])!==a)){ra=null;return}G=f;t=a}E.capture(null);E.capture(tb());T=Math.atan2(r[1][1]-r[0][1],r[1][0]-r[0][0]);gb=[(r[0][0]+r[1][0])/2,(r[0][1]+r[1][1])/2];c=Math.abs(Math.sin(T));d=Math.abs(Math.cos(T));T=c<Math.sin(0.125*Math.PI)?0:d<Math.cos(0.375*Math.PI)?Math.PI/2:Math.tan(T)>0?Math.PI/4:-Math.PI/4;hb=Hb(T,gb);G=f;t=a}else return;ya(b)};aa.end=function(a,b){if(ba){window.clearTimeout(ba);ba=null}window.setTimeout(function(){W(\"contextmenu\", ta.contextmenuListener)},0);var c=Array.prototype.slice.call(b.touches),d=C(c)===0;d||function(){var e;for(e=0;e<C(b.changedTouches);++e)(function(){for(var f=b.changedTouches[e].identifier,i=0;i<C(c);++i)if(c[i].identifier===f){c.splice(i,1);return}})()}();d=C(c)===0;ha=C(c)===1;ra=C(c)===2;if(d){Za=null;if(Da===0&&(isFinite(k.x)||isFinite(k.y))&&m.rubberBand){na=Date.now();Ba=true;Wa(db)}else{Da===1&&qa.mouseUp(null,null);c=[];na=hb=gb=T=null}Da=null}else if(ha||ra)aa.start(a,b,true)};var Za=null, va=null,ib=null;aa.moved=function(a,b){if(ha||ra)if(!(ha&&D==null)){ya(b);va=E.widgetCoordinates(z.canvas,b.touches[0]);if(C(b.touches)>1)ib=E.widgetCoordinates(z.canvas,b.touches[1]);if(!G&&t===-1&&ha&&ba&&!yb([va.x,va.y],[D.x,D.y],3)){window.clearTimeout(ba);ba=null}Za||(Za=setTimeout(function(){if(!G&&t===-1&&ha&&Ga()&&ca(ea())){var c=ea();if(ca(c)){var d=va,e;e=A()?(d.x-D.x)/h(O(ea()))[3]:(d.y-D.y)/h(O(ea()))[3];da(c)[5]+=e;D=d;Y()}}else if(ha){d=va;e=Date.now();var f={x:d.x-D.x,y:d.y-D.y};c= e-na;na=e;if(Da===1){B[v]+=f.x;B[u]+=f.y;xa()&&ma&&Wa(cb)}else if(m.pan){k.x=f.x/c;k.y=f.y/c;Z(f,m.rubberBand?xb:0,t,G)}D=d}else if(!G&&t===-1&&ra&&Ga()&&ca(ea())){f=A()?v:u;e=[va,ib].map(function(Q){return A()?[Q.x,sa]:[Ea,Q.y]});c=Math.abs(r[1][f]-r[0][f]);var i=Math.abs(e[1][f]-e[0][f]),l=c>0?i/c:1;if(i===c)l=1;c=ea();if(ca(c)){var sa=w(Ia(ka(O(c))),[0,(r[0][f]+r[1][f])/2])[1],Fa=w(Ia(ka(O(c))),[0,(e[0][f]+e[1][f])/2])[1];ja(da(c),w([1,0,0,l,0,-l*sa+Fa],da(c)));D=d;Y();r=e}}else if(ra&&m.zoom){d= la(B,P());var Ea=(r[0][0]+r[1][0])/2;sa=(r[0][1]+r[1][1])/2;e=[va,ib].map(function(Q){return T===0?[Q.x,sa]:T===Math.PI/2?[Ea,Q.y]:w(hb,[Q.x,Q.y])});f=Math.abs(r[1][0]-r[0][0]);c=Math.abs(e[1][0]-e[0][0]);var S=f>0?c/f:1;if(c===f||T===Math.PI/2)S=1;var $a=(e[0][0]+e[1][0])/2;c=Math.abs(r[1][1]-r[0][1]);i=Math.abs(e[1][1]-e[0][1]);l=c>0?i/c:1;if(i===c||T===0)l=1;Fa=(e[0][1]+e[1][1])/2;A()&&function(){var Q=S;S=l;l=Q;Q=$a;$a=Fa;Fa=Q;Q=Ea;Ea=sa;sa=Q}();if(j()[0]*S>ia())S=ia()/j()[0];f=[];for(c=0;c<K();++c)f.push(l); for(c=0;c<K();++c)if(h(c)[3]*f[c]>V(c))f[c]=V(c)/h(c)[3];if(G){if(S!==1&&(S<1||j()[0]!==ia()))pa(j(),w([S,0,0,1,-S*Ea+$a,0],j()))}else if(t===-1){if(S!==1&&(S<1||j()[0]!==ia()))pa(j(),w([S,0,0,1,-S*Ea+$a,0],j()));for(c=0;c<K();++c)if(f[c]!==1&&(f[c]<1||h(c)[3]!==V(c)))pa(h(c),w([1,0,0,f[c],0,-f[c]*sa+Fa],h(c)))}else if(f[t]!==1&&(f[t]<1||h(t)[3]!==V(t)))pa(h(t),w([1,0,0,f[t],0,-f[t]*sa+Fa],h(t)));ga();d=Ta(d,P());B[v]=d[v];B[u]=d[u];r=e;Xa();Y();Ja()}Za=null},1))}};this.setXRange=function(a,b,c,d){b= o(0)[0]+o(0)[2]*b;c=o(0)[0]+o(0)[2]*c;if(q(o(0))>s(o(0))){if(b>q(o(0)))b=q(o(0));if(c<s(o(0)))c=s(o(0))}else{if(b<q(o(0)))b=q(o(0));if(c>s(o(0)))c=s(o(0))}var e=Pa(a);e=Ib(e,O(a),b,c,A(),g(),o(O(a)),m.maxZoom);b=e.xZoom;c=e.yZoom;e=e.panPoint;var f=la(B,P());j()[0]=b;if(c&&d)h(O(a))[3]=c;j()[4]=-e[v]*b;if(c&&d)h(O(a))[5]=-e[u]*c;Ha();a=Ta(f,P());B[v]=a[v];B[u]=a[u];ga();Xa();Y();Ja()};this.getSeries=function(a){return Pa(a)};this.rangeChangedCallbacks=[];this.updateConfig=function(a){for(var b in a)if(a.hasOwnProperty(b))m[b]= a[b];Db();Xa();Y();Ja()};this.updateConfig({});if(window.TouchEvent&&!window.MSPointerEvent&&!window.PointerEvent){qa.touchStart=aa.start;qa.touchEnd=aa.end;qa.touchMoved=aa.moved}else{H=function(){};qa.touchStart=H;qa.touchEnd=H;qa.touchMoved=H}}");
	}

	static String locToJsString(AxisValue loc) {
		switch (loc) {
		case MinimumValue:
			return "min";
		case MaximumValue:
			return "max";
		case ZeroValue:
			return "zero";
		case BothSides:
			return "both";
		}
		assert false;
		return "";
	}

	private static final int TICK_LENGTH = 5;
	private static final int CURVE_LABEL_PADDING = 10;
	private static final int DEFAULT_CURVE_LABEL_WIDTH = 100;
	private static final int CURVE_SELECTION_DISTANCE_SQUARED = 400;

	static int toZoomLevel(double zoomFactor) {
		return (int) Math.floor(Math.log(zoomFactor) / Math.log(2.0) + 0.5) + 1;
	}
}
